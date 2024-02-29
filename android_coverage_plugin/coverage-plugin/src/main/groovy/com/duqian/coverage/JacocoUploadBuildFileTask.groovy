package com.duqian.coverage

import okhttp3.*
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
/**
 * Description:上传build后的产物，这里上传classes
 * @author n20241 Created by 杜小菜 on 2021/9/22 - 16:25 .
 * E-mail: duqian2010@gmail.com
 */
class JacocoUploadBuildFileTask extends DefaultTask {
    private static final String TAG = "dq-JacocoUploadClassTask"
    public static final String TYPE_FILE_EC = ".ec"
    public static final String TYPE_FILE_ZIP = ".zip"
    public static final String TYPE_FILE_APK = ".apk"
    public static final String TYPE_FILE_TXT = ".txt"
    private JacocoReportExtension extension

    void setExtension(JacocoReportExtension extension) {
        this.extension = extension
    }

    @TaskAction
    def uploadBuildFile() {
        try {
            if (!extension.isJacocoEnable) {
                println "$TAG uploadBuildFile ignored."
                return
            }
            long start = System.currentTimeMillis()
            //后续反编译apk获取class的话，可以使用uploadApk()
            //本地diff,是取分支的差异，后续服务器处理，拉取remote代码对比提交点
            uploadDiffFiles()
            long uploadDiffTime = System.currentTimeMillis() - start
            println "$TAG uploadDiffTime $uploadDiffTime"

            //全量copy class，本地不做class差异上报，后端根据commitId做diff
            copyClassesAndZip()
            long copyClassesAndZipTime = System.currentTimeMillis() - start
            println "$TAG copyClassesAndZipTime $copyClassesAndZipTime"

            uploadClassFiles()
            long uploadClassFilesTime = System.currentTimeMillis() - start
            println "$TAG uploadClassFilesTime $uploadClassFilesTime"

            uploadSourceFiles()
            long uploadSourceFilesTime = System.currentTimeMillis() - start
            println "$TAG uploadSourceFilesTime $uploadSourceFilesTime"
        } catch (Exception e) {
            println "$TAG uploadBuildFile error=$e"
        }
    }

    private def uploadSourceFiles() {
        //1,copy src,只处理自己包名下的class
        String rootDir = getSrcSavedDir()
        def packageNameToPath = getPackagePath()
        String targetDir = rootDir + File.separator + packageNameToPath
        File targetFile = new File(targetDir)
        FileUtil.deleteDirectory(targetDir)
        try {
            targetFile.mkdirs()
        } catch (Exception e) {
            println "$TAG uploadSourceFiles,mkdirs failed:$e"
        }
        println "$TAG copySourceFiles saved to =${targetFile.getAbsolutePath()}"
        Set<Project> projects = project.rootProject.subprojects
        int count = 0
        projects.forEach {
            def rootSrcPath = it.projectDir.getAbsolutePath() + File.separator
            def mainSrcDir = rootSrcPath + "src/main/java/" + packageNameToPath
            count = JacocoUtils.copySrcDir(mainSrcDir, targetFile, count)

            //debug的目录也要copy
            def debugSrcDir = rootSrcPath + "src/debug/java/" + packageNameToPath
            count = JacocoUtils.copySrcDir(debugSrcDir, targetFile, count)

            //release的
            def releaseSrcDir = rootSrcPath + "src/release/java/" + packageNameToPath
            count = JacocoUtils.copySrcDir(releaseSrcDir, targetFile, count)
        }

        println "$TAG src count=${count}"

        //2,dir -->zip
        String srcDir = getSrcSavedDir()
        String zipFilePath = getSrcZipSavedDir()
        boolean hasZip = compressToZip(srcDir, zipFilePath)
        if (hasZip) {
            FileUtil.deleteDirectory(srcDir)
        }
        println "$TAG hasZip=$hasZip,srcDir=$srcDir,zipFilePath=$zipFilePath"

        //3,upload src
        File file = new File(zipFilePath)
        if (file != null && file.exists() && file.isFile() && file.length() > 0) {
            println "$TAG  SourceFiles size=${file.size()}"
            syncUploadFiles(file, TYPE_FILE_ZIP)
        } else {
            println "$TAG uploadSourceFiles failed,file not exists: ${file}"
        }
    }

    private static int copySrcDir(srcDir, File targetFile, int count) {
        println "$TAG copySourceFiles currentSrcDir=${srcDir}"
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

    private def copyClassesAndZip() {
        //1,copy all dq-class to outer dir
        copyBuildClassDirs()
        //2,zip
        String classesDir = getClassSavedDir()
        String zipFilePath = getClassZipSavedDir()
        boolean hasZip = compressToZip(classesDir, zipFilePath)
        //println "$TAG classesDir=$classesDir,zipFilePath=$zipFilePath"
        if (hasZip) {
            FileUtil.deleteDirectory(classesDir)
        }
    }

    private def uploadClassFiles() {
        //3,upload zip.上面的可能失败，没成功是没有zip文件的
        String classZipFilePath = getClassZipSavedDir()
        File file = new File(classZipFilePath)
        if (file != null && file.exists() && file.isFile() && file.length() > 0) {
            //println "$TAG uploadClass zip $file,size=${file.size()}"
            syncUploadFiles(file, TYPE_FILE_ZIP)
        } else {
            println "$TAG uploadClassFiles failed,file not exists: ${file}"
        }
    }

    private String getUploadRootDir() {
        //File parentFile = project.projectDir.getParentFile().getParentFile()
        //String rootDir = parentFile.getAbsolutePath() + File.separator + "jacoco_upload"
        return JacocoUtils.getUploadRootDir(project, extension)
    }

    private String getClassSavedDir() {
        return getUploadRootDir() + File.separator + "classes"
    }

    private String getClassZipSavedDir() {
        String rootDir = getUploadRootDir()
        return "${rootDir}/classes.zip"
    }

    private String getSrcSavedDir() {
        return getUploadRootDir() + File.separator + "src"
    }

    private String getSrcZipSavedDir() {
        String rootDir = getUploadRootDir()
        return "${rootDir}/src.zip"
    }

    private String getPackagePath() {
        //def android = project.extensions.android
        //def applicationId = android.defaultConfig.applicationId
        String realPackageName = extension.packageName
        //println "$TAG applicationId :$applicationId,packageName=${extension.packageName}"
        String packagePath = realPackageName.replaceAll("\\.", Matcher.quoteReplacement(File.separator))
        return packagePath
    }

    /*private static int copyClasses(Project it, String classesDir, String packageNameToPath, String targetDir, int count) {
        def project = it
        // 过滤不需要统计的class文件
        def finalClassDir = project.files(project.files(classesDir).files.collect {
            project.fileTree(dir: it,
                    excludes: JacocoReportExtension.defaultExcludes)
        })
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
    }*/

    /**
     * copy指定目录下面的class
     * @return 保存文件的根目录
     */
    private String copyBuildClassDirs() {
        //过滤不需要的文件
        try {
            String rootDir = getClassSavedDir()
            //只处理自己包名下的class：com.duqian.cc
            String packageNameToPath = getPackagePath()
            String targetDir = rootDir + File.separator + packageNameToPath
            File targetFile = new File(targetDir)
            FileUtil.deleteDirectory(targetDir)
            targetFile.mkdirs()
            println "$TAG copyBuildClassDirs targetDir=$targetDir，packageNameToPath=$packageNameToPath"
            Set<Project> projects = project.rootProject.subprojects
            int count = 0

            //遍历kotlin编译的class
            def kotlin = JacocoUtils.hasKotlin(project.plugins)

            def flavorName = JacocoUtils.takeCurrentFlavor(project)
            println "$TAG kotlin=$kotlin,flavorName=$flavorName"

            def flavor = extension.flavorName
            println "$TAG kotlin=$kotlin,flavorName=$flavorName,flavor=$flavor"

            projects.forEach {
                def currentBuildDir = it.buildDir.getAbsolutePath()
                println "$TAG currentBuildDir=$currentBuildDir"
                //copy java编译后的class
                String classesDir = "$currentBuildDir\\intermediates\\javac\\debug\\classes\\$packageNameToPath"
                count = JacocoUtils.copyClasses(it, classesDir, packageNameToPath, targetDir, count)


                //kotlin
                flavor = "hiiclubApkDebug"
                String classesDirKotlin = "$currentBuildDir\\tmp\\kotlin-classes\\" + flavor + "\\$packageNameToPath"
                count = JacocoUtils.copyClasses(it, classesDirKotlin, packageNameToPath, targetDir, count)
                println "$TAG kotlin=$kotlin,classesDirKotlin=$classesDirKotlin, count:$count"
            }
            println "$TAG copy class count=${count}"
            return rootDir
        } catch (Exception ignored) {
            println "$TAG copyBuildClassDirs failed:$ignored"
        }
        return ""
    }

    private def compressToZip(String classesDir, String zipFilePath) {
        try {
            FileUtil.deleteFile(zipFilePath)
            FileUtil.zipFolder(classesDir, zipFilePath)
            return true
        } catch (Exception e) {
            e.printStackTrace()
            println "$TAG compressToZip failed:$e"
        }
        return false
    }

    private def uploadDiffFiles() {
        def diffFilePath = "${project.buildDir}/outputs/diff/diffFiles.txt"
        File diffFile = new File(diffFilePath)
        if (diffFile != null && diffFile.exists() && diffFile.isFile() && diffFile.length() > 0) {
            //println "$TAG uploadDiffFiles ${diffFile.getAbsolutePath()}"
            syncUploadFiles(diffFile, TYPE_FILE_TXT)
        } else {
            //println "$TAG uploadDiffFiles failed:diffFile not exists}"
        }
    }

    private def uploadApk() {
        def apkDir = "${project.buildDir}/outputs/apk/debug/"
        File rootFile = new File(apkDir)
        File apkFile = null
        if (rootFile.exists() && rootFile.listFiles() != null) {
            for (File file : rootFile.listFiles()) {
                if (file.getName().endsWith(".apk")) {
                    apkFile = new File(apkDir + file.getName())
                    break
                }
            }
        }
        if (apkFile != null && apkFile.exists() && apkFile.isFile() && apkFile.length() > 0) {
            println "$TAG uploadApk ${apkFile.getAbsolutePath()}"
            syncUploadFiles(apkFile, TYPE_FILE_APK)
        } else {
            println "$TAG uploadApk failed:apk not exists}"
        }
    }

    /**
     * 文件上传，如果是本地server，要确保测试设备与server在同一个局域网
     * */
    private def syncUploadFiles(File file, String type) {
        try {
            String currentBranchName = JacocoUtils.getCurrentBranchName()
            String currentCommitId = JacocoUtils.getCurrentCommitId()
            //println("$TAG currentBranchName:$currentBranchName,currentCommitId=$currentCommitId")
            OkHttpClient client = buildHttpClient()
            RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
            String fileName = file.getName()
            // 处理上传的参数：用户名，uid，版本信息,分支信息等
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", fileName, fileBody)
                    .addFormDataPart("appName", JacocoUtils.COV_APP_NAME)
                    .addFormDataPart("branch", currentBranchName)
                    .addFormDataPart("commitId", currentCommitId)
                    .addFormDataPart("type", "$type")
                    .build()
            String url = "${JacocoUtils.JACOCO_HOST}/coverage/upload"
            println("$TAG syncUploadFiles,appName=${JacocoUtils.COV_APP_NAME},commitId=$currentCommitId,size=${file.length()},currentBranchName=$currentBranchName")
            println("$TAG upload url =$url")
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build()
            Call call = client.newCall(request)
            Response response = call.execute()
            handleResponse(response, file)
        } catch (Exception e) {
            println("$TAG,syncUploadFiles e=$e")
        }
    }

    private def handleResponse(Response response, File file) {
        ResponseBody responseBody = null
        try {
            responseBody = response.body()
            def str = responseBody.string()
            println("$TAG syncUploadFiles str =$str")
        } catch (Exception e) {
            println("$TAG syncUploadFiles error =$e")
        } finally {
            responseBody.close()
        }
    }

    private static OkHttpClient buildHttpClient() {
        return new OkHttpClient.Builder()
                .callTimeout(90, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(5 * 60L, TimeUnit.SECONDS)
                .build()
    }

}