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

    /**
     * 获取公共入参
     */
    public static CommonParams getCommonParams(HttpServletRequest request, String TAG) {
        Map<String, String> paramsMap = CommonUtils.parseRequestParams(request);
        String appName = paramsMap.get(Constants.KEY_APP_NAME);
        String versionCode = paramsMap.get(Constants.KEY_VERSION_CODE);
        String branchName = paramsMap.get(Constants.KEY_BRANCH_NAME);
        String commitId = paramsMap.get(Constants.KEY_COMMIT_ID);
        String type = paramsMap.get(Constants.KEY_PARAM_TYPE);

        if (commitId==null){
            commitId = "577082371ba3f40f848904baa39083f14b2695b0"; // TODO-dq: 2021/9/30 表单提交
        }
        CommonParams commonParams = new CommonParams(appName, versionCode, branchName, commitId, type);
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
