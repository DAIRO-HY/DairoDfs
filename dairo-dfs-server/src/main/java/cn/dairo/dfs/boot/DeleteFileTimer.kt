package cn.dairo.dfs.boot

import cn.dairo.dfs.config.SystemConfig
import cn.dairo.dfs.dao.DfsFileDao
import cn.dairo.dfs.dao.DfsFileDeleteDao
import cn.dairo.dfs.service.DfsFileDeleteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

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
     * 文件彻底操作Service
     */
    @Autowired
    private lateinit var dfsFileDeleteService: DfsFileDeleteService

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
            this.dfsFileDeleteDao.delete(deleteIds.joinToString(separator = ","))
            localIds.forEach {
                this.dfsFileDeleteService.deleteLocalFile(it)
            }
        }
    }
}
