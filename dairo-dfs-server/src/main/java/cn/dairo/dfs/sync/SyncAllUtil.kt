package cn.dairo.dfs.sync

import com.fasterxml.jackson.databind.node.ObjectNode
import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.sync.bean.SyncInfo
import cn.dairo.dfs.sync.sync_handle.DfsFileSyncHandle
import cn.dairo.dfs.sync.sync_handle.LocalFileSyncHandle
import cn.dairo.lib.Json

/**
 * 全量同步工具
 */
object SyncAllUtil {

    /**
     * 标记全量同步是否正在进行中
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
     * 开始同步
     */
    fun start() {
        synchronized(this) {
            if (SyncLogUtil.isRuning) {//日志同步正在进行中
                return
            }
            if (this.mIsRuning) {//并发防止
                return
            }
            this.mIsRuning = true
        }
        try {
            this.doSync()
        } finally {
            synchronized(this) {
                this.mIsRuning = false
            }
        }
    }

    private fun doSync() {
        SyncLogUtil.syncInfoList.forEach { info ->
            try {

                //断面ID,从主机端获取的数据ID不得大于该值
                val aopId = this.getAopId(info)
                val tbNames = Constant.dbService.selectList(
                    String::class, "select name from sqlite_master where type = 'table'"
                )
                tbNames.forEach {
                    if (it == "sql_log") {//不要同步日志数据表
                        return@forEach
                    }
                    this.loopSync(info, it, 0, aopId)
                }

                //从日志数据表中删除当前已经同步成功的服务端日志
                Constant.dbService.exec("delete from sql_log where source = ? and id < ?", info.domain, aopId)

                //设置日志同步最后的ID
                SyncLogUtil.saveLastId(info, aopId)
            } catch (e: Exception) {
                info.state = 2
                info.msg = e.toString()
            }
        }
    }

    /**
     * 循环同步数据，直到包数据同步完成
     */
    private fun loopSync(info: SyncInfo, tbName: String, lastId: Long, aopId: Long) {

        val masterIds = this.getTableId(info, tbName, lastId, aopId)
        if (masterIds.isEmpty()) {//同步主机端的数据已经全部取完
            return
        }

        //设置本次获取到的最后一个ID
        val currentLastId = if (masterIds.contains(",")) {
            masterIds.substring(masterIds.lastIndexOf(",") + 1).toLong()
        } else {
            masterIds.toLong()
        }

        //得到需要
        val needSyncIds = this.filterNotExistsId(tbName, masterIds)
        if (needSyncIds.isEmpty()) {//本次获取到的数据，本地已经全部存在，继续获取下一篇段数据

            //再次同步
            this.loopSync(info, tbName, currentLastId, aopId)
            return
        }

        //得到需要同步的数据
        val data = this.getTableData(info, tbName, needSyncIds)

        //插入数据
        this.insertData(info, tbName, data)

        //再次同步
        this.loopSync(info, tbName, currentLastId, aopId)
    }

    /**
     * 获取一个断面ID，防止再全量同步的过程中，主机又增加数据，导致全量同步数据不完整
     */
    private fun getAopId(info: SyncInfo): Long {
        val url = info.domain + "/sync/get_aop_id"
        val aopId = SyncHttp.request(url)
        return aopId.toLong()
    }

    /**
     * 从主机过去数据ID
     */
    private fun getTableId(info: SyncInfo, tbName: String, lastId: Long, aopId: Long): String {
        val url = info.domain + "/sync/get_table_id?tbName=$tbName&lastId=$lastId&aopId=$aopId"
        val ids = SyncHttp.request(url)
        return ids
    }

    /**
     * 筛选出本地不存在的ID
     */
    private fun filterNotExistsId(tbName: String, ids: String): String {

        //得到已经存在的ID列表
        val existsIdSet =
            Constant.dbService.selectList(Long::class, "select id from $tbName where id in ($ids)").toHashSet()

        //得到本地不存在的id
        val notExistsIds = ids.split(",").filter {
            !existsIdSet.contains(it.toLong())
        }
        return notExistsIds.joinToString(separator = ",") { it }
    }

    /**
     * 从同步主机端取数据
     */
    private fun getTableData(info: SyncInfo, tbName: String, ids: String): String {
        val url = info.domain + "/sync/get_table_data?tbName=$tbName&ids=$ids"
        val data = SyncHttp.request(url)
        return data
    }

    /**
     * 同步主机数据
     */
    private fun insertData(info: SyncInfo, tbName: String, data: String) {
        val jsonData = Json.readValue(data)
        jsonData.forEach { item ->
            item as ObjectNode
            when (tbName) {

                //当前请求的是本地文件存储表，先去下载文件
                "local_file" -> LocalFileSyncHandle.bySyncAll(info, item)

                //如果是用户文件表
                "dfs_file" -> DfsFileSyncHandle.handle(item)
            }

            //要插入的字段
            val fields = ArrayList<String>()
            val values = ArrayList<String?>()
            item.fields().forEach {
                fields.add(it.key)

                val value = it.value
                if (value.isNull) {//null值
                    values.add(null)
                } else {
                    values.add(it.value.asText())
                }
            }
            Constant.dbService.exec(
                "insert into $tbName(${fields.joinToString()}) values (${fields.joinToString { "?" }})",
                *values.toArray()
            )
        }
    }
}