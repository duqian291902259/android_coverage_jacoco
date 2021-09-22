package site.duqian.spring.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import site.duqian.spring.utils.CmdUtil;
import site.duqian.spring.utils.CommonUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static site.duqian.spring.Constants.KEY_APP_NAME;

@Controller
@RequestMapping("/coverage")
public class ReportController {
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);// slf4j日志记录器

    @RequestMapping(value = "/report", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String report(HttpServletRequest request, HttpServletResponse resp) {
        //遍历请求参数
        CommonUtils.printParams(request);
        String appName = request.getParameter(KEY_APP_NAME);
        log.debug("appName=" + appName);

        String rootDir = System.getProperty("user.dir");

        //todo-dq git clone pull update
        CmdUtil.runProcess("java -jar jacoco/jacococli.jar report /download/cc-android/3.8.1/coverage.exec --classfiles classes --sourcefiles /jacoco/git/app/src/main/java/ --html /src/main/resources/web/temp/cc");
        return "{\"result\":0,\"data\":\"success\"}";
    }
}