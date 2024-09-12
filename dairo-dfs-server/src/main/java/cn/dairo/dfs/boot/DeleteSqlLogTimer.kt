package cn.dairo.dfs.boot

import cn.dairo.dfs.config.Constant
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 删除同步SQL日志
 */
@Component
class DeleteSqlLogTimer {

    /**
     * 每天凌晨3点执行
     */
//    @Scheduled(cron = "*/5 * * * * *")  // 10,000毫秒 = 10秒
    @Scheduled(cron = "0 0 3 * * ?")
    fun delete() {
        //当前数据条数
        val count = Constant.dbService.selectSingleOne("select count(*) from sql_log") as Int
        if(count < 100000){//如果日志数量在允许范围内，不做任何处理
            return
        }

        //删除30天之前的数据
        val deleteTargetDate = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
//        val deleteTargetDate = System.currentTimeMillis() - 30 * 1000
        Constant.dbService.exec("delete from sql_log where date < $deleteTargetDate")
    }
}
