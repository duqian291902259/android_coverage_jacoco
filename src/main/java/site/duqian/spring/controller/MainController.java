package site.duqian.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 进入了但是并没有访问页面
 */
@Controller
public class MainController {

    @RequestMapping("/static")
    public String index(){
        System.out.println("进入MainController中的方法！");
        return "index.html";
    }
}