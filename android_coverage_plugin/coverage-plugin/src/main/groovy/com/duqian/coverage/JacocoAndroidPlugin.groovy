package com.duqian.coverage

//import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.ApplicationPlugin
import org.gradle.api.tasks.TaskContainer
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Description:Jacoco,覆盖率插件
 * @author n20241 Created by 杜小菜 on 2021/9/10 - 10:04 .
 * E-mail: duqian2010@gmail.com
 */
class JacocoAndroidPlugin implements Plugin<ProjectInternal> {

    private static String TAG = "dq-jacoco"
    private static String GROUP = TAG
    private static String TASK_JACOCO_ALL = "jacocoAllTaskLauncher"
    private static String TASK_JACOCO_DOWNLOAD_EC = "jacocoDownloadEcData"
    private static String TASK_JACOCO_UPLOAD_BUILD_FILES = "jacocoUploadBuildFiles"
    private static String TASK_JACOCO_BRANCH_DIFF_CLASS = "jacocoBranchDiffClass"
    private static String TASK_JACOCO_REPORT = "jacocoReport"

    @Override
    void apply(ProjectInternal project) {
        JacocoReportExtension jacocoReportExtension = project.extensions.create("jacocoReportConfig", JacocoReportExtension)
        project.plugins.apply(JacocoPlugin)

        //配置BuildConfig.因为无论release还是debug都需要根据是否开启覆盖率统计来设置下相关信息，代码里面需要。
        boolean isGitlabCiOpen = JacocoUtils.isGitlabCi() && "${project.rootProject.ext.coverageEnabledCi}" == "true"
        def isOpenJacoco = isGitlabCiOpen || "${project.rootProject.ext.coverageEnabled}" == "true"
        println "$TAG dq-coverage isGitlabCiOpen=$isGitlabCiOpen,coverageEnable=${isOpenJacoco}"

        //外部项目模块存在依赖，所以都更新下此刻拿不到JacocoReportExtension属性值
        updateBuildConfigField(project, isOpenJacoco)

        //开启了jacoco才注册任务，只在application的模块注册
        if (isOpenJacoco) {
            project.plugins.all {
                registerJacocoTasks(project, jacocoReportExtension)
                if (it instanceof ApplicationPlugin) {
                    println "$TAG registerJacocoTasks $it"
                    /*JacocoTransform jacocoTransform = new JacocoTransform(project, jacocoReportExtension)
                    android.registerTransform(jacocoTransform)
                    println("$TAG,registerJacocoTransform $android")*/
                } else {
                    // println "$TAG not registerJacocoTasks $it"
                }
            }

            /*project.tasks.whenTaskAdded {
                if (it.name.startsWith(TASK_JACOCO_UPLOAD_BUILD_FILES)) {
                    it.doLast {
                        println "$TAG  upload for jacoco,finished"
                    }
                }
            }*/
        }
    }

    private registerJacocoTasks(ProjectInternal project, JacocoReportExtension jacocoReportExtension) {
        //project.afterEvaluate {
        //最好只在app里面处理，不用各个moudle里面搞task
        Task jacocoReportEntryTask = findOrCreateJacocoReportTask(project.tasks)
        JacocoDownloadTask jacocoDownloadTask = project.tasks.findByName(TASK_JACOCO_DOWNLOAD_EC)
        if (jacocoDownloadTask == null) {//download .ec
            jacocoDownloadTask = project.tasks.create(TASK_JACOCO_DOWNLOAD_EC, JacocoDownloadTask)
            jacocoDownloadTask.setExtension(jacocoReportExtension)
            jacocoDownloadTask.setGroup(GROUP)
        }

        //todo-dq 本地提交点之间的增量，获取增量报告
        BranchDiffClassTask branchDiffClassTask = project.tasks.findByName(TASK_JACOCO_BRANCH_DIFF_CLASS)
        if (branchDiffClassTask == null) {//pull copy diff class
            branchDiffClassTask = project.tasks.create(TASK_JACOCO_BRANCH_DIFF_CLASS, BranchDiffClassTask)
            branchDiffClassTask.setGroup(GROUP)
            branchDiffClassTask.setExtension(jacocoReportExtension)
        }

        JacocoUploadBuildFileTask jacocoUploadTask = project.tasks.findByName(TASK_JACOCO_UPLOAD_BUILD_FILES)
        if (jacocoUploadTask == null) {//upload build classes
            jacocoUploadTask = project.tasks.create(TASK_JACOCO_UPLOAD_BUILD_FILES, JacocoUploadBuildFileTask)
            jacocoUploadTask.setExtension(jacocoReportExtension)
            jacocoUploadTask.setGroup(GROUP)
            //jacocoUploadTask.dependsOn(branchDiffClassTask)
        }

        Task jacocoReportTask = project.tasks.findByName(TASK_JACOCO_REPORT)
        if (jacocoReportTask == null) {//cc coverage report
            jacocoReportTask = createReportTask(project, null)
            //jacocoReportTask.enabled = false
            println("$TAG,$TASK_JACOCO_REPORT " + jacocoReportTask)
            //jacocoReportTask.dependsOn(jacocoDownloadTask)
        }

        //自由组合、控制一些逻辑的执行
        jacocoReportEntryTask.doFirst {
            //jacocoDownloadTask.downloadJacocoEcFile()
            if (jacocoReportExtension.isDiffJacoco) {
                branchDiffClassTask.makeDiffClass()
            }
            jacocoUploadTask.uploadBuildFile()
            println("$TAG jacocoReportEntryTask " + it)
        }

        jacocoReportEntryTask.doLast {//要有ec文件，否则本地无法生成
            jacocoReportTask.generate()
        }

        //压缩并上传class/apk文件
        def uploadTask = project.tasks.findByName(TASK_JACOCO_UPLOAD_BUILD_FILES)
        def flavorName = jacocoReportExtension.flavorName
        def flavor = flavorName.substring(0, 1).toUpperCase() + flavorName.substring(1)
        Task buildTask = project.tasks.findByName("assemble" + flavor)
        println("$TAG prepare buildTask=$buildTask,flavor=" + flavor)
        if (buildTask != null && uploadTask != null) {
            println("$TAG start uploadTask=" + buildTask)
            buildTask.finalizedBy(uploadTask)
        }
        //}
    }

    /**
     * 初始化构建的module到BuildConfig
     */
    private static void updateBuildConfigField(Project project, boolean isJacocoEnable) {
        def currentBranchName = JacocoUtils.getCurrentBranchName()
        String commitId = JacocoUtils.getCurrentCommitId()

        String entryModule = ""
        if (project.hasProperty("COV_ENTRY_MODULE")) {
            entryModule = project.rootProject.ext.COV_ENTRY_MODULE + ""
        }

        def jacocoHost = JacocoUtils.JACOCO_HOST
        if (project.hasProperty("COV_JACOCO_HOST")) {
            jacocoHost = project.rootProject.ext.COV_JACOCO_HOST + ""
            JacocoUtils.JACOCO_HOST = jacocoHost
        }

        //自定义应用名称，一定要有,不设置，让报错
        if (!project.hasProperty("COV_APP_NAME")) {
            println("覆盖率系统需要一个自定义显示的AppName，请在项目根目录的gradle.properties中定义：COV_APP_NAME=yourAppName")
        }
        String appName = project.rootProject.ext.COV_APP_NAME + ""
        JacocoUtils.COV_APP_NAME = appName

        //println "$TAG updateBuildConfigField appName=$appName,project=${project.getName()},entryModule=$entryModule,jacocoHost=$jacocoHost"

        Set<Project> set = project.getRootProject().getAllprojects()
        println("$TAG currentBranchName ${currentBranchName},commitId ${commitId},isJacocoEnable ${isJacocoEnable}")
        try {
            for (Project proj : set) {
                def moduleName = proj.getName()
                if (moduleName == "api") continue
                if (moduleName.contains(entryModule) || moduleName.contains("coverage-library") || moduleName.contains("app")) {
                    def defaultConfig = project.android.defaultConfig
                    defaultConfig.buildConfigField "String", "CURRENT_BRANCH_NAME", "\"" + currentBranchName + "\""
                    defaultConfig.buildConfigField "String", "CURRENT_COMMIT_ID", "\"" + commitId + "\""
                    defaultConfig.buildConfigField "boolean", "IS_JACOCO_ENABLE", "" + isJacocoEnable + ""
                    defaultConfig.buildConfigField "String", "JACOCO_HOST", "\"" + jacocoHost + "\""
                    defaultConfig.buildConfigField "String", "COV_APP_NAME", "\"" + appName + "\""

                    //println "$TAG updateBuildConfigField moduleName=$moduleName,entryModule=$entryModule"
                }
            }
        } catch (Exception e) {//工程根目录也能获取到，作为模块名获取android就有异常
            println("$TAG updateBuildConfigField error $e")
        }
    }

    private static Task findOrCreateJacocoReportTask(TaskContainer tasks) {
        Task jacocoTestReportTask = tasks.findByName(TASK_JACOCO_ALL)
        if (!jacocoTestReportTask) {
            jacocoTestReportTask = tasks.create(TASK_JACOCO_ALL)
            jacocoTestReportTask.group = GROUP
            jacocoTestReportTask.description = "Try to generate coverage report!!"
        }
        jacocoTestReportTask
    }

    private static JacocoReport createReportTask(ProjectInternal project, variant) {
        def sourceDirs = JacocoUtils.getAllJavaDir(project) //sourceDirs(variant)
        def classDir
        /*if (project.jacocoReportConfig.isDiffJacoco) {//todo-dq 增量覆盖率 diffFiles
            classDir = new ArrayList<>()
            classDir.add("$project.projectDir/classes")
        } else {
            classDir = JacocoUtils.getAllClassDir(project)
        }*/
        classDir = JacocoUtils.getAllClassDir(project)
        // 过滤不需要统计的class文件
        def finalClassDir = project.files(project.files(classDir).files.collect {
            project.fileTree(dir: it,
                    excludes: JacocoReportExtension.defaultExcludes)
        })
        //println("$TAG java classDir size= ${classDir.size()},finalClassDir.size=${finalClassDir.size()}")

        //指定ec文件
        def executionDataDir = "${project.buildDir}/outputs/code_coverage/connected/"
        def executionDataPaths = project.files(project.files(executionDataDir).files.collect {
            project.fileTree(dir: it,
                    includes: ["*.ec"])
        })

        JacocoReport reportTask = project.tasks.create(TASK_JACOCO_REPORT, JacocoReport)
        reportTask.doFirst {
            println("$TAG,createReportTask :do first.executionDataDir=$executionDataDir")
        }
        reportTask.group = GROUP
        reportTask.description = "Generates Jacoco coverage reports whole project."
        reportTask.executionData.setFrom(executionDataPaths)

        reportTask.sourceDirectories.setFrom(sourceDirs)
        reportTask.classDirectories.setFrom(finalClassDir)

        reportTask.reports {
            def destination = project.jacocoReportConfig.destination

            csv.enabled project.jacocoReportConfig.csv.enabled
            xml.enabled project.jacocoReportConfig.xml.enabled
            html.enabled project.jacocoReportConfig.html.enabled
            html.outputLocation = project.layout.buildDirectory.dir('JacocoHtml')

            if (csv.enabled) {
                csv.destination new File((destination == null) ? "${project.buildDir}/jacoco/jacoco.csv" : "${destination.trim()}/jacoco.csv")
            }

            //println("$TAG html.enabled = ${html.enabled},destination: $reportTask.reports.html.destination")
            if (html.enabled) {
                html.destination new File((destination == null) ? "${project.buildDir}/jacoco/jacocoHtml" : "${destination.trim()}/jacocoHtml")
            }

            if (xml.enabled) {
                xml.destination new File((destination == null) ? "${project.buildDir}/jacoco/jacoco.xml" : "${destination.trim()}/jacoco.xml")
            }
        }
        reportTask
    }

}
