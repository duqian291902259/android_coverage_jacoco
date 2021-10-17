package site.duqian.spring.controller;

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
import site.duqian.spring.utils.GitRepoUtil;
import site.duqian.spring.utils.CommonUtils;
import site.duqian.spring.utils.FileUtils;
import site.duqian.spring.utils.Md5Util;
import site.duqian.spring.utils.SpringContextUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.concurrent.Executor;

@Controller
@RequestMapping("/coverage")
public class UploadQueryController {
    private static final Logger logger = LoggerFactory.getLogger(UploadQueryController.class);// slf4j日志记录器

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
            Executor prodExecutor = (Executor) SpringContextUtil.getBean(Constants.THREAD_EXECUTOR_NAME);
            prodExecutor.execute(() -> {
                isCloning = true;
                //后台执行clone代码的逻辑
                boolean cloneSrc = GitRepoUtil.cloneSrc(commonParams);
                System.out.println("cloneSrc=" + cloneSrc + ",commonParams=" + commonParams);
                logger.debug("cloneSrc=" + cloneSrc + ",commonParams=" + commonParams);
                isCloning = false;
                //GitRepoUtil.checkOut(commonParams);
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
                String fileName = fileItem.getOriginalFilename();
                InputStream inputStream = fileItem.getInputStream();
                System.out.println("fileName=" + fileName + ",inputStream=" + inputStream);
                fileName = saveFile(dirPath, fileName, inputStream);
                responseMsg = "{\"code\":200,\"msg\":\"upload success\",\"fileName\":" + fileName + "}";
            } else {
                responseMsg = "{\"code\":402,\"msg\":\"upload failed,fileItem is null" + "\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseMsg = "{\"code\":401,\"msg\":\"upload failed，" + e.getMessage() + "\"}";
        }
        //todo-dq 中文乱码
        //responseMsg = new String(responseMsg.getBytes(), StandardCharsets.UTF_8);
        return responseMsg;
    }

    private String saveFile(String dirPath, String fileName, InputStream ins) throws IOException {
        //设置服务器端存放文件的位置
        File savedFile = new File(dirPath, fileName);
        int index = fileName.lastIndexOf(".");
        String suffix = index>0?fileName.substring(index):"";
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

        //解压classes/src文件夹
        unZipFile(savedFile, suffix, parentFile);

        return lastFileName;
    }

    private void unZipFile(File savedFile, String suffix, File parentFile) {
        Executor prodExecutor = (Executor) SpringContextUtil.getBean(Constants.THREAD_EXECUTOR_NAME);
        prodExecutor.execute(() -> {
            //解压classes.zip-》classes  src.zip->src
            if (suffix.contains(".zip") || suffix.contains(".rar")) {
                if (parentFile != null && savedFile.length() > 0) {
                    FileUtils.unzip(parentFile.getAbsolutePath(), savedFile.getAbsolutePath());
                }
            }
        });
    }

    /**
     * 查询可下载的文件流列表
     */
    private void realQueryFile(HttpServletRequest request, HttpServletResponse resp) throws IOException {
        //设置状态码
        PrintWriter out = resp.getWriter();
        CommonParams commonParams = CommonUtils.getCommonParams(request, "realQueryFile");
        System.out.println("realQueryFile=" + commonParams);
        String dirPath = FileUtils.getSaveDir(commonParams);
        File rootFile = new File(dirPath);
        System.out.println("realQueryFile getSaveDir=" + rootFile.getAbsolutePath() + ",exists=" + rootFile.exists());
        File[] files = rootFile.listFiles();
        if (FileUtils.isEmpty(files)) {
            out.println("{\"files\":[]}");
        } else {
            StringBuilder sb = new StringBuilder();
            for (File file : files) {
                String fileName = file.getName();
                if (!fileName.startsWith(".") && fileName.contains(commonParams.getType() + "")) {
                    //ec文件下载,/rootDir/appName/branchName/commitId/xxx.ec
                    /*String absolutePath = file.getAbsolutePath().replace(FileUtils.getJacocoDownloadDir(), "");
                    logger.debug("ec absolutePath="+absolutePath);
                    sb.append(absolutePath).append("\",");*/
                    sb.append(commonParams.getAppName()).append(File.separator)
                            .append(commonParams.getBranchName()).append(File.separator)
                            .append(commonParams.getCommitId()).append(File.separator)
                            .append("&").append(Constants.KEY_PARAM_FILENAME).append("=").append(fileName)
                            //.append(fileName)
                            .append("\",");
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