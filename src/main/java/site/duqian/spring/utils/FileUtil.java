package site.duqian.spring.utils;

import site.duqian.spring.Constants;

import java.io.File;

public class FileUtil {

    public static String getSaveDir() {
        //rootDir=C:\Users\N20241/download/,rootDir2=D:\DusanAndroid\SpringWeb/download/,rootDir3=D:\DusanAndroid\SpringWeb/download/
        String rootDir = System.getProperty("user.dir");
        System.out.println("rootDir=" + rootDir);
        return rootDir;
    }

    public static File getSaveDir(String appName, String branchName) {
        //rootDir=C:\Users\N20241/download/,rootDir2=D:\DusanAndroid\SpringWeb/download/,rootDir3=D:\DusanAndroid\SpringWeb/download/
        //String rootDir = System.getProperty("user.home") + fileDir;
        String rootDir = System.getProperty("user.dir") + Constants.KEY_PARAM_DOWNLOAD_DIR;
        System.out.println("rootDir=" + rootDir);
        return new File(rootDir, appName + File.separator + branchName);
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
