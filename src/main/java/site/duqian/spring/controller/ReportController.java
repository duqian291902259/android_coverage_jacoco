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
import site.duqian.spring.utils.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
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

        return handleReportRequest(commonParams);
    }

    private String handleReportRequest(CommonParams commonParams) {
        String jacocoDownloadDir = FileUtils.getJacocoDownloadDir();
        File downloadFile = new File(jacocoDownloadDir);
        boolean exists = downloadFile.exists();
        logger.debug("root dir:" + FileUtils.getRootDir() + ",download dir=" + jacocoDownloadDir + ",exists=" + exists);
        //路径区分大小写
        File file = new File(FileUtils.getSourceDir(commonParams));
        logger.debug(file + " exists=" + file.exists());

        printRootDir(file);
        //生成报告，失败的原因可能是找不到class,src,ec
        int generateReportCode = generateReport(commonParams);
        String msg = "{\"result\":0,\"data\":\"success\"}";
        if (generateReportCode != Constants.CODE_SUCCESS) {
            ReportResponse reportResponse = new ReportResponse("", "");
            reportResponse.setData("报告生成失败:" + generateReportCode);
            msg = new Gson().toJson(reportResponse);
        } else {
            //返回报告的预览路径和下载url
            String reportRelativePath = FileUtils.getReportRelativePath(commonParams);
            String reportUrl = Constants.REPORT_SERVER_HOST_URL + "/" + reportRelativePath + "/" + FileUtils.getReportDirName(commonParams);
            String reportZipUrl = Constants.REPORT_SERVER_HOST_URL + "/" + reportRelativePath + "/" + FileUtils.getReportZipFileName(commonParams);
            String ts = "?ts=" + System.currentTimeMillis();
            ReportResponse reportResponse = new ReportResponse(reportUrl + ts, reportZipUrl + ts);
            reportResponse.setData("覆盖率报告已生成，请点击在线查阅或下载");
            msg = new Gson().toJson(reportResponse);
        }
        String logMsg = "handle report=" + msg + ",incremental=" + commonParams.isIncremental();
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
        String reportPath = FileUtils.getJacocoReportPath(commonParams);
        String jarPath = FileUtils.getJacocoJarPath();
        List<String> ecFileList = new ArrayList<>();
        //String execPath = FileUtils.getEcFilesDir(commonParams) + File.separator + "**.ec";
        //execPath = FileUtils.getEcFilesDir(commonParams) + File.separator + "63fda2c017ae88dfa4e2edbf97e04c12.ec";
        File rootEcDir = new File(FileUtils.getEcFilesDir(commonParams));
        if (!rootEcDir.exists() || rootEcDir.listFiles() == null) {
            return Constants.ERROR_CODE_NO_FILES;
        }
        boolean hasEcFile = false;
        for (File file : rootEcDir.listFiles()) {
            if (file.getName().endsWith(Constants.TYPE_FILE_EC)) {
                hasEcFile = true;
                ecFileList.add(file.getAbsolutePath());
            }
        }
        if (!hasEcFile) {
            return Constants.ERROR_CODE_NO_EC_FILE;
        }
        String classesPath = FileUtils.getClassDir(commonParams);
        String srcPath = FileUtils.getSourceDir(commonParams);
        File classFile = new File(classesPath);
        if (!classFile.exists()) {
            String saveDir = FileUtils.getSaveDir(commonParams);
            String zipFile = FileUtils.getClassZipFile(commonParams);
            FileUtils.unzip(saveDir, zipFile);
        }
        if (!classFile.exists() || classFile.listFiles() == null || classFile.listFiles().length == 0) {
            return Constants.ERROR_CODE_NO_CLASSES;
        }

        File srcFile = new File(srcPath);
        if (!srcFile.exists() || srcFile.listFiles() == null || srcFile.listFiles().length == 0) {
            return Constants.ERROR_CODE_NO_SRC;
        }
        logger.debug("generateReport classesPath=" + classesPath);
        logger.debug("generateReport srcPath=" + srcPath);

        boolean incremental = commonParams.isIncremental();
        if (incremental) {//diff 报告  copy出指定的class文件到新的目录,diff报告的路径不同
            String diffClassPath = DiffUtils.INSTANCE.handleDiffClasses(commonParams);
            if (!TextUtils.isEmpty(classesPath)) {
                classesPath = diffClassPath;
            }
        }
        boolean isGenerated = generateReport(reportPath, jarPath, ecFileList, classesPath, srcPath);
        logger.debug("generateReport=" + isGenerated + "," + commonParams);
        if (isGenerated) {
            zipReport(reportPath, commonParams);
            return Constants.CODE_SUCCESS;
        }
        return Constants.CODE_FAILED;
    }

    /**
     * 在docker部署，发现不支持正则表达式，file not found ，所以要拼接所有的，而且要在bash环境下执行
     */
    private boolean generateReport(String reportPath, String jarPath, List<String> execPaths, String classesPath, String srcPath) {
        /*boolean isGenerated = CmdUtil.generateReportByCmd(jarPath,
                execPaths.get(0),
                classesPath,
                srcPath,
                reportPath);*/
        List<String> cmdList = new ArrayList<>();
        cmdList.add("java");
        cmdList.add("-jar");
        cmdList.add(jarPath);
        cmdList.add("report");
        if (execPaths != null && execPaths.size() > 0) {
            cmdList.addAll(execPaths);
        }
        cmdList.add("--classfiles");
        cmdList.add(classesPath);
        cmdList.add("--sourcefiles");
        cmdList.add(srcPath);
        cmdList.add("--html");
        cmdList.add(reportPath);
        cmdList.add("--encoding=utf8");

        String[] commandArray = new String[cmdList.size()];
        for (int i = 0; i < commandArray.length; i++) {
            commandArray[i] = cmdList.get(i);
        }
        return CmdUtil.INSTANCE.runProcess(commandArray);
    }

    private void zipReport(String reportPath, CommonParams commonParams) {
        Executor prodExecutor = (Executor) SpringContextUtil.getBean(Constants.THREAD_EXECUTOR_NAME);
        prodExecutor.execute(() -> {
            String reportZipPath = FileUtils.getReportZipPath(commonParams);
            try {
                logger.debug("zipReport reportPath " + reportPath + ",reportZipPath=" + reportZipPath);
                //存在也要处理，因为可能有更新
                FileUtils.deleteFile(reportZipPath);
                FileUtils.zipFolder(reportPath, reportZipPath);
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
            });
        }
    }

}