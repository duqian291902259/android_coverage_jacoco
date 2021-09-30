package site.duqian.spring.controller;

import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;
import site.duqian.spring.utils.CommonUtils;
import site.duqian.spring.utils.FileUtil;
import site.duqian.spring.utils.Md5Util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.util.Map;

@Controller
@RequestMapping("/coverage")
public class UploadQueryController {
    private String appName = "cc-android";
    private String versionCode = "3.8.1";
    private String branchName = "dev";//当前需要覆盖率报告的分支

    //URL_HOST + "/coverage/upload")
    @RequestMapping(value = "/upload", method = {RequestMethod.POST})
    @ResponseBody
    protected String upload(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        CommonUtils.printParams(request);
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        if ("get".equalsIgnoreCase(request.getMethod())) {
            return "{\"result\":404,\"data\":\"not supported\"}";
        }
        return handleUpload(request, resp);
    }

    //http://192.168.56.1:8090/coverage/queryFile?appName=duqian&versionCode=100&branch=dev
    @RequestMapping(value = "/queryFile", method = {RequestMethod.GET})
    protected void queryFile(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        CommonUtils.printParams(request);
        request.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        realQueryFile(request, resp);
    }

    /**
     * 本地server地址上传，一定要确保测试设备与server在同一个局域网
     */
    private String handleUpload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String responseMsg = "ok";
        try {
            Map<String, String> paramsMap = CommonUtils.parseRequestParams(request);
            String appName = paramsMap.get(Constants.KEY_APP_NAME);
            String versionCode = paramsMap.get(Constants.KEY_VERSION_CODE);
            String branchName = paramsMap.get(Constants.KEY_BRANCH_NAME);
            String commitId = paramsMap.get(Constants.KEY_COMMIT_ID);
            String type = paramsMap.get(Constants.KEY_PARAM_TYPE);

            if (appName == null || "".equals(appName)) {
                appName = this.appName;
            }
            if (branchName == null || "".equals(branchName)) {
                branchName = this.branchName;
            }
            CommonParams commonParams = new CommonParams(appName, versionCode, branchName, commitId, type);
            System.out.println("parseRequestParams:commonParams=" + commonParams);

            String dirPath = FileUtil.getSaveDir(commonParams);

            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(new File(dirPath));

            String contentType = request.getContentType();//"multipart/form-data"
            MultipartFile fileItem = ((StandardMultipartHttpServletRequest) request).getFile("file");
            System.out.println(",contentType=" + contentType + ",appName=" + appName + ",versionCode=" + versionCode);

            if (fileItem != null) {
                String fileName = fileItem.getOriginalFilename();
                InputStream inputStream = fileItem.getInputStream();
                System.out.println("fileName=" + fileName + ",inputStream=" + inputStream);
                fileName = saveFile(dirPath, fileName, inputStream);
                responseMsg = "{\"code\":200,\"msg\":\"upload success\",\"fileName\":" + fileName + "}";
            } else {
                responseMsg = "{\"code\":402,\"msg\":\"upload failed,file is null,appName=" + appName + " versionCode=" + versionCode + "\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseMsg = "{\"code\":401,\"msg\":\"upload failed，" + e.getMessage() + "\"}";
        }
        //todo-dq 中文乱码
        //responseMsg = new String(responseMsg.getBytes(), StandardCharsets.UTF_8);
        System.out.println("responseMsg=" + responseMsg);
        return responseMsg;
    }

    private String saveFile(String dirPath, String fileName, InputStream ins) throws IOException {
        //设置服务器端存放文件的位置
        File savedFile = new File(dirPath, fileName);
        String suffix = fileName.substring(fileName.lastIndexOf("."));
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
        String lastFileName = Md5Util.string2MD5(savedFile.getAbsolutePath());
        if (suffix.startsWith(".ec")) {
            File dest = new File(dirPath, lastFileName + suffix);
            if (dest.exists()) {
                dest.delete();
            }
            savedFile.renameTo(dest);
            System.out.println("saved to:" + dest.getAbsolutePath());
        } else {
            lastFileName = fileName;
            System.out.println("saved to:" + savedFile.getAbsolutePath());
        }
        return lastFileName;
    }

    private void realQueryFile(HttpServletRequest request, HttpServletResponse resp) throws IOException {
        String appName = request.getParameter(Constants.KEY_APP_NAME);
        String versionCode = request.getParameter(Constants.KEY_VERSION_CODE);
        String branchName = request.getParameter(Constants.KEY_BRANCH_NAME);
        String commitId = request.getParameter(Constants.KEY_COMMIT_ID);
        String type = request.getParameter(Constants.KEY_PARAM_TYPE);
        branchName = URLDecoder.decode(branchName, "utf-8");
        if (commitId == null || branchName == null) {
            resp.setStatus(401);
            resp.getWriter().write("error commitId is " + commitId + ",or branchName is null");
            return;
        }
        //设置状态码
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();
        CommonParams commonParams = new CommonParams(appName, versionCode, branchName, commitId, type);
        System.out.println("realQueryFile=" + commonParams);
        String dirPath = FileUtil.getSaveDir(commonParams);
        File rootFile = new File(dirPath);
        System.out.println("realQueryFile getSaveDir=" + rootFile.getAbsolutePath() + ",exists=" + rootFile.exists() + ",appName=" + appName + ",branchName=" + branchName);
        File[] files;
        if (!rootFile.exists() || isEmpty(files = rootFile.listFiles())) {
            out.println("{\"files\":[]}");
        } else {
            StringBuilder sb = new StringBuilder();
            for (File file : files) {
                if (!file.getName().startsWith(".")) {
                    //忽略隐藏文件? ,/rootDir/appName/branchName/commitId/&fileName=xxx
                    sb.append(Constants.KEY_PARAM_DOWNLOAD_DIR)
                            .append(appName).append(File.separator)
                            .append(branchName).append(File.separator)
                            .append(commitId).append(File.separator)
                            .append("&").append(Constants.KEY_PARAM_FILENAME).append("=").append(file.getName())
                            .append("\",");
                }
            }
            sb.delete(sb.length() - 1, sb.length());
            out.println(String.format("{\"files\":[%s]}", sb));
        }
        out.close();
    }

    public static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }
}