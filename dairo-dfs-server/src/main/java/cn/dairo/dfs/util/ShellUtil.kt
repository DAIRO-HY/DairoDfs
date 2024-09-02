package cn.dairo.dfs.util

import java.io.InputStream
import kotlin.concurrent.thread

/**
 * 命令行工具
 * 可以传递输入输出流，只是在window平台和Linux平台使用转移字符时，可能会导致指令执行失败，如遇到这类问题，请先尝试解决指令代码的错误
 * 因为有可能通过控制台输入的指令和java执行的指令有一些差异，同一个指令，有可能直接通过控制执行没问题，但是通过java程序执行就有问题
 * 还有一种可能，同一个指令，通过java执行，在window环境没有问题，但是在linux环境就有问题
 *
 * 如：ffmpeg -i - -vf "select=eq(n\,0)" -q:v 1 -f image2pipe -vcodec mjpeg -
 * 上面这个指令在windows平台没有问题，但是在linux平台就报错
 * 若在Linux平台，改成ffmpeg -i - -vf select=eq(n\,0) -q:v 1 -f image2pipe -vcodec mjpeg -就可以解决这个问题
 */
object ShellUtil {

    /**
     * 将结果输出到输入流
     * @param cmd 指令
     * @param useErrorStream 使用错误输出流，有些指令的输出结果时存在errorStream中的
     */
    fun execToByteArray(
        cmd: String,
        useErrorStream: Boolean = false
    ): ByteArray {
        var data = ByteArray(0)
        this.execToInputStream(cmd, useErrorStream) {
            data = it.readAllBytes()
        }
        return data
    }

    /**
     * 将结果输出到输入流
     * @param cmd 指令
     * @param useErrorStream 使用错误输出流，有些指令的输出结果时存在errorStream中的
     * @param success 输入流回调函数
     */
    fun execToInputStream(
        cmd: String,
        useErrorStream: Boolean = false,
        success: (iStream: InputStream) -> Unit
    ) {
        val pb = ProcessBuilder(cmd.cmdList)
        if (useErrorStream) {
            pb.redirectErrorStream(true) // 将错误流重定向到标准流
        }
        val process = pb.start()
        try {
            val inputStreamThread = thread {//异步线程读取流，防止死锁
                process.inputStream.use {
                    success(it)
                }
            }
            var error = ""
            val errorStreamThread = thread {//异步线程读取错误流，防止死锁
                process.errorStream.use {
                    error = String(it.readAllBytes())
                }
            }
            val exitCode = process.waitFor()

            //默认超时10分钟
            inputStreamThread.join(10 * 60 * 1000)
            errorStreamThread.join(10 * 60 * 1000)
            if (exitCode != 0) {
                throw RuntimeException(error)
            }
        } finally {
            process.destroyForcibly()
        }
    }

    /**
     * 将输入流传入Shell并执行CMD
     * @param cmd 指令
     * @param data 字节数组
     * @param useErrorStream 使用错误输出流，有些指令的输出结果时存在errorStream中的
     */
    fun execByData(
        cmd: String, data: ByteArray, useErrorStream: Boolean = false
    ) {
        this.execByInputStream(cmd, data.inputStream(), useErrorStream)
    }

    /**
     * 将输入流传入Shell并执行CMD
     * @param cmd 指令
     * @param iStream 输入流
     * @param useErrorStream 使用错误输出流，有些指令的输出结果时存在errorStream中的
     */
    fun execByInputStream(
        cmd: String, iStream: InputStream, useErrorStream: Boolean = false
    ) {
        val pb = ProcessBuilder(cmd.cmdList)
        if (useErrorStream) {
            pb.redirectErrorStream(true) // 将错误流重定向到标准流
        }
        val process = pb.start()
        try {
            thread {
                process.outputStream.use {
                    iStream.transferTo(it)
                }
            }
            var result = ""
            val inputStreamThread = thread {
                process.inputStream.use {
                    result = String(it.readAllBytes())
                }
            }
            var error = ""
            val errorStreamThread = thread {//读取错误流，防止死锁
                process.errorStream.use {
                    error = String(it.readAllBytes())
                }
            }
            val exitCode = process.waitFor()

            //默认超时10分钟
            inputStreamThread.join(10 * 60 * 1000)
            errorStreamThread.join(10 * 60 * 1000)
            if (exitCode != 0) {
                if (useErrorStream) {
                    throw RuntimeException(result)
                }
                throw RuntimeException(error)
            }
        } finally {
            process.destroyForcibly()
        }
    }

    /**
     * 执行cmd指令并传入一个输入流，并且返回一个执行结果的字节数组
     * @param cmd 指令
     * @param inData 输入数据
     * @param useErrorStream 使用错误输出流，有些指令的输出结果时存在errorStream中的
     */
    fun execByByteArrayToByteArray(
        cmd: String, inData: ByteArray, useErrorStream: Boolean = false
    ): ByteArray {
        var outData = ByteArray(0)
        this.execByInputStreamToInputStream(cmd, inData.inputStream(), useErrorStream) {
            outData = it.readAllBytes()
        }
        return outData
    }

    /**
     * 执行cmd指令并传入一个输入流，并且返回一个执行结果的字节数组
     * @param cmd 指令
     * @param iStream 输入流
     * @param useErrorStream 使用错误输出流，有些指令的输出结果时存在errorStream中的
     */
    fun execByInputStreamToByteArray(
        cmd: String, iStream: InputStream, useErrorStream: Boolean = false
    ): ByteArray {
        var data = ByteArray(0)
        this.execByInputStreamToInputStream(cmd, iStream, useErrorStream) {
            data = it.readAllBytes()
        }
        return data
    }

    /**
     * 执行cmd指令并传入一个输入流，并且返回一个执行结果的输入流
     * @param cmd 指令
     * @param iStream 输入流
     * @param useErrorStream 使用错误输出流，有些指令的输出结果时存在errorStream中的
     * @param success 输入流回调函数
     */
    fun execByInputStreamToInputStream(
        cmd: String, iStream: InputStream, useErrorStream: Boolean = false, success: (iStream: InputStream) -> Unit
    ) {
        val pb = ProcessBuilder(cmd.cmdList)
        if (useErrorStream) {
            pb.redirectErrorStream(true) // 将错误流重定向到标准流
        }
        val process = pb.start()
        try {
            thread {
                process.outputStream.use {
                    iStream.transferTo(it)
                }
            }
            val inputStreamThread = thread {//异步线程读取流，防止死锁
                process.inputStream.use {
                    success(it)
                }
            }
            var error = ""
            val errorStreamThread = thread {//读取错误流，防止死锁
                process.errorStream.use {
                    error = String(it.readAllBytes())
                }
            }
            val exitCode = process.waitFor()

            //默认超时10分钟
            inputStreamThread.join(10 * 60 * 1000)
            errorStreamThread.join(10 * 60 * 1000)
            if (exitCode != 0) {
                throw RuntimeException(error)
            }
        } finally {
            process.destroyForcibly()
        }
    }

    /**
     * 执行Shell
     * @param cmd 指令
     */
    fun exec(cmd: String, useErrorStream: Boolean = false): String {
        val pb = ProcessBuilder(cmd.cmdList)
        if (useErrorStream) {
            pb.redirectErrorStream(true) // 将错误流重定向到标准流
        }
        val process = pb.start()
        try {
            var result = ""
            val inputStreamThread = thread {//异步线程读取流，防止死锁
                process.inputStream.use {
                    result = String(it.readAllBytes())
                }
            }
            var error = ""
            val errorStreamThread = thread {//读取错误流，防止死锁
                process.errorStream.use {
                    error = String(it.readAllBytes())
                }
            }
            val exitCode = process.waitFor()

            //默认超时10分钟
            inputStreamThread.join(10 * 60 * 1000)
            errorStreamThread.join(10 * 60 * 1000)
            if (exitCode != 0) {
                throw RuntimeException(error)
            }
            return result
        } finally {
            process.destroyForcibly()
        }
    }

    /**
     * 将指令字符串转换成指令List
     */
    private val String.cmdList: List<String>
        get() {
            val cmdList = ArrayList<String>()
            var cmdTemp = this + " "
            while (cmdTemp.isNotEmpty()) {
                val nextIndex: Int
                if (cmdTemp.startsWith("\"")) {//如果指令有使用双引号
                    nextIndex = cmdTemp.indexOf("\" ")
                    cmdList.add(cmdTemp.substring(1, nextIndex))
                    cmdTemp = cmdTemp.substring(nextIndex + 2)
                } else {
                    nextIndex = cmdTemp.indexOf(" ")
                    cmdList.add(cmdTemp.substring(0, nextIndex))
                    cmdTemp = cmdTemp.substring(nextIndex + 1)
                }
            }
            return cmdList.filter { it.isNotEmpty() }
        }
}
