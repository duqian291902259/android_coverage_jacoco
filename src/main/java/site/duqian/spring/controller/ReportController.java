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
import site.duqian.spring.git_helper.GitRepoUtil;
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
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ReportController.class);

    /**
     * 生成报告
     */
    @RequestMapping(value = "/report", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String report(HttpServletRequest request, HttpServletResponse resp) throws Exception {
        request.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);

        CommonParams commonParams = CommonUtils.getCommonParams(request, "report");

        String jacocoDownloadDir = FileUtil.getJacocoDownloadDir();
        File downloadFile = new File(jacocoDownloadDir);
        boolean exists = downloadFile.exists();
        logger.debug("root dir:" + FileUtil.getRootDir() + ",download dir=" + jacocoDownloadDir + ",exists=" + exists);
        //路径区分大小写
        File file = new File("/cc-jacoco-download/cc-android/dev_dq_#411671_coverage/src");
        logger.debug(file+" exists=" + file.exists());

        printRootDir(file);//new File("/")

        if (commonParams.getCommitId() == null) {
            String commitId = GitRepoUtil.getCurrentCommitId();// "577082371ba3f40f848904baa39083f14b2695b0";
            commonParams.setCommitId(commitId); // TODO-dq: 2021/9/30 表单提交为空，获取最新的？
            logger.debug("getCurrentCommitId=" + commitId);
        }
        //生成报告，失败的原因可能是找不到class,src,ec
        int generateReportCode = generateReport(commonParams);
        String msg = "{\"result\":0,\"data\":\"success\"}";
        if (generateReportCode != Constants.CODE_SUCCESS) {
            //msg = "{\"result\":0,\"data\":\"报告生成失败.\"}";
            ReportResponse reportResponse = new ReportResponse("", "");
            reportResponse.setData("报告生成失败:" + generateReportCode);
            msg = new Gson().toJson(reportResponse);
        } else {
            //String executeHttpServer = CmdUtil.execute(Constants.CMD_HTTP_SERVER_REPORT);
            //logger.debug("executeHttpServer:" + executeHttpServer);
            //返回报告的预览路径和下载url
            String reportRelativePath = FileUtil.getReportRelativePath(commonParams);
            String reportUrl = Constants.REPORT_SERVER_HOST_URL + "/" + reportRelativePath + "/" + FileUtil.getReportDirName(commonParams);
            String reportZipUrl = Constants.REPORT_SERVER_HOST_URL + "/" + reportRelativePath + "/" + FileUtil.getReportZipFileName(commonParams);
            String ts = "?ts=" + System.currentTimeMillis();
            ReportResponse reportResponse = new ReportResponse(reportUrl + ts, reportZipUrl + ts);
            reportResponse.setData("覆盖率报告已生成，请点击在线查阅或下载");
            msg = new Gson().toJson(reportResponse);
        }
        String logMsg = "handle report=" + msg + ",incremental=" + commonParams.isIncremental();
        System.out.println(logMsg);
        logger.debug(logMsg);
        return msg;
    }

    private void printRootDir(File file) {
        if (file == null || !file.exists() || file.isFile()) {
            return;
        }
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            for (File dockerFile : files) {
                logger.debug("root docker dir=" + dockerFile + ",exists=" + dockerFile.exists());
            }
            logger.debug("root / files=" + files.length);
        }
    }

    /**
     * 生成报告，合并ec文件
     *
     * @param commonParams 路径动态配置
     */
    private int generateReport(CommonParams commonParams) {
        String reportPath = FileUtil.getJacocoReportPath(commonParams);
        String jarPath = FileUtil.getJacocoJarPath();
        //String execPath = FileUtil.getEcFilesDir(commonParams) + File.separator + "f5f66fb9a08b1d457edbec4d6a08cbc8.ec";
        String execPath = FileUtil.getEcFilesDir(commonParams) + File.separator + "**.ec";
        String classesPath = FileUtil.getClassDir(commonParams);
        String srcPath = FileUtil.getSourceDir(commonParams);
        File classFile = new File(classesPath);
        if (!classFile.exists()) {
            String saveDir = FileUtil.getSaveDir(commonParams);
            String zipFile = FileUtil.getClassZipFile(commonParams);
            FileUtil.unzip(saveDir, zipFile);
        }
        if (!classFile.exists() || classFile.listFiles() == null || classFile.listFiles().length == 0) {
            return Constants.ERROR_CODE_NO_CLASSES;
        }
        //无论如何都更新下源码
        updateRepoSource(commonParams);

        File srcFile = new File(srcPath);
        if (!srcFile.exists() || srcFile.listFiles() == null || srcFile.listFiles().length == 0) {
            return Constants.ERROR_CODE_NO_SRC;
        }
        File rootEcDir = new File(FileUtil.getEcFilesDir(commonParams));
        if (!rootEcDir.exists() || rootEcDir.listFiles() == null) {
            return Constants.ERROR_CODE_NO_FILES;
        }
        boolean hasEcFile = false;
        for (File file : rootEcDir.listFiles()) {
            if (file.getName().endsWith(Constants.TYPE_FILE_EC)) {
                hasEcFile = true;
                break;
            }
        }
        if (!hasEcFile) {
            return Constants.ERROR_CODE_NO_EC_FILE;
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
        logger.debug("generateReport=" + isGenerated + "," + commonParams);
        if (isGenerated) {
            zipReport(reportPath, commonParams);
            //todo-dq 多人同时操作时，如何异步？删除临时的src和class？或者直接替换
            return Constants.CODE_SUCCESS;
        }
        return Constants.CODE_FAILED;
    }

    private String getDiffSrc(CommonParams commonParams, List<String> diffFiles) {
        String diffSrcDirPath = FileUtil.getDiffSrcDirPath(commonParams);
        if (diffFiles != null && diffFiles.size() > 0) {
            String srcDirPath = FileUtil.getSourceDir(commonParams);
            logger.debug("getDiffSrc srcDirPath=" + srcDirPath);
            for (String diffFile : diffFiles) {
                try {
                    int index = diffFile.indexOf(Constants.APP_PACKAGE_NAME);
                    if (index < 0 || diffFile.endsWith(".java") || diffFile.endsWith(".kt")) {
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
            logger.debug("getDiffClasses classDir=" + classDir);
            for (String diffFile : diffFiles) {
                try {
                    int index = diffFile.indexOf(Constants.APP_PACKAGE_NAME);
                    if (index < 0 || diffFile.endsWith(".java") || diffFile.endsWith(".kt")) {
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
                logger.debug("zipReport reportPath " + reportPath + ",reportZipPath=" + reportZipPath);
                //存在也要处理，因为可能有更新
                FileUtil.deleteFile(reportZipPath);
                FileUtil.zipFolder(reportPath, reportZipPath);
            } catch (Exception e) {
                logger.error(commonParams + ",zipReport error " + e);
            }
        });
    }

    private static boolean isCloning = false;

    private void updateRepoSource(CommonParams commonParams) {
        if (isCloning) {
            return;
        }
        //上传了ec文件才clone源码
        if (commonParams != null) {
            Executor prodExecutor = (Executor) SpringContextUtil.getBean(Constants.THREAD_EXECUTOR_NAME);
            prodExecutor.execute(() -> {
                isCloning = true;
                //后台执行clone代码的逻辑
                boolean cloneSrc = GitRepoUtil.cloneSrc(commonParams);
                System.out.println("cloneSrc=" + cloneSrc + ",commonParams=" + commonParams);
                logger.debug("cloneSrc=" + cloneSrc + ",commonParams=" + commonParams);
                isCloning = false;
                //GitRepoUtil.checkOut(commonParams);
            });
        }
    }

}