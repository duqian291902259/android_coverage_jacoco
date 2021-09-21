package site.duqian.spring.git;

import org.springframework.util.StringUtils;

public class PathUtils {
    /**
     获取类本地地址
     */
    public static String getClassFilePath(String baseDir, String version, String classPath) {
        StringBuilder builder = new StringBuilder(baseDir);
        if(!StringUtils.isEmpty(version)){
            builder.append("/");
            builder.append(version);
        }
        builder.append("/");
        builder.append(classPath);
        return builder.toString();
    }


    /**
     * 取远程代码本地存储路径
     */
    public static String getLocalDir(String repoUrl, String localBaseRepoDir, String version) {
        StringBuilder localDir = new StringBuilder(localBaseRepoDir);
        if (StringUtils.isEmpty(repoUrl)) {
            return "";
        }
        localDir.append("/");
        String repoName = repoUrl.substring(repoUrl.lastIndexOf("/")+1);
        localDir.append(repoName);
        if(!StringUtils.isEmpty(version)){
            localDir.append("/");
            localDir.append(version);
        }
        return localDir.toString();
    }
}
