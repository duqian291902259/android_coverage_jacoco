package com.duqian.coverage_library

import android.annotation.SuppressLint
import android.content.Context

/**
 * Description:Jacoco工具类:空实现，用于release包
 *
 * @author n20241 Created by 杜小菜 on 2021/9/8 - 11:55 . E-mail: duqian2010@gmail.com
 */
object JacocoHelper {

    @SuppressLint("CgiLint")
    //const val LOCAL_HOST = "http://192.168.11.201:8090"
    const val LOCAL_HOST = "http://192.168.3.85:8090"
    const val JACOCO_HOST = LOCAL_HOST //Android覆盖率平台服务器地址
    var mDeviceId: String? = ""

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
}