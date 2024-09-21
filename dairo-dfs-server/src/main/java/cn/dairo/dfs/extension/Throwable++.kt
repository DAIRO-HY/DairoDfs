package cn.dairo.dfs.extension

import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * 输出错误的调用栈信息
 */
val Throwable.stackMessage: String
    get() {
        val oStream = ByteArrayOutputStream()
        val ps = PrintStream(oStream)
        this.printStackTrace(ps)
        return String(oStream.toByteArray())
    }

