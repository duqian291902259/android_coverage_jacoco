package site.duqian.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import site.duqian.spring.bean.CommonParams;
import site.duqian.spring.utils.CmdUtil;
import site.duqian.spring.utils.CommonUtils;
import site.duqian.spring.utils.FileUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;

@Controller
@RequestMapping("/coverage")
public class ReportController {

    @RequestMapping(value = "/report", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String report(HttpServletRequest request, HttpServletResponse resp) throws Exception {
        request.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);

        CommonParams commonParams = CommonUtils.getCommonParams(request, "report");

        //生成报告，失败的原因可能是找不到class,src,ec
        generateReport(commonParams);
        //printWriter = new PrintWriter(resp.getWriter());
        //printWriter.write("cloning or update repository");
        String msg = "{\"cmd\":0,\"data\":\"success\"}";
        System.out.println("handle report=" + msg);

        return msg;
    }

    /**
     * 生成报告，合并ec文件
     *
     * @param commonParams 路径动态配置
     */
    private void generateReport(CommonParams commonParams) {
        String jarPath = FileUtil.getJacocoJarPath();
        String execPath = FileUtil.getEcFilesDir(commonParams) + File.separator + "**.ec";
        String classesPath = FileUtil.getClassDir(commonParams);
        String srcPath = FileUtil.getSourceDir(commonParams);
        String reportPath = FileUtil.getJacocoReportPath(commonParams);
        boolean isGenerated = CmdUtil.generateReportByCmd(jarPath,
                execPath,
                classesPath,
                srcPath,
                reportPath);
        System.out.println("generateReport=" + isGenerated);
    }
}