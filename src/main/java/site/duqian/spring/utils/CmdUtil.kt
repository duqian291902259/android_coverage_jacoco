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
            println("**********")
            runProcess("cd jacoco")
            runProcess("ls")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun printLines(cmd: String, ins: InputStream) {
        var line: String? = null
        val `in` = BufferedReader(
            InputStreamReader(ins)
        )
        while (`in`.readLine().also { line = it } != null) {
            println("$cmd $line")
        }
    }

    @Throws(Exception::class)
    fun runProcess(command: String) {
        val pro = Runtime.getRuntime().exec(command)
        printLines("$command out:", pro.inputStream)
        printLines("$command err:", pro.errorStream)
        pro.waitFor()
        println(command + " exitValue() " + pro.exitValue())
    }
}