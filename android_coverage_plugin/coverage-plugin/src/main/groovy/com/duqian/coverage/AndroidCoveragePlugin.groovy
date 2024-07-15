package com.duqian.coverage

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.tasks.TaskContainer
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Description:覆盖率插件
 *
 * Created by 杜乾 on 2024/7/15 - 10:52.
 * E-mail: duqian2010@gmail.com
 */
class AndroidCoveragePlugin implements Plugin<ProjectInternal> {

    private static String TAG = "DQ-Coverage"
    private static String GROUP = TAG
    private static String TASK_JACOCO_ALL = "coverageAllTaskLauncher"
    private static String TASK_JACOCO_DOWNLOAD_EC = "coverageDownloadEcData"
    private static String TASK_JACOCO_UPLOAD_BUILD_FILES = "coverageUploadBuildFiles"
    private static String TASK_JACOCO_BRANCH_DIFF_CLASS = "coverageBranchDiffClass"
    private static String TASK_JACOCO_REPORT = "coverageReport"
    private static String entryModule = "app" // 入口模块

    @Override
    void apply(ProjectInternal project) {
        CoverageReportExtension jacocoReportExtension = project.extensions.create("CoverageReportConfig", CoverageReportExtension)
        project.plugins.apply(JacocoPlugin)

        //配置BuildConfig.因为无论release还是debug都需要根据是否开启覆盖率统计来设置下相关信息，代码里面需要。
        def isOpenJacoco = "${project.rootProject.ext.coverageEnabled}" == "true"
        println "$TAG coverageEnable=${isOpenJacoco}"

        if (project.hasProperty("COV_ENTRY_MODULE")) {
            entryModule = project.rootProject.ext.COV_ENTRY_MODULE + ""
        }
        println "$TAG entryModule=${entryModule}"

        //外部项目模块存在依赖，所以都更新下此刻拿不到JacocoReportExtension属性值
        updateBuildConfigField(project, isOpenJacoco)

        //开启了jacoco才注册任务，只在application的模块注册。todo-dq src和classes的路径问题，精简到只copy开启了覆盖率统计的模块
        if (isOpenJacoco) {
            //project.plugins.all {
            def isEntry = project.name == entryModule
            println "$TAG project=${project.name},isEntry=$isEntry"
            if (isEntry) {
                registerJacocoTasks(project, jacocoReportExtension)
            }
        }
        project.afterEvaluate {
            println "$TAG afterEvaluate"
            project.tasks.forEach {
                if (it.name.toLowerCase().contains("jacoco")) {
                    println "$TAG taskName = ${it.name}"
                }
            }
        }
    }

    private registerJacocoTasks(ProjectInternal project, CoverageReportExtension jacocoReportExtension) {
        //最好只在app里面处理，不用各个moudle里面搞task
        Task jacocoReportEntryTask = findOrCreateJacocoReportTask(project.tasks)
        DownloadEcTask jacocoDownloadTask = project.tasks.findByName(TASK_JACOCO_DOWNLOAD_EC)
        if (jacocoDownloadTask == null) {//download .ec
            jacocoDownloadTask = project.tasks.create(TASK_JACOCO_DOWNLOAD_EC, DownloadEcTask)
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

        UploadBuildFileTask jacocoUploadTask = project.tasks.findByName(TASK_JACOCO_UPLOAD_BUILD_FILES)
        if (jacocoUploadTask == null) {//upload build classes
            jacocoUploadTask = project.tasks.create(TASK_JACOCO_UPLOAD_BUILD_FILES, UploadBuildFileTask)
            jacocoUploadTask.setExtension(jacocoReportExtension)
            jacocoUploadTask.setGroup(GROUP)
            jacocoUploadTask.dependsOn(branchDiffClassTask)
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
        /*def uploadTask = project.tasks.findByName(TASK_JACOCO_UPLOAD_BUILD_FILES)
        def flavorName = jacocoReportExtension.flavorName
        def flavor = flavorName.substring(0, 1).toUpperCase() + flavorName.substring(1)
        Task buildTask = project.tasks.findByName("assemble" + flavor)
        if (buildTask != null && uploadTask != null) {
            println("$TAG start uploadTask=" + buildTask)
            buildTask.finalizedBy(uploadTask)
        }*/
    }

    /**
     * 设置BuildConfig的值
     * @param currentBranchName
     * @param commitId
     * @param jacocoHost
     * @param appName
     * @param isJacocoEnable
     */
    private static void resetBuildConfig(Project project, String currentBranchName, String commitId, String jacocoHost, String appName, boolean isJacocoEnable) {
        println("$TAG resetBuildConfig,isJacocoEnable=$isJacocoEnable")
        def defaultConfig = project.android.defaultConfig
        defaultConfig.buildConfigField "String", "CUR_BCH_NAME", "\"" + currentBranchName + "\""
        defaultConfig.buildConfigField "String", "CUR_CMT_ID", "\"" + commitId + "\""
        defaultConfig.buildConfigField "String", "COV_HOST", "\"" + jacocoHost + "\""
        defaultConfig.buildConfigField "String", "COV_P_NAME", "\"" + appName + "\""
        defaultConfig.buildConfigField "boolean", "IS_COV_ENABLE", "" + isJacocoEnable + ""
    }


    /**
     * 初始化构建的module到BuildConfig
     */
    private static void updateBuildConfigField(Project project, boolean isJacocoEnable) {
        def currentBranchName = CoverageUtils.getCurrentBranchName()
        String commitId = CoverageUtils.getCurrentCommitId()

        if (!isJacocoEnable) {//设置默认值
            resetBuildConfig(project, "", commitId, "", "", false)
            return
        }

        def jacocoHost = CoverageUtils.JACOCO_HOST
        if (project.hasProperty("COV_JACOCO_HOST")) {
            jacocoHost = project.rootProject.ext.COV_JACOCO_HOST + ""
            CoverageUtils.JACOCO_HOST = jacocoHost
        }
        //自定义应用名称，一定要有,不设置，让报错
        if (!project.hasProperty("COV_APP_NAME")) {
            println("覆盖率系统需要一个自定义显示的AppName，请在项目根目录的gradle.properties中定义：COV_APP_NAME=yourAppName")
        }
        String appName = project.rootProject.ext.COV_APP_NAME + ""
        CoverageUtils.COV_APP_NAME = appName

        Set<Project> set = project.getRootProject().getAllprojects()
        println("$TAG currentBranchName ${currentBranchName},commitId ${commitId},isJacocoEnable ${isJacocoEnable}")
        try {
            for (Project proj : set) {
                def moduleName = proj.getName()
                if (moduleName == "api") continue
                if (moduleName.contains(entryModule) || moduleName.contains("coverage-library") || moduleName.contains("app")) {
                    resetBuildConfig(project, currentBranchName, commitId, jacocoHost, appName, isJacocoEnable)
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
        def sourceDirs = CoverageUtils.getAllJavaDir(project) //sourceDirs(variant)
        def classDir
        /*if (project.CoverageReportConfig.isDiffJacoco) {//todo-dq 增量覆盖率 diffFiles
            classDir = new ArrayList<>()
            classDir.add("$project.projectDir/classes")
        } else {
            classDir = JacocoUtils.getAllClassDir(project)
        }*/
        classDir = CoverageUtils.getAllClassDir(project)
        // 过滤不需要统计的class文件
        def finalClassDir = project.files(project.files(classDir).files.collect {
            project.fileTree(dir: it,
                    excludes: CoverageReportExtension.defaultExcludes)
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
            def destination = project.CoverageReportConfig.destination

            csv.enabled project.CoverageReportConfig.csv.enabled
            xml.enabled project.CoverageReportConfig.xml.enabled
            html.enabled project.CoverageReportConfig.html.enabled
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
