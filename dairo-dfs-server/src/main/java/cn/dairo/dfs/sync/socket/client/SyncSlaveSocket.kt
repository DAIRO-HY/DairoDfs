package cn.dairo.dfs.sync.socket.client

import cn.dairo.dfs.sync.socket.SyncDataType
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.*
import kotlin.concurrent.thread

/**
 * 应用启动执行
 */
@Order(Int.MAX_VALUE)//值越小越先执行
@Component
class SyncSlaveSocket : ApplicationRunner {

    /**
     * 心跳间隔时间
     */
    private val HEART_TIMER = 1000L

    /**
     * 同步Socket服务端端口
     */
    @Value("\${sync.port}")
    private var port: Int = 0

    /**
     * 输入流
     */
    private lateinit var iStream: InputStream

    /**
     * 输出流
     */
    private lateinit var oStream: OutputStream

    /**
     * Socket连接
     */
    private lateinit var socket: Socket

    /**
     * 初始化数据
     * 主要是实例化里面的静态参数
     */
    override fun run(args: ApplicationArguments?) {
        mInstant = this
        thread {
            //this.openSocket()
        }
    }

    private fun openSocket() {
        val host = "localhost"
        this.isReatsrting = false
        try {
            this.socket = Socket(host, this.port)
            this.iStream = this.socket.inputStream
            this.oStream = this.socket.outputStream
            this.sendKey()
            thread {

                //发送心跳数据
                this.sendHeartData()
            }
            this.receiverData()
        } catch (e: IOException) {
            this.restart()
        }
    }

    private fun sendKey() =
        try {
            this.oStream.write(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8))
        } catch (e: IOException) {
            this.restart()
        }


    /**
     * 接收消息
     */
    private fun receiverData() {
        while (true) {
            val data = this.iStream.read()
            when (data) {
                SyncDataType.HEART_DATA -> SyncSlave.instant.doSync()
            }
        }
    }

    /**
     * 发送心跳数据
     */
    private fun sendHeartData() = try {
        while (true) {//指定间隔时间发送心跳数据
            Thread.sleep(HEART_TIMER)
            this.oStream.write(SyncDataType.HEART_DATA)
        }
    } catch (e: IOException) {
        this.restart()
    }

    private var isReatsrting = false

    /**
     * 重启客户端连接
     */
    private fun restart() {
        synchronized(this) {
            if (this.isReatsrting) {
                return
            }
            this.isReatsrting = true
            Thread.sleep(3000)
            thread {
                this.openSocket()
            }
        }
    }

    companion object {
        private lateinit var mInstant: SyncSlaveSocket

        /**
         * 全局实例
         */
        val instant get() = mInstant
    }
}
