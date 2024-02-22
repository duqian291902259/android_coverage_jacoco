package com.duqian.coverage;

import org.gradle.api.Project;
import org.gradle.internal.impldep.org.eclipse.jgit.annotations.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

/**
 * 文件操作工具类
 *
 * @author g7734
 */
public class FileUtil {

    /**
     * 路径分隔符
     */
    private static final String REPLACEMENT_SEP = Matcher.quoteReplacement(File.separator);

    /**
     * 文本文件转换为指定编码的字符串
     *
     * @param file 文本文件
     * @return 转换后的字符串
     * @throws IOException 异常
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
        String str;
        StringBuilder writer = new StringBuilder();
        InputStreamReader isReader = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(isReader);
        // 将输入流写入输出流
        char[] buffer = new char[1024];
        int n;
        while (-1 != (n = bufferedReader.read(buffer))) {
            writer.append(buffer, 0, n);
        }
        close(bufferedReader);
        close(isReader);
        close(in);
        str = writer.toString();
        return str;
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 输出内容到文件
     */
    public static void output(Project project, String path, String content) {
        if (isEmpty(path) || isEmpty(content)) {
            return;
        }
        File outputFile = new File(path);
        try {
            if (!outputFile.exists()) {
                boolean hasDir = true;
                String dir = path.substring(0, path.lastIndexOf(File.separator));
                File dirFile = new File(dir);
                if (!dirFile.exists()) {
                    hasDir = dirFile.mkdirs();
                    System.out.println("Make dir: " + dir + ", success:" + hasDir);
                }
                if (hasDir && !outputFile.createNewFile()) {
                    System.out.println("Create file failed! " + outputFile.getName());
                    return;
                }
            }
            FileOutputStream out = new FileOutputStream(outputFile, false);
            out.write(content.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out
                .println("[" + project.getName() + "]" + "Outputting file:" + path + ", success:" + outputFile.exists());
    }

    /**
     * 格式化文件路径
     */
    public static String formatPath(String path) {
        return isEmpty(path) ? path :
                path.replaceAll("/", REPLACEMENT_SEP)
                        .replaceAll("\\\\", REPLACEMENT_SEP);
    }

    /**
     * Returns true if the parameter is null or of zero length
     */
    public static boolean isEmpty(final CharSequence s) {
        if (s == null) {
            return true;
        }
        return s.length() == 0;
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
                new FileOutputStream(zipFilePath));
        // 打开要输出的文件
        File file = new File(srcFilePath);
        // 压缩
        zipFiles(file.getParent() + File.separator, file.getName(), outZip);
        // 完成,关闭
        outZip.finish();
        outZip.close();
    }

    private static void zipFiles(String folderPath, String filePath, java.util.zip.ZipOutputStream zipOut)
            throws IOException {
        if (zipOut == null) {
            return;
        }
        File file = new File(folderPath + filePath);
        // 判断是不是文件
        if (file.isFile()) {
            java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath);
            zipOut.putNextEntry(zipEntry);
            try (FileInputStream inputStream = new FileInputStream(file)) {
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
                java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(filePath + File.separator);
                zipOut.putNextEntry(zipEntry);
                zipOut.closeEntry();
                return;
            }
            // 如果有子文件, 遍历子文件
            for (final String s : fileList) {
                zipFiles(folderPath, filePath + File.separator + s, zipOut);
            }
        }
    }

    public static boolean copyFile(File source, File target) {
        try {
            File parentFile = target.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (target.exists()) {
                target.delete();
            } else {
                target.createNewFile();
            }
            InputStream is = new FileInputStream(source);
            OutputStream os = new FileOutputStream(target);
            byte[] buffer = new byte[1024 * 2];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            is.close();
            os.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*public static void copyFile(@NonNull File from, @NonNull File to) throws IOException {
        java.nio.file.Files.copy(
                from.toPath(),
                to.toPath(),
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);
    }*/

    public static boolean copyFile(String source, String target) {
        try {
            String newDir = target.substring(0, target.lastIndexOf("/"));
            new File(newDir).mkdirs();
            copyFile(new File(source), new File(target));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void copyDirectory(File from, File to) throws IOException {
        mkdirs(to);
        File[] children = from.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isFile()) {
                    copyFileToDirectory(child, to);
                } else if (child.isDirectory()) {
                    copyDirectoryToDirectory(child, to);
                } else {
                    throw new IllegalArgumentException(
                            "Don't know how to copy file " + child.getAbsolutePath());
                }
            }
        }
    }

    public static void copyFileToDirectory(@NonNull final File from, @NonNull final File to) throws IOException {
        copyFile(from, new File(to, from.getName()));
    }

    public static void copyDirectoryToDirectory(@NonNull final File from, @NonNull final File to) throws IOException {
        copyDirectory(from, new File(to, from.getName()));
    }

    public static boolean saveStringToFile(String content, @NonNull final File file) throws IOException {
        try {
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (file.exists()) {
                file.delete();
            } else {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file.getName(), false);

            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);

            bufferWriter.write(content);

            bufferWriter.close();

            System.out.println("saveStringToFile Done");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static File mkdirs(File folder) {
        if (!folder.mkdirs() && !folder.isDirectory()) {
            throw new RuntimeException("Cannot create directory " + folder);
        }
        return folder;
    }


}
