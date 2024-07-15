package site.duqian.spring.gitlab

import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.models.Commit
import org.gitlab4j.api.models.Project
import org.gitlab4j.api.utils.ISO8601
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


/**
 * Description:操作Gitlab的API
 *
 * Created by Dusan on 2024/6/27 - 18:14.
 * E-mail: duqian2010@gmail.com
 */
object GitlabApiHelper {

    private var gitLabApi: GitLabApi? = null
    private const val GITLAB_HOST = ""
    //可以外部传参数修改这个值
    var PERSONAL_ACCESS_TOKEN = "" //todo-dq 按照项目来存储token
    private const val mProjectId = 1000

    private val loggerInstance = Logger.getLogger("GitlabApi")

    private fun initGitlabApi(): GitLabApi {
        // Create a GitLabApi instance to communicate with your GitLab server
        if (gitLabApi == null) {
            try {
                gitLabApi = GitLabApi(GITLAB_HOST, PERSONAL_ACCESS_TOKEN)
                gitLabApi?.let {
                    // Set the connect timeout to 1 second and the read timeout to 5 seconds
                    it.setRequestTimeout(10 * 1000, 15 * 1000)

                    // Log using the shared logger and default level of FINE
                    it.enableRequestResponseLogging()
                    // Log using the shared logger and the INFO level
                    it.enableRequestResponseLogging(java.util.logging.Level.INFO)
                    // Log using the specified logger and the INFO level
                    it.enableRequestResponseLogging(loggerInstance, java.util.logging.Level.INFO);
                    // Log using the shared logger, at the INFO level, and include up to 1024 bytes of entity logging
                    it.enableRequestResponseLogging(java.util.logging.Level.INFO, 1024)
                    // Log using the specified logger, at the INFO level, and up to 1024 bytes of entity logging
                    it.enableRequestResponseLogging(loggerInstance, java.util.logging.Level.INFO, 1024)
                }
            } catch (e: Exception) {
                loggerInstance.log(Level.INFO, "error $e")
            }
        }
        return gitLabApi!!
    }

    fun getProjects(): List<Project> {
        // Get the list of projects your account has access to
        val projects = initGitlabApi().projectApi.projects ?: listOf()
        loggerInstance.log(Level.INFO, "projects size= ${projects.size}")
        println("project size= ${projects.size}")
        return projects
    }

    fun getCommitList(): Int {
        // Get a list of commits associated with the specified branch that fall within the specified time window
        val since: Date = ISO8601.toDate("2024-06-27T00:00:00Z")
        val until = Date() // now

        val commits: List<Commit> =
            initGitlabApi().commitsApi.getCommits(mProjectId, "dev_app_opt_fix", since, until)
        val size = commits.size
        loggerInstance.log(Level.INFO, "commits size= $size")
        println("project commits size= $size")
        return size
    }

    fun getProject2() {
        // Get a Pager instance that will page through the projects with 10 projects per page
        val projectPager = initGitlabApi().projectApi.getProjects(10)
        while (projectPager.hasNext()) {
            for (project in projectPager.next()) {
                println("project=" + project.name + " -: " + project.description)
            }
        }
    }
}