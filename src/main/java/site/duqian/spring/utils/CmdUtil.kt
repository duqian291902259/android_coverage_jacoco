package site.duqian.spring.utils

import kotlin.jvm.JvmStatic
import kotlin.Throws
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception

object CmdUtil {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            runProcess("pwd")
            runProcess("pwd")
            println("********** start")
            //runProcess("cd jacoco")
            //runProcess("git log")
            val rootDir = "/"//System.getProperty("user.dir")
            runProcess("chmod -r 777 ${rootDir}jacoco/jacococli.jar")
            runProcess("java -jar ${rootDir}jacoco/jacococli.jar report  ${rootDir}download/cc-android/3.8.1/coverage.exec --classfiles classes --sourcefiles  ${rootDir}jacoco/git/app/src/main/java/ --html  ${rootDir}src/main/resources/web/temp/cc")
            println("********** end")

        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            println(command + " exitValue() " + pro.exitValue())
            inputStream.close()
            errorStream.close()
            return true
        } catch (e: Exception) {
            println("runProcess $e")
        }
        return false
    }
}