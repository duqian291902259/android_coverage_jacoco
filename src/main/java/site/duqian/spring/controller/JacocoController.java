package site.duqian.spring.controller;

import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import site.duqian.spring.Utils.CommonUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Controller
@RequestMapping("/WebServer/JacocoApi")
public class JacocoController {
    private final String fileDir = "/download/";
    private String appName = "cc-android";
    private String versionCode = "3.8.1";
    private static final String KEY_APP_NAME = "appName";
    private static final String KEY_VERSION_CODE = "versionCode";

    //URL_HOST + "/WebServer/JacocoApi/uploadEcFile")
    @RequestMapping(value = "/uploadEcFile", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    protected String uploadEcFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        if ("get".equalsIgnoreCase(request.getMethod())) {
            return "{\"result\":404,\"data\":\"not supported\"}";
        }
        return realUploadEcFile(request, resp);
    }

    //http://192.168.56.1:8090/WebServer/JacocoApi/queryEcFile?appName=duqian&versionCode=100
    @RequestMapping(value = "/queryEcFile", method = {RequestMethod.GET})
    protected void queryEcFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        CommonUtils.printParams(request);
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        realQueryEcFile(request, resp);
    }

    /**
     * 本地server地址上传，一定要确保测试设备与server在同一个局域网
     */
    private String realUploadEcFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        //输出到客户端
        //PrintWriter out = response.getWriter();
        String responseMsg = "ok";
        try {
            Map<String, String> paramsMap = CommonUtils.parseRequestParams(request);
            String appName = paramsMap.get(KEY_APP_NAME);
            String versionCode = paramsMap.get(KEY_VERSION_CODE);
            System.out.println("parseRequestParams:appName=" + appName + ",versionCode=" + versionCode);

            if (appName == null || "".equals(appName)) {
                appName = this.appName;
            }
            if (versionCode == null || "".equals(versionCode)) {
                versionCode = this.versionCode;
            }
            String dirPath = getSaveDir(appName, versionCode).getAbsolutePath();

            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(new File(dirPath));

            // 这种方式获取file有问题
            /*ServletFileUpload fileUpload = new ServletFileUpload(factory);
            fileUpload.setSizeMax(30 * 1024 * 1024);
            List<FileItem> fileItemList = fileUpload.parseRequest(new ServletRequestContext(request));*/

            String contentType = request.getContentType();//"multipart/form-data"
            MultipartFile fileItem = ((StandardMultipartHttpServletRequest) request).getFile("file");
            System.out.println(",contentType=" + contentType + ",appName=" + appName + ",versionCode=" + versionCode);

            if (fileItem != null) {
                String fileName = fileItem.getOriginalFilename();
                InputStream inputStream = fileItem.getInputStream();
                System.out.println("fileName=" + fileName + ",inputStream=" + inputStream);
                saveFile(dirPath, fileName, inputStream);
                responseMsg = "{\"code\":200,\"msg\":\"上传成功\",\"fileName\":" + fileName + "}";
            } else {
                responseMsg = "{\"code\":402,\"msg\":\"上传失败,file is null,appName=" + appName + " versionCode=" + versionCode + "\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseMsg = "{\"code\":401,\"msg\":\"上传失败，" + e.getMessage() + "\"}";
        }
        //out.println(responseMsg);
        //out.close();
        return responseMsg;
    }

    private void saveFile(String dirPath, String fileName, InputStream ins) throws IOException {
        //设置服务器端存放文件的位置
        File locate = new File(dirPath, fileName);
        System.out.println("save=" + locate.getAbsolutePath());

        File parentFile = locate.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();//用于确保文件目录存在,如果为单级目录可以去掉
        }
        locate.createNewFile(); //创建新文件

        OutputStream ous = new FileOutputStream(locate); //输出

        byte[] buffer = new byte[1024]; //缓冲字节
        int len = 0;
        while ((len = ins.read(buffer)) > -1)
            ous.write(buffer, 0, len);
        ins.close();
        ous.close();
    }

    private void realQueryEcFile(HttpServletRequest request, HttpServletResponse resp) throws IOException {
        String appName = request.getParameter(KEY_APP_NAME);
        String verCode = request.getParameter(KEY_VERSION_CODE);
        if (appName == null || verCode == null) {
            resp.setStatus(401);
            resp.getWriter().write("error appName==null || versionCode==null");
            return;
        }
        //设置状态码
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();

        File f = getSaveDir(appName, verCode);
        System.out.println("realQueryEcFile getSaveDir=" + f.getAbsolutePath() + ",exists=" + f.exists() + ",appName=" + appName + ",verCode=" + verCode);
        File[] files;
        if (!f.exists() || isEmpty(files = f.listFiles())) {
            out.println("{\"files\":[]}");
        } else {
            StringBuilder sb = new StringBuilder();
            for (File file : files) {
                if (!file.getName().startsWith(".")) {//隐藏文件
                    sb.append(fileDir).append(appName).append("/").append(verCode).append("/").append(file.getName()).append("\",");
                }
            }
            sb.delete(sb.length() - 1, sb.length());
            out.println(String.format("{\"files\":[%s]}", sb.toString()));
        }
        out.close();
    }

    private File getSaveDir(String appName, String verCode) {
        //rootDir=C:\Users\N20241/download/,rootDir2=D:\DusanAndroid\SpringWeb/download/,rootDir3=D:\DusanAndroid\SpringWeb/download/
        //String rootDir = System.getProperty("user.home") + fileDir;
        String rootDir = System.getProperty("user.dir") + fileDir;
        //String path = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        System.out.println("rootDir=" + rootDir);
        return new File(rootDir, appName + "/" + verCode);
    }

    public static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }
}