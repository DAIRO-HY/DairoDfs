package cn.dairo.dfs.sync

import com.fasterxml.jackson.databind.JsonNode
import cn.dairo.dfs.boot.Boot
import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.config.SystemConfig
import cn.dairo.dfs.controller.app.sync.SyncWebSocketHandler
import cn.dairo.dfs.extension.bean
import cn.dairo.dfs.extension.md5
import cn.dairo.dfs.sync.bean.SyncInfo
import cn.dairo.dfs.sync.sync_handle.DfsFileSyncHandle
import cn.dairo.dfs.sync.sync_handle.LocalFileSyncHandle
import cn.dairo.lib.Json
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.File
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * 应用启动执行
 */
@Order(Int.MAX_VALUE)//值越小越先执行
@Component
class SyncLogBoot : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        SyncLogUtil.init()
        SyncLogUtil.loopStart()
    }
}

object SyncLogUtil {

    /**
     * 当前同步主机信息
     */
    lateinit var syncInfoList: List<SyncInfo>

    /**
     * 是否正在同步中
     */
    private var mIsRuning = false

    private val socket = SyncWebSocketHandler::class.bean

    /**
     * 记录等待了的时间
     */
    private var waitTimes = 0L

    /**
     * 获取运行状态
     */
    val isRuning: Boolean
        get() {
            synchronized(this) {
                return this.mIsRuning
            }
        }

    /**
     * 获取循环执行间隔时间
     */
    val loopTimer: Long
        get() {
            if (SystemConfig.instance.syncTimer <= 0) {
                return Long.MAX_VALUE
            }
            return SystemConfig.instance.syncTimer.toLong()
        }

    /**
     * 最后同步的ID存放目录
     */
    private val syncLastIdFilePath by lazy {
        "${Boot.service.dataPath}/sync_last_id"
    }


    /**
     * 获取配置同步主机
     */
    fun init() {
        this.syncInfoList = SystemConfig.instance.syncDomains.mapIndexed { index, it ->
            val info = SyncInfo()
            info.domain = it
            info.no = index + 1
            info
        }
    }

    /**
     * 等待中的请求
     */
    private val waitingHttpList = ConcurrentHashMap<HttpURLConnection, Boolean>()

    /**
     * 定时轮询
     */
    fun loopStart() = thread {

        //先停止掉之前所有的轮询
        this.waitingHttpList.keys.forEach {
            try {
                it.disconnect()
            } catch (e: Exception) {
            }

            //停止执行
            this.waitingHttpList[it] = true
        }
        while (true) {//直到上次打开的轮询全部结束之后才继续
            if (this.waitingHttpList.isEmpty()) {
                break
            }
            sleep(500)
        }
        this.syncInfoList.forEach {
            wait(it)
        }
    }

    private fun wait(info: SyncInfo) {
        thread {
            while (true) {
                sleep(1000)
                val http =
                    URL(info.domain + "/${SystemConfig.instance.token}/wait?lastId=" + this.getLastId(info)).openConnection() as HttpURLConnection
                this.waitingHttpList[http] = false
                try {
                    http.connect()
                    val iStream = http.inputStream
                    iStream.use {
                        var tag: Int
                        while (it.read().also { tag = it } != -1) {

                            //记录最有一次心跳时间
                            info.lastHeartTime = System.currentTimeMillis()
                            info.msg = "心跳检测中。"
                            this.socket.send(info)
                            if (tag == 1) {//接收到的标记为1时，代表服务器端有新的日志
                                this.start()
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    //e.printStackTrace()
                    info.msg = "服务端心跳检查失败。"
                    this.socket.send(info)

                    //如果网络连接报错，则等待一段时间之后在恢复
                    sleep(10000)
                } finally {
                    http.disconnect()
                }
                if (!this.waitingHttpList.containsKey(http) || this.waitingHttpList[http] == true) {//如果已经被移除，则终止轮询
                    break
                }

                //每次同步完成之后都重新开启新的请求
                this.waitingHttpList.remove(http)
                if (info.state == 2) {//如果同步发生了错误
                    break
                }
            }
        }
    }

    /**
     * 启动执行
     * @param isForce 是否强制执行
     */
    fun start(isForce: Boolean = false) {
        synchronized(this) {
            if (SyncAllUtil.isRuning) {//全量同步正在进行中
                return
            }
            if (this.mIsRuning) {//并发防止
                return
            }
            this.mIsRuning = true
        }
        try {
            if (isForce) {//强行执行
                SyncLogUtil.syncInfoList.forEach {
                    it.state = 0
                }
            }
            this.syncInfoList.forEach {
                if (it.state != 0) {//只允许待机中的同步
                    return@forEach
                }
                it.state = 1//标记为同步中
                it.msg = ""
                this.socket.send(it)
                this.requestSqlLog(it)
            }
        } finally {
            synchronized(this) {
                this.mIsRuning = false
            }
            this.waitTimes = 0
        }
    }

    /**
     * 循环取sql日志
     * @return 是否处理完成
     */
    private fun requestSqlLog(info: SyncInfo) {

        //得到最后请求的id
        val lastId = this.getLastId(info)
        val url = "${info.domain}/get_log?lastId=$lastId"
        try {
            val data = SyncHttp.request(url)
            if (data == "[]") {//已经没有sql日志

                //执行日志sql
                excuteSqlLog(info)

                info.state = 0//同步完成，标记为待机中
                info.msg = ""
                info.lastTime = System.currentTimeMillis()//最后一次同步完成时间
                this.socket.send(info)
                return
            }
            val jsonData = Json.readValue(data)
            addLog(info, jsonData)
            val lastLog = jsonData.path(jsonData.size() - 1)

            //执行成功之后立即将当前日志的日期保存到本地,降低sql被重复执行的BUG
            this.saveLastId(info, lastLog["id"].asText().toLong())

            //执行日志sql
            excuteSqlLog(info)

            //递归调用，直到服务端日志同步完成
            this.requestSqlLog(info)
        } catch (e: Exception) {
            info.state = 2//标记为同步失败
            info.msg = e.message ?: e.toString()
            this.socket.send(info)
        }
    }

    /**
     * 从主机请求到的日志保存到本地日志
     */
    private fun addLog(info: SyncInfo, jsonData: JsonNode) {
        jsonData.forEach {
            val id = it.path("id").longValue()
            val date = it.path("date").longValue()
            val sql = it.path("sql").textValue()
            val paramJson = it.path("param").textValue()

            try {
                Constant.dbService.exec(
                    "insert into sql_log(id,date,sql,param,state,source) values(?,?,?,?,?,?)",
                    id,
                    date,
                    sql,
                    paramJson,
                    0,
                    info.domain
                )
            } catch (e: Exception) {

                //日志已经添加
                if (e.message == "[SQLITE_CONSTRAINT_PRIMARYKEY] A PRIMARY KEY constraint failed (UNIQUE constraint failed: sql_log.id)") {
                    return@forEach
                } else {
                    throw e
                }
            }
        }
    }

    /**
     * 执行日志里的sql语句
     */
    private fun excuteSqlLog(info: SyncInfo) {
        val list =
            Constant.dbService.selectList("select * from sql_log where state in (0,2) order by id asc limit 1000")
        if (list.isEmpty()) {
            return
        }
        list.forEach {
            val id = it["id"] as Long
            val sql = it["sql"] as String
            val paramJsonStr = it["param"] as String

            //sql语句的参数列表
            val params = Json.readList(paramJsonStr, Any::class.java) as ArrayList

            //日志执行结束后执行sql
            var afterSql: String? = null
            val handleSql = sql.replace(" ", "").replace("\n", "").lowercase()
            if (handleSql.startsWith("insertintolocal_file")) {//如果当前sql语句是往本地文件表里添加一条数据
                LocalFileSyncHandle.bySyncLog(info, params)
            } else if (handleSql.startsWith("insertintodfs_file")) {//如果该sql语句是添加文件
                afterSql = DfsFileSyncHandle.handleBySyncLog(info, params)
            } else {
            }

            val ps = Constant.dbService.getStatement(sql)
            params.forEachIndexed { i, v ->
                ps.setObject(i + 1, v)
            }
//            paramIndexToValue.forEach { (index, value) ->
//                ps.setObject(index.toInt(), value)
//            }
            try {
                ps.executeUpdate()
                if (afterSql != null) {
                    Constant.dbService.exec(afterSql)
                }
                Constant.dbService.exec("update sql_log set state = 1 where id = ?", id)
            } catch (e: Exception) {
                Constant.dbService.exec("update sql_log set state = 2, err = ? where id = ?", e.toString(), id)
                throw e
            } finally {
                ps.close()
            }
        }

        //记录当前同步的数据条数
        info.syncCount += list.size
        this.socket.send(info)
        excuteSqlLog(info)
    }

    /**
     * 保存最后一次请求的日志ID
     */
    fun saveLastId(info: SyncInfo, lastId: Long) {

        //记录最后一次请求到的日志ID文件
        val lastLogIdFile = File(this.syncLastIdFilePath + "." + info.domain.md5)

        //执行成功之后立即将当前日志的日期保存到本地,降低sql被重复执行的BUG
        lastLogIdFile.writeText(lastId.toString())
    }

    /**
     * 保存最后一次请求的日志ID
     */
    fun getLastId(info: SyncInfo): Long {

        //记录最后一次请求到的日志ID文件
        val lastLogIdFile = File(this.syncLastIdFilePath + "." + info.domain.md5)

        //从文件获取最后一次请求到的日志ID
        return try {
            lastLogIdFile.readText().toLong()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * 发送同步通知
     */
    fun sendNotify() {
        this.syncInfoList.forEach { info ->
            val url = "${info.domain}/push_notify"
            try {
                SyncHttp.request(url)
            } catch (e: Exception) {
                info.state = 2//标记为同步失败
                info.msg = "发送同步通知失败：$e"
            }
        }
    }
}
