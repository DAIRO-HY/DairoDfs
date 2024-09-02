package cn.dairo.dfs.sync.socket.server

import cn.dairo.dfs.sync.socket.SyncDataType
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread


@Order(Int.MAX_VALUE - 1)//值越小越先执行
@Component
class SyncMasterSocket : ApplicationRunner {

    /**
     * hold住的客户端Socket
     */
    private val clientList = ArrayList<SyncMasterSocketMessage>()

    /**
     * 同步Socket服务端端口
     */
    @Value("\${sync.port}")
    private var port: Int = 0

    /**
     * 启动执行
     */
    override fun run(args: ApplicationArguments) {
        mInstance = this
//        thread {
//            this.createSocketServer()
//        }
    }

    /**
     * 创建Socket服务端
     */
    private fun createSocketServer() {
        val socketServer = ServerSocket(this.port)
        while (true) {
            val socket = socketServer.accept()
            thread {
                this.holdSocket(socket)
            }
        }
    }

    /**
     * 将连接保持住
     */
    private fun holdSocket(socket: Socket) {
        val key = String(socket.inputStream.readNBytes(8))
        synchronized(this.clientList) {
            val smsm = this.clientList.find { it.key == key }
            this.clientList.add(SyncMasterSocketMessage(key, socket))
            if (smsm != null) {
                smsm.close()
            }
        }

    }

    /**
     * 通知所有分机同步数据
     */
    private fun syncNotify() {
        this.clientList.forEach {
            it.sendData(SyncDataType.SYNC_DATA)
        }
    }

    /**
     * 移除一个连接对象
     */
    fun remove(smsm: SyncMasterSocketMessage) {
        synchronized(this.clientList) {
            this.clientList.remove(smsm)
        }
    }

    companion object {
        private lateinit var mInstance: SyncMasterSocket

        /**
         * 通知分机更新数据
         */
        fun notifySlave() {
            mInstance.syncNotify()
        }

        val instance get() = mInstance
    }
}