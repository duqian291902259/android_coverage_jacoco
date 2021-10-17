package site.duqian.spring.bean

import site.duqian.spring.Constants
import java.io.Serializable

/**
 * Description:封装响应
 * @author n20241 Created by 杜小菜 on 2021/9/30 - 9:55 .
 * E-mail: duqian2010@gmail.com
 */

open class BaseResponse(
    val result: Int = 0,
    var message: String? = "",
    var data: String? = ""
) : Serializable {
    override fun toString(): String {
        return "CommonParams(result=$result, message=$message)"
    }
}

data class ReportResponse(
    val reportUrl: String? = "",
    var reportZipUrl: String? = ""
) : BaseResponse() {
    override fun toString(): String {
        return "CommonParams(reportUrl=$reportUrl, reportZipUrl=$reportZipUrl)"
    }
}

/**
 * 响应报告管理页面的文件列表
 */
data class FileListResp(
    val reportHostUrl: String? = Constants.REPORT_SERVER_HOST_URL,
    val fileList: List<ReportFileItem>?
) : BaseResponse() {
    val fileSize: Int = fileList?.size ?: 0

    override fun toString(): String {
        return "FileListResp(reportHostUrl=$reportHostUrl, fileList=$fileList)"
    }
}

data class ReportFileItem(
    val basePath: String? = "",
    var fileName: String? = "",
    var date: String? = "",
) : Serializable {
    override fun toString(): String {
        return "ReportFileItem(basePath=$basePath, fileName=$fileName)"
    }
}