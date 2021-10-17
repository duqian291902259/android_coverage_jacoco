package site.duqian.spring.bean

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