package site.duqian.spring.controller;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;
import site.duqian.spring.bean.FileListResp;
import site.duqian.spring.bean.ReportFileItem;
import site.duqian.spring.manager.ThreadManager;
import site.duqian.spring.utils.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文件管理，提供获取特定文件的功能，如报告
 */
@Controller
@RequestMapping("/coverage")
public class FileMgrController {
    private static final Logger logger = LoggerFactory.getLogger(FileMgrController.class);// slf4j日志记录器

    @RequestMapping(value = "/report/manager", method = {RequestMethod.GET})
    @ResponseBody
    protected String queryFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        return realQueryReports(request, resp);
    }

    /**
     * 查询可下载的文件流列表
     * {"reportUrl":"http://127.0.0.1:8080/cc-jacoco\\cc-ios/dev_coverage/37e9e766/diff-8cbe0fe8/",
     * "reportZipUrl":"http://127.0.0.1:8080/cc-jacoco\\cc-ios/dev_coverage/37e9e766/ios_37e9e766_8cbe0fe8.zip",
     * "result":0,"message":"","data":"IOS覆盖率报告已生成，请点击在线查阅或下载"}
     */
    private String realQueryReports(HttpServletRequest request, HttpServletResponse resp) throws IOException {
        CommonParams commonParams = CommonUtils.getCommonParams(request, "realQueryReports");
        //report/appName
        String dirPath = FileUtils.getReportRootDir() + File.separator + commonParams.getAppName();
        File rootFile = new File(dirPath);
        System.out.println("realQueryReports getSaveDir=" + rootFile.getAbsolutePath() + ",exists=" + rootFile.exists());

        List<ReportFileItem> list = new ArrayList<>();
        findReportZipInDir(rootFile, list);

        //按时间逆序,取前100个报告
        Collections.sort(list);
        if (list.size() > Constants.LIMIT_REPORT_NUM) {
            list = list.subList(0, Constants.LIMIT_REPORT_NUM);
        }

        //增加ios和pc的两个报告
        /*ReportFileItem iosReport = new ReportFileItem(Constants.IOS_REPORT_PATH, Constants.IOS_REPORT_FILE_NAME + ".zip", "2021-11-18 11:50");
        list.add(iosReport);
        ReportFileItem pcReport = new ReportFileItem(Constants.PC_REPORT_PATH, Constants.PC_REPORT_FILE_NAME + ".zip", "2021-11-18 10:20");
        list.add(pcReport);*/

        FileListResp fileListResp = new FileListResp(Constants.REPORT_SERVER_HOST_URL, list);
        logger.debug("fileListResp=" + fileListResp.getFileSize());
        String serverHostUrl = CommonUtils.getReportServerHost(commonParams);
        fileListResp.setReportHostUrl(serverHostUrl);
        return new Gson().toJson(fileListResp);
    }

    private void findReportZipInDir(File rootFile, List<ReportFileItem> list) {
        if (!rootFile.exists() || rootFile.isFile()) {
            return;
        }
        File[] files = rootFile.listFiles();
        if (files != null) {
            //遍历文件夹下的所有文件(包括子目录)，限定下遍历层级，要不然报告太多了很耗时
            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile() && file.exists()) {
                    if (fileName.contains("null")) {
                        continue;
                    }
                    if (fileName.endsWith(Constants.TYPE_FILE_ZIP) || fileName.endsWith(Constants.TYPE_FILE_RAR)) {
                        String path = file.getAbsolutePath();
                        int index = path.lastIndexOf(".");
                        String reportPath = path.substring(0, index);
                        File reportDirFile = new File(reportPath);
                        //修复反复解压的问题：release_v3.9.9_2c4cc227.zip，解压后的目录是report导致一致认为没有解压

                        if (!reportDirFile.exists()) {
                            //尝试解压或者直接不显示
                            ThreadManager.getBackgroundPool().execute(() -> {
                                boolean unzip = FileUtils.unzip(file.getParentFile().getAbsolutePath(), file.getAbsolutePath());
                                logger.debug("findReportZipInDir no report dir,unzip=" + file + "," + unzip);
                            });
                            //continue;
                        }
                        String reportRootDir = FileUtils.getRootDir();//FileUtils.getJacocoDownloadDir();
                        String basePath = path.replace(reportRootDir, "").replace(fileName, "");
                        long time = file.lastModified();
                        if (basePath.startsWith(File.separator)) {
                            int length = basePath.length();
                            basePath = basePath.substring(1, length);
                            //logger.debug("findReportZipInDir basePath2=" + basePath);
                        }

                        //时间格式,HH是24小时制，hh是AM PM12小时制
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        String dateString = sdf.format(new Date(time));

                        ReportFileItem reportFileItem = new ReportFileItem(basePath, fileName, dateString);
                        reportFileItem.setModifyTime(time);
                        //logger.debug("ReportFileItem=" + reportFileItem);
                        list.add(reportFileItem);
                    }
                } else {
                    //防止遍历已经解压的报告的文件夹
                    if (!fileName.startsWith("com")) {
                        findReportZipInDir(file, list);
                    } else {//有一个子目录为com，就直接退出遍历当前父目录
                        break;
                    }
                }
            }
        }
    }
}