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
import site.duqian.spring.utils.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static site.duqian.spring.utils.FileUtils.deleteDirectory;

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
        CommonUtils.printParams(request);
        request.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        return realQueryReports(request, resp);
    }

    /**
     * 查询可下载的文件流列表
     */
    private String realQueryReports(HttpServletRequest request, HttpServletResponse resp) throws IOException {
        //PrintWriter out = resp.getWriter();        //out.close();
        CommonParams commonParams = CommonUtils.getCommonParams(request, "realQueryReports");
        //report/appName
        String dirPath = FileUtils.getReportRootDir() + File.separator + commonParams.getAppName();
        File rootFile = new File(dirPath);
        System.out.println("realQueryFile getSaveDir=" + rootFile.getAbsolutePath() + ",exists=" + rootFile.exists());

        List<ReportFileItem> list = new ArrayList<>();
        findReportZipInDir(rootFile, list);
        FileListResp fileListResp = new FileListResp(Constants.REPORT_SERVER_HOST_URL, list);
        logger.debug("fileListResp=" + fileListResp.getFileSize());
        return new Gson().toJson(fileListResp);
    }

    private void findReportZipInDir(File rootFile, List<ReportFileItem> list) {
        if (!rootFile.exists() || rootFile.isFile()) {
            return;
        }
        File[] files = rootFile.listFiles();
        if (files != null) {
            //遍历文件夹下的所有文件(包括子目录)
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    if (fileName.endsWith(Constants.TYPE_FILE_ZIP) || fileName.endsWith(Constants.TYPE_FILE_RAR)) {
                        String path = file.getAbsolutePath();
                        String basePath = path.replace(FileUtils.getJacocoDownloadDir(), "").replace(fileName, "");
                        Calendar cal = Calendar.getInstance();
                        long time = file.lastModified();
                        cal.setTimeInMillis(time);
                        String date = cal.getTime().toLocaleString();
                        ReportFileItem reportFileItem = new ReportFileItem(basePath, fileName,date);
                        logger.debug("ReportFileItem=" + reportFileItem);
                        list.add(reportFileItem);
                    }
                } else {
                    findReportZipInDir(file, list);
                }
            }
        }
    }
}