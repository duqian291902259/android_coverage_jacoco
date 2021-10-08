package site.duqian.spring.utils;

import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Description:通用工具类
 *
 * @author n20241 Created by 杜小菜 on 2021/9/30 - 11:31 .
 * E-mail: duqian2010@gmail.com
 */
public class CommonUtils {

    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * 获取公共入参
     */
    public static CommonParams getCommonParams(HttpServletRequest request, String TAG) {
        Map<String, String> paramsMap = CommonUtils.parseRequestParams(request);
        String appName = paramsMap.get(Constants.KEY_APP_NAME);
        String versionCode = paramsMap.get(Constants.KEY_VERSION_CODE);
        String branchName = paramsMap.get(Constants.KEY_BRANCH_NAME);
        String commitId = paramsMap.get(Constants.KEY_COMMIT_ID);
        String commitId2 = paramsMap.get(Constants.KEY_COMMIT_ID2);
        String type = paramsMap.get(Constants.KEY_PARAM_TYPE);
        CommonParams commonParams = new CommonParams(appName, versionCode, branchName, commitId, type);
        boolean incremental = Boolean.parseBoolean(request.getParameter(Constants.KEY_PARAM_INCREMENTAL));
        commonParams.setIncremental(incremental);
        commonParams.setCommitId2(commitId2);
        if (incremental) {// TODO: 2021/10/8 优化diff文件夹的名称，以第二个commit命名？
            String fileName = Constants.DIFF_DIR_NAME + commitId2;//System.currentTimeMillis();
            commonParams.setDiffFileName(fileName);
        }
        System.out.println(TAG + " parseRequestParams=" + commonParams);
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
}
