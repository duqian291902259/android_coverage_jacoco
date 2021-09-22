package site.duqian.spring.controller;

import org.apache.catalina.core.ApplicationPart;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import site.duqian.spring.Constants;
import site.duqian.spring.Utils.CommonUtils;
import site.duqian.spring.Utils.Md5Util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Controller
@RequestMapping("/WebServer/JacocoApi")
public class JacocoController {
    private String appName = "cc-android";
    private String versionCode = "3.8.1";
    private String branchName = "dev";//当前需要覆盖率报告的分支

    //URL_HOST + "/WebServer/JacocoApi/uploadEcFile")
    @RequestMapping(value = "/uploadEcFile", method = {RequestMethod.POST})
    @ResponseBody
    protected String uploadEcFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        CommonUtils.printParams(request);
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
            String appName = paramsMap.get(Constants.KEY_APP_NAME);
            String versionCode = paramsMap.get(Constants.KEY_VERSION_CODE);
            String branchName = paramsMap.get(Constants.KEY_BRANCH_NAME);
            System.out.println("parseRequestParams:branchName=" + branchName + ",versionCode=" + versionCode + ",versionCode=" + versionCode);

            if (appName == null || "".equals(appName)) {
                appName = this.appName;
            }
            if (branchName == null || "".equals(branchName)) {
                branchName = this.branchName;
            }
            String dirPath = getSaveDir(appName, branchName).getAbsolutePath();

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
                //((DiskFileItem) ((ApplicationPart) ((StandardMultipartHttpServletRequest.StandardMultipartFile) fileItem).part).fileItem).tempFile;
                //String fileName = MD5Utils.string2MD5(fileItem.get);
                InputStream inputStream = fileItem.getInputStream();
                System.out.println("fileName=" + fileName + ",inputStream=" + inputStream);
                fileName = saveFile(dirPath, fileName, inputStream);
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

    private String saveFile(String dirPath, String fileName, InputStream ins) throws IOException {
        //设置服务器端存放文件的位置
        File savedFile = new File(dirPath, fileName);
        System.out.println("save=" + savedFile.getAbsolutePath());

        File parentFile = savedFile.getParentFile();
        if (parentFile != null) {
            parentFile.mkdirs();//用于确保文件目录存在,如果为单级目录可以去掉
        }
        savedFile.createNewFile(); //创建新文件

        OutputStream ous = new FileOutputStream(savedFile); //输出

        byte[] buffer = new byte[1024]; //缓冲字节
        int len = 0;
        while ((len = ins.read(buffer)) > -1)
            ous.write(buffer, 0, len);
        ins.close();
        ous.close();
        String md5 = Md5Util.string2MD5(savedFile.getAbsolutePath());
        File dest = new File(dirPath, md5 + ".ec");
        if (dest.exists()) {
            dest.delete();
        }
        savedFile.renameTo(dest);
        return md5;
    }

    private void realQueryEcFile(HttpServletRequest request, HttpServletResponse resp) throws IOException {
        String appName = request.getParameter(Constants.KEY_APP_NAME);
        String verCode = request.getParameter(Constants.KEY_VERSION_CODE);
        String branchName = request.getParameter(Constants.KEY_BRANCH_NAME);
        if (appName == null || verCode == null) {
            resp.setStatus(401);
            resp.getWriter().write("error appName==null || versionCode==null");
            return;
        }
        //设置状态码
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();

        File f = getSaveDir(appName, branchName);
        System.out.println("realQueryEcFile getSaveDir=" + f.getAbsolutePath() + ",exists=" + f.exists() + ",appName=" + appName + ",verCode=" + verCode);
        File[] files;
        if (!f.exists() || isEmpty(files = f.listFiles())) {
            out.println("{\"files\":[]}");
        } else {
            StringBuilder sb = new StringBuilder();
            for (File file : files) {
                if (!file.getName().startsWith(".")) {//隐藏文件
                    //sb.append(Constants.fileDir).append(appName).append("/").append(verCode).append("/").append(file.getName()).append("\",");
                    sb.append(Constants.KEY_PARAM_DOWNLOAD_DIR).append(appName).append("/").append(verCode).append("/&" + Constants.KEY_PARAM_FILENAME + "=").append(file.getName()).append("\",");
                }
            }
            sb.delete(sb.length() - 1, sb.length());
            out.println(String.format("{\"files\":[%s]}", sb.toString()));
        }
        out.close();
    }

    private File getSaveDir(String appName, String branchName) {
        //rootDir=C:\Users\N20241/download/,rootDir2=D:\DusanAndroid\SpringWeb/download/,rootDir3=D:\DusanAndroid\SpringWeb/download/
        //String rootDir = System.getProperty("user.home") + fileDir;
        String rootDir = System.getProperty("user.dir") + Constants.KEY_PARAM_DOWNLOAD_DIR;
        System.out.println("rootDir=" + rootDir);
        return new File(rootDir, appName + "/" + branchName);
    }

    public static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }
}