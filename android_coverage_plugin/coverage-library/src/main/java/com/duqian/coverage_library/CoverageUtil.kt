package com.duqian.coverage_library

import java.io.File

/**
 * Description:覆盖率工具类
 *
 * Created by 杜乾 on 2024/7/15 - 10:18.
 * E-mail: duqian2010@gmail.com
 */
object CoverageUtil {

    fun deleteDirectory(filePath: String): Boolean {
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        var filePath = filePath
        if (!filePath.endsWith(File.separator)) {
            filePath += File.separator
        }
        val dirFile = File(filePath)
        if (!dirFile.exists() || !dirFile.isDirectory) {
            return false
        }
        var flag = true
        val files = dirFile.listFiles()
        if (files != null) {
            //遍历删除文件夹下的所有文件(包括子目录)
            for (file in files) {
                if (file.isFile) {
                    //删除子文件
                    flag = deleteFile(file.absolutePath)
                    if (!flag) {
                        break
                    }
                } else {
                    //删除子目录
                    flag = deleteDirectory(file.absolutePath)
                    if (!flag) {
                        break
                    }
                }
            }
        }
        return if (!flag) {
            false
        } else dirFile.delete()
        //删除当前空目录
    }

    /**
     * 删除文件
     *
     * @param filePath 被删除的文件路径
     */
    fun deleteFile(filePath: String?): Boolean {
        var result = false
        val file = File(filePath)
        if (file.exists()) {
            result = file.delete()
        }
        return result
    }
}