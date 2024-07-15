package com.duqian.coverage_library

import java.io.File

/**
 * Description:coverage callback
 *
 * Created by 杜乾 on 2024/7/15 - 10:19.
 * E-mail: duqian2010@gmail.com
 */
interface JacocoCallback {
    fun onEcDumped(ecPath: String?)
    fun onEcUploaded(isSingleFile: Boolean, ecFile: File)
    fun onIgnored(failedMsg: String?)
    fun onLog(TAG: String?, msg: String?)
}