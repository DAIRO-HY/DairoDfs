package cn.dairo.dfs.config

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

/**
 * 系统配置
 */
class SystemConfig private constructor() {

    /**
     * 文件上传限制(MB)
     */
    var uploadMaxSize = 10 * 1024L

    /**
     * 垃圾箱存放最大时间,默认30天
     */
    var trashSaveTime = 30L * 24 * 60 * 60 * 1000

    /**
     * 文件保存文件夹列表
     */
    var saveFolderList: List<String> = listOf("./data")

    /**
     * 同步域名
     */
    var syncDomains: List<String> = ArrayList()

    companion object {

        /**
         * 单例实例
         */
        private var mInstance: SystemConfig? = null

        val instance: SystemConfig
            get() {
                if (mInstance == null) {
                    val systemJsonFile = File(Constant.SYSTEM_JSON_PATH)
                    if (systemJsonFile.exists()) {//若配置文件存在
                        try {
                            mInstance = ObjectMapper().readValue(systemJsonFile, SystemConfig::class.java)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            mInstance = SystemConfig()
                        }
                    } else {
                        mInstance = SystemConfig()
                    }
                }
                return mInstance!!
            }

        /**
         * 数据持久化
         */
        fun save() {
            if (!File(Constant.SYSTEM_JSON_PATH).exists()) {//文件不存在时创建文件夹
                File(Constant.SYSTEM_JSON_PATH).parentFile.mkdirs()
            }
            ObjectMapper().writeValue(File(Constant.SYSTEM_JSON_PATH), instance)
        }
    }
}