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

    /**
     * 删除目录下所有文件
     *
     * @param dir
     */
    public static void removeDir(File dir) {
        if (null == dir) {
            return;
        }
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                removeDir(file);
            } else {
                System.out.println(file + ":" + file.delete());
            }
        }
    }
}
