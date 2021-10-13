package site.duqian.spring.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import site.duqian.spring.Constants;
import site.duqian.spring.bean.CommonParams;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Description:文件、存储路径管理
 *
 * @author n20241 Created by 杜小菜 on 2021/9/30 - 10:02 .
 * E-mail: duqian2010@gmail.com
 */
public class FileUtils {
    private static final long FILE_COPY_BUFFER_SIZE = 1024 * 1024 * 30;
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);// slf4j日志记录器

    public static String getProjectDir() {
        return System.getProperty("user.dir");
    }

    public static String getRootDir() {
        String rootDir = getProjectDir();
        if ("/".equals(rootDir)) {
            rootDir = "";
        }
        return rootDir;
    }

    public static String getSaveDir(CommonParams commonParams) {
        String rootDir = getBranchDir(commonParams) + File.separator + commonParams.getCommitId();
        //System.out.println("getSaveDir=" + rootDir);
        return rootDir;
    }

    public static String getJacocoDownloadDir() {
        String rootDir = getRootDir() + File.separator + Constants.REPORT_DOWNLOAD_ROOT_DIR;
        //System.out.println("getJacocoDownloadDir=" + rootDir);
        File file = new File(rootDir);//.getParentFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        return rootDir;
    }

    public static String getBranchDir(CommonParams commonParams) {
        String rootDir = getJacocoDownloadDir() + commonParams.getAppName() + File.separator+ commonParams.getBranchName();//
        //System.out.println("getBranchDir=" + rootDir);
        return rootDir;
    }

    public static String getSourceDir(CommonParams commonParams) {
        String rootDir = getSaveDir(commonParams) + File.separator + Constants.SOURCE_DIR_NAME;
        //System.out.println("getSourceDir=" + rootDir);
        return rootDir;
    }

    public static String getGitCloneDir(CommonParams commonParams) {
        String rootDir = getBranchDir(commonParams) + File.separator + Constants.GIT_SOURCE_DIR_NAME;
        //System.out.println("getSourceDir=" + rootDir);
        return rootDir;
    }

    public static String getClassDir(CommonParams commonParams) {
        String rootDir = getSaveDir(commonParams) + File.separator + Constants.CLASS_DIR_NAME;
        //System.out.println("getClassDir=" + rootDir);
        return rootDir;
    }

    public static String getEcFilesDir(CommonParams commonParams) {
        String rootDir = getSaveDir(commonParams);
        //System.out.println("getEcFilesDir=" + rootDir);
        return rootDir;
    }

    public static String getDiffFilePath(CommonParams commonParams) {
        return getSaveDir(commonParams) + File.separator + Constants.DIFF_FILES_NAME;
    }

    //获取diff的源码路径
    public static String getDiffSrcDirPath(CommonParams commonParams) {
        String dirName = Constants.DIFF_DIR_NAME;
        if (!CommonUtils.isEmpty(commonParams.getDiffFileName())) {
            dirName = commonParams.getDiffFileName();
        }
        return getSaveDir(commonParams) + File.separator + dirName + File.separator + Constants.SOURCE_DIR_NAME;
    }

    //获取diff的class路径
    public static String getDiffClassDirPath(CommonParams commonParams) {
        String dirName = Constants.DIFF_DIR_NAME;
        if (!CommonUtils.isEmpty(commonParams.getDiffFileName())) {
            dirName = commonParams.getDiffFileName();
        }
        return getSaveDir(commonParams) + File.separator + dirName + File.separator + Constants.CLASS_DIR_NAME;
    }

    public static String getJacocoJarPath() {
        //String rootDir = FileUtils.getProjectDir() + File.separator;
        return Constants.JACOCO_CLI_FILE_NAME;
    }

    public static String getReportRelativePath(CommonParams commonParams) {
        //return File.separator + commonParams.getBranchName() + File.separator + commonParams.getCommitId();
        //return File.separator + commonParams.getBranchName().replaceAll("#","") + File.separator + commonParams.getCommitId();
        return commonParams.getCommitId();
    }

    /**
     * 报告的根目录
     */
    public static String getJacocoReportPath(CommonParams commonParams) {
        return getReportRootDir() + File.separator + getReportRelativePath(commonParams) + File.separator + getReportDirName(commonParams);
    }

    public static String getReportDirName(CommonParams commonParams) {
        String dirName = Constants.REPORT_DIR_NAME;
        if (!CommonUtils.isEmpty(commonParams.getDiffFileName())) {
            dirName = commonParams.getDiffFileName();
        }
        return dirName;
    }

    public static String getReportZipFileName(CommonParams commonParams) {
        if (!CommonUtils.isEmpty(commonParams.getDiffFileName())) {
            return commonParams.getDiffFileName() + Constants.TYPE_FILE_ZIP;
        }
        return commonParams.getCommitId() + Constants.TYPE_FILE_ZIP;
    }

    public static String getReportZipPath(CommonParams commonParams) {
        return getReportRootDir() + File.separator + getReportRelativePath(commonParams) + File.separator + getReportZipFileName(commonParams);
    }

    public static String getReportRootDir() {
        return FileUtils.getJacocoDownloadDir() + Constants.REPORT_DIR_NAME;
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
        } catch (Exception e) {
            e.printStackTrace();
            unzipOk = false;
        }
        return unzipOk;
    }

    public static List<String> readDiffFilesFromTxt(String txtPath) {
        List<String> list = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(txtPath));//构造一个BufferedReader类来读取文件
            String textLine = null;
            while ((textLine = br.readLine()) != null) {//使用readLine方法，一次读一行
                logger.debug("readDiffFilesFromTxt=" + textLine);
                if (!"".equals(textLine)) {
                    list.add(textLine);
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }
        }
        return list;
    }

    /**
     * 拷贝文件
     *
     * @param originFilePath 源文件
     * @param targetFilePath 目标文件
     */
    public static boolean copyFile(String originFilePath, String targetFilePath) {
        boolean isSuccess = true;
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int length;
            File originFile = new File(originFilePath);
            if (originFile.isFile() && originFile.exists()) { // 文件存在时
                new File(targetFilePath).getParentFile().mkdirs();
                inStream = new FileInputStream(originFilePath); // 读入原文件
                fs = new FileOutputStream(targetFilePath);
                byte[] buffer = new byte[1024 * 4];
                while ((length = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, length);
                }
                fs.flush();
            } else {
                isSuccess = false;
            }
        } catch (Exception e) {
            isSuccess = false;
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException ignore) {
                }
            }
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (Exception ignore) {
                }
            }
        }
        return isSuccess;
    }

    /**
     * Internal copy file method.
     *
     * @param srcFile          the validated source file, must not be {@code null}
     * @param destFile         the validated destination file, must not be {@code null}
     * @param preserveFileDate whether to preserve the file date
     * @throws IOException if an error occurs
     */
    public static boolean copyFile(File srcFile, File destFile, boolean preserveFileDate) {
        if (destFile.exists() && destFile.isDirectory()) {
            logger.debug("Destination '" + destFile + "' exists but is a directory");
            return false;
        }
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            destFile.getParentFile().mkdirs();
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input = fis.getChannel();
            output = fos.getChannel();
            long size = input.size();
            long pos = 0;
            long count = 0;
            while (pos < size) {
                count = Math.min(size - pos, FILE_COPY_BUFFER_SIZE);
                pos += output.transferFrom(input, pos, count);
            }
        } catch (Exception e) {
           logger.debug("copyFile error "+e);
            return false;
        } finally {
            closeQuietly(output);
            closeQuietly(fos);
            closeQuietly(input);
            closeQuietly(fis);
        }

        if (srcFile.length() != destFile.length()) {
            logger.debug("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
            return false;
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
        return true;
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception ioe) {
            // ignore
        }
    }

    public static void doCopyDirectory(File srcDir, File destDir, FileFilter filter,
                                       boolean preserveFileDate, List<String> exclusionList) throws IOException {
        // recurse
        File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
        if (srcFiles == null) {  // null if abstract pathname does not denote a directory, or if an I/O error occurs
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (!destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs() && !destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
        }
        if (!destDir.canWrite()) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        for (File srcFile : srcFiles) {
            File dstFile = new File(destDir, srcFile.getName());
            if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
                if (srcFile.isDirectory()) {
                    doCopyDirectory(srcFile, dstFile, filter, preserveFileDate, exclusionList);
                } else {
                    copyFile(srcFile, dstFile, preserveFileDate);
                }
            }
        }
        // Do this last, as the above has probably affected directory metadata
        if (preserveFileDate) {
            destDir.setLastModified(srcDir.lastModified());
        }
    }

    public static String getClassZipFile(CommonParams commonParams) {
        return getSaveDir(commonParams) + File.separator + Constants.CLASS_ZIP_FILE_NAME;
    }
}
