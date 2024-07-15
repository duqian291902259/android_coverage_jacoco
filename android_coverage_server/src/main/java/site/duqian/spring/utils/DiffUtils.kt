package site.duqian.spring.utils

import org.slf4j.LoggerFactory
import site.duqian.spring.bean.CommonParams
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
        logger.debug("handleDiff  diffFilePath=$diffFilePath")

        val diffFiles = FileUtils.readDiffFilesFromTxt(diffFilePath)

        val diffClassesPath = getDiffClasses(commonParams, diffFiles)
        logger.debug("handleDiff diffClassesPath=$diffClassesPath")
        //也copy一下源码
        getDiffSrc(commonParams, diffFiles)
        return diffClassesPath
    }

    private var mCountDiffFile = 0
    private fun getDiffSrc(commonParams: CommonParams?, diffFiles: List<String>?): String? {
        val diffSrcDirPath = FileUtils.getDiffSrcDirPath(commonParams)
        val diffDirFile = File(diffSrcDirPath)
        if (diffDirFile.isDirectory && diffDirFile.listFiles()?.isNotEmpty() == true) {
            mCountDiffFile = diffDirFile.listFiles()?.size ?: 0
            return diffSrcDirPath
        }
        mCountDiffFile = 0
        if (diffFiles != null && diffFiles.isNotEmpty()) {
            val srcDirPath = FileUtils.getSourceDir(commonParams)
            logger.debug("getDiffSrc srcDirPath=$srcDirPath")
            logger.debug("getDiffSrc diffSrcDirPath=$diffSrcDirPath")
            for (diffFile in diffFiles) {
                try {
                    if (!diffFile.endsWith(".java") && !diffFile.endsWith(".kt")) {
                        continue
                    }
                    val fileName = diffFile.substring(diffFile.lastIndexOf(File.separator) + 1)
                    val rootDir = File(srcDirPath)
                    //logger.debug("getDiffSrc rootDir=$rootDir,fileName=$fileName")
                    repeatCopyFiles(rootDir, fileName, diffSrcDirPath)
                } catch (e: Exception) {
                    logger.debug("getDiffSrc error=$e")
                }
            }
            logger.debug("getDiffSrc count file=$mCountDiffFile")
        }
        return if (mCountDiffFile == 0) {
            ""
        } else diffSrcDirPath
    }

    private fun repeatCopyFiles(
        rootDir: File,
        fileName: String,
        diffDirPath: String,
    ) {
        if (rootDir.exists() && rootDir.isDirectory && rootDir.listFiles() != null) {
            //logger.debug("start repeatCopyFiles rootDir=$rootDir,fileName=$fileName")
            for (file in rootDir.listFiles()) {
                val name = file.name
                if (file.isFile) {
                    if (!name.contains(fileName)) continue
                    //logger.debug("getDiffFile file=$file")
                    var relativePath = name
                    val absolutePath = file.absolutePath
                    val srcStr = File.separator + "src" + File.separator
                    val classesStr = File.separator + "classes" + File.separator
                    if (absolutePath.contains(srcStr)) {
                        relativePath = absolutePath.substring(
                            absolutePath.indexOf(srcStr) + srcStr.length,
                            absolutePath.length
                        )
                    }
                    if (absolutePath.contains(classesStr)) {
                        relativePath = absolutePath.substring(
                            absolutePath.indexOf(classesStr) + classesStr.length,
                            absolutePath.length
                        )
                    }
                    val destFile = File(diffDirPath + File.separator + relativePath)
                    //logger.debug("copyDiffFile destFile=" + destFile.absolutePath)
                    val hasCopied = FileUtils.copyFile(file, destFile, true)
                    if (hasCopied) {
                        mCountDiffFile++
                    }
                } else {
                    repeatCopyFiles(file, fileName, diffDirPath)
                }
            }
        }
    }

    private fun getDiffClasses(commonParams: CommonParams?, diffFiles: List<String>?): String {
        val diffClassDirPath = FileUtils.getDiffClassDirPath(commonParams)
        val diffClassDirFile = File(diffClassDirPath)
        if (diffClassDirFile.isDirectory && diffClassDirFile.listFiles()?.isNotEmpty() == true) {
            mCountDiffFile = diffClassDirFile.listFiles()?.size ?: 0
            return diffClassDirPath
        }
        mCountDiffFile = 0
        if (diffFiles != null && diffFiles.isNotEmpty()) {
            val classDir = FileUtils.getClassDir(commonParams)
            logger.debug("getDiffClasses classDir=$classDir")
            for (diffFile in diffFiles) {
                try {
                    val fileName = File(diffFile).name
                    val rootDir = File(classDir)
                    val realName = fileName.substring(0, fileName.lastIndexOf("."))
                    repeatCopyFiles(rootDir, realName, diffClassDirPath)
                } catch (e: Exception) {
                    logger.debug("getDiffClasses error=$e")
                }
            }
            logger.debug("getDiffClasses count file=$mCountDiffFile")
        }
        return if (mCountDiffFile == 0) {
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