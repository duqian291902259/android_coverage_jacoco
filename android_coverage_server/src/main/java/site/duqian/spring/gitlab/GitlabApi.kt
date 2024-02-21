package site.duqian.spring.gitlab

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

//@RetrofitClient(baseUrl = GitlabConstants.GITLAB_API_URL)
interface GitlabApi {
    /**
     * 获取提交列表
     */
    @GET("projects?private_token="+GitlabConstants.GITLAB_ACCESS_TOKNE)
    fun getGitInfo(): Call<ResponseBody>

    /**
     * 获取diff信息
     */
    @GET("projects/{id}/repository/compare?private_token="+GitlabConstants.GITLAB_ACCESS_TOKNE)
    fun getCompareCommits(
        @Path("id") id: Int,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("from_project_id") from_project_id: Int?=0,
        @Query("straight") straight: Boolean? = null//	Comparison method, true for direct comparison between from and to (from..to), false to compare using merge base (from…to)’. Default is false.

    ): Call<ResponseBody>
/**
     * 获取diff信息
     */
    @GET("projects/{id}/repository/compare?private_token="+GitlabConstants.GITLAB_ACCESS_TOKNE)
    fun getCompareDiffBean(
        @Path("id") id: Int,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("from_project_id") from_project_id: Int?=0,
        @Query("straight") straight: Boolean? = null//	Comparison method, true for direct comparison between from and to (from..to), false to compare using merge base (from…to)’. Default is false.
    ): Call<GitLabDiffBean>

    /**
     * 获取提交列表
     */
    @GET("projects/{projectId}/repository/commits?private_token="+GitlabConstants.GITLAB_ACCESS_TOKNE)
    fun getCommits(
        @Path("projectId") projectId: Int,
        @Query("ref_name") refName: String? = null,
        @Query("since") since: String? = null,
        @Query("until") until: String? = null,
        @Query("all") all: Boolean? = null
    ): Call<ResponseBody>


    /**
     * 搜索分支
     */
    @GET("projects/{projectId}/repository/branches?private_token="+GitlabConstants.GITLAB_ACCESS_TOKNE)
    fun getBranchBySearch(
        @Path("projectId") projectId: Int,
        @Query("search") sha: String = "",
    ): Call<ResponseBody>

    /**
     * 获取单个分支信息
     */
    @GET("projects/{projectId}/repository/branches/{name}?private_token="+GitlabConstants.GITLAB_ACCESS_TOKNE)
    fun getBranchByName(
        @Path("projectId") projectId: Int,
        @Path("name") name: String,
    ): Call<ResponseBody>
}


