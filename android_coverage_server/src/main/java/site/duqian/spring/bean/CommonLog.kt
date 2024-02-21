package site.duqian.spring.bean

import java.io.Serializable

/**
 * Description:封装公参
 * @author n20241 Created by 杜小菜 on 2021/9/30 - 9:55 .
 * E-mail: duqian2010@gmail.com
 */
data class CommonLog(
    var viewCount: Long = 0L,
) : Serializable {
    var lastDeleteTime: Long = 0L
    override fun toString(): String {
        return "CommonLog(viewCount=$viewCount, lastDeleteTime=$lastDeleteTime)"
    }
}