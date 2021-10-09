package site.duqian.spring.controller;

import com.google.gson.Gson;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;
import site.duqian.spring.bean.ReportResponse;
import site.duqian.spring.utils.CmdUtil;
import site.duqian.spring.utils.CommonUtils;
import site.duqian.spring.utils.FileUtil;
import site.duqian.spring.utils.SpringContextUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;

@Controller
@RequestMapping("/coverage")
public class ReportController {

    private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(ReportController.class);

    /**
     * 生成报告
     * http://127.0.0.1:8090/temp/cc-start-coverage/index.html
     * http://127.0.0.1:8090/temp/cc-all-coverage/index.html
     * <p>
     * http://127.0.0.1:8090/temp/cc-start-coverage.rar
     * http://127.0.0.1:8090/temp/cc-all-coverage.rar
     */
    @RequestMapping(value = "/report", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String report(HttpServletRequest request, HttpServletResponse resp) throws Exception {
        request.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);

        CommonParams commonParams = CommonUtils.getCommonParams(request, "report");

        if (commonParams.getCommitId() == null) {
            commonParams.setCommitId("577082371ba3f40f848904baa39083f14b2695b0"); // TODO-dq: 2021/9/30 表单提交为空，获取最新的？
        }
        //生成报告，失败的原因可能是找不到class,src,ec
        boolean generateReport = generateReport(commonParams);
        String msg = "{\"result\":0,\"data\":\"success\"}";
        if (!generateReport) {
            msg = "{\"result\":0,\"data\":\"generate report failed.\"}";
        } else {
            //返回报告的预览路径和下载url
            String reportRelativePath = FileUtil.getReportRelativePath(commonParams);
            String reportUrl = Constants.REPORT_SERVER_HOST_URL + "/" + reportRelativePath + "/" + FileUtil.getReportDirName(commonParams);
            String reportZipUrl = Constants.REPORT_SERVER_HOST_URL + "/" + reportRelativePath + "/" + FileUtil.getReportZipFileName(commonParams);

            //防止url中出现反斜杠
            //reportUrl = reportUrl.replaceAll(File.separator, "/");
            //reportZipUrl = reportZipUrl.replaceAll(File.separator, "/");
            String ts = "?ts=" + System.currentTimeMillis();
            ReportResponse reportResponse = new ReportResponse(reportUrl + ts, reportZipUrl + ts);
            msg = new Gson().toJson(reportResponse);
        }
        String logMsg = "handle report=" + msg + ",incremental=" + commonParams.isIncremental();
        System.out.println(logMsg);
        Logger.debug(logMsg);
        return msg;
    }

    /**
     * 生成报告，合并ec文件
     *
     * @param commonParams 路径动态配置
     */
    private boolean generateReport(CommonParams commonParams) {
        String reportPath = FileUtil.getJacocoReportPath(commonParams);
        String jarPath = FileUtil.getJacocoJarPath();
        String execPath = FileUtil.getEcFilesDir(commonParams) + File.separator + "**.ec";
        String classesPath = FileUtil.getClassDir(commonParams);
        String srcPath = FileUtil.getSourceDir(commonParams);
        File classFile = new File(classesPath);
        if (!classFile.exists()) {
            String saveDir = FileUtil.getSaveDir(commonParams);
            String zipFile = saveDir + File.separator + Constants.JACOCO_CLASS_ZIP_FILE_NAME;
            FileUtil.unzip(saveDir, zipFile);
        }
        boolean incremental = commonParams.isIncremental();
        if (incremental) {
            //diff 报告  copy出指定的class文件到新的目录,diff报告的路径需要不同
            String diffFilePath = FileUtil.getDiffFilePath(commonParams);
            List<String> diffFiles = FileUtil.readDiffFilesFromTxt(diffFilePath);
            classesPath = getDiffClasses(commonParams, diffFiles);
            srcPath = getDiffSrc(commonParams, diffFiles);
        }

        boolean isGenerated = CmdUtil.generateReportByCmd(jarPath,
                execPath,
                classesPath,
                srcPath,
                reportPath);
        System.out.println("generateReport=" + isGenerated);
        Logger.debug("generateReport=" + isGenerated + "," + commonParams);
        if (isGenerated) {
            zipReport(reportPath, commonParams);
            //todo-dq 多人同时操作时，如何异步？删除临时的src和class？或者直接替换
        }
        return isGenerated;
    }

    private String getDiffSrc(CommonParams commonParams, List<String> diffFiles) {
        String diffSrcDirPath = FileUtil.getDiffSrcDirPath(commonParams);
        if (diffFiles != null && diffFiles.size() > 0) {
            String srcDirPath = FileUtil.getSourceDir(commonParams);
            Logger.debug("getDiffSrc srcDirPath=" + srcDirPath);
            for (String diffFile : diffFiles) {
                try {
                    int index = diffFile.indexOf(Constants.APP_PACKAGE_NAME);
                    if (index < 0) {
                        continue;
                    }
                    String realFilePath = srcDirPath + File.separator + diffFile;
                    String relativePath = diffFile.substring(index);
                    //Logger.debug("getDiffSrc realFilePath=" + realFilePath);
                    File destFile = new File(diffSrcDirPath + relativePath);
                    //Logger.debug("getDiffSrc destFile=" + destFile.getAbsolutePath());
                    FileUtil.copyFile(new File(realFilePath), destFile, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return diffSrcDirPath;
    }

    private String getDiffClasses(CommonParams commonParams, List<String> diffFiles) {
        String diffClassDirPath = FileUtil.getDiffClassDirPath(commonParams);
        if (diffFiles != null && diffFiles.size() > 0) {
            String classDir = FileUtil.getClassDir(commonParams);
            Logger.debug("getDiffClasses classDir=" + classDir);
            for (String diffFile : diffFiles) {
                try {
                    int index = diffFile.indexOf(Constants.APP_PACKAGE_NAME);
                    if (index < 0) {
                        continue;
                    }
                    String fileName = new File(diffFile).getName();
                    String realName = fileName.substring(0, fileName.lastIndexOf("."));
                    int lastSeparatorIndex = diffFile.lastIndexOf("/");
                    //取出差异class的路径
                    String relativePath = diffFile.substring(index, lastSeparatorIndex + 1);
                    String realDirPath = classDir + relativePath;
                    File rootDir = new File(realDirPath);
                    if (rootDir.exists() && rootDir.isDirectory() && rootDir.listFiles() != null) {
                        for (File file : rootDir.listFiles()) {
                            String name = file.getName();
                            if (name.contains(realName)) {
                                //Logger.debug("getDiffClasses file=" + file);
                                File destFile = new File(diffClassDirPath + relativePath + name);
                                //Logger.debug("getDiffClasses destFile=" + destFile.getAbsolutePath());
                                FileUtil.copyFile(file, destFile, true);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return diffClassDirPath;
    }

    private void zipReport(String reportPath, CommonParams commonParams) {
        Executor prodExecutor = (Executor) SpringContextUtil.getBean(Constants.THREAD_EXECUTOR_NAME);
        prodExecutor.execute(() -> {
            String reportZipPath = FileUtil.getReportZipPath(commonParams);
            try {
                Logger.debug("zipReport reportPath " + reportPath + ",reportZipPath=" + reportZipPath);
                //存在也要处理，因为可能有更新
                FileUtil.deleteFile(reportZipPath);
                FileUtil.zipFolder(reportPath, reportZipPath);
            } catch (Exception e) {
                Logger.error(commonParams + ",zipReport error " + e);
            }
        });
    }
}