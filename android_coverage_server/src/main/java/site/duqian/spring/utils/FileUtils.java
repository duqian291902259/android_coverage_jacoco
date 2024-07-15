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
        if (Constants.TYPE_FILE_CUSTOM.equalsIgnoreCase(commonParams.getType())) {
            return getUploadDir() + File.separator + commonParams.getUploadDir();
        }
        return getBranchDir(commonParams) + File.separator + commonParams.getCommitId();
    }

    public static String getUploadDir() {
        //String rootDir = getRootDir() + File.separator + Constants.FILE_UPLOAD_ROOT_DIR;
        String rootDir = getJacocoDownloadDir() + Constants.FILE_UPLOAD_ROOT_DIR;
        File file = new File(rootDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return rootDir;
    }

    public static String getJacocoDownloadDir() {
        String rootDir = getRootDir() + File.separator + Constants.REPORT_DOWNLOAD_ROOT_DIR;
        File file = new File(rootDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return rootDir;
    }

    public static String getBranchDir(CommonParams commonParams) {
        return getJacocoDownloadDir() + commonParams.getAppName() + File.separator + commonParams.getBranchName();
    }

    public static String getSourceDir(CommonParams commonParams) {
        return getSaveDir(commonParams) + File.separator + Constants.SOURCE_DIR_NAME;
    }

    public static String getGitCloneDir(CommonParams commonParams) {
        return getRootDir() + File.separator + commonParams.getAppName() + File.separator + Constants.GIT_SOURCE_DIR_NAME;
    }

    public static String getClassDir(CommonParams commonParams) {
        return getSaveDir(commonParams) + File.separator + Constants.CLASS_DIR_NAME;
    }

    public static String getEcFilesDir(CommonParams commonParams) {
        String newEcFileDirPath = getSaveDir(commonParams) + File.separator + Constants.EC_FILES_DIR_NAME;
        if (!new File(newEcFileDirPath).exists()) {//返回旧版本的路径
            return getBranchDir(commonParams) + File.separator + Constants.EC_FILES_DIR_NAME;
        }
        return newEcFileDirPath;
    }

    public static String getDiffFilePath(CommonParams commonParams) {
        return getSaveDir(commonParams) + File.separator + Constants.DIFF_FILES_NAME;
    }

    //获取diff的源码路径
    public static String getDiffSrcDirPath(CommonParams commonParams) {
        String dirName = Constants.DIFF_DIR_NAME;
        String diffFileName = commonParams.getDiffFileName();
        if (!CommonUtils.isEmpty(diffFileName)) {
            dirName = diffFileName;
        }
        return getSaveDir(commonParams) + File.separator + dirName + File.separator + Constants.SOURCE_DIR_NAME;
    }

    //获取diff的class路径
    public static String getDiffClassDirPath(CommonParams commonParams) {
        String dirName = Constants.DIFF_DIR_NAME;
        String diffFileName = commonParams.getDiffFileName();
        if (!CommonUtils.isEmpty(diffFileName)) {
            dirName = diffFileName;
        }
        return getSaveDir(commonParams) + File.separator + dirName + File.separator + Constants.CLASS_DIR_NAME;
    }

    public static String getJacocoJarPath() {
        return Constants.JACOCO_CLI_FILE_NAME;
    }

    public static String getReportRelativePath(CommonParams commonParams) {
        return commonParams.getAppName() + File.separator + commonParams.getCommitId();
    }

    /**
     * 报告的根目录
     */
    public static String getJacocoReportPath(CommonParams commonParams) {
        return getReportRootDir() + File.separator + getReportRelativePath(commonParams) + File.separator + getReportDirName(commonParams);
    }

    /**
     * 报告的最终目录名称，如：release_v3.9.9_41d87bd8_diff_2c4cc227，release_v3.9.9_2c4cc227.zip
     */
    public static String getReportDirName(CommonParams commonParams) {
        // 报告的名称修正
        //String dirName = Constants.REPORT_DIR_NAME;
        String dirName = commonParams.getBranchId() + Constants.FILE_NAME_SPLIT + commonParams.getCommitId();
        String diffFileName = commonParams.getDiffFileName();
        if (!CommonUtils.isEmpty(diffFileName)) {
            dirName = diffFileName;
        }
        return dirName;
    }

    /**
     * zip的目录名称+.zip
     */
    public static String getReportZipFileName(CommonParams commonParams) {
        return getReportDirName(commonParams) + Constants.TYPE_FILE_ZIP;
    }

    public static String getReportZipPath(CommonParams commonParams) {
        return getReportRootDir() + File.separator + getReportRelativePath(commonParams) + File.separator + getReportZipFileName(commonParams);
    }

    public static String getReportRootDir() {
        return FileUtils.getJacocoDownloadDir() + Constants.REPORT_DIR_NAME;
    }

    public static String getClassZipFile(CommonParams commonParams) {
        return getSaveDir(commonParams) + File.separator + Constants.CLASS_ZIP_FILE_NAME;
    }

    public static String getSrcZipFile(CommonParams commonParams) {
        return getSaveDir(commonParams) + File.separator + Constants.SRC_ZIP_FILE_NAME;
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
            System.out.println("deleteDirectory:" + filePath + " not exists || not isDirectory");
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
            System.out.println(filePath + "deleteDirectory failed");
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

    /**
     * 解压路径有问题，导致无法找到class目录和子目录
     * classes\com\netease\cc\coverage_library\CCJacocoHelper.class
     * fh.getFileNameW().replaceAll("\\\\", Matcher.quoteReplacement(File.separator)));
     * 其中 Matcher.quoteReplacement(File.separator) 部分之所以不直接使用 File.separator 是因为使用了replaceAll函数，若系统分隔符为反斜杠会被识别为转移符号，抛出 java.lang.IllegalArgumentException: character to be escaped is missing 异常
     *
     * @param dir    父目录
     * @param srcZip zip文件
     * @return 是否解压成功
     */
    public static boolean unzip(String dir, String srcZip) {
        if (dir.endsWith("/")) {
            logger.debug("unzip dir1=" + dir + ",srcZip=" + srcZip);
            dir = dir.substring(0, dir.length() - 1);
        }
        logger.debug("unzip dir=" + dir + ",srcZip=" + srcZip);
        boolean unzipOk = true;//default is true
        try (ZipInputStream inZip = new ZipInputStream(new FileInputStream(srcZip))) {
            ZipEntry zipEntry;
            String szName;
            int printCount = 0;
            while ((zipEntry = inZip.getNextEntry()) != null) {
                String fullName = zipEntry.getName();
                if (fullName.startsWith(Constants.REPORT_DIR_NAME)) {
                    logger.debug("unzip 历史原因，旧的zip包，report开头的不处理," + fullName);
                    break;
                }
                szName = fullName.replaceAll("\\\\", "/");
                //logger.debug("unzip szName=" + szName + ",fullName=" + fullName);

                if (zipEntry.isDirectory()) {
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(dir + "/" + szName);//File.separator
                    if (!folder.mkdirs()) {
                        if (printCount <= 15) {
                            logger.debug("unzip folder mkdirs false1," + folder);
                        }
                        unzipOk = false;
                        printCount++;
                    }
                } else {
                    File file1 = new File(dir + "/" + szName);
                    File parentDir = new File(file1.getParent());
                    if (!parentDir.exists()) {
                        boolean mkdirs = parentDir.mkdirs();
                        if (printCount <= 13) {
                            logger.debug("unzip parentDir mkdirs," + mkdirs);
                        }
                        printCount++;
                    }
                    file1.delete();
                    if (!file1.createNewFile()) {
                        //unzipOk = false;
                        if (printCount <= 15) {
                            logger.debug("unzip createNewFile false," + file1);
                        }
                        printCount++;
                    }
                    try (FileOutputStream fos = new FileOutputStream(file1)) {
                        int len;
                        byte[] buffer = new byte[1024];
                        while ((len = inZip.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                            fos.flush();
                        }
                    } catch (Exception e) {
                        logger.debug("unzip error=" + e);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("unzip error2=" + e);
            unzipOk = false;
        }
        return unzipOk;
    }

    public static int tryServerUnZip(String zipFilePath, boolean unzip) {
        int execCmd = -1;
        if (!unzip) {// TODO: 2021/10/27 linux服务器解压有问题 window本地上传的zip无效，无法解压成文件夹的形式
            //execCmd = CmdUtil.runProcess("jar xvf " + zipFilePath);
            //logger.debug("tryServerUnZip jar xvf =" + execCmd + ",zipFilePath=" + zipFilePath);
            execCmd = CmdUtil.runProcess("unzip " + zipFilePath, false);
            logger.debug("tryServerUnZip unzip cmd=" + execCmd + ",zipFilePath=" + zipFilePath);
        }
        logger.debug("tryServerUnZip unzip=" + unzip + ",zipFilePath=" + zipFilePath);
        return execCmd;
    }

    public static List<String> readDiffFilesFromTxt(String txtPath) {
        List<String> list = new ArrayList<>();
        BufferedReader br = null;
        try {
            File file = new File(txtPath);
            if (!file.exists()) {
                return list;
            }
            br = new BufferedReader(new FileReader(txtPath));//构造一个BufferedReader类来读取文件
            String textLine = null;
            while ((textLine = br.readLine()) != null) {//使用readLine方法，一次读一行
                //logger.debug("readDiffFilesFromTxt=" + textLine);
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
            logger.debug("copyFile error " + e);
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

    /**
     * 获取path配置文件内容
     */
    public static String getFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            try {
                return file2String(file);
            } catch (Exception e) {
                logger.debug("FileUtil get file \"" + path + "\" exception ", e);
            }
        }
        return "";
    }

    /**
     * 文本文件转换为指定编码的字符串
     *
     * @param file 文本文件
     * @return 转换后的字符串
     */
    public static String file2String(File file) throws IOException {
        String fileContent = "";
        if (null == file) {
            return fileContent;
        }
        if (!file.exists()) {
            return fileContent;
        }
        FileInputStream fis = new FileInputStream(file);
        return inputStream2String(fis);
    }

    /**
     * 输入流转成字符串
     */
    public static String inputStream2String(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        StringBuilder writer = new StringBuilder();
        InputStreamReader isReader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(isReader);
        // 将输入流写入输出流
        char[] buffer = new char[1024];
        int n;
        while (-1 != (n = bufferedReader.read(buffer))) {
            writer.append(buffer, 0, n);
        }
        bufferedReader.close();
        isReader.close();
        in.close();
        return writer.toString();
    }

    /**
     * 保存内容配置文件
     */
    public static void saveFile(String directory, String fileName, String content) {
        String path = String.format("%s/%s", directory, fileName);
        File dirFile = new File(directory);
        File file = new File(path);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                logger.debug("FileUtils", "save file \"" + path + "\" exception ", e);
                return;
            }
        }
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(file, false);
            bw = new BufferedWriter(fw);
            bw.write(content);
            bw.flush();
        } catch (Exception e) {
            logger.debug("FileUtil write content to file \"" + path + "\" exception " + e);
        } finally {
            IOUtils.close(fw);
            IOUtils.close(bw);
        }
    }

    public static void removeCacheFile(String path) {
        try {
            // TODO: 2022/1/26 获取报告的id，过滤
            //String[] exclude = new String[]{"343e200d", "5b5c5a66", "221e8e2d", "5927731c", "332eb555", "24ca6131", "66666", "88888"};
            /*String[] exclude = new String[]{"DQ_#480324_lint耗时优化", "dev", "DQ_#411671_code_coverage_v2", "ccstart_main"};
            List<String> list = Arrays.asList(exclude);
            String rootDir = path + "android/";
            File rootFile = new File(rootDir);
            if (rootFile.exists() && rootFile.isDirectory()) {
                for (File file : rootFile.listFiles()) {
                    if (!list.contains(file.getName())) {
                        FileUtils.deleteDirectory(file.getAbsolutePath());
                        logger.debug("FileUtil delete " + file);
                    } else {
                        logger.debug("FileUtil ignored delete " + file);
                    }
                }
            }
            FileUtils.deleteDirectory(path + "coverage-demo");
            FileUtils.deleteDirectory(path + "report/");
            */

            /*FileUtils.deleteDirectory(path + "FoldScreenAdapt_RankEntrance_487452");
            FileUtils.deleteDirectory(path + "DQ_#478951_code_coverage");
            FileUtils.deleteDirectory(path + "DQ_#CopyCCID_488524");
            FileUtils.deleteDirectory(path + "controller_callback");
            FileUtils.deleteDirectory(path + "branch_#485236_Flutter插件化加载so调研优化");
            FileUtils.deleteDirectory(path + "489441_land_video_scalable_recovery");
            FileUtils.deleteDirectory(path + "489362_querylive_scheme_fix_push_problem");
            FileUtils.deleteDirectory(path + "486169_recom_sn");*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
