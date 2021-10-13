package site.duqian.spring.utils

import org.slf4j.LoggerFactory
import site.duqian.spring.Constants
import site.duqian.spring.bean.CommonParams
import java.io.File

/**
 * Description:获取差异class，src
 * @author n20241 Created by 杜小菜 on 2021/10/13 - 14:19 .
 * E-mail: duqian2010@gmail.com
 */
object DiffUtils {
    private val logger = LoggerFactory.getLogger(DiffUtils::class.java)

    /**
     * 获取diff class的路径
     */
    fun handleDiffClasses(commonParams: CommonParams?, classesPath: String): String {
        var classesPath = classesPath
        val diffFilePath = FileUtils.getDiffFilePath(commonParams)
        val diffFiles = FileUtils.readDiffFilesFromTxt(diffFilePath)
        val diffClassesPath = getDiffClasses(commonParams, diffFiles)
        //String diffSrcPath = getDiffSrc(commonParams, diffFiles);
        logger.debug("generateReport diffClassesPath=$diffClassesPath")
        if (!TextUtils.isEmpty(diffClassesPath)) {
            classesPath = diffClassesPath
        }
        /*if (!TextUtils.isEmpty(diffSrcPath)) {
            srcPath = diffSrcPath;
        }*/return classesPath
    }

    fun getDiffSrc(commonParams: CommonParams?, diffFiles: List<String>?): String? {
        val diffSrcDirPath = FileUtils.getDiffSrcDirPath(commonParams)
        var hasDiffSrc = false
        if (diffFiles != null && diffFiles.isNotEmpty()) {
            val srcDirPath = FileUtils.getSourceDir(commonParams)
            logger.debug("getDiffSrc srcDirPath=$srcDirPath")
            for (diffFile in diffFiles) {
                try {
                    var index = diffFile.indexOf(Constants.APP_PACKAGE_NAME)
                    if (index < 0) {
                        index = diffFile.indexOf(Constants.APP_PACKAGE_NAME2)
                    }
                    if (index < 0 || !diffFile.endsWith(".java") && !diffFile.endsWith(".kt")) {
                        continue
                    }
                    val relativePath = diffFile.substring(index)
                    val realFilePath = srcDirPath + relativePath
                    logger.debug("getDiffSrc realFilePath=$realFilePath")
                    val destFile = File(diffSrcDirPath + relativePath)
                    //System.out.println("getDiffSrc destFile=" + destFile.getAbsolutePath());
                    val hasCopied = FileUtils.copyFile(File(realFilePath), destFile, true)
                    if (hasCopied) {
                        hasDiffSrc = true
                    }
                } catch (e: Exception) {
                    logger.debug("getDiffSrc error=$e")
                }
            }
        }
        return if (!hasDiffSrc) {
            ""
        } else diffSrcDirPath
    }

    fun getDiffClasses(commonParams: CommonParams?, diffFiles: List<String>?): String {
        val diffClassDirPath = FileUtils.getDiffClassDirPath(commonParams)
        var hasDiffClass = false
        if (diffFiles != null && diffFiles.isNotEmpty()) {
            val classDir = FileUtils.getClassDir(commonParams)
            logger.debug("getDiffClasses classDir=$classDir")
            for (diffFile in diffFiles) {
                try {
                    var index = diffFile.indexOf(Constants.APP_PACKAGE_NAME)
                    if (index < 0) {
                        index = diffFile.indexOf(Constants.APP_PACKAGE_NAME2)
                    }
                    if (index < 0) {
                        continue
                    }
                    val fileName = File(diffFile).name
                    val realName = fileName.substring(0, fileName.lastIndexOf("."))
                    val lastSeparatorIndex = diffFile.lastIndexOf("/")
                    //取出差异class的路径
                    val relativePath = diffFile.substring(index, lastSeparatorIndex + 1)
                    val realDirPath = classDir + relativePath
                    val rootDir = File(realDirPath)
                    if (rootDir.exists() && rootDir.isDirectory && rootDir.listFiles() != null) {
                        for (file in rootDir.listFiles()) {
                            val name = file.name
                            if (name.contains(realName)) {
                                logger.debug("getDiffClasses file=$file")
                                val destFile = File(diffClassDirPath + relativePath + name)
                                //System.out.println("getDiffClasses destFile=" + destFile.getAbsolutePath());
                                val hasCopied = FileUtils.copyFile(file, destFile, true)
                                if (hasCopied) {
                                    hasDiffClass = true
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    //e.printStackTrace();
                    logger.debug("getDiffClasses error=$e")
                }
            }
        }
        return if (!hasDiffClass) {
            ""
        } else diffClassDirPath
    }

}