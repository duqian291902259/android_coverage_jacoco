package site.duqian.spring.controller;

import com.google.gson.Gson;
import org.apache.jasper.tagplugins.jstl.core.If;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;
import site.duqian.spring.bean.DeleteResponse;
import site.duqian.spring.bean.UploadResponse;
import site.duqian.spring.gitlab.GitLabService;
import site.duqian.spring.manager.DatabaseManager;
import site.duqian.spring.utils.CommonUtils;
import site.duqian.spring.utils.FileUtils;
import site.duqian.spring.utils.TextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;

import static site.duqian.spring.Constants.KEY_APP_NAME;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);// slf4j日志记录器

    @RequestMapping("/index")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/git", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String git(HttpServletRequest request, HttpServletResponse resp) {
        GitLabService.getGitlabService().testGitlabApi();
        return "{\"result\":0,\"data\":\"duqian\"}";
    }

    /**
     * http://jacoco.dev.duqian.cn/user/version
     */
    @RequestMapping(value = "/version", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String version(HttpServletRequest request, HttpServletResponse resp) {
        //遍历请求参数
        CommonUtils.printParams(request);
        String appName = request.getParameter(KEY_APP_NAME);
        log.debug("appName=" + appName);


        DatabaseManager.connect();
        // TODO: 2022/1/28 升级版本
        return "{\"result\":0,\"version\":\"3.0.1\"}";

        //return "[{\"label\":\"dev_dq_#411671_coverage\",\"value\":\"dev_dq_#411671_coverage\"},{\"label\":\"dev\",\"value\":\"dev\"}]";
    }

    private final Gson gson = new Gson();

    /**
     * http://127.0.0.1:8090/user/delete?dir=android/release_v3.9.10/ec2
     * http://127.0.0.1:8090/user/delete?path=android/release_v3.9.10/ec/jacoco_659b3ac0_.ec
     * 删除目录
     * http://jacoco.dev.duqian.cn/user/delete?dir=android/release_v3.9.10/ec
     * 删除文件
     * http://jacoco.dev.duqian.cn/user/delete?path=android/release_v3.9.10/ec/jacoco_5e94131c_37a80c7c8ad9d461.ec
     * <p>
     * {
     * code: 200,
     * msg: "delete=true",
     * filePath: "C:\AndroidDev\CCProjects\cc_coverage_server\dq-coverage\\android/release_v3.9.10/ec/jacoco_659b3ac0_.ec",
     * }
     */
    @RequestMapping(value = "/delete", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String delete(HttpServletRequest request, HttpServletResponse resp) {
        //遍历请求参数,可以按照path删除单个文件，也可以按照dir删除
        String path = request.getParameter(Constants.KEY_PARAM_PATH);
        String dir = request.getParameter(Constants.KEY_PARAM_DIR);
        String jacocoDownloadDir = FileUtils.getJacocoDownloadDir();
        //CommonParams commonParams = CommonUtils.getCommonParams(request, "delete");
        //String appName = commonParams.getAppName();
        //String dirPath = TextUtils.isEmpty(appName) ? jacocoDownloadDir : jacocoDownloadDir + File.separator + appName;
        String dirPath = jacocoDownloadDir;
        String deleteDir = dirPath + dir;
        if (dir != null && dir.equalsIgnoreCase(Constants.APP_CC_ANDROID)) {
            return "{\"result\":404,\"no permission,hasDelete\":\"false\"}";
        }
        if (deleteDir.equalsIgnoreCase(jacocoDownloadDir + File.separator + Constants.APP_CC_ANDROID) ||
                deleteDir.equalsIgnoreCase(jacocoDownloadDir)) {
            return "{\"result\":404,\"no permission,hasDelete\":\"false\"}";
        }
        if (!TextUtils.isEmpty(dir)) {
            boolean deleteDirectory = FileUtils.deleteDirectory(deleteDir);
            String msg = "delete=" + deleteDirectory + ",deleteDir=" + deleteDir;
            log.debug(msg);
            String result = gson.toJson(new DeleteResponse(200, msg, deleteDir));
            return result;
        }
        if (!TextUtils.isEmpty(path)) {
            String filePath = dirPath + File.separator + path;
            boolean deleteFile = FileUtils.deleteFile(filePath);
            String msg = "delete=" + deleteFile + ",delete filePath=" + filePath;
            log.debug(msg);
            String result = gson.toJson(new DeleteResponse(200, msg, filePath));
            return result;
        }
        return "{\"result\":0,\"hasDelete\":\"true\"}";
    }

    /**
     * http://127.0.0.1:8090/user/rename?path=android/release_v3.9.10/ec/jacoco_659b3ac0_1.ec&newpath=android/release_v3.9.10/ec/jacoco_659b3ac0_22.ec
     *
     * http://jacoco.dev.duqian.cn/user/rename?path=android/release_v3.9.10/ec/jacoco_5e94131c_37a80c7c8ad9d461.ec&&newpath=android/release_v3.9.10/ec/jacoco_5e94131c_37a80c7c8ad9d461_error.ec
     * {
     * code: 200,
     * msg: "rename=true,msg=true,rename C:\AndroidDev\CCProjects\cc_coverage_server\dq-coverage\\android/release_v3.9.10/ec/jacoco_659b3ac0_1.ec to newFilePath=C:\AndroidDev\CCProjects\cc_coverage_server\dq-coverage\\android/release_v3.9.10/ec/jacoco_659b3ac0_22.ec",
     * filePath: "",
     * }
     */
    @RequestMapping(value = "/rename", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String rename(HttpServletRequest request, HttpServletResponse resp) {
        //遍历请求参数,可以按照path删除单个文件，也可以按照dir删除
        String path = request.getParameter(Constants.KEY_PARAM_PATH);
        String newPath = request.getParameter(Constants.KEY_PARAM_NEW_PATH);
        //CommonParams commonParams = CommonUtils.getCommonParams(request, "rename");
        String jacocoDownloadDir = FileUtils.getJacocoDownloadDir();
        String oldFilePath = jacocoDownloadDir + path;
        String newFilePath = jacocoDownloadDir + newPath;
        if (oldFilePath.equalsIgnoreCase(jacocoDownloadDir)) {
            return "{\"result\":404,\"no permission,rename=\":\"false\"}";
        }
        if (!TextUtils.isEmpty(path)) {
            File oldFile = new File(oldFilePath);
            boolean renameTo = oldFile.renameTo(new File(newFilePath));
            String msg = "rename=" + renameTo + ",rename " + oldFilePath + " to newFilePath=" + newFilePath;
            log.debug(msg);
            String result = gson.toJson(new DeleteResponse(200, msg, ""));
            return result;
        }

        return "{\"result\":0,\"rename\":\"true\"}";
    }
}