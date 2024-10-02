package cn.dairo.dfs.sync

import cn.dairo.dfs.boot.Boot
import cn.dairo.dfs.controller.app.sync.SyncWebSocketHandler
import cn.dairo.dfs.extension.bean
import cn.dairo.dfs.extension.toDataSize
import cn.dairo.dfs.sync.bean.SyncServerInfo
import org.springframework.http.HttpStatus
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 文件同步工具
 */
object SyncFileUtil {

    /**
     * 实时同步消息的Socket
     */
    private val socket = SyncWebSocketHandler::class.bean

    /**
     * 数据存储目录
     */
    private val dataPath = Boot::class.bean.dataPath

    /**
     * 开始同步
     * @param info 同步主机信息
     * @param md5 文件md5
     * @param retryTimes 记录出错重试次数
     * @return 存储目录
     */
    fun download(info: SyncServerInfo, md5: String, retryTimes: Int = 0): String {

        //得到系统临时目录@TODO:linux环境不是以/结尾
//        val tmpPath = System.getProperty("java.io.tmpdir")

        val tmpPath = this.dataPath + "/temp/"

        //得到文件存储目录
        val savePath = tmpPath + md5
        val saveFile = File(savePath)

        //断点下载开始位置
        var downloadStart = 0L
        if (saveFile.exists()) {//若文件已经存在
            downloadStart = saveFile.length()
        }
        val httpUrl = URL(info.url + "/download/$md5")
        val conn = httpUrl.openConnection() as HttpURLConnection
        try {

            //已经下载文件大小
            var downloadedSize = downloadStart
            conn.setRequestProperty("Range", "bytes=${downloadStart}-")
            conn.requestMethod = "GET"

            //连接超时
            conn.connectTimeout = 30000

            //读数据超时
            conn.readTimeout = 30000
            conn.connect()

            //返回状态码
            val httpStatus = conn.responseCode
            if (httpStatus == HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value()) {//文件可能已经下载完成
                return savePath
            }
            if (httpStatus != HttpStatus.OK.value() && httpStatus != HttpStatus.PARTIAL_CONTENT.value()) {//请求数据发生错误
                conn.errorStream.use {
                    throw RuntimeException(String(it.readAllBytes()))
                }
            }

            //文件总大小
            val total = conn.contentLengthLong + downloadedSize

            //以追加的方式写入文件
            FileOutputStream(saveFile, true).use { oStream ->
                conn.inputStream.use { iStream ->
                    val cache = ByteArray(128 * 1024)
                    var len: Int
                    while (iStream.read(cache, 0, cache.size).also { len = it } != -1) {
                        oStream.write(cache, 0, len)
                        downloadedSize += len.toLong()
                        info.msg = "正在同步文件：${downloadedSize.toDataSize}/${total.toDataSize}"
                        this.socket.send(info)
                    }
                }
            }
            info.msg = ""
            return savePath
        } catch (e: Exception) {
            if (retryTimes < 10) {//重试10次
                Thread.sleep(15_000)
                return this.download(info, md5, retryTimes + 1)
            } else {
                throw e
            }
        } finally {
            conn.disconnect()
        }
    }
}