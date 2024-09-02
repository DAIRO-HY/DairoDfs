package cn.dairo.dfs.sync.socket.client

import cn.dairo.lib.Json
import cn.dairo.lib.server.dbtool.DBBase
import cn.dairo.lib.server.dbtool.SqliteTool
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.concurrent.thread

/**
 * 应用启动执行
 */
@Component
class SyncSlave : ApplicationRunner {

    /**
     * 是否正在同步中
     */
    var isRuning = false

    /**
     * 出错次数
     */
    private var errorCount = 0

    /**
     * 最大出错重试次数
     */
    private var MAX_ERROR_COUNT = 3

    /**
     * 当前同步错误信息
     */
    var currentError: String? = null

    /**
     * 数据存放文件夹
     */
    @Value("\${data.path}")
    private lateinit var dataPath: String

    /**
     * db文件路径
     */
    @Value("\${sqlite.path}")
    private lateinit var dbPath: String

    /**
     * sqlite数据库连接
     */
    private val db: DBBase by lazy {
        //SqliteTool(this.dbPath)
        SqliteTool("./data/dairo-dfs2.sqlite")
    }

    /**
     * 最后同步的ID存放目录
     */
    private val syncLastIdFilePath: String by lazy {
        "$dataPath/sync_last_id"
    }

    /**
     * 初始化数据
     * 主要是实例化里面的静态参数
     */
    override fun run(args: ApplicationArguments?) {
        mInstant = this
    }

    /**
     * 管理员强制重新执行
     */
    fun reDoSync() {
        this.errorCount = 0
        this.doSync()
    }

    /**
     * 执行同步
     */
    fun doSync() {
        synchronized(this) {
            if (this.isRuning) {
                return
            }
            this.isRuning = true
        }
        thread {
            while (true) {
                println("-->同步数据执行中")
                if (this.errorCount >= this.MAX_ERROR_COUNT) {

                    //有错误,并且尝试了多次还是有错误,这种情况需要管理员确认之后才能和继续执行
                    break
                }

                //有发生错误
                val isFinish = this.requestSqlLog()
                if (isFinish) {//执行结束,并且没有错误
                    break
                }
            }
            synchronized(this) {
                this.isRuning = false
            }
            println("-->同步数据执行完了")
        }
    }

    /**
     * 循环去sql日志
     * @return 是否处理完成
     */
    private fun requestSqlLog(): Boolean {
        val lastIdStr = try {
            File(syncLastIdFilePath).readText()
        } catch (e: Exception) {
            null
        }
        val lastId: Long
        if (lastIdStr != null) {
            lastId = lastIdStr.toLong()
        } else {
            lastId = 0
        }
        val masterHost = "http://localhost:8030"
        val url = URL("$masterHost/sync/get_sql_log?lastId=$lastId")
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"

            //连接超时
            conn.connectTimeout = 30000

            //读数据超时
            conn.readTimeout = 30000
            conn.connect()

            //返回状态码
            val httpStatus = conn.responseCode
            if (httpStatus != HttpStatus.OK.value()) {//请求数据发生错误
                conn.errorStream.use {
                    this.currentError = String(it.readAllBytes())
                }
                this.errorCount++
                return true
            }
            var data: String?
            conn.inputStream.use {
                data = String(it.readAllBytes())
            }
            if (data == "[]") {//已经没有sql日志
                return true
            }
            addLog(data!!)
            excuteSqlLog()
            this.errorCount = 0
            this.currentError = null
            return false
        } catch (e: Exception) {
            this.currentError = e.toString()
            this.errorCount++
            return true
        } finally {
            conn.disconnect()
        }
    }

    /**
     * 执行日志里的sql语句
     */
    private fun addLog(data: String) {
        val jsonData = Json.readValue(data)
        jsonData.forEach {
            val id = it.path("id").longValue()
            val date = it.path("date").longValue()
            val sql = it.path("sql").textValue()
            val paramJson = it.path("param").textValue()

            this.db.exec(
                "insert into sql_log(id,date,sql,param,state,source) values(?,?,?,?,?,?)",
                id,
                date,
                sql,
                paramJson,
                0,
                "127.0.0.1"
            )
        }
        val lastLog = jsonData.path(jsonData.size() - 1)
        val lastId = lastLog["id"].longValue()

        //执行成功之后立即将当前日志的日期保存到本地,降低sql被重复执行的BUG
        saveSyncLastId(lastId)
    }

    /**
     * 执行日志里的sql语句
     */
    private fun excuteSqlLog() {
        val list = this.db.selectList("select * from sql_log where state in (0,2) order by id asc limit 10000")
        if (list.isEmpty()) {
            return
        }
        list.forEach {
            val id = it["id"] as Long
            val sql = it["sql"] as String
            val paramJson = it["param"] as String

            val ps = this.db.connection!!.prepareStatement(sql)
            val paramIndexToValue = Json.readValue(paramJson, HashMap::class.java) as Map<String, Any?>
            paramIndexToValue.forEach { (index, value) ->
                ps.setObject(index.toInt(), value)
            }
            try {
                ps.executeUpdate()
                this.db.exec("update sql_log set state = 1 where id = ?", id)
            } catch (e: Exception) {
                this.db.exec("update sql_log set state = 2, err = ? where id = ?", e.toString(), id)
                throw e
            } finally {
                ps.close()
            }
        }
        excuteSqlLog()
    }

    /**
     * 保存最后一次更新的日志id
     */
    private fun saveSyncLastId(id: Long) {
        File(this.syncLastIdFilePath).writeText(id.toString())
    }


    companion object {
        private lateinit var mInstant: SyncSlave

        /**
         * 全局实例
         */
        val instant get() = mInstant
    }
}
