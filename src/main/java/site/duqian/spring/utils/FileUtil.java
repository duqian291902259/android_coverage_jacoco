package site.duqian.spring.utils;

import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;

import java.io.File;

/**
 * Description:文件、存储路径管理
 *
 * @author n20241 Created by 杜小菜 on 2021/9/30 - 10:02 .
 * E-mail: duqian2010@gmail.com
 */
public class FileUtil {

    public static String getRootDir() {
        String rootDir = System.getProperty("user.dir");
        File parentFile = new File(rootDir).getParentFile();
        if (parentFile != null) {
            rootDir = parentFile.getAbsolutePath();
        }
        System.out.println("getRootDir=" + rootDir);
        return rootDir;
    }

    public static String getSaveDir(CommonParams commonParams) {
        String rootDir = getRootDir() + Constants.KEY_PARAM_DOWNLOAD_DIR + commonParams.getAppName() + File.separator + commonParams.getBranchName() + File.separator + commonParams.getCommitId();
        System.out.println("getSaveDir=" + rootDir);
        return rootDir;
    }

    public static File getSaveDir(String appName, String branchName) {
        String rootDir = getRootDir() + Constants.KEY_PARAM_DOWNLOAD_DIR;
        System.out.println("getSaveDir=" + rootDir);
        return new File(rootDir, appName + File.separator + branchName);
    }

    public static String getFileSuffixByType(String typeString) {
        String suffix = ".ec";
        int type = 0;
        try {
            type = Integer.parseInt(typeString);
        } catch (Exception e) {
        }
        if (type == Constants.TYPE_FILE_EC) {
            suffix = ".ec";
        } else if (type == Constants.TYPE_FILE_ZIP) {
            suffix = ".zip";
        } else if (type == Constants.TYPE_FILE_TXT) {
            suffix = ".txt";
        } else if (type == Constants.TYPE_FILE_CLASS) {
            suffix = ".class";
        } else {

        }
        return suffix;
    }

    public static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length == 0;
    }

    public static boolean deleteDirectory(String filePath) {
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        if (files != null) {
            //遍历删除文件夹下的所有文件(包括子目录)
            for (File file : files) {
                if (file.isFile()) {
                    //删除子文件
                    flag = deleteFile(file.getAbsolutePath());
                    if (!flag) {
                        break;
                    }
                } else {
                    //删除子目录
                    flag = deleteDirectory(file.getAbsolutePath());
                    if (!flag) {
                        break;
                    }
                }
            }
        }
        if (!flag) {
            return false;
        }
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 删除文件
     *
     * @param filePath 被删除的文件路径
     */
    public static boolean deleteFile(String filePath) {
        boolean result = false;
        File file = new File(filePath);
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }
}
