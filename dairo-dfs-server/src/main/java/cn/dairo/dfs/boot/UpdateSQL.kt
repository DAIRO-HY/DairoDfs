package cn.dairo.dfs.boot

import cn.dairo.dfs.config.Constant
import cn.dairo.lib.server.dbtool.DBBase
import cn.dairo.lib.server.dbtool.DBService
import cn.dairo.lib.server.dbtool.SqliteTool
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.File
import java.nio.charset.Charset

/**
 * 项目启动初始化sqlite数据库
 */
@Order(Int.MIN_VALUE + 1)//值越小越先执行
@Component
class UpdateSQL : ApplicationRunner {

    /**
     * 文件路径
     */
    @Value("\${sqlite.path}")
    private lateinit var dbPath: String

    /**
     * 数据库版本号
     */
    private val VERSION = 3

    override fun run(args: ApplicationArguments) {

        //db文件是否已经存在
        val isExists = File(this.dbPath).exists()
        if (!isExists) {

            //创建文件夹
            File(this.dbPath).parentFile.mkdirs()
        }
        Constant.dbService.use {
            val oldVersion = it.selectSingleOne("PRAGMA USER_VERSION") as Int
            upgrade(it, oldVersion, this.VERSION)
            if (this.VERSION != oldVersion) {

                //设置数据库版本号
                it.exec("PRAGMA USER_VERSION = $VERSION")
            }
        }
    }

    /**
     * 更新表结构
     */
    private fun upgrade(db: DBService, oldVersion: Int, newVersion: Int) {
        this.create(db)
        if (oldVersion < 4) {
            //"ALTER TABLE channel ADD acl_state INTEGER NOT NULL DEFAULT 0".exec(db)
        }

    }

    /**
     * 第一次运行时,创建表
     */
    private fun create(db: DBService) {
        "sql/create/dfs_file.sql".execBYFile(db)
        "sql/create/local_file.sql".execBYFile(db)
        "sql/create/user.sql".execBYFile(db)
        "sql/create/share.sql".execBYFile(db)
        "sql/create/sql_log.sql".execBYFile(db)
        "sql/create/user_token.sql".execBYFile(db)
    }

    private fun String.execBYFile(db: DBService) = UpdateSQL::class.java.classLoader.getResourceAsStream(this).use {
        val sql = String(it.readAllBytes(), Charset.forName("UTF-8"))
        sql.exec(db)
    }

    private fun String.exec(db: DBService) {
        try {
            db.exec(this)
        } catch (e: Exception) {
            //e.printStackTrace()
        }
    }
}