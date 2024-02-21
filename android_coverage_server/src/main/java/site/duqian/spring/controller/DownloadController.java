package site.duqian.spring.controller;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import site.duqian.spring.Constants;
import site.duqian.spring.utils.CommonUtils;
import site.duqian.spring.utils.FileUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static site.duqian.spring.Constants.*;

/**
 * 文件下载管理
 */
@Controller
public class DownloadController {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DownloadController.class);

    //http://192.168.56.1:8090/download?path=path
    @RequestMapping(value = "/download", method = {RequestMethod.GET})
    protected void queryEcFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        handleDownloadFile(request, resp);
    }

    //http://127.0.0.1:8090/download?path=cc-pc%2Fdev%2F1a34635a%2Fpc_dev_1a34635a.zip
    //http://172.18.69.161:8090/download?path=download/android/coverage.ec
    private void handleDownloadFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> paramsMap = CommonUtils.parseRequestParams(request);
        String path = paramsMap.get(Constants.KEY_PARAM_PATH);
        path = URLDecoder.decode(path, StandardCharsets.UTF_8.toString());
        String fileType = paramsMap.get(KEY_PARAM_TYPE);
        logger.debug("download path=" + path);
        //如果是中文数据，需要转码。
        path = new String(path.getBytes("ISO8859-1"), StandardCharsets.UTF_8);
        //filename = new String(filename.getBytes("ISO8859-1"), StandardCharsets.UTF_8);

        //得到保存文件的位置
        String relativePath = path.replaceAll("&" + KEY_PARAM_FILENAME + "=", "");
        String rootDir = getDownloadRootDir(fileType, relativePath);
        String fileRealPath = rootDir + relativePath;
        System.out.println("handleDownloadFile fileRealPath=" + fileRealPath);

        //判断文件是否存在
        File file = new File(fileRealPath);
        if (!file.exists()) {
            resp.setStatus(404);
            resp.getWriter().write("error:file not exists");
            return;
        }

        String fileName = file.getName();
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
        logger.debug("download fileName=" + fileName);

        //设置消息头，告诉浏览器，这是下载的文件,点击下载后就会自动打开文件保存的窗口
        resp.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
    }

    private String getDownloadRootDir(String fileType, String relativePath) {
        String rootDir = "/";
        if (Constants.TYPE_FILE_CUSTOM.equalsIgnoreCase(fileType)) {
            rootDir = FileUtils.getUploadDir() + File.separator;
        } else {
            rootDir = FileUtils.getJacocoDownloadDir();
        }

        //报告管理的下载url有点特殊，包含了cc-jacoco
        if (relativePath.startsWith(Constants.CC_JACOCO_DIR)) {
            rootDir = FileUtils.getRootDir() + File.separator;
        }
        //cc-pc/dev/1a34635a/pc_dev_1a34635a.zip
        if (relativePath.contains(Constants.CC_IOS_DIR) || relativePath.contains(Constants.CC_PC_DIR)) {
            rootDir = FileUtils.getRootDir() + File.separator;
        }
        return rootDir;
    }
}