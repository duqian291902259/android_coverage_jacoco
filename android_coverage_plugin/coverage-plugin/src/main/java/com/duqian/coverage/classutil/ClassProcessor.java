package com.duqian.coverage.classutil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public abstract class ClassProcessor {

    private List<String> includes;

    public ClassProcessor(List<String> includes) {
        this.includes = includes;
    }

    public void doClass(File fileIn, File fileOut) throws IOException {
        processClass(fileIn, fileOut);
    }

    public void doJar(File jarIn, File jarOut) throws IOException {
        try {
            processJar(jarIn, jarOut, Charset.forName("UTF-8"), Charset.forName("UTF-8"));
        } catch (IllegalArgumentException e) {
            if ("MALFORMED".equals(e.getMessage())) {
                processJar(jarIn, jarOut, Charset.forName("GBK"), Charset.forName("UTF-8"));
            } else {
                throw e;
            }
        }
    }

    @SuppressWarnings("NewApi")
    private void processJar(File jarIn, File jarOut, Charset charsetIn, Charset charsetOut) throws IOException {
        ZipFile zipFile = new ZipFile(jarIn);
        ZipInputStream zis = null;
        ZipOutputStream zos = null;
        try {
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(jarIn)), charsetIn);
            if (jarOut != null) {
                zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(jarOut)), charsetOut);
            }
            ZipEntry entryIn;
            Set<String> processedEntryNamesMap = new HashSet<>();
            while ((entryIn = zis.getNextEntry()) != null) {
                final String entryName = entryIn.getName();
                if (!processedEntryNamesMap.contains(entryName)) {
                    ZipEntry entryOut = new ZipEntry(entryIn);
                    // Set compress method to default, fixed #12
                    if (entryOut.getMethod() != ZipEntry.DEFLATED) {
                        entryOut.setMethod(ZipEntry.DEFLATED);
                    }
                    entryOut.setCompressedSize(-1);
                    if (zos != null) {
                        zos.putNextEntry(entryOut);
                    }

                    if (!entryIn.isDirectory()) {
                        processJar(zipFile, zis, zos, entryIn, entryOut);
                    }

                    if (zos != null) {
                        zos.closeEntry();
                    }
                    processedEntryNamesMap.add(entryName);
                }
            }
        } finally {
            closeQuietly(zos);
            closeQuietly(zis);
        }
    }

    abstract void processJar(ZipFile zipFile, ZipInputStream zis, ZipOutputStream zos, ZipEntry entryIn,
        ZipEntry entryOut) throws IOException;

    abstract void processClass(File fileIn, File fileOut) throws IOException;

    public static final String kotlinClass = File.separator + "kotlin-classes" + File.separator;
    public static final String javaClass = File.separator + "javac" + File.separator;

    public static String filePath2ClassName(File fileIn) {
        String path = fileIn.getAbsolutePath();
        String className;
        if (path.contains(javaClass)) {
            String offset = File.separator + "classes" + File.separator;
            int index = path.indexOf(offset, path.indexOf(javaClass));
            className = path.substring(index + offset.length());
        } else if (path.contains(kotlinClass)) {
            int index = path.indexOf(File.separator, path.indexOf(kotlinClass) + kotlinClass.length());
            className = path.substring(index + 1);
        } else {
            className = fileIn.getName();
        }
        return className;
    }

    boolean shouldIncludeClass(File fileIn) {
//        System.out.println("processClass 2 ="+className);
        return shouldIncludeClass(filePath2ClassName(fileIn));
    }

    boolean shouldIncludeClass(String className) {
        //将win下的分隔符转化为mac的
        className = className.replaceAll("\\\\", "/");

        String name = className.substring(className.lastIndexOf("/") + 1);
        if (!className.endsWith(".class") || name.equals("R.class")
            || name.equals("R2.class")
            || name.equals("BR.class")
            || name.equals("BuildConfig.class")
            || name.startsWith("R$")
            || className.startsWith("androidx")) {
            return false;
        }

        for (String include : includes) {
            if (className.startsWith(include.replaceAll("\\.", "/"))) {
                return true;
            }
        }
        return false;
    }

    protected void closeQuietly(Closeable target) {
        if (target != null) {
            try {
                target.close();
            } catch (Exception e) {
                // Ignored.
            }
        }
    }

    protected void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int c;
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
    }
}
