package site.duqian.spring.controller;

import com.google.gson.Gson;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;
import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;
import site.duqian.spring.bean.UploadResponse;
import site.duqian.spring.manager.ThreadManager;
import site.duqian.spring.utils.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.concurrent.Executor;

@Controller
@RequestMapping("/coverage")
public class UploadQueryController {

    private static final Logger logger = LoggerFactory.getLogger(UploadQueryController.class);// slf4j日志记录器

    private final Gson gson = new Gson();

    //URL_HOST + "/coverage/upload")
    @RequestMapping(value = "/upload", method = {RequestMethod.POST})
    @ResponseBody
    protected String upload(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
        //CommonUtils.printParams(request);
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(200);
        if ("get".equalsIgnoreCase(request.getMethod())) {
            return "{\"result\":404,\"data\":\"not supported\"}";
        }
        CommonParams commonParams = CommonUtils.getCommonParams(request, "upload");

        //上传了文件后，clone代码,todo-dq 全量才clone
        //updateRepoSource(commonParams);

        return handleUpload(request, commonParams);
    }

    private static boolean isCloning = false;

    private void updateRepoSource(CommonParams commonParams) {
        if (isCloning) {
            return;
        }
        //上传了ec文件才clone源码
        if (commonParams != null && Constants.TYPE_FILE_EC.equals(commonParams.getType())) {
            Executor prodExecutor = SpringContextUtil.get();
            prodExecutor.execute(() -> {
                isCloning = true;
                //后台执行clone代码的逻辑
                boolean cloneSrc = GitRepoUtil.cloneSrc(commonParams);
                System.out.println("cloneSrc=" + cloneSrc + ",commonParams=" + commonParams);
                logger.debug("cloneSrc=" + cloneSrc + ",commonParams=" + commonParams);
                isCloning = false;
            });
        }
    }

    //http://192.168.56.1:8090/coverage/queryFile?appName=duqian&versionCode=100&branch=dev&commitId=xxx
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
    private String handleUpload(HttpServletRequest request, CommonParams commonParams) throws IOException {
        request.setCharacterEncoding("UTF-8");
        String responseMsg = "ok";
        try {
            String dirPath = FileUtils.getSaveDir(commonParams);
            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(new File(dirPath));

            String contentType = request.getContentType();//"multipart/form-data"
            MultipartFile fileItem = ((StandardMultipartHttpServletRequest) request).getFile("file");
            System.out.println(",contentType=" + contentType);

            if (fileItem != null) {
                String fileName = fileItem.getOriginalFilename() != null ? fileItem.getOriginalFilename() : "" + System.currentTimeMillis() + ".temp";
                if (fileName != null && fileName.endsWith(Constants.TYPE_FILE_EC)) {
                    dirPath = FileUtils.getEcFilesDir(commonParams);
                }
                InputStream inputStream = fileItem.getInputStream();
                System.out.println("upload dirPath=" + dirPath + ",fileName=" + fileName);
                fileName = saveFile(commonParams, dirPath, fileName, inputStream);
                //优化返回，对象转json
                //responseMsg = "{\"code\":200,\"msg\":\"upload success\",\"fileName\":" + fileName + "}";
                responseMsg = gson.toJson(new UploadResponse(200, "upload success", fileName));
            } else {
                //responseMsg = "{\"code\":402,\"msg\":\"upload failed,fileItem is null" + "\"}";
                responseMsg = gson.toJson(new UploadResponse(402, "upload failed,fileItem is null", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            //responseMsg = "{\"code\":401,\"msg\":\"upload failed，" + e.getMessage() + "\"}";
            responseMsg = gson.toJson(new UploadResponse(401, "upload failed", e.getMessage()));
        }
        return responseMsg;
    }

    private String saveFile(CommonParams commonParams, String dirPath, String fileName, InputStream ins) throws IOException {
        //设置服务器端存放文件的位置
        File savedFile = new File(dirPath, fileName);
        int index = fileName.lastIndexOf(".");
        String suffix = index > 0 ? fileName.substring(index) : "";
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

        logger.debug("saved to:" + savedFile.getAbsolutePath());
        //解压classes/src文件夹
        unZipFile(commonParams, savedFile, suffix, parentFile);
        return fileName;
    }

    private void unZipFile(CommonParams commonParams, File savedFile, String suffix, File parentFile) {
        logger.debug("unZipFile suffix=" + suffix + ",savedFile=" + savedFile);
        ThreadManager.getBackgroundPool().execute(() -> realUnZipFile(commonParams, savedFile, suffix, parentFile));
    }

    private void realUnZipFile(CommonParams commonParams, File savedFile, String suffix, File parentFile) {
        //解压classes.zip-》classes  src.zip->src
        if (suffix.contains(Constants.TYPE_FILE_ZIP) || suffix.contains(Constants.TYPE_FILE_RAR)) {
            if (parentFile != null && savedFile != null && savedFile.length() > 0) {
                logger.debug("unZipFile,saveFile=" + savedFile);
                //删除classes根目录
                if (Constants.CLASS_ZIP_FILE_NAME.equals(savedFile.getName())) {
                    String classDir = FileUtils.getClassDir(commonParams);
                    boolean deleteDirectory = FileUtils.deleteDirectory(classDir);
                    logger.debug("unZipFile,deleteDirectory=" + deleteDirectory + ",classDir=" + classDir);
                }
                String zipFilePath = savedFile.getAbsolutePath();
                try {
                    String rootDir = parentFile.getAbsolutePath();
                    boolean unzip = FileUtils.unzip(rootDir, zipFilePath);
                    logger.debug("unZipFile =" + unzip + "，rootDir=" + rootDir + ",fileName=" + savedFile.getName());
                    FileUtils.tryServerUnZip(zipFilePath, unzip);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.debug("unZipFile =" + false + ",saveFile=" + savedFile);
                }
            }
        }
    }

    /**
     * 查询可下载的文件流列表,主要是ec
     */
    private void realQueryFile(HttpServletRequest request, HttpServletResponse resp) throws IOException {
        PrintWriter out = resp.getWriter();
        CommonParams commonParams = CommonUtils.getCommonParams(request, "realQueryFile");
        System.out.println("realQueryFile=" + commonParams);
        String dirPath = FileUtils.getSaveDir(commonParams);
        String type = commonParams.getType();
        boolean isEcFile = Constants.TYPE_FILE_EC.equals(type);
        if (isEcFile) {
            dirPath = FileUtils.getEcFilesDir(commonParams);
        }
        File rootFile = new File(dirPath);
        System.out.println("realQueryFile getSaveDir=" + rootFile.getAbsolutePath() + ",exists=" + rootFile.exists());
        File[] files = rootFile.listFiles();
        if (FileUtils.isEmpty(files)) {
            out.println("{\"files\":[]}");
        } else {
            StringBuilder sb = new StringBuilder();
            for (File file : files) {
                String fileName = file.getName();
                if (!fileName.startsWith(".") && fileName.contains(type + "")) {
                    //ec文件下载,/rootDir/appName/branchName/commitId/ec/xxx.ec
                    //ec文件下载,/rootDir/appName/branchName/ec/xxx.ec
                    //如果直接拼接路径给客户端，下载会被拒绝。org.apache.catalina.connector.ClientAbortException: java.io.IOException: 远程主机强迫关闭了一个现有的连接。
                    sb.append(commonParams.getAppName()).append(File.separator)
                            .append(commonParams.getBranchName()).append(File.separator);
                    if (isEcFile) {
                        //ec file改成了commitId外面存放了
                        sb.append(Constants.EC_FILES_DIR_NAME).append(File.separator);
                    } else {
                        sb.append(commonParams.getCommitId()).append(File.separator);
                    }
                    sb.append("&").append(Constants.KEY_PARAM_FILENAME).append("=")
                            .append(fileName)
                            .append(",");//\"
                }
            }
            if (sb.length() > 0) {
                sb.delete(sb.length() - 1, sb.length());
            }
            out.println(String.format("{\"files\":[%s]}", sb));
        }
        out.close();
    }
}