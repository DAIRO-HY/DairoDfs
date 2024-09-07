package cn.dairo.dfs.boot

import cn.dairo.dfs.dao.DfsFileDao
import cn.dairo.dfs.service.DfsFileDeleteService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 删除回收站数据计时器
 */
@Component
class DeleteTrashTimer {

    @Value("\${config.trash-timeout}")
    private var trashTimeout = 0L

    /**
     * 文件操作Dao
     */
    @Autowired
    private lateinit var dfsFileDao: DfsFileDao

    /**
     * 文件彻底操作Service
     */
    @Autowired
    private lateinit var dfsFileDeleteService: DfsFileDeleteService

    /**
     * 删除回收站数据
     * 每天凌晨3点执行
     */
//    @Scheduled(cron = "*/10 * * * * *")  // 10,000毫秒 = 10秒
    @Scheduled(cron = "0 0 3 * * ?")
    fun deleteTrashData() {

        //当前时间戳
        val nowTime = System.currentTimeMillis()
        while (true) {
            val time = nowTime - this.trashTimeout * 24 * 60 * 60 * 1000
            val deleteIdsList = this.dfsFileDao.getIdsByDeleteAndTimeout(time)
            if (deleteIdsList.isEmpty()) {
                break
            }
            this.dfsFileDeleteService.addDelete(deleteIdsList)
        }
    }
}
