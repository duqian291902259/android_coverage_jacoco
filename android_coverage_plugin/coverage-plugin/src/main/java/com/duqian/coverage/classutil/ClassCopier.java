package com.duqian.coverage.classutil;


import com.duqian.coverage.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ClassCopier extends ClassProcessor {

    private String classDir;

    public ClassCopier(String classDir, List<String> includes) {
        super(includes);
        this.classDir = classDir;
    }

    @Override
    void processJar(ZipFile zipFile, ZipInputStream zis, ZipOutputStream zos, ZipEntry entryIn, ZipEntry entryOut)
        throws IOException {
        String entryName = entryIn.getName();
        if (shouldIncludeClass(entryName)) {
            //记录第三方moudle的class
            File file = new File(classDir + File.separator + entryName);
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            copy(zipFile.getInputStream(entryIn), outputStream);
        }
    }

    @Override
    void processClass(File fileIn, File fileOut) throws IOException {
        if (shouldIncludeClass(fileIn)) {
            File parentFile = fileOut.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            FileUtil.copyFile(fileIn, fileOut);
        }
    }

}
