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
import java.util.concurrent.Executor;

@Controller
@RequestMapping("/coverage")
public class ReportController {

    private static final org.slf4j.Logger Log = LoggerFactory.getLogger(GitRepoUtil.class);

    @RequestMapping(value = "/report", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String report(HttpServletRequest request, HttpServletResponse resp) throws Exception {
        request.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);

        boolean incremental = Boolean.parseBoolean(request.getParameter(Constants.KEY_PARAM_INCREMENTAL));
        CommonParams commonParams = CommonUtils.getCommonParams(request, "report");
        commonParams.setIncremental(incremental);

        if (commonParams.getCommitId() == null) {
            commonParams.setCommitId("577082371ba3f40f848904baa39083f14b2695b0"); // TODO-dq: 2021/9/30 表单提交为空，获取最新的？
        }
        //生成报告，失败的原因可能是找不到class,src,ec
        boolean generateReport = generateReport(commonParams);
        //printWriter = new PrintWriter(resp.getWriter());
        //printWriter.write("cloning or update repository");
        String msg = "{\"result\":0,\"data\":\"success\"}";
        if (!generateReport) {
            msg = "{\"result\":0,\"data\":\"generate report failed.\"}";
        } else {
            //返回报告的预览路径和下载url
            String reportRelativePath = FileUtil.getReportRelativePath(commonParams);
            String reportUrl = Constants.REPORT_SERVER_HOST_URL + reportRelativePath + File.separator + Constants.REPORT_DIR_NAME;
            String reportZipUrl = Constants.REPORT_SERVER_HOST_URL + reportRelativePath + File.separator + FileUtil.getReportZipFileName(commonParams);
            //reportUrl = URLEncoder.encode(reportUrl, "UTF-8");
            //reportZipUrl = URLEncoder.encode(reportZipUrl, "UTF-8");
            ReportResponse reportResponse = new ReportResponse(reportUrl, reportZipUrl);
            msg = new Gson().toJson(reportResponse);
        }
        String logMsg = "handle report=" + msg + ",incremental=" + incremental;
        System.out.println(logMsg);
        Log.debug(logMsg);
        return msg;
    }

    /**
     * 生成报告，合并ec文件
     *
     * @param commonParams 路径动态配置
     */
    private boolean generateReport(CommonParams commonParams) {
        String reportPath = FileUtil.getJacocoReportPath(commonParams);
        File reportIndexFile = new File(reportPath + File.separator + "index.html");
        if (reportIndexFile.exists()) {
            System.out.println("has generateReport=" + true);
            String reportZipPath = FileUtil.getReportZipPath(commonParams);
            if (!new File(reportZipPath).exists()) {
                zipReport(reportPath, commonParams);
            }
            return true;
        }

        String jarPath = FileUtil.getJacocoJarPath();
        String execPath = FileUtil.getEcFilesDir(commonParams) + File.separator + "**.ec";
        String classesPath = FileUtil.getClassDir(commonParams);
        String srcPath = FileUtil.getSourceDir(commonParams);
        File classFile = new File(classesPath);
        if (!classFile.exists()) {
            String saveDir = FileUtil.getSaveDir(commonParams);
            FileUtil.unzip(saveDir, saveDir + File.separator + "classes.zip");
        }
        if (commonParams.isIncremental()) {
            //todo diff 报告  copy出指定的class文件到另外的目录 , 删除web里面的临时报告 html
        }

        boolean isGenerated = CmdUtil.generateReportByCmd(jarPath,
                execPath,
                classesPath,
                srcPath,
                reportPath);
        System.out.println("generateReport=" + isGenerated);
        if (isGenerated) {
            zipReport(reportPath, commonParams);
        }
        return isGenerated;
    }

    private void zipReport(String reportPath, CommonParams commonParams) {
        Executor prodExecutor = (Executor) SpringContextUtil.getBean(Constants.THREAD_EXECUTOR_NAME);
        prodExecutor.execute(() -> {
            String reportZipPath = FileUtil.getReportZipPath(commonParams);
            try {
                File file = new File(reportZipPath);
                //存在不处理
                if (file.exists() && file.length() > 0 && file.isFile()) {
                    return;
                }
                FileUtil.zipFolder(reportPath, reportZipPath);
            } catch (Exception e) {
                Log.error(commonParams + ",zipReport error " + e);
            }
        });
    }
}