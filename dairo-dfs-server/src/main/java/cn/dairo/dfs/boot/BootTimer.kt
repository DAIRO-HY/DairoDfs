package cn.dairo.dfs.boot

import cn.dairo.dfs.config.SystemConfig
import cn.dairo.lib.server.dbtool.DBService
import cn.dairo.lib.server.dbtool.SqliteTool
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File

/**
 * 定时处理
 */
@Component
class BootTimer {

    /**
     * 文件路径
     */
    @Value("\${sqlite.path}")
    private lateinit var dbPath: String

    /**
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    fun clearData() {
        SqliteTool(this.dbPath).use { db ->

            //删除用户文件数据
            this.deleteDfsFileData(db)

            //删除磁盘文件数据
            this.deleteLocalFile(db)
        }
    }

    /**
     * 删除用户文件数据
     */
    private fun deleteDfsFileData(db: DBService) {

        //垃圾箱最长保存时间
        val trashSaveTime = SystemConfig.instance.trashSaveTime

        //当前时间戳
        val nowTime = System.currentTimeMillis()
        while (true) {

            //不能边查询边删除数据,所以只能先将要删除的id保存的list
            val deleteIdsList = ArrayList<Int>()

            //获取被删除的文件,每次获取10000件
            db.selectResult("select id,deleteDate from dfs_file where deleteDate is not null limit 10000") {
                val id = it.getInt("id")
                val deleteDate = it.getLong("deleteDate")
                if (nowTime - deleteDate > trashSaveTime) {
                    deleteIdsList.add(id)
                }
            }
            if (deleteIdsList.isEmpty()) {
                break
            }

            //循环删除数据
            deleteIdsList.forEach {
                db.exec("delete from dfs_file where id = $it")
            }
        }
    }

    /**
     * 删除磁盘文件数据
     */
    private fun deleteLocalFile(db: DBService) {
        while (true) {

            //不能边查询边删除数据,所以只能先将要删除的id保存到Map
            val deleteIdToPathMap = HashMap<Int, String>()

            //获取没有被使用的文件
            db.selectResult(
                """
                    select * from (
                        select id,path,count from local_file as lf left join (
                            select localId,count(*) as count from dfs_file where localId > 0 group by localId) as df
                        on lf.id = df.localId
                    ) as groupLf where count is null limit 10000
                """.trimIndent()
            ) {
                val id = it.getInt("id")
                val path = it.getString("path")
                deleteIdToPathMap[id] = path
            }
            if (deleteIdToPathMap.isEmpty()) {
                break
            }

            //循环删除数据
            deleteIdToPathMap.forEach { (id, path) ->

                //删除文件
                File(path).delete()
                db.exec("delete from local_file where id = $id")
            }
        }
    }
}
