package site.duqian.spring.bean

import site.duqian.spring.Constants
import site.duqian.spring.utils.TextUtils
import java.io.Serializable
import java.net.URL

/**
 * Description:封装公参
 * @author n20241 Created by 杜小菜 on 2021/9/30 - 9:55 .
 * E-mail: duqian2010@gmail.com
 */
data class CommonParams(
    val appName: String? = "android",
    var branchName: String? = "dev",
    var commitId: String? = "10000",
    val type: String? = Constants.TYPE_FILE_EC //上传文件的类型：1，具体扩展名 2，自定义路径
) : Serializable {
    var isIncremental = false //是否增量
    var commitId2: String? = "10000"
    var uid: String? = "21390839"
    var userName: String? = "duqian02"
    var baseBranchName: String? = "dev" //对比的分支
    var diffFileName: String? = "" //diff文件夹名字
    var requestUrl: String? = ""
    var os: String? = "Android" //平台
    var uploadDir: String? = "" //自定义上传路径,部分相对路径，方便服务器生成目录

    fun getHost(): String {
        if (TextUtils.isEmpty(requestUrl)) return ""
        val url = URL(requestUrl) // 创建URL对象
        val host: String = url.host // 获取主机地址
        println("getHostUrl：$host")
        return host
    }
    fun getPath(): String {
        if (TextUtils.isEmpty(requestUrl)) return ""
        val url = URL(requestUrl) // 创建URL对象
        val path: String = url.path
        println("getPath：$path")
        return path
    }

    fun getPort(): String {
        if (TextUtils.isEmpty(requestUrl)) return ""
        val url = URL(requestUrl) // 创建URL对象
        val port: String = url.port.toString() // 获取主机地址
        println("getPort：$port")
        return port
    }

    fun getBranchId(): String {
        //'dev_#411671_android_coverage'.match(/(?<=#)\d{6}/)[0]
        /*try {
            val pattern = "/(?<=#)\\d{6}/"
            val isMatch: Boolean = Pattern.matches(pattern, branchName)
            println("$branchName 中是否包含了#xxxxx? $isMatch")
        } catch (e: Exception) {
        }*/

        var branchId = branchName
        val index = branchName?.indexOf("#") ?: -1
        if (index >= 0) {
            branchId = branchName!!.substring(index + 1, index + 1 + 6)
        }
        return if (TextUtils.isEmpty(branchId)) "dev" else branchId!!
    }

    override fun toString(): String {
        return "CommonParams(appName=$appName, requestUrl=$requestUrl, branchName=$branchName, commitId=$commitId, commitId2=$commitId2, type=$type, isIncremental=$isIncremental, diffFileName=$diffFileName)"
    }
}