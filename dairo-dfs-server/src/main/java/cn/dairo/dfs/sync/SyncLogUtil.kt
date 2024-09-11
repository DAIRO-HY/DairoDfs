package cn.dairo.dfs.sync

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import cn.dairo.dfs.boot.Boot
import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.config.SystemConfig
import cn.dairo.dfs.extension.md5
import cn.dairo.dfs.sync.bean.SyncInfo
import cn.dairo.dfs.sync.sync_handle.LocalFileSyncHandle
import cn.dairo.lib.Json
import java.io.File
import java.util.*

/**
 * 应用启动执行
 */
object SyncLogUtil {

    /**
     * 当前同步主机信息
     */
    lateinit var syncInfoList: List<SyncInfo>

    /**
     * 是否正在同步中
     */
    private var mIsRuning = false

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
//        this.syncInfoList = arrayListOf(SyncInfo().apply {
//            this.domain = "http://localhost:8030"
//        })
    }

    /**
     * 管理员强制重新执行
     */
    fun reDoSync() {
        this.start()
    }

    /**
     * 执行同步
     */
    fun start() {
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
            this.syncInfoList.forEach {
                it.state = 1//标记为同步中
                this.requestSqlLog(it)
            }
        } finally {
            synchronized(this) {
                this.mIsRuning = false
            }
        }
    }

    /**
     * 循环取sql日志
     * @return 是否处理完成
     */
    private fun requestSqlLog(info: SyncInfo) {

        //得到最后请求的id
        val lastId = this.getLastId(info)
        val url = "${info.domain}/sync/get_log?lastId=$lastId"
        try {
            val data = SyncHttp.request(url)
            if (data == "[]") {//已经没有sql日志
                info.state = 0//同步完成，标记为待机中
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
            info.msg = e.toString()
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
            Constant.dbService.selectList("select * from sql_log where state in (0,2) order by id asc limit 10000")
        if (list.isEmpty()) {
            return
        }
        list.forEach {
            val id = it["id"] as Long
            val sql = it["sql"] as String
            val paramJsonStr = it["param"] as String

            //sql语句的参数列表
            val params = Json.readList(paramJsonStr, Any::class.java) as ArrayList

            //如果当前sql语句是往本地文件表里添加一条数据
            if (sql.replace(" ", "").replace("\n", "").startsWith("insertintolocal_file")) {
                LocalFileSyncHandle.bySyncLog(info, params)
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
                Constant.dbService.exec("update sql_log set state = 1 where id = ?", id)
            } catch (e: Exception) {
                Constant.dbService.exec("update sql_log set state = 2, err = ? where id = ?", e.toString(), id)
                throw e
            } finally {
                ps.close()
            }
        }
        excuteSqlLog(info)
    }

    /**
     * 保存最后一次请求的日志ID
     */
    fun saveLastId(info: SyncInfo, lastId: Long) {

        //记录最后一次请求到的日志ID文件
        val lastLogIdFile = File(this.syncLastIdFilePath + "." + info.domain!!.md5)

        //执行成功之后立即将当前日志的日期保存到本地,降低sql被重复执行的BUG
        lastLogIdFile.writeText(lastId.toString())
    }

    /**
     * 保存最后一次请求的日志ID
     */
    fun getLastId(info: SyncInfo): Long {

        //记录最后一次请求到的日志ID文件
        val lastLogIdFile = File(this.syncLastIdFilePath + "." + info.domain!!.md5)

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
            val url = "${info.domain}/sync/push_notify"
            try {
                SyncHttp.request(url)
            } catch (e: Exception) {
                info.state = 2//标记为同步失败
                info.msg = "发送同步通知失败：$e"
            }
        }
    }
}
