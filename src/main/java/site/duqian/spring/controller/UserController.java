package site.duqian.spring.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import site.duqian.spring.utils.CommonUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static site.duqian.spring.Constants.KEY_APP_NAME;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);// slf4j日志记录器

    @RequestMapping("/index")
    public String index() {
        return "index";
    }

    @RequestMapping("/static")
    public String test() {
        System.out.println("进入MainController中的方法！");
        return "index.html";
    }

    @RequestMapping(value = "/test", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String test(HttpServletRequest request, HttpServletResponse resp) {
        //遍历请求参数
        CommonUtils.printParams(request);
        String appName = request.getParameter(KEY_APP_NAME);
        log.debug("appName="+appName);

        //return "{\"result\":0,\"data\":\"duqian\"}";

        return "[{\"label\":\"dev_dq_#411671_coverage\",\"value\":\"dev_dq_#411671_coverage\"},{\"label\":\"dev\",\"value\":\"dev\"}]";
    }
}