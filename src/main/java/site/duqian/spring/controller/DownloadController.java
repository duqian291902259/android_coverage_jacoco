package site.duqian.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import site.duqian.spring.utils.CommonUtils;
import site.duqian.spring.utils.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static site.duqian.spring.Constants.KEY_PARAM_FILENAME;
import static site.duqian.spring.Constants.KEY_PARAM_PATH;

/**
 * 文件下载管理
 */
@Controller
public class DownloadController {

    //http://192.168.56.1:8090/download?path=path
    @RequestMapping(value = "/download", method = {RequestMethod.GET})
    protected void queryEcFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        CommonUtils.printParams(request);
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        handleDownloadFile(request, resp);
    }

    //http://172.18.69.161:8090/download?path=/download/cc-android/&fileName=coverage.ec
    private void handleDownloadFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        String path = request.getParameter(KEY_PARAM_PATH);
        String filename = request.getParameter(KEY_PARAM_FILENAME);
        //如果是中文数据，需要转码。
        path = new String(path.getBytes("ISO8859-1"), StandardCharsets.UTF_8);
        filename = new String(filename.getBytes("ISO8859-1"), StandardCharsets.UTF_8);

        //得到保存文件的位置
        String fileRealPath = FileUtils.getJacocoDownloadDir() + path + filename;
        System.out.println("handleDownloadFile fileRealPath=" + fileRealPath);

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
        resp.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
    }
}