package site.duqian.spring.controller;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import site.duqian.spring.Constants;
import site.duqian.spring.bean.BranchItem;
import site.duqian.spring.bean.BranchListResp;
import site.duqian.spring.bean.CommonLog;
import site.duqian.spring.bean.CommonParams;
import site.duqian.spring.manager.ThreadManager;
import site.duqian.spring.utils.CmdUtil;
import site.duqian.spring.utils.CommonUtils;
import site.duqian.spring.utils.FileUtils;
import site.duqian.spring.utils.TextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static site.duqian.spring.Constants.KEY_APP_NAME;

@Controller
@RequestMapping("/api")
public class WebApiController {
    private static final Logger logger = LoggerFactory.getLogger(WebApiController.class);// slf4j日志记录器

    @RequestMapping(value = "/init", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String initInfo(HttpServletRequest request, HttpServletResponse resp) {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        CommonParams commonParams = CommonUtils.getCommonParams(request, "realQueryReports");
        String jacocoDownloadDir = FileUtils.getJacocoDownloadDir();
        String dirPath = jacocoDownloadDir + File.separator + commonParams.getAppName();
        File rootFile = new File(dirPath);

        //返回APP名称列表,不存在时，返回默认的应用名称列表
        List<String> appList = new ArrayList<>();
        //appList.add("android");
        //appList.add("cc-audio");
        File downloadFile = new File(jacocoDownloadDir);
        if (downloadFile.exists() && downloadFile.listFiles() != null) {
            File[] files = downloadFile.listFiles();
            for (File file : files) {
                String fileName = file.getName();
                if (file.isDirectory() && !Constants.REPORT_DIR_NAME.equals(fileName) && !Constants.FILE_UPLOAD_ROOT_DIR.equals(fileName) && !fileName.startsWith("_") && !fileName.startsWith(".")) {
                    logger.debug("初始化，appName=" + fileName);
                    if (!appList.contains(fileName) && !TextUtils.isEmpty(fileName) && !"null".equalsIgnoreCase(fileName)) {
                        appList.add(fileName);
                    }
                }
            }
        }

        List<BranchItem> branchItems = new ArrayList<>();
        BranchListResp branchListResp = new BranchListResp(appList, branchItems);
        //存在文件
        if (rootFile.exists() && rootFile.isDirectory() && rootFile.listFiles() != null) {
            File[] files = rootFile.listFiles();
            if (files != null && files.length > 0) {//遍历文件夹下的所有分支名，按时间顺序来，最新的提交分支在最前面
                for (File file : files) {
                    String fileName = file.getName();
                    //非commitId无视。
                    if (file.isDirectory() && !Constants.EC_FILES_DIR_NAME.equalsIgnoreCase(fileName)) {
                        BranchItem branchItem = new BranchItem();
                        branchItem.setBranchLabel(fileName);
                        branchItem.setBranchName(fileName);
                        //获取最新和最旧的提交点
                        updateBranchCommit(branchItem, file);
                        branchItems.add(branchItem);
                    }
                }
                branchListResp = new BranchListResp(appList, branchItems);
            }
        }
        String branchJson = new Gson().toJson(branchListResp);
        logger.debug("初始化，获取branchJson=" + branchJson);

        initWhenLaunch();
        return branchJson;
    }

    private static void initWhenLaunch() {
        ThreadManager.getBackgroundPool().execute(() -> {
            String path = FileUtils.getJacocoDownloadDir();
            System.out.println("2.9.1，initWhenLaunch getJacocoDownloadDir=" + path);
            FileUtils.removeCacheFile(path);

            handleLocalLogFile(path);
        });
    }

    private static void handleLocalLogFile(String rootDir) {
        //根目录创建一个log文件，记录相关的事件
        String logFileName = "log.txt";
        String path = rootDir + logFileName;
        File logFile = new File(path);
        if (logFile.exists()) {
            //读取文件
            String fileContent = FileUtils.getFile(path);
            //计数
            Gson gson = new Gson();
            CommonLog commonLog = gson.fromJson(fileContent, CommonLog.class);
            if (commonLog == null) {
                commonLog = new CommonLog(0);
            }
            commonLog.setViewCount(commonLog.getViewCount() + 1);
            deleteCache(commonLog);
            logger.debug("log.txt " + commonLog);
            //写入文件
            fileContent = gson.toJson(commonLog);
            logger.debug("log.txt new fileContent" + fileContent);
            FileUtils.saveFile(rootDir, logFileName, fileContent);
        } else {
            try {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void deleteCache(CommonLog commonLog) {
        long lastDeleteTime = commonLog.getLastDeleteTime();
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - lastDeleteTime >= 7 * 24 * 60 * 60 * 1000L) {
            String path = FileUtils.getJacocoDownloadDir();
            File rootDir = new File(path);
            //todo 遍历，时间早于一周的，删除
            commonLog.setLastDeleteTime(currentTimeMillis);
        }
    }

    private void updateBranchCommit(BranchItem branchItem, File dir) {
        if (dir.exists() && dir.isDirectory() && dir.listFiles() != null) {
            File latestFile = null;
            File oldFile = null;
            long latestModifiedTime = 0;
            long oldModifiedTime = 0;
            try {
                List<String> commitList = new ArrayList<>();
                for (File file : dir.listFiles()) {
                    //有ec文件并且有class，src
                    if (file.isDirectory() && checkReportRes(file)) {
                        commitList.add(file.getName());
                        long lastModified = file.lastModified();
                        if (lastModified >= latestModifiedTime) {
                            latestFile = file;
                            latestModifiedTime = lastModified;
                            //logger.debug("/api/int latestModifiedTime=" + latestModifiedTime + ",latestFile=" + latestFile);
                        }

                        if (lastModified <= oldModifiedTime || oldModifiedTime == 0) {
                            oldFile = file;
                            oldModifiedTime = lastModified;
                            //logger.debug("/api/int oldModifiedTime=" + oldModifiedTime + ",oldFile=" + oldFile);
                        }
                    }
                }
                branchItem.setCommitList(commitList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //最新提交点
            if (latestFile != null) {
                String foundName = latestFile.getName();
                branchItem.setLatestCommit(foundName);
            }
            //最早的提交点
            if (oldFile != null) {
                String foundName = oldFile.getName();
                branchItem.setOldCommit(foundName);
            }
        }
    }

    private boolean checkReportRes(File dirFile) {
        if (dirFile == null) return false;
        String fileName = dirFile.getName();
        boolean isEcDir = Constants.EC_FILES_DIR_NAME.equalsIgnoreCase(fileName);
        return !isEcDir && fileName.length() >= 8;
    }
}