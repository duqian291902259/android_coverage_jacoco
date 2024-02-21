package com.duqian.coverage


import org.codehaus.groovy.runtime.IOGroovyMethods
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Description:获取当前分支与基准分支（gradle中可以配置，默认master）的差异class，并保存
 * @author n20241 Created by 杜小菜 on 2021/9/16 - 17:52 .
 * E-mail: duqian2010@gmail.com
 */
class BranchDiffClassTask extends DefaultTask {
    private final static String TAG = "dq-jacoco-BranchDiffClassTask"
    private final static String TEMP_DIR = "app"
    def currentName//当前分支名
    private JacocoReportExtension extension

    void setExtension(JacocoReportExtension extension) {
        this.extension = extension
    }

    @TaskAction
    def getDiffClass() {
        //获取差异class
        println "pullDiffClasses start"
        getDiffSrcAndClasses()
        println "pullDiffClasses end"
    }

    private def getDiffSrcAndClasses() {
        currentName = JacocoUtils.getCurrentBranchName()
        println "$TAG currentName:" + currentName + ",baseBranch=$extension.branchName"
        //获得两个分支的差异文件
        def diff = "git diff origin/${extension.branchName} origin/${currentName} --name-only".execute().text
        //println "$TAG diff:$diff"

        //List<String> diffFiles = getDiffClassFiles(diff)
        List<String> diffFiles = getDiffSourceFiles(diff)

        String path = writerDiffToFile(diffFiles, true)

        File diffFile = new File(path)
        println("$TAG,diffFiles size=" + diffFiles.size() + ",path=" + path)

        FileUtil.copyFile(diffFile, new File(JacocoUtils.getUploadRootDir(project, extension) + File.separator + diffFile.getName()))

        //copy all classes

        //两个分支差异文件的目录
        //handleDiffClasses(currentName, diffFiles)

        //copy class到 app/classes
        //copyDiffClass(diffFiles)
    }

    private void copyDiffClass(List<String> diffFiles) {
        //todo-dq 当前分支差异的class需要全部拷贝
    }

    private void handleDiffClasses(String currentName, List<String> diffFiles) {
        def currentDir = "${project.rootDir.parentFile}/temp/${currentName}/${TEMP_DIR}"
        def branchDir = "${project.rootDir.parentFile}/temp/${extension.branchName}/${TEMP_DIR}"
        //println("$TAG currentDir=$currentDir")
        project.delete(currentDir)
        project.delete(branchDir)
        new File(currentDir).mkdirs()
        new File(branchDir).mkdirs()

        //先把两个分支的所有class copy到temp目录
        copyBranchClass(currentName, currentDir)
        copyBranchClass(extension.branchName, branchDir)
        //再根据diffFiles 删除不需要的class
        deleteOtherFile(diffFiles, branchDir)
        deleteOtherFile(diffFiles, currentDir)

        //删除空文件夹
        deleteEmptyDir(new File(branchDir))
        deleteEmptyDir(new File(currentDir))
        //todo-dq createDiffMethod(currentDir, branchDir) writerDiffMethodToFile()
    }

    def gitPush(String shell, String commitMsg) {
        println("jacoco 执行git命令 local dq")
//
        String[] cmds
        if (Utils.windows) {
            cmds = new String[3]
            cmds[0] = extension.getGitBashPath()
            cmds[1] = shell
            cmds[2] = commitMsg
        } else {
            cmds = new String[2]
            cmds[0] = shell
            cmds[1] = commitMsg
        }
        println("cmds=" + cmds)
        Process pces = Runtime.getRuntime().exec(cmds)
        String result = IOGroovyMethods.getText(new BufferedReader(new InputStreamReader(pces.getIn())))
        String error = IOGroovyMethods.getText(new BufferedReader(new InputStreamReader(pces.getErr())))

        println("jacoco git succ :" + result)
        println("jacoco git error :" + error)

        pces.closeStreams()
    }


    private String writerDiffToFile(List<String> diffFiles, boolean isSource) {
        String path = "${project.buildDir.getAbsolutePath()}/outputs/diff/diffFiles.txt"
        if (!isSource) {
            path = "${project.buildDir.getAbsolutePath()}/outputs/diff/diffClassFiles.txt"
        }
        File parent = new File(path).getParentFile();
        if (!parent.exists()) parent.mkdirs()

        //println("$TAG,writerDiffToFile size=" + diffFiles.size() + " to >" + path)

        FileOutputStream fos = new FileOutputStream(path)
        for (String str : diffFiles) {
            fos.write((str + "\n").getBytes())
        }
        fos.close()
        return path
    }

    private def deleteOtherFile(List<String> diffFiles, String dir) {
        readFiles(dir, {
            String path = ((File) it).getAbsolutePath().replace(dir, TEMP_DIR)
            //path/to/xxx.class
            return diffFiles.contains(path)
        })
    }

    private void readFiles(String dirPath, Closure closure) {
        File file = new File(dirPath);
        if (!file.exists()) {
            return
        }
        File[] files = file.listFiles();
        for (File classFile : files) {
            if (classFile.isDirectory()) {
                readFiles(classFile.getAbsolutePath(), closure);
            } else {
                if (classFile.getName().endsWith(".class")) {
                    if (!closure.call(classFile)) {
                        classFile.delete()
                    }
                } else {
                    classFile.delete()
                }
            }
        }
    }

    private void copyBranchClass(String currentName, GString currentDir) {
        String[] cmds
        if (CommonUtils.isWindowsOS()) {
            cmds = new String[5]
            cmds[0] = extension.getGitBashPath()
            cmds[1] = extension.pullDiffClassShell
            cmds[2] = currentName
            cmds[3] = project.rootDir.getAbsolutePath()
            cmds[4] = currentDir.toString()
        } else {
            cmds = new String[4]
            cmds[0] = extension.pullDiffClassShell
            cmds[1] = currentName
            cmds[2] = project.rootDir.getAbsolutePath()
            cmds[3] = currentDir.toString()
        }

        println("$TAG,cmds=" + cmds)
        Process pces = Runtime.getRuntime().exec(cmds)
        String result = IOGroovyMethods.getText(new BufferedReader(new InputStreamReader(pces.getIn())))
        String error = IOGroovyMethods.getText(new BufferedReader(new InputStreamReader(pces.getErr())))
        //String text = pces.getText()

        println("$TAG,copyClassShell succ :$result")
        println("$TAG,copyClassShell error :$error,text=")

        pces.closeStreams()
    }

    private List<String> getDiffSourceFiles(String diff) {
        List<String> diffFiles = new ArrayList<>()
        if (diff == null || diff == '') {
            return diffFiles
        }
        String[] strings = diff.split("\n")
        strings.each {
            if (it.endsWith('.java') || it.endsWith(".kt")) {
                if (isInclude(it)) {
                    diffFiles.add(it)
                    //println("$TAG,getDiffFiles include $it")
                }
            }
        }
        return diffFiles
    }

    private List<String> getDiffClassFiles(String diff) {
        List<String> diffFiles = new ArrayList<>()
        if (diff == null || diff == '') {
            return diffFiles
        }
        String[] strings = diff.split("\n")
        def classes = "/classes/"
        strings.each {
            if (it.endsWith('.class')) {
                String classPath = it.substring(it.indexOf(classes) + classes.length())
                if (isInclude(classPath)) {
                    def shouldAdd = true
                    /*if (jacocoExtension.excludeClass != null) {
                        boolean exclude = jacocoExtension.excludeClass.call(it)
                        shouldAdd = !exclude
                    }*/
                    if (shouldAdd) {
                        diffFiles.add(it)
                        //println("$TAG,getDiffFiles include $it")
                    }
                }
            }
        }
        return diffFiles
    }

    def isInclude(String filePath) {
        List<String> includes = extension.includes
        for (String str : includes) {
            //if (filePath.startsWith(str.replaceAll("\\.", "/"))) {
            if (filePath.contains(str.replaceAll("\\.", "/"))) {
                return true
            }
        }
        return false
    }

    private boolean deleteEmptyDir(File dir) {
        //println("$TAG delete dir=$dir")
        if (dir.isDirectory()) {
            boolean flag = true
            for (File f : dir.listFiles()) {
                if (deleteEmptyDir(f))
                    f.delete()
                else
                    flag = false
            }
            return flag
        }
        return false
    }
}