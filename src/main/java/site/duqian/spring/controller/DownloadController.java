package site.duqian.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import site.duqian.spring.Utils.CommonUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 文件下载管理
 */
@Controller
public class DownloadController {
    private static final String KEY_PARAM_PATH = "path";
    private final String fileDir = "/download/";

    @RequestMapping("/static")
    public String index() {
        System.out.println("进入MainController中的方法！");
        return "index.html";
    }

    //http://192.168.56.1:8090/download?path=path
    @RequestMapping(value = "/download", method = {RequestMethod.GET})
    protected void queryEcFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        CommonUtils.printParams(request);
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        handleDownloadFile(request, resp);
    }

    private void handleDownloadFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        String path = request.getParameter(KEY_PARAM_PATH);
        //如果是中文数据，需要转码。
        path = new String(path.getBytes("ISO8859-1"), StandardCharsets.UTF_8);
        System.out.println("handleDownloadFile path=" + path);

        //得到保存文件的位置
        String fileRealPath = getSaveDir() +File.separator+ path;
        //通过文件名拿到文件绝对路径
        System.out.println(fileRealPath);

        //判断文件是否存在
        File file = new File(fileRealPath);
        if (!file.exists()) {
            resp.setStatus(404);
            resp.getWriter().write("error:file not exists");
            return;
        }

        //读取该文件并把数据写给浏览器
        FileInputStream inputStream = null;
        ServletOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(fileRealPath);
            outputStream = resp.getOutputStream();
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, len);
            }
        } catch (Exception e) {
            System.out.println("handleDownloadFile error=" + e);
        } finally {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
        }

        //设置消息头，告诉浏览器，这是下载的文件
        String name = path;
        resp.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(name, "UTF-8"));
    }

    private String getSaveDir() {
        //rootDir=C:\Users\N20241/download/,rootDir2=D:\DusanAndroid\SpringWeb/download/,rootDir3=D:\DusanAndroid\SpringWeb/download/
        String rootDir = System.getProperty("user.dir");
        System.out.println("rootDir=" + rootDir);
        return rootDir;
    }
}