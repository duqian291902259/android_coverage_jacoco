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
import site.duqian.spring.manager.ThreadManager;
import site.duqian.spring.utils.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        String os = commonParams.getOs();
        logger.debug("os:" + os);
        String serverHostUrl = CommonUtils.getReportServerHost(commonParams);
        if (Constants.OS_IOS.equalsIgnoreCase(os)) {
            //ios mock一个报告。也可以转发请求，调用其他端的服务，返回一个报告的结果
            //String reportHostUrl = serverHostUrl + "/" + Constants.REPORT_DOWNLOAD_ROOT_DIR;
            sleepRandom();
            String reportUrl = serverHostUrl + "/" + Constants.IOS_REPORT_PATH + Constants.IOS_REPORT_FILE_NAME + "/";
            String reportZipUrl = serverHostUrl + "/" + Constants.IOS_REPORT_PATH + Constants.IOS_REPORT_FILE_NAME + ".zip";
            ReportResponse reportResponse = new ReportResponse(reportUrl, reportZipUrl);
            reportResponse.setData("IOS覆盖率报告已生成，请点击在线查阅或下载");
            return new Gson().toJson(reportResponse);
        } else if (Constants.OS_PC.equalsIgnoreCase(os)) {
            sleepRandom();
            //pc mock一个报告。也可以转发请求，调用其他端的服务，返回一个报告的结果
            //String reportHostUrl = serverHostUrl + "/" + Constants.REPORT_DOWNLOAD_ROOT_DIR;
            String reportUrl = serverHostUrl + "/" + Constants.PC_REPORT_PATH + Constants.PC_REPORT_FILE_NAME + "/";
            String reportZipUrl = serverHostUrl + "/" + Constants.PC_REPORT_PATH + Constants.PC_REPORT_FILE_NAME + ".zip";
            ReportResponse reportResponse = new ReportResponse(reportUrl, reportZipUrl);
            reportResponse.setData("PC覆盖率报告已生成，请点击在线查阅或下载");
            return new Gson().toJson(reportResponse);
        }
        //android
        // TODO: 2022/1/26 设置接口超时
        return handleReportRequest(commonParams);
    }

    private void sleepRandom() {
        try {
            int millis = new Random().nextInt(2) + 1;
            Thread.sleep(millis * 1000L);
            logger.debug("sleepRandom: " + millis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String handleReportRequest(CommonParams commonParams) {
        String jacocoDownloadDir = FileUtils.getJacocoDownloadDir();
        File downloadFile = new File(jacocoDownloadDir);
        boolean exists = downloadFile.exists();
        logger.debug("root dir:" + FileUtils.getRootDir() + ",download dir=" + jacocoDownloadDir + ",exists=" + exists);
        //路径区分大小写
        //printRootDir(file);
        //生成报告，失败的原因可能是找不到class,src,ec
        int generateReportCode = generateReport(commonParams);
        String msg = "{\"result\":0,\"data\":\"success\"}";
        if (generateReportCode != Constants.CODE_SUCCESS) {
            ReportResponse reportResponse = new ReportResponse("", "");
            String errorMsg = "请确认覆盖率文件/构建产物是否上传";
            if (generateReportCode == Constants.ERROR_CODE_DIFF_FAILED) {
                errorMsg = "未获取到增量修改，可尝试生成全量报告";
            } else if (generateReportCode == Constants.ERROR_CODE_NO_EC_FILE) {
                errorMsg = "没有上传ec文件，请进入APP调试面板，点击生成并上传覆盖率文件";
            } else if (generateReportCode == Constants.ERROR_CODE_NO_CLASSES) {
                errorMsg = "没有找到class文件，请确认打包机是否正常构建APK，或者手动上传";
            } else if (generateReportCode == Constants.ERROR_CODE_NO_SRC) {
                errorMsg = "没有找到源码，请确认打包机是否正常构建APK，或者手动上传";
            } else if (generateReportCode == Constants.ERROR_CODE_CMD_FAILED) {
                errorMsg = "命令执行失败（ec解析错误，malformed input）";
            }
            reportResponse.setData("报告生成失败:" + generateReportCode + " " + errorMsg);
            msg = new Gson().toJson(reportResponse);
        } else {
            //返回报告的预览路径和下载url
            String reportRelativePath = FileUtils.getReportRelativePath(commonParams);
            String serverHostUrl = CommonUtils.getReportServerHost(commonParams);
            String reportHostUrl = serverHostUrl + "/" + Constants.REPORT_DOWNLOAD_ROOT_DIR + Constants.REPORT_DIR_NAME + "/" + reportRelativePath + "/";
            String reportUrl = reportHostUrl + FileUtils.getReportDirName(commonParams);
            String reportZipUrl = reportHostUrl + FileUtils.getReportZipFileName(commonParams);
            String ts = "?ts=" + System.currentTimeMillis();
            ReportResponse reportResponse = new ReportResponse(reportUrl + ts, reportZipUrl + ts);
            reportResponse.setData(commonParams.getAppName() + "覆盖率报告已生成，请点击在线查阅或下载");
            msg = new Gson().toJson(reportResponse);
            checkZipFile(commonParams);
        }
        String logMsg = "handle report=" + msg + ",incremental=" + commonParams.isIncremental();
        logger.debug(logMsg);
        return msg;
    }

    /**
     * 有可能没有压缩成功
     */
    private void checkZipFile(CommonParams commonParams) {
        String reportZipPath = FileUtils.getReportZipPath(commonParams);
        File file = new File(reportZipPath);
        if (file.exists() && file.isFile() && file.length() > 0) {
            logger.debug("checkZipFile exist=" + reportZipPath);
            return;
        }
        String reportPath = FileUtils.getJacocoReportPath(commonParams);
        logger.debug("zipReport reportPath=" + reportPath);
        zipReport(reportPath, commonParams);
    }

    /**
     * 生成报告，合并ec文件
     *
     * @param commonParams 路径动态配置
     */
    private int generateReport(CommonParams commonParams) {
        String reportPath = FileUtils.getJacocoReportPath(commonParams);
        boolean deleteReportTargetDir = FileUtils.deleteDirectory(reportPath);
        logger.error("generateReport reportPath=" + reportPath + "，deleteReportTargetDir=" + deleteReportTargetDir);

        String jarPath = FileUtils.getJacocoJarPath();
        String saveDir = FileUtils.getSaveDir(commonParams);

        List<String> ecFileList = getEcFiles(commonParams, saveDir);

        boolean hasEcFile = ecFileList.size() > 0;
        int errorCode = 0;
        if (!hasEcFile) {
            logger.error("generateReport ERROR_CODE_NO_EC_FILE");
            errorCode = Constants.ERROR_CODE_NO_EC_FILE;
            return errorCode;
        }
        String classesPath = FileUtils.getClassDir(commonParams);
        String srcPath = FileUtils.getSourceDir(commonParams);

        //校验class
        File classFile = new File(classesPath);
        if (!classFile.exists() || classFile.listFiles() == null || classFile.listFiles().length == 0) {
            String zipFile = FileUtils.getClassZipFile(commonParams);
            boolean unzip = FileUtils.unzip(saveDir, zipFile);
            FileUtils.tryServerUnZip(zipFile, unzip);
        }
        if (!classFile.exists() || classFile.listFiles() == null || classFile.listFiles().length == 0) {
            errorCode = Constants.ERROR_CODE_NO_CLASSES;
            logger.error("generateReport ERROR_CODE_NO_CLASSES");
        }

        //校验src
        File srcFile = new File(srcPath);
        if (!srcFile.exists() || srcFile.listFiles() == null || srcFile.listFiles().length == 0) {
            String zipSrcFile = FileUtils.getSrcZipFile(commonParams);
            boolean unzip = FileUtils.unzip(saveDir, zipSrcFile);
            FileUtils.tryServerUnZip(zipSrcFile, unzip);
        }
        if (!srcFile.exists() || srcFile.listFiles() == null || srcFile.listFiles().length == 0) {
            //errorCode = Constants.ERROR_CODE_NO_SRC;
            logger.error("generateReport Constants.ERROR_CODE_NO_SRC");
        }
        logger.debug("generateReport classesPath=" + classesPath);
        logger.debug("generateReport srcPath=" + srcPath);

        boolean incremental = commonParams.isIncremental();
        if (incremental) {//diff 报告  copy出指定的class文件到新的目录,diff报告的路径不同
            return handleDiffReport(commonParams, reportPath, jarPath, ecFileList, classesPath, srcPath);
        } else {
            boolean isGenerated = generateReport(reportPath, jarPath, ecFileList, classesPath, srcPath, commonParams.getAppName());
            logger.debug("generateReport=" + isGenerated + "," + commonParams);
            if (isGenerated) {
                zipReport(reportPath, commonParams);
                return Constants.CODE_SUCCESS;
            }
            errorCode = Constants.ERROR_CODE_CMD_FAILED;
        }
        return errorCode;
    }

    private List<String> getEcFiles(CommonParams commonParams, String saveDir) {
        //ec文件，同分支的全部遍历处理，时间最近的放在最前面（合并时，如果方法不同，直接不合并，丢弃旧的，todo 待验证是否准确）
        List<String> ecFileList = new ArrayList<>();
        String ecFilesDir = FileUtils.getEcFilesDir(commonParams);
        File rootEcDir = new File(ecFilesDir);
        if (!rootEcDir.exists()) {
            rootEcDir = new File(saveDir);
        }
        logger.error("generateReport rootEcDir=" + rootEcDir.getAbsolutePath());
        if (rootEcDir.exists() && rootEcDir.isDirectory() && rootEcDir.listFiles() != null) {
            int length = "jacoco_115999be_31fcb2373e8b341d.ec".length();
            for (File file : rootEcDir.listFiles()) {
                String fileName = file.getName();
                if (fileName.endsWith(Constants.TYPE_FILE_EC)) {
                    String path = file.getAbsolutePath();
                    /*path = new String(path.getBytes(StandardCharsets.UTF_8));
                    if (path.contains("\u0001") || fileName.length() != length) {//
                        //临时修复路径非法的问题
                        logger.error("generateReport malformed input around byte 24=" + path);
                    } else {*/
                    ecFileList.add(path);
                    //}
                }
            }
        }
        return ecFileList;
    }

    /**
     * 增量报告
     */
    private int handleDiffReport(CommonParams commonParams, String reportPath, String jarPath, List<String> ecFileList, String classesPath, String srcPath) {
        String diffClassPath = "";
        //gitlab获取两个分支点的diff信息
        diffClassPath = DiffUtils.INSTANCE.handleDiffFileByGitlab(commonParams);
        if (TextUtils.isEmpty(diffClassPath)) {//取本地上传的差异的分支文件列表
            diffClassPath = DiffUtils.INSTANCE.handleDiffClasses(commonParams);
            logger.error("generateReport gitlab diff null,handle local Diff Classes " + diffClassPath);
        } else {
            logger.debug("generateReport gitlab diff classes success " + diffClassPath);
        }
        if (!TextUtils.isEmpty(diffClassPath)) {
            classesPath = diffClassPath;
            srcPath = FileUtils.getDiffSrcDirPath(commonParams);
        } else {
            logger.error("generateReport,found no diff classes " + diffClassPath);
            return Constants.ERROR_CODE_DIFF_FAILED;
        }

        boolean isGenerated = generateReport(reportPath, jarPath, ecFileList, classesPath, srcPath, commonParams.getAppName());
        logger.debug("generateReport=" + isGenerated + "," + commonParams);
        if (isGenerated) {
            zipReport(reportPath, commonParams);
            return Constants.CODE_SUCCESS;
        }
        return Constants.ERROR_CODE_CMD_FAILED;
    }

    /**
     * 在docker部署，发现不支持正则表达式，file not found ，所以要拼接所有的.ec文件，而且要在bash环境下执行
     */
    private boolean generateReport(String reportPath, String jarPath, List<String> execPaths, String classesPath, String srcPath, String appName) {
        if (TextUtils.isEmpty(appName)) {
            appName = "dq-coverage";
        }
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
        cmdList.add("--name=" + appName);

        String[] commandArray = new String[cmdList.size()];
        for (int i = 0; i < commandArray.length; i++) {
            commandArray[i] = cmdList.get(i);
        }
        return CmdUtil.INSTANCE.runProcess(commandArray);
    }

    private void zipReport(String reportPath, CommonParams commonParams) {
        ThreadManager.getBackgroundPool().execute(() -> realZipReport(reportPath, commonParams));
    }

    private void realZipReport(String reportPath, CommonParams commonParams) {
        String reportZipPath = FileUtils.getReportZipPath(commonParams);
        try {
            logger.debug("reportZipPath=" + reportZipPath);
            //存在也要处理，因为可能有更新
            FileUtils.deleteFile(reportZipPath);
            FileUtils.zipFolder(reportPath, reportZipPath);
        } catch (Exception e) {
            logger.error(commonParams + ",zipReport error " + e);
        }
    }
}