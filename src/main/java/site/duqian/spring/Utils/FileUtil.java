package site.duqian.spring.Utils;

import java.io.File;

public class FileUtil {
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
