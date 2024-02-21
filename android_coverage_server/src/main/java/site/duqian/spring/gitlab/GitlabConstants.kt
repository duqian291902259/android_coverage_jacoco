package site.duqian.spring.gitlab

/**
 * Description:常量
 * @author n20241 Created by 杜小菜 on 2021/10/15 - 11:14 .
 * E-mail: duqian2010@gmail.com
 */
object GitlabConstants {
    /**
     * gitlab的个人setting的AccessToken里面获取
     */
    const val GITLAB_ACCESS_TOKNE = "fQxsftqfUYL3Uz9rHsPs"
    const val GITLAB_BASE_URL = "https://git-cc.nie.duqian.cn"
    const val GITLAB_API_URL = "$GITLAB_BASE_URL/api/v4/"
}

interface DiffCallBack {
    fun onDiff(list: List<String>?)
}

interface DiffClassCallBack {
    fun onFinish(diffClassPath: String?)
}