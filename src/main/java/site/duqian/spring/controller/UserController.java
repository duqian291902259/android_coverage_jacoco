package site.duqian.spring.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import site.duqian.spring.Utils.CommonUtils;
import site.duqian.spring.git_helper.JGitController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static site.duqian.spring.Constants.KEY_APP_NAME;
import static site.duqian.spring.Constants.KEY_PARAM_PATH;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);// slf4j日志记录器

    @RequestMapping("/index")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/test", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String test(HttpServletRequest request, HttpServletResponse resp) {
        //遍历请求参数
        CommonUtils.printParams(request);
        String appName = request.getParameter(KEY_APP_NAME);
        log.debug("appName="+appName);
        return "{\"result\":0,\"data\":\"duqian\"}";
    }
}