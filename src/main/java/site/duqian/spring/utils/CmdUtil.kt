package site.duqian.spring.utils

import kotlin.jvm.JvmStatic
import kotlin.Throws
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception

object CmdUtil {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val os = System.getProperty("os.name").toLowerCase()
            println("********** $os")

            if (os.contains("win")) {
            } else {
                runProcess("pwd")
                runProcess("http-server -p 8095")
            }
            println("********** start")
            //runProcess("git log")
            val rootDir = System.getProperty("user.dir") + File.separator
            val jarPath = "${rootDir}jacococli.jar"
            val execPath = "${rootDir}download/cc-android/dev_dq_#411671_coverage/8ab3adfcec889990a6db1fbaae361d59.ec"
            val classesPath = "${rootDir}jacoco/classes/"
            val srcPath = "${rootDir}jacoco/tempSrc/main/java/"
            val reportPath = "${rootDir}jacoco/report"
            runProcess("chmod -r 777 $jarPath")
            runProcess("cd $reportPath")

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
                reportPath
            )
            runProcess(cmds)
            //win两个命令都能执行，todo-dq mac不行
            //runProcess("java -jar $jarPath report $execPath --classfiles $classesPath --sourcefiles $srcPath --html $reportPath")
            println("********** end")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun runProcess(command: Array<String>): Boolean {
        try {
            val pro = Runtime.getRuntime().exec(command)
            val inputStream = pro.inputStream
            printLines("$command out:", inputStream)
            val errorStream = pro.errorStream
            printLines("$command err:", errorStream)
            pro.waitFor()
            println(command + " exitValue=${pro.exitValue()}")
            inputStream.close()
            errorStream.close()
            return true
        } catch (e: Exception) {
            println("runProcess $e")
        }
        return false
    }

    @JvmStatic
    @Throws(Exception::class)
    private fun printLines(cmd: String, ins: InputStream) {
        var line: String? = ""
        val `in` = BufferedReader(
            InputStreamReader(ins)
        )
        while (`in`.readLine().also { line = it } != null) {
            println("$cmd $line")
        }
        `in`.close()
    }

    @JvmStatic
    fun runProcess(command: String): Boolean {
        try {
            val pro = Runtime.getRuntime().exec(command)
            val inputStream = pro.inputStream
            printLines("$command out:", inputStream)
            val errorStream = pro.errorStream
            printLines("$command err:", errorStream)
            pro.waitFor()
            println(command + " exitValue=${pro.exitValue()}")
            inputStream.close()
            errorStream.close()
            return true
        } catch (e: Exception) {
            println("runProcess $e")
        }
        return false
    }
}