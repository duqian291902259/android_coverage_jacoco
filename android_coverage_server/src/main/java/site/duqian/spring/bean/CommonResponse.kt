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
    var reportHostUrl: String? = Constants.REPORT_SERVER_HOST_URL,
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
) : Serializable, Comparable<ReportFileItem> {
    var modifyTime: Long? = 0

    override fun toString(): String {
        return "ReportFileItem(basePath=$basePath, fileName=$fileName)"
    }

    override operator fun compareTo(other: ReportFileItem): Int {
        return (this.modifyTime?.let { other.modifyTime?.minus(it) })?.toInt() ?: 0
    }

}

/**
 * 响应报告分支列表
 */
data class BranchListResp(
    val appList: List<String>?,
    val branchList: List<BranchItem>?
) : BaseResponse() {
    val branchSize: Int = branchList?.size ?: 0

    override fun toString(): String {
        return "BranchListResp(branchSize=$branchSize, appList=$appList, branchList=$branchList)"
    }
}


data class BranchItem(
    var branchName: String? = "",
    var branchLabel: String? = "",
    var latestCommit: String? = "",
    var oldCommit: String? = "",
) : Serializable {
    override fun toString(): String {
        return "BranchItem(branchName=$branchName, branchLabel=$branchLabel, latestCommit=$latestCommit, oldCommit=$oldCommit)"
    }
}

open class UploadResponse(
    val code: Int = 0,
    var msg: String? = "",
    var fileName: String? = ""
) : Serializable {
    override fun toString(): String {
        return "UploadResponse(code=$code, msg=$msg, fileName=$fileName)"
    }
}

open class DeleteResponse(
    val code: Int = 0,
    var msg: String? = "",
    var filePath: String? = ""
) : Serializable {
    override fun toString(): String {
        return "UploadResponse(code=$code, msg=$msg, fileName=$filePath)"
    }
}