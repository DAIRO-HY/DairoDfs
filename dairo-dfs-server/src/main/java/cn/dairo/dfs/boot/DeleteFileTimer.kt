package cn.dairo.dfs.boot

import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.config.SystemConfig
import cn.dairo.dfs.dao.DfsFileDao
import cn.dairo.dfs.dao.DfsFileDeleteDao
import cn.dairo.dfs.dao.LocalFileDao
import cn.dairo.dfs.service.DfsFileDeleteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File

/**
 * 彻底删除文件计时器
 */
@Component
class DeleteFileTimer {

    @Value("\${config.delete-file-timeout}")
    private var deleteFileTimeout = 0L

    /**
     * 文件操作Dao
     */
    @Autowired
    private lateinit var dfsFileDeleteDao: DfsFileDeleteDao

    /**
     * 文件操作Dao
     */
    @Autowired
    private lateinit var dfsFileDao: DfsFileDao

    /**
     * 文件彻底操作Service
     */
    @Autowired
    private lateinit var localFileDao: LocalFileDao

    /**
     * 删除文件
     * 每天凌晨3点执行
     */
//    @Scheduled(cron = "*/10 * * * * *")  // 10,000毫秒 = 10秒
    @Scheduled(cron = "0 0 3 * * ?")
    fun deleteFile() {

        //当前时间戳
        val nowTime = System.currentTimeMillis()
        while (true) {
            val time = nowTime - this.deleteFileTimeout * 24 * 60 * 60 * 1000
            val deleteList = this.dfsFileDeleteDao.selectIdsByTimeout(time)
            if (deleteList.isEmpty()) {
                break
            }

            //记录要删除的本地文件id
            val localIds = HashSet<Long>()
            val deleteIds = deleteList.map {
                localIds.add(it.localId!!)
                it.id!!
            }

            //彻底删除文件表数据
            //删除文件不需要同步日志,所以不使用mybatis提交,让每个分机端走各自的删除逻辑,防止文件误删
            Constant.dbService.exec("delete from dfs_file_delete where id in (${deleteIds.joinToString(separator = ",")})")
            localIds.forEach {
                if (this.dfsFileDao.isFileUsing(it)) {//文件还在使用中
                    return
                }
                if (this.dfsFileDeleteDao.isFileUsing(it)) {//文件还在使用中
                    return
                }
                val localDto = this.localFileDao.selectOne(it) ?: return
                val file = File(localDto.path!!)
                if (file.exists()) {
                    if (!file.delete()) {//文件删除不成功的话不做任何处理
                        return
                    }
                }

                //删除本地文件表数据
                //删除文件不需要同步日志,所以不使用mybatis提交,让每个分机端走各自的删除逻辑,防止文件误删
                Constant.dbService.exec("delete from local_file where id = ${localDto.id}")

            }
        }
    }
}
