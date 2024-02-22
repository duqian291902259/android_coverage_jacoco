/*
package com.duqian.coverage

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.duqian.coverage.classutil.ClassCopier
import groovy.io.FileType
import org.codehaus.groovy.runtime.IOGroovyMethods
import org.gradle.api.Project
import org.gradle.internal.FileUtils

*/
/**
 * Description:处理覆盖率相关的class操作，后续在此做增量探针的逻辑
 * @author n20241 Created by 杜小菜 on 2021/9/10 - 18:28 .
 * E-mail: duqian2010@gmail.com
 *//*

class JacocoTransform extends Transform {
    private static final String TAG = "dq-jacocoTransform"
    private Project project
    private JacocoReportExtension jacocoExtension

    JacocoTransform(Project project, JacocoReportExtension jacocoExtension) {
        this.project = project
        this.jacocoExtension = jacocoExtension
    }

    @Override
    String getName() {
        return JacocoTransform.class.getSimpleName()
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        println(TAG + ":dq-coverage transform")

        def dirInputs = new HashSet<>()
        def jarInputs = new HashSet<>()

        if (!transformInvocation.isIncremental()) {
            transformInvocation.getOutputProvider().deleteAll()
        }

        transformInvocation.inputs.each { input ->
            input.directoryInputs.each { dirInput ->
                dirInputs.add(dirInput)
            }
            input.jarInputs.each { jarInput ->
                jarInputs.add(jarInput)
            }
        }

        if (!dirInputs.isEmpty() || !jarInputs.isEmpty()) {
            if (jacocoExtension.isJacocoEnable) {
                println("${TAG} isJacocoEnable")
                //copy class到 dq-start/classes
                copy(transformInvocation, dirInputs, jarInputs, jacocoExtension.srcIncludes)
                //提交classes 到git
                gitPush(jacocoExtension.gitPushShell, "auto commit diff-classes for coverage")
                //todo-dq 获取差异方法集
                //BranchDiffClassTask branchDiffTask = project.tasks.findByName(JacocoAndroidPlugin.TASK_JACOCO_BRANCH_DIFF_CLASS)
                //branchDiffTask.getDiffClass()
            }
            //有diff的地方插入探针
            inject(transformInvocation, dirInputs, jarInputs, jacocoExtension.srcIncludes)
        }
    }

    private def copy(TransformInvocation transformInvocation, def dirInputs, def jarInputs, List<String> includes) {
        def classDir = "${project.projectDir}/classes"
        ClassCopier copier = new ClassCopier(classDir, includes)
        if (!transformInvocation.incremental) {
            FileUtils.deletePath(new File(classDir))
        }
        if (!dirInputs.isEmpty()) {
            dirInputs.each { dirInput ->
                if (transformInvocation.incremental) {
                    dirInput.changedFiles.each { entry ->
                        File fileInput = entry.getKey()
                        File fileOutputJacoco = new File(fileInput.getAbsolutePath().replace(dirInput.file.getAbsolutePath(), classDir))
                        Status fileStatus = entry.getValue()

                        switch (fileStatus) {
                            case Status.ADDED:
                            case Status.CHANGED:
                                if (fileInput.isDirectory()) {
                                    return // continue.
                                }
                                copier.doClass(fileInput, fileOutputJacoco)
                                break
                            case Status.REMOVED:
                                if (fileOutputJacoco.exists()) {
                                    if (fileOutputJacoco.isDirectory()) {
                                        fileOutputJacoco.deleteDir()
                                    } else {
                                        fileOutputJacoco.delete()
                                    }
                                    println("${TAG},REMOVED output file Name:${fileOutputJacoco.name}")
                                }
                                break
                        }
                    }
                } else {
                    dirInput.file.traverse(type: FileType.FILES) { fileInput ->
                        File fileOutputJacoco = new File(fileInput.getAbsolutePath().replace(dirInput.file.getAbsolutePath(), classDir))
                        copier.doClass(fileInput, fileOutputJacoco)
                    }
                }
            }
        }

        */
/*if (!jarInputs.isEmpty()) {
            jarInputs.each { jarInput ->
                File jarInputFile = jarInput.file
                copier.doJar(jarInputFile, null)
            }
        }*//*

    }


    def inject(TransformInvocation transformInvocation, def dirInputs, def jarInputs, List<String> includes) {
        //ClassInjector injector = new ClassInjector(includes)

        if (!dirInputs.isEmpty()) {
            dirInputs.each { dirInput ->
                File dirOutput = transformInvocation.outputProvider.getContentLocation(dirInput.getName(),
                        dirInput.getContentTypes(), dirInput.getScopes(),
                        Format.DIRECTORY)
                FileUtils.mkdirs(dirOutput)

                if (transformInvocation.incremental) {
                    dirInput.changedFiles.each { entry ->
                        File fileInput = entry.getKey()
                        File fileOutputTransForm = new File(fileInput.getAbsolutePath().replace(
                                dirInput.file.getAbsolutePath(), dirOutput.getAbsolutePath()))
                        FileUtils.mkdirs(fileOutputTransForm.parentFile)
                        Status fileStatus = entry.getValue()
                        switch (fileStatus) {
                            case Status.ADDED:
                            case Status.CHANGED:
                                if (fileInput.isDirectory()) {
                                    return // continue.
                                }
                                */
/*if (jacocoExtension.jacocoEnable &&
                                        DiffAnalyzer.getInstance().containsClass(getClassName(fileInput))) {
                                    injector.doClass(fileInput, fileOutputTransForm)
                                } else {*//*

                                FileUtils.copyFile(fileInput, fileOutputTransForm)
                                //}
                                break
                            case Status.REMOVED:
                                if (fileOutputTransForm.exists()) {
                                    if (fileOutputTransForm.isDirectory()) {
                                        fileOutputTransForm.deleteDir()
                                    } else {
                                        fileOutputTransForm.delete()
                                    }
                                    println("REMOVED output file Name:${fileOutputTransForm.name}")
                                }
                                break
                        }
                    }
                } else {
                    dirInput.file.traverse(type: FileType.FILES) { fileInput ->
                        File fileOutputTransForm = new File(fileInput.getAbsolutePath().replace(dirInput.file.getAbsolutePath(), dirOutput.getAbsolutePath()))
                        FileUtils.mkdirs(fileOutputTransForm.parentFile)
                        */
/*if (jacocoExtension.jacocoEnable &&
                                DiffAnalyzer.getInstance().containsClass(getClassName(fileInput))) {
                            injector.doClass(fileInput, fileOutputTransForm)
                        } else {*//*

                        FileUtils.copyFile(fileInput, fileOutputTransForm)
                        //}
                    }
                }
            }
        }

        if (!jarInputs.isEmpty()) {
            jarInputs.each { jarInput ->
                File jarInputFile = jarInput.file
                File jarOutputFile = transformInvocation.outputProvider.getContentLocation(
                        jarInputFile.getName(), getOutputTypes(), getScopes(), Format.JAR
                )

                FileUtils.mkdirs(jarOutputFile.parentFile)

                switch (jarInput.status) {
                    case Status.NOTCHANGED:
                        if (transformInvocation.incremental) {
                            break
                        }
                        break
                    case Status.ADDED:
                    case Status.CHANGED:
                        */
/* if (jacocoExtension.jacocoEnable) {
                             injector.doJar(jarInputFile, jarOutputFile)
                         } else {*//*

                        FileUtils.copyFile(jarInputFile, jarOutputFile)
                        //}
                        break
                    case Status.REMOVED:
                        if (jarOutputFile.exists()) {
                            jarOutputFile.delete()
                        }
                        break
                }
            }
        }
    }

    private def gitPush(String shell, String commitMsg) {
        println("${TAG} jacoco 执行git命令 local dq")
        String[] cmds
        if (CommonUtils.isWindowsOS()) {
            cmds = new String[3]
            cmds[0] = jacocoExtension.getGitBashPath()
            cmds[1] = shell
            cmds[2] = commitMsg
        } else {
            cmds = new String[2]
            cmds[0] = shell
            cmds[1] = commitMsg
        }
        println("${TAG} cmds=" + cmds)
        Process pces = Runtime.getRuntime().exec(cmds)
        String result = IOGroovyMethods.getText(new BufferedReader(new InputStreamReader(pces.getIn())))
        String error = IOGroovyMethods.getText(new BufferedReader(new InputStreamReader(pces.getErr())))

        println("${TAG} jacoco git result :$result,error=$error")
        pces.closeStreams()
    }
}*/
