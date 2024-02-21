package site.duqian.spring.utils

import org.slf4j.LoggerFactory
import site.duqian.spring.Constants
import site.duqian.spring.bean.CommonParams
import site.duqian.spring.gitlab.DiffCallBack
import site.duqian.spring.gitlab.DiffClassCallBack
import site.duqian.spring.gitlab.GitLabService
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
    fun handleDiffClasses(commonParams: CommonParams?): String {
        val diffFilePath = FileUtils.getDiffFilePath(commonParams)
        val diffFiles = FileUtils.readDiffFilesFromTxt(diffFilePath)
        val diffClassesPath = getDiffClasses(commonParams, diffFiles)
        logger.debug("generateReport diffClassesPath=$diffClassesPath")
        //也copy一下源码
        getDiffSrc(commonParams, diffFiles);
        return diffClassesPath
    }

    private fun getDiffSrc(commonParams: CommonParams?, diffFiles: List<String>?): String? {
        val diffSrcDirPath = FileUtils.getDiffSrcDirPath(commonParams)
        var hasDiffSrc = false
        var count = 0
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
                    //logger.debug("getDiffSrc realFilePath=$realFilePath")
                    val destFile = File(diffSrcDirPath + relativePath)
                    //System.out.println("getDiffSrc destFile=" + destFile.getAbsolutePath());
                    val hasCopied = FileUtils.copyFile(File(realFilePath), destFile, true)
                    if (hasCopied) {
                        hasDiffSrc = true
                        count++
                    }
                } catch (e: Exception) {
                    logger.debug("getDiffSrc error=$e")
                }
            }
            logger.debug("getDiffSrc count file=$count")
        }
        return if (!hasDiffSrc) {
            ""
        } else diffSrcDirPath
    }

    private fun getDiffClasses(commonParams: CommonParams?, diffFiles: List<String>?): String {
        val diffClassDirPath = FileUtils.getDiffClassDirPath(commonParams)
        var hasDiffClass = false
        var count = 0
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
                                //logger.debug("getDiffClasses file=$file")
                                val destFile = File(diffClassDirPath + relativePath + name)
                                //System.out.println("getDiffClasses destFile=" + destFile.getAbsolutePath());
                                val hasCopied = FileUtils.copyFile(file, destFile, true)
                                if (hasCopied) {
                                    hasDiffClass = true
                                    count++
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    //e.printStackTrace();
                    logger.debug("getDiffClasses error=$e")
                }
            }
            logger.debug("getDiffClasses count file=$count")
        }
        return if (!hasDiffClass) {
            ""
        } else diffClassDirPath
    }

    /**
     * 根据gitlab的返回结果处理差异
     */
    fun handleDiffFileByGitlab(commonParams: CommonParams): String {
        val list: List<String>? = GitLabService.getGitlabService().getGitDiffInfoSync(commonParams)
        val diffClassesPath = getDiffClasses(commonParams, list)
        logger.debug("handleDiffFileByGitlab diffClassesPath=$diffClassesPath")
        //也copy一下源码
        getDiffSrc(commonParams, list)
        return diffClassesPath
    }

}