package com.duqian.coverage_library

import android.content.Context

/**
 * Description:Jacoco工具类:空实现，用于release包
 * Created by 杜乾 on 2024/7/15 - 10:19.
 * E-mail: duqian2010@gmail.com
 */
object JacocoHelper {

    /**
     * 初始化数据，应用初始化后是固定的了
     * 最外层调用的时候先初始化
     */
    fun initAppData(
        isOpenCoverage: Boolean,
        currentBranchName: String = "",
        currentCommitId: String = "",
        appName: String = "",
        hostUrl: String = "",
    ) {

    }

    fun generateEcFileAndUpload(context: Context?, deviceId: String?, callback: JacocoCallback?) {
        callback?.onIgnored("It's release app,has no implementation")
    }

    /**
     * 生成ec文件
     * isAppend 是否追加模式写ec文件
     */
    @Synchronized
    fun generateEcFile(context: Context?): Boolean {
        return false
    }

    fun getJacocoEcFileSaveDir(context: Context?): String {
        return ""
    }

    fun deleteDirectory(filePath: String): Boolean {
        return true
    }
}