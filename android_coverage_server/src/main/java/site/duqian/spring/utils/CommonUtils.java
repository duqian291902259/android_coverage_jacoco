package site.duqian.spring.utils;

import org.slf4j.LoggerFactory;
import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Description:通用工具类
 *
 * @author n20241 Created by 杜小菜 on 2021/9/30 - 11:31 .
 * E-mail: duqian2010@gmail.com
 */
public class CommonUtils {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * 获取公共入参
     */
    public static CommonParams getCommonParams(HttpServletRequest request, String TAG) {
        //String requestURI = request.getRequestURI();//这个只是相对路径

        Map<String, String> paramsMap = CommonUtils.parseRequestParams(request);
        String appName = paramsMap.get(Constants.KEY_APP_NAME);
        String branchName = paramsMap.get(Constants.KEY_BRANCH_NAME);
        String baseBranchName = paramsMap.get(Constants.KEY_BASE_BRANCH_NAME);
        String commitId = paramsMap.get(Constants.KEY_COMMIT_ID);
        String commitId2 = paramsMap.get(Constants.KEY_COMMIT_ID2);
        String type = paramsMap.get(Constants.KEY_PARAM_TYPE);
        String uid = paramsMap.get(Constants.KEY_PARAM_UID);
        String userName = paramsMap.get(Constants.KEY_PARAM_USER_NAME);
        String os = paramsMap.get(Constants.KEY_PARAM_OS);
        String uploadDir = paramsMap.get(Constants.KEY_UPLOAD_DIR);
        if (commitId != null && commitId.length() >= 8) {
            commitId = commitId.substring(0, 8);
        }
        if (commitId2 != null && commitId2.length() >= 8) {
            commitId2 = commitId2.substring(0, 8);
        }
        CommonParams commonParams = new CommonParams(appName, branchName, commitId, type);
        boolean incremental = Boolean.parseBoolean(request.getParameter(Constants.KEY_PARAM_INCREMENTAL));
        commonParams.setIncremental(incremental);
        commonParams.setCommitId2(commitId2);
        commonParams.setBaseBranchName(baseBranchName);
        commonParams.setUid(uid);
        commonParams.setUserName(userName);
        commonParams.setOs(os);
        commonParams.setUploadDir(uploadDir);

        String requestUrl = "";
        try {
            requestUrl = request.getRequestURL().toString();
        } catch (Exception e) {
            logger.debug("get requestUrl error " + e);
        }
        commonParams.setRequestUrl(requestUrl);
        if (incremental) {//优化diff文件夹的名称，以第二个commit命名
            String split = Constants.FILE_NAME_SPLIT;
            String preName = commonParams.getBranchId() + split + commitId + split + Constants.DIFF_DIR_NAME + split;
            String fileName = preName + commitId2;
            if (!TextUtils.isEmpty(baseBranchName)) {
                fileName = preName + baseBranchName;
            }
            commonParams.setDiffFileName(fileName);
        }
        System.out.println(TAG + "getCommonParams=" + commonParams);
        return commonParams;
    }

    public static void printParams(HttpServletRequest request) {
        //遍历请求参数
        Set<Map.Entry<String, String>> set = parseRequestParams(request).entrySet();
        for (Map.Entry<String, String> entry : set) {
            String key = entry.getKey();
            if (!key.equals("submit")) {
                System.out.println("param:key=" + key + ",value=" + entry.getValue());
            }
        }
    }

    public static Map<String, String> parseRequestParams(HttpServletRequest request) {
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

    public static boolean isWindowsOS() {
        return System.getProperties().getProperty("os.name").toLowerCase().contains("windows");
    }

    public static String getReportServerHost(CommonParams commonParams) {
        String serverHostUrl = Constants.REPORT_SERVER_HOST_URL;
        String requestUrl = commonParams.getRequestUrl();
        if (!TextUtils.isEmpty(requestUrl)) {
            String host = commonParams.getHost();
            String port = commonParams.getPort();
            String path = commonParams.getPath();
            logger.debug("host=" + host + ",port=" + port + ",path=" + path);
            if (requestUrl.contains(Constants.SERVER_HOST_DOMAIN)) {
                serverHostUrl = Constants.REPORT_SERVER_HOST_URL;
            } else {
                serverHostUrl = Constants.LOCAL_REPORT_HOST_URL;
                String requestPort = "8090";
                if (requestUrl.contains(requestPort)) {
                    //serverHostUrl = Constants.LOCAL_REPORT_HOST_URL.replace(requestPort, "8080");
                    serverHostUrl = requestUrl.replace(path, "").replace(requestPort, "8080");
                }
            }
        }
        logger.debug("getReportServerHost requestUrl=" + requestUrl + ",serverHostUrl=" + serverHostUrl);
        return serverHostUrl;
    }
}
