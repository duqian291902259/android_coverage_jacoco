package site.duqian.spring.controller;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;
import site.duqian.spring.git_helper.GitRepoUtil;
import site.duqian.spring.utils.CmdUtil;
import site.duqian.spring.utils.CommonUtils;
import site.duqian.spring.utils.FileUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

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

        //生成报告，失败的原因可能是找不到class,src,ec
        boolean generateReport = generateReport(commonParams);
        //printWriter = new PrintWriter(resp.getWriter());
        //printWriter.write("cloning or update repository");
        String msg = "{\"cmd\":0,\"data\":\"success\"}";
        if (!generateReport) {
            msg = "{\"cmd\":0,\"data\":\"generate report failed.\"}";
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
            //todo copy出指定的class文件到另外的目录
        }

        boolean isGenerated = CmdUtil.generateReportByCmd(jarPath,
                execPath,
                classesPath,
                srcPath,
                reportPath);
        System.out.println("generateReport=" + isGenerated);
        return isGenerated;
    }
}