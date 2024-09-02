package cn.dairo.dfs.boot

import cn.dairo.dfs.config.Constant
import cn.dairo.lib.server.dbtool.DBBase
import cn.dairo.lib.uc.auto.UCInit
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * 应用启动执行
 */
@Order(Int.MAX_VALUE)//值越小越先执行
@Component
class UCInitBoot : ApplicationRunner {

    /**
     * 初始化数据
     * 主要是实例化里面的静态参数
     */
    override fun run(args: ApplicationArguments?) {
        UCInit.databaseConnection = (Constant.dbService as DBBase).connection!!
    }
}
