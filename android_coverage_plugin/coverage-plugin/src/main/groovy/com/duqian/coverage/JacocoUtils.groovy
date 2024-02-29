package com.duqian.coverage

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.invocation.Gradle
import org.gradle.api.plugins.PluginContainer

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Description:覆盖率插件相关的工具方法
 * @author n20241 Created by 杜小菜 on 2021/9/27 - 11:26 .
 * E-mail: duqian2010@gmail.com
 */
class JacocoUtils {

    private static String TAG = "dq-jacoco-utils"
    static String JACOCO_HOST = "http://192.168.11.201:18090" //默认的服务器地址，外部local.properties里面可以配置修改

    static String COV_APP_NAME = ""

    static String getCurrentBranchName() {
        //def currentBranchName = "git name-rev --name-only HEAD".execute().text.replaceAll("\n", "")
        def currentBranchName = "git rev-parse --abbrev-ref HEAD".execute().text.replaceAll("\n", "")
        println "$TAG currentBranchName:$currentBranchName"
        if (currentBranchName.contains("/")) {
            currentBranchName = currentBranchName.substring(currentBranchName.lastIndexOf("/") + 1)
        }
        if (currentBranchName.toLowerCase() == "head" && isGitlabCi()) {
            currentBranchName = getBranchNameForCI()
            println "$TAG getBranchNameForCI:$currentBranchName"
        }
        return currentBranchName
    }

    //$ git rev-parse HEAD
    //$ git rev-parse --short HEAD
    static String getCurrentCommitId() {
        def commitId = "git rev-parse HEAD".execute().text.replaceAll("\n", "")
        //由于打包的名字里面的长度为ce1f7f00，8位
        if (commitId != null && commitId.length() >= 8) {
            commitId = commitId.substring(0, 8)
        }
        //println "$TAG commitId:$commitId"
        return commitId
    }

    //获取所有module 的源码路径
    static ArrayList<String> getAllJavaDir(ProjectInternal project) {
        Set<Project> projects = project.rootProject.subprojects
        List<String> javaDir = new ArrayList<>(projects.size())
        projects.forEach {
            String srcPath = "$it.projectDir\\src\\main\\java"
            //println("$TAG java src= $srcPath")
            javaDir.add(srcPath)
        }
        println("$TAG java src size= ${javaDir.size()}")
        return javaDir
    }

    //获取所有module 的class路径
    static Object getAllClassDir(ProjectInternal project) {
        Set<Project> projects = project.rootProject.subprojects
        List<String> classDir = new ArrayList<>(projects.size())
        projects.forEach {
            String classesDir = "$it.buildDir\\intermediates\\javac\\debug\\classes"
            classDir.add(classesDir)
            def kotlin = hasKotlin(it.plugins)
            if (kotlin) {
                classDir.add("$it.buildDir\\tmp\\kotlin-classes\\debug")
            }
        }

        return classDir
    }

    /**
     *  id 'kotlin-android-extensions' id 'kotlin-kapt' 'kotlin-android'
     */
    static boolean hasKotlin(PluginContainer plugins) {
        plugins.findPlugin('kotlin-android') || plugins.findPlugin('kotlin-android-extensions') || plugins.findPlugin('kotlin-kapt')
    }

    static String getUploadRootDir(ProjectInternal project, JacocoReportExtension extension) {
        File parentFile = project.projectDir.getParentFile().getParentFile()
        String rootDir = parentFile.getAbsolutePath() + File.separator + "dq-coverage" + File.separator + JacocoUtils.COV_APP_NAME
        return rootDir
    }

    static def getBuildType(ProjectInternal project) {
        def taskNames = project.gradle.startParameter.taskNames
        for (tn in taskNames) {
            if (tn.startsWith("assemble")) {
                return tn.replaceAll("assemble", "").toLowerCase()
            }
        }
        return ""
    }

    static def sourceDirs(variant) {
        variant.sourceSets.java.srcDirs.collect { it.path }.flatten()
    }

    static def classesDirs(variant) {
        if (variant.hasProperty('javaCompileProvider')) {
            variant.javaCompileProvider.get().destinationDir
        } else {
            variant.javaCompile.destinationDir
        }
    }

    static def executionDataFile(ProjectInternal project, Task testTask, variant) {
        //testTask.jacoco.destinationFile.path
        def unitTestsData = "$project.buildDir/jacoco/${testTask}.exec"
        def androidTestsData = fileTree(dir: "${project.buildDir}/outputs/code_coverage/${variant.name.capitalize()}AndroidTest/connected/",
                includes: ["**/*.ec"])
        files([unitTestsData, androidTestsData])
    }

    /**
     * 是否是 GitlabCi
     * @return
     */
    static boolean isGitlabCi() {
        return isEnvTrue("CI")
    }

    static boolean isEnvTrue(String key) {
        def sEnv = System.getenv(key)
        def sProperty = System.getProperties().getProperty(key)
        return sEnv == "true" || sProperty == "true"
    }

    static String getBranchNameForCI() {
        return getEnv("CI_COMMIT_REF_NAME")
    }

    static String getEnv(String key) {
        return System.getenv(key)
    }

    static int copyClasses(Project it, String classesDir, String packageNameToPath, String targetDir, int count) {
        def project = it
        // 过滤不需要统计的class文件
        def finalClassDir = project.files(project.files(classesDir).files.collect {
            project.fileTree(dir: it,
                    excludes: JacocoReportExtension.defaultExcludes)
        })
        println "$TAG copyClasses finalClassDir=$finalClassDir"

        for (String path : finalClassDir) {
            println "$TAG copy class path=$path"
            int index = path.indexOf(packageNameToPath)
            if (index >= 0) {
                String suffix = path.substring(index + packageNameToPath.length())
                boolean copied = FileUtil.copyFile(new File(path), new File(targetDir + suffix))
                if (copied) {
                    count++
                } else {
                    println "$TAG ignored copy-->path=${targetDir + suffix},copied=$copied"
                }
            }
        }
        return count
    }

    static int copySrcDir(srcDir, File targetFile, int count) {
        //println "$TAG copySourceFiles currentSrcDir=${srcDir}"
        if (!srcDir.contains(File.separator + "api" + File.separator)) {
            File srcFile = new File(srcDir)
            if (srcFile.isDirectory() && srcFile.listFiles() != null) {
                FileUtil.copyDirectory(srcFile, targetFile)
                count++
            } else {
                //println "$TAG copySourceFiles ingored srcFile=${srcFile}"
            }
        } else {
            //println "$TAG copySourceFiles ingored src=${srcDir}"
        }
        return count
    }

    static String takeCurrentFlavor(Project project) {
        String tskReqStr = project.getRootProject().gradle.getStartParameter().getTaskRequests().toString()
        println "tskReqStr=$tskReqStr"

        Pattern pattern

        if (tskReqStr.contains("assemble"))
            pattern = Pattern.compile("assemble(\\w+)(Release|Debug)")
        else
            pattern = Pattern.compile("generate(\\w+)(Release|Debug)")

        Matcher matcher = pattern.matcher(tskReqStr)

        if (matcher.find())
            return matcher.group(1).toLowerCase()
        else {
            println "NO MATCH FOUND"
            return ""
        }
    }
}
