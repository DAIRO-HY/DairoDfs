package cn.dairo.dfs.sync.socket.server

import cn.dairo.dfs.sync.socket.SyncDataType
import java.io.IOException
import java.net.Socket
import kotlin.concurrent.thread

class SyncMasterSocketMessage(val key: String, private val socket: Socket) {

    /**
     * 记录最后一次在线时间
     */
    var lastOnlineTime = System.currentTimeMillis()

    /**
     * 输入流
     */
    private val iStream = this.socket.inputStream

    /**
     * 输出流
     */
    private val oStream = this.socket.outputStream

    init {
        thread {
            this.receiverData()
        }
    }

    /**
     * 接收数据
     */
    private fun receiverData() = try {
        while (true) {

            //每次只读一个数据
            val data = this.iStream.read()
            this.handleData(data)
        }
    } catch (e: IOException) {
        SyncMasterSocket.instance.remove(this)
    }

    /**
     * 发送数据
     */
    fun sendData(data: Int) {
        try {
            this.oStream.write(data)
        } catch (e: IOException) {
            SyncMasterSocket.instance.remove(this)
        }
    }

    /**
     * 关闭Socket连接
     */
    fun close() {
        this.socket.close()
    }

    /**
     * 处理接收到的数据
     * @param data 接收到的数据
     */
    private fun handleData(data: Int) {
        println("收到客户端数据:$data")
        when (data) {
            SyncDataType.HEART_DATA -> this.lastOnlineTime = System.currentTimeMillis()
        }
    }
}