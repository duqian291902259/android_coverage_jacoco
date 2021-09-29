package site.duqian.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import site.duqian.spring.Constants;
import site.duqian.spring.git_helper.GitRepoUtil;
import site.duqian.spring.utils.CmdUtil;
import site.duqian.spring.utils.CommonUtils;
import site.duqian.spring.utils.FileUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Map;

@Controller
@RequestMapping("/coverage")
public class ReportController {

    private static final String repositoryUrl = "https://git-cc.nie.netease.com/android/cc.git";

    @RequestMapping(value = "/report", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String report(HttpServletRequest request, HttpServletResponse resp) throws Exception {
        request.setCharacterEncoding("UTF-8");
        //遍历请求参数
        //CommonUtils.printParams(request);
        Map<String, String> paramsMap = CommonUtils.parseRequestParams(request);
        String appName = paramsMap.get(Constants.KEY_APP_NAME);
        String branchName = paramsMap.get(Constants.KEY_BRANCH_NAME);
        String baseBranchName = paramsMap.get(Constants.KEY_BASE_BRANCH_NAME);
        System.out.println("parseRequestParams:branchName=" + branchName + ",appName=" + appName + ",baseBranchName=" + baseBranchName);

        //git clone pull update
        //cloneSrc(appName, branchName);
        generateReport(appName, branchName);

        String msg = "{\"cmd\":0,\"data\":\"success\"}";
        System.out.println("handle report=" + msg);
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        return msg;
    }

    private void cloneSrc(String appName, String branchName) {
        String sourceDir = FileUtil.getSaveDir(appName, branchName + "_src").getAbsolutePath();
        boolean checkGitWorkSpace = GitRepoUtil.checkGitWorkSpace(repositoryUrl, sourceDir + File.separator + "cc");
        System.out.println("checkGitWorkSpace " + checkGitWorkSpace);
        try {
            String cmd = "";
            String cmdPull = "git -C " + sourceDir + " pull";
            if (!checkGitWorkSpace) {
                cmd = "git clone -b " + branchName + " " + repositoryUrl + " " + sourceDir;
            } else {
                cmd = cmdPull;
            }
            //printWriter = new PrintWriter(resp.getWriter());
            //printWriter.write("cloning or update repository");
            System.out.println("runProcess cmd:" + cmd);
            int result = CmdUtil.runProcess(cmd);
            if (!checkGitWorkSpace && result == 128) {
                CmdUtil.runProcess(cmdPull);
                System.out.println("cmdPull:" + result);
            }
            System.out.println("clone or update end:" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateReport(String appName, String branchName) {
        // TODO: 2021/9/23 合并ec为一个文件 路径动态配置
        String rootDir = System.getProperty("user.dir") + File.separator;
        String jarPath = rootDir + "jacococli.jar";
        //String execPath = rootDir + "download/cc-android/dev_dq_#411671_coverage/8ab3adfcec889990a6db1fbaae361d59.ec";
        String execPath = rootDir + "download/cc-android/dev_dq_#411671_coverage/**.ec";
        String classesPath = rootDir + "jacoco/classes/";
        String srcPath = rootDir + "jacoco/tempSrc/main/java/";
        String reportPath = rootDir + "jacoco/report";
        boolean isGenerated = CmdUtil.generateReportByCmd(jarPath,
                execPath,
                classesPath,
                srcPath,
                reportPath);
        System.out.println("generateReport=" + isGenerated);
    }
}