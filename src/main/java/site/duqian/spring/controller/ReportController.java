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
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);

        //遍历请求参数
        //CommonUtils.printParams(request);
        Map<String, String> paramsMap = CommonUtils.parseRequestParams(request);
        String appName = paramsMap.get(Constants.KEY_APP_NAME);
        String branchName = paramsMap.get(Constants.KEY_BRANCH_NAME);
        String baseBranchName = paramsMap.get(Constants.KEY_BASE_BRANCH_NAME);
        System.out.println("parseRequestParams:branchName=" + branchName + ",appName=" + appName + ",baseBranchName=" + baseBranchName);

        //String rootDir = System.getProperty("user.dir");
        String sourceDir = FileUtil.getSaveDir(appName, branchName).getAbsolutePath();
        boolean checkGitWorkSpace = GitRepoUtil.checkGitWorkSpace(repositoryUrl, sourceDir+File.separator+"cc");
        String result = "";
        if (!checkGitWorkSpace) {
            try {
                Process process = CmdUtil.execute("git clone " + repositoryUrl+" "+sourceDir);
                result = CmdUtil.getText(process);
                System.out.println("clone end:" + result);
                if ("".equals(result)) {
                    result = "clone repository success!";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("checkGitWorkSpace=" + checkGitWorkSpace + ",result:" + result);

        //todo-dq git clone pull update
        CmdUtil.runProcess("java -jar jacoco/jacococli.jar report /download/cc-android/3.8.1/coverage.exec --classfiles classes --sourcefiles /jacoco/git/app/src/main/java/ --html /src/main/resources/web/temp/cc");
        return "{\"result\":0,\"data\":\"success\"}";
    }
}