package site.duqian.spring.bean

import site.duqian.spring.Constants
import java.io.Serializable

/**
 * Description:封装公参
 * @author n20241 Created by 杜小菜 on 2021/9/30 - 9:55 .
 * E-mail: duqian2010@gmail.com
 */
data class CommonParams(
    val appName: String? = "cc-android",
    var versionCode: String? = "3.8.3",
    val branchName: String? = "dev",
    var commitId: String? = "10000",
    val type: String? = Constants.TYPE_FILE_EC
) : Serializable {

    var isIncremental = false //是否增量
    var commitId2: String? = "10000"

    override fun toString(): String {
        return "CommonParams(appName=$appName, versionCode=$versionCode, branchName=$branchName, commitId=$commitId, commitId2=$commitId2, type=$type, isIncremental=$isIncremental)"
    }
}