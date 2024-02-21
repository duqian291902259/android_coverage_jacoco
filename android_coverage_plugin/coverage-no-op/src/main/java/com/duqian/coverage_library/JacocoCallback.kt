package com.duqian.coverage_library

import java.io.File

/**
 * Description:回调
 * @author n20241 Created by 杜小菜 on 2022/1/21 - 10:01 .
 * E-mail: duqian2010@gmail.com
 */
interface JacocoCallback {
    fun onEcDumped(ecPath: String?)
    fun onEcUploaded(isSingleFile: Boolean, ecFile: File)
    fun onIgnored(failedMsg: String?)
    fun onLog(TAG: String?, msg: String?)
}