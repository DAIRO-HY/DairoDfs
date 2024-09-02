package cn.dairo.dfs.boot

import cn.dairo.dfs.dao.DfsFileDao
import cn.dairo.dfs.dao.LocalFileDao
import cn.dairo.dfs.service.DfsFileService
import cn.dairo.dfs.sync.SyncLogUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.File

/**
 * 应用启动执行
 */
@Order(Int.MIN_VALUE)//值越小越先执行
@Component
class Boot : ApplicationRunner {

    /**
     * 文件路径
     */
    @Value("\${sqlite.path}")
    lateinit var dbPath: String

    /**
     * 数据存放文件夹
     */
    @Value("\${data.path}")
    lateinit var dataPath: String

    /**
     * 运行环境
     */
    @Value("\${active}")
    lateinit var mActive: String

    /**
     * 是否开发环境
     */
    val isDev get() = mActive == "dev"

    /**
     * 是否生产环境
     */
    val isProd get() = mActive == "prod"


    /**
     * 文件数据操作Service
     */
    @Autowired
    lateinit var dfsFileService: DfsFileService


    /**
     * 文件数据操作Dao
     */
    @Autowired
    lateinit var dfsFileDao: DfsFileDao

    /**
     * 本地存储文件数据操作Dao
     */
    @Autowired
    lateinit var localFileDao: LocalFileDao

    /**
     * 初始化数据
     * 主要是实例化里面的静态参数
     */
    override fun run(args: ApplicationArguments?) {
        mService = this

        //创建临时目录
        File(this.dataPath + "/temp").mkdirs()
        SyncLogUtil.init()
    }

    companion object {
        private lateinit var mService: Boot

        /**
         * 全局service
         */
        val service get() = mService
    }
}
