package site.duqian.spring.utils;

import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Description:文件、存储路径管理
 *
 * @author n20241 Created by 杜小菜 on 2021/9/30 - 10:02 .
 * E-mail: duqian2010@gmail.com
 */
public class FileUtil {

    public static String getProjectDir() {
        String rootDir = System.getProperty("user.dir");
        System.out.println("getProjectDir=" + rootDir);
        return rootDir;
    }

    public static String getRootDir() {
        String rootDir = getProjectDir();
        File parentFile = new File(rootDir).getParentFile();
        if (parentFile != null) {
            rootDir = parentFile.getAbsolutePath();
        }
        System.out.println("getRootDir=" + rootDir);
        return rootDir;
    }

    public static String getSaveDir(CommonParams commonParams) {
        String rootDir = getBranchDir(commonParams) + File.separator + commonParams.getCommitId();
        System.out.println("getSaveDir=" + rootDir);
        return rootDir;
    }

    public static String getJacocoDownloadDir() {
        String rootDir = getRootDir() + Constants.KEY_PARAM_DOWNLOAD_DIR;
        System.out.println("getBranchDir=" + rootDir);
        return rootDir;
    }

    public static String getBranchDir(CommonParams commonParams) {
        String rootDir = getJacocoDownloadDir() + commonParams.getAppName() + File.separator + commonParams.getBranchName();
        System.out.println("getBranchDir=" + rootDir);
        return rootDir;
    }

    public static String getSourceDir(CommonParams commonParams) {
        String rootDir = getBranchDir(commonParams) + File.separator + "src";
        System.out.println("getSourceDir=" + rootDir);
        return rootDir;
    }

    public static String getClassDir(CommonParams commonParams) {
        String rootDir = getSaveDir(commonParams) + File.separator + "classes";
        System.out.println("getClassDir=" + rootDir);
        return rootDir;
    }

    public static String getEcFilesDir(CommonParams commonParams) {
        String rootDir = getSaveDir(commonParams);
        System.out.println("getEcFilesDir=" + rootDir);
        return rootDir;
    }

    public static String getJacocoJarPath() {
        String rootDir = FileUtil.getProjectDir() + File.separator;
        String jarPath = rootDir + "jacococli.jar";
        System.out.println("getJacocoJarPath=" + jarPath);
        return jarPath;
    }

    public static String getJacocoReportPath(CommonParams commonParams) {
        String rootDir = FileUtil.getJacocoDownloadDir() + "report" + File.separator + commonParams.getAppName() + File.separator + commonParams.getBranchName() + File.separator + commonParams.getCommitId();
        String reportPath = rootDir + "jacoco/report";
        System.out.println("getJacocoJarPath=" + reportPath);
        File file = new File(reportPath);
        file.mkdirs();
        return reportPath;
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
        } else if (type == Constants.TYPE_FILE_RAR) {
            suffix = ".rar";
        } else if (type == Constants.TYPE_FILE_TXT) {
            suffix = ".txt";
        } else if (type == Constants.TYPE_FILE_CLASS) {
            suffix = ".class";
        } else if (type == Constants.TYPE_FILE_APK) {
            suffix = ".apk";
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


    public static void zipFolder(String srcFilePath, String zipFilePath) throws IOException {
        // 创建Zip包
        java.util.zip.ZipOutputStream outZip = new java.util.zip.ZipOutputStream(
                new java.io.FileOutputStream(zipFilePath));
        // 打开要输出的文件
        java.io.File file = new java.io.File(srcFilePath);
        // 压缩
        zipFiles(file.getParent() + java.io.File.separator, file.getName(), outZip);
        // 完成,关闭
        outZip.finish();
        outZip.close();
    }

    private static void zipFiles(String folderPath, String filePath, java.util.zip.ZipOutputStream zipOut)
            throws IOException {
        if (zipOut == null) {
            return;
        }
        java.io.File file = new java.io.File(folderPath + filePath);
        // 判断是不是文件
        if (file.isFile()) {
            java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath);
            zipOut.putNextEntry(zipEntry);
            try (java.io.FileInputStream inputStream = new java.io.FileInputStream(file)) {
                int len;
                byte[] buffer = new byte[100000];

                while ((len = inputStream.read(buffer)) != -1) {
                    zipOut.write(buffer, 0, len);
                }
            } catch (Exception e) {
                throw e;
            }
            zipOut.closeEntry();
        } else {
            // 文件夹的方式,获取文件夹下的子文件
            String[] fileList = file.list();
            // 如果没有子文件, 则添加进去即可
            if (fileList == null || fileList.length <= 0) {
                java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath + java.io.File.separator);
                zipOut.putNextEntry(zipEntry);
                zipOut.closeEntry();
            }
            // 如果有子文件, 遍历子文件
            for (final String s : fileList) {
                zipFiles(folderPath, filePath + File.separator + s, zipOut);
            }
        }
    }

    public static boolean unzip(String dir, String srcZip) {
        boolean unzipOk = true;//default is true
        try (ZipInputStream inZip = new ZipInputStream(new FileInputStream(srcZip))) {
            ZipEntry zipEntry;
            String szName;
            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();

                if (zipEntry.isDirectory()) {
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(dir + File.separator + szName);
                    if (!folder.mkdirs()) {
                        unzipOk = false;
                    }
                } else {
                    File file1 = new File(dir + File.separator + szName);
                    File parentDir = new File(file1.getParent());
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    if (!file1.createNewFile()) {
                        unzipOk = false;
                    }
                    try (FileOutputStream fos = new FileOutputStream(file1)) {
                        int len;
                        byte[] buffer = new byte[1024];
                        while ((len = inZip.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                            fos.flush();
                        }
                    } catch (Exception ignore) {

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            unzipOk = false;
        }
        return unzipOk;
    }
}
