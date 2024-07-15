package com.duqian.coverage_library

import java.io.File

interface JacocoCallback {
    fun onEcDumped(ecPath: String?)
    fun onEcUploaded(isSingleFile: Boolean, ecFile: File)
    fun onIgnored(failedMsg: String?)
    fun onLog(TAG: String?, msg: String?)
}