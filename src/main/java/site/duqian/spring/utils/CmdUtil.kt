package site.duqian.spring.utils

import org.slf4j.LoggerFactory
import java.io.*
import java.util.*

/**
 * cmd工具类，测试
 */
object CmdUtil {
    private val Logger = LoggerFactory.getLogger(CmdUtil::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val os = System.getProperty("os.name").toLowerCase()
            println("********** $os")

            println("********** start")
            //runProcess("git log")
            val rootDir = FileUtil.getProjectDir()
            val jarPath = "${rootDir}jacococli.jar"
            val execPath = "${rootDir}download/cc-android/dev_dq_#411671_coverage/**.ec"
            val classesPath = "${rootDir}jacoco/classes/"
            val srcPath = "${rootDir}jacoco/tempSrc/main/java/"
            val reportPath = "${rootDir}jacoco/report"
            generateReportByCmd(jarPath, execPath, classesPath, srcPath, reportPath)
            //win两个命令都能执行，todo-dq mac不行？
            //runProcess("java -jar $jarPath report $execPath --classfiles $classesPath --sourcefiles $srcPath --html $reportPath")

            if (os.contains("win")) {
            } else {
                runProcess("pwd")
                runProcess("http-server -p 8095")
                runProcess("chmod -r 777 $jarPath")
                runProcess("cd $reportPath")
            }

            //val currentBranchName = "dev_dq_#411671_coverage"
            //val process:Process = execute("git diff origin/master origin/${currentBranchName} --name-only")
            //val process:Process = execute("git clone https://github.com/duqian291902259/AndroidUI.git")
            //val text = getText(process)
            println("********** end")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun generateReportByCmd(
        jarPath: String,
        execPath: String,
        classesPath: String,
        srcPath: String,
        reportPath: String
    ): Boolean {
        val cmds = arrayOf(
            "java",
            "-jar",
            jarPath,
            "report",
            execPath,
            "--classfiles",
            classesPath,
            "--sourcefiles",
            srcPath,
            "--html",
            reportPath,
            "--encoding=utf8"
        )
        return runProcess(cmds)
    }

    private fun runProcess(command: Array<String>): Boolean {
        try {
            val pro = Runtime.getRuntime().exec(command)
            val cmdString = command.contentToString()
            Logger.debug("cmdString=$cmdString")
            val inputStream = pro.inputStream
            printLines("$cmdString out:", inputStream)
            val errorStream = pro.errorStream
            val errorList = printLines("$cmdString err:", errorStream)
            pro.waitFor()
            val exitValue = pro.exitValue()
            println(command + " exitValue=$exitValue")
            inputStream.close()
            errorStream.close()
            if (exitValue > 0 || errorList.isNotEmpty()) {
                return false
            }
            return true
        } catch (e: Exception) {
            println("runProcess $e")
        }
        return false
    }

    @JvmStatic
    @Throws(Exception::class)
    private fun printLines(cmd: String, ins: InputStream): List<String> {
        val list = mutableListOf<String>()
        var line: String? = ""
        val `in` = BufferedReader(
            InputStreamReader(ins)
        )
        var shortCmd = cmd
        if (cmd.length > 10) {
            shortCmd = cmd.substring(0, 10)
        }
        while (`in`.readLine().also { line = it } != null) {
            val message = "$shortCmd -->printLines:>> $line"
            println(message)
            Logger.debug(message)
            if (line != "") {
                list.add(line!!)
            }
        }
        `in`.close()
        return list
    }

    @JvmStatic
    fun runProcess(command: String): Int {
        try {
            val pro = Runtime.getRuntime().exec(command)
            val inputStream = pro.inputStream
            printLines("$command out:", inputStream)
            val errorStream = pro.errorStream
            printLines("$command err:", errorStream)
            pro.waitFor()
            val exitValue = pro.exitValue()
            println("$command exitValue=$exitValue")
            inputStream.close()
            errorStream.close()
            return exitValue
        } catch (e: Exception) {
            println("runProcess $e")
        }
        return -1
    }

    //执行cmd命令，获取返回结果
    fun execCmd(command: String?): String {
        val sb = StringBuilder()
        try {
            val process = Runtime.getRuntime().exec(command)
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String
            while (bufferedReader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
        } catch (e: Exception) {
            return e.toString()
        }
        return sb.toString()
    }

    @Throws(IOException::class)
    @JvmStatic
    fun execute(self: String?): String {
        val exec = Runtime.getRuntime().exec(self)
        return getText(exec)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun getText(self: Process): String {
        val text: String = getText(BufferedReader(InputStreamReader(self.inputStream)))
        closeStreams(self)
        return text
    }

    @Throws(IOException::class)
    fun getText(reader: BufferedReader?): String {
        var bufferedReader = reader
        val answer = StringBuilder()
        val charBuffer = CharArray(8192)
        var nbCharRead: Int
        try {
            while (bufferedReader!!.read(charBuffer).also { nbCharRead = it } != -1) {
                answer.append(charBuffer, 0, nbCharRead)
            }
            val temp = bufferedReader
            bufferedReader = null
            temp.close()
        } finally {
            tryClose(bufferedReader, true) // ignore result
        }
        return answer.toString()
    }

    fun tryClose(closeable: AutoCloseable?, logWarning: Boolean): Throwable? {
        var thrown: Throwable? = null
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: Exception) {
                thrown = e
                if (logWarning) {
                    println("tryClose error $e")
                }
            }
        }
        return thrown
    }

    fun closeStreams(self: Process) {
        try {
            self.errorStream.close()
        } catch (ignore: IOException) {
        }
        try {
            self.inputStream.close()
        } catch (ignore: IOException) {
        }
        try {
            self.outputStream.close()
        } catch (ignore: IOException) {
        }
    }

    fun isWindowsOS(): Boolean {
        return System.getProperties().getProperty("os.name").toLowerCase().contains("windows")
    }

    @JvmStatic
    fun executeShellCmd(shell: String, msg: String): Boolean {
        println("执行shell命令:$shell")
        var cmds: Array<String>
        if (CommonUtils.isWindowsOS()) {
            cmds = arrayOf(getGitBashPath(), shell, msg)
        } else {
            cmds = arrayOf(shell, "", msg)
        }
        return runProcess(cmds)
    }

    //git-bash的路径，如果找不到，自行配置
    private var gitBashPath: String = ""
    private fun getGitBashPath(): String {
        if (!TextUtils.isEmpty(gitBashPath)) {
            return gitBashPath
        }
        try {
            val result = execute("where git")
            val paths = result.split("\n").toTypedArray()
            for (path in paths) {
                val file = File(path)
                val parentFile = file.parentFile
                if (parentFile != null) {
                    val gitBash = File(parentFile.parent + File.separator + "git-bash.exe")
                    if (gitBash.exists()) {
                        //给路径加上双引号，解决有空格的路径导致无法执行cmd的问题
                        gitBashPath = "\"" + gitBash.absolutePath + "\""
                    }
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return gitBashPath
    }
}