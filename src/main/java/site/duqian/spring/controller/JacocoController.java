package site.duqian.spring.controller;

import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
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
    @RequestMapping(value = "/uploadEcFile", method = {RequestMethod.POST})
    protected void uploadEcFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        realUploadEcFile(request, resp);
    }

    //http://192.168.56.1:8090/WebServer/JacocoApi/queryEcFile?appName=duqian&versionCode=100
    @RequestMapping(value = "/queryEcFile", method = {RequestMethod.GET})
    protected void queryEcFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        printParams(request);
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        realQueryEcFile(request, resp);
    }

    private void realUploadEcFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        //输出到客户端
        PrintWriter out = response.getWriter();
        try {
            Map<String, String> paramsMap = parseRequestParams(request);
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
            //factory.setSizeThreshold(300 * 1024 * 1024);//无效
            factory.setRepository(new File(dirPath));

            ServletFileUpload fileUpload = new ServletFileUpload(factory);
            //fileUpload.setSizeMax(30 * 1024 * 1024);

            String contentType = request.getContentType();//"multipart/form-data"
            // 这种方式获取file有问题
            //List<FileItem> fileItemList = fileUpload.parseRequest(new ServletRequestContext(request));

            MultipartFile fileItem = ((StandardMultipartHttpServletRequest) request).getFile("file");
            System.out.println(",contentType=" + contentType + ",appName=" + appName + ",versionCode=" + versionCode);

            if (fileItem != null) {
                String fileName = fileItem.getOriginalFilename();
                InputStream inputStream = fileItem.getInputStream();
                System.out.println("fileName=" + fileName + ",inputStream=" + inputStream);
                saveFile(dirPath, fileName, inputStream);
                out.println("{\"code\":200,\"msg\":\"上传成功\",\"dirPath\":\"dirPath\"}");
                //fileItem.delete();
            } else {
                out.println("{\"code\":402,\"msg\":\"上传失败,file is null,appName=" + appName + " versionCode=" + versionCode + "\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("{\"code\":401,\"msg\":\"上传失败，" + e.getMessage() + "\"}");
        }
        out.close();

    }

    private void saveFile(String dirPath, String fileName, InputStream ins) throws IOException {
        //String fileName = fileItem.getName();
        //String remoteFilename = new String(fileName.getBytes(), "UTF-8");
        //File remoteFile = new File(remoteFilename);
        //String saveFileName = remoteFile.getName();

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
                    sb.append("\"/download/").append(appName).append("/").append(verCode).append("/").append(file.getName()).append("\",");
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

    private void printParams(HttpServletRequest request) {
        //遍历请求参数
        Set<Map.Entry<String, String>> set = parseRequestParams(request).entrySet();
        for (Map.Entry<String, String> entry : set) {
            String key = entry.getKey();
            if (!key.equals("submit")) {
                System.out.println("param:key=" + key + ",value=" + entry.getValue());
                /*if (KEY_APP_NAME.equals(key)) {
                    appName = entry.getValue();
                } else if (KEY_VERSION_CODE.equals(key)) {
                    versionCode = entry.getValue();
                }*/
            }
        }
    }

    private Map<String, String> parseRequestParams(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() != 0) {
                    map.put(paramName, paramValue);
                }
            }
        }
        return map;
    }
}