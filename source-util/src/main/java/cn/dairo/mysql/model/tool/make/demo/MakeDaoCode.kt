package cn.dairo.mysql.model.tool.make.demo

import cn.dairo.mysql.model.tool.code.tool.DbTool

object MakeDaoCode {
    /**
     * 数据库名称
     */
    const val DB_NAME = "music_db"

    /**
     * 数据库连接url
     */
    const val URL = "127.0.0.1:3306"

    /**
     * 数据库登录名
     */
    const val USER = "root"

    /**
     * 数据库连接密码
     */
    const val PWD = "Ths@00010001"

    /**
     * 项目源码路径
     */
    const val PROJECT = "./dairo-music-server/src/main"

    /**
     * 要生成的表名
     */
    private const val TABLE_NAME = "swiper"

    /**
     * 项目源码路径
     */
    const val PROJECT_PATH = "$PROJECT/java"

    /**
     * bean包名
     */
    const val BEAN_PKG_NAME = "cn.dairo.music.dao.dto"

    /**
     * dao包名
     */
    const val DAO_PKG_NAME = "cn.dairo.music.dao"

    /**
     * seivice包名
     */
    const val DAO_SERVICE_PKG_NAME = "cn.dairo.music.service"

    /**
     * impl包名
     */
    const val IMPL_PKG_NAME = "cn.dairo.music.service.impl"

    /**
     * 生成的bean.java存放路径
     */
    val BEAN_PATH = PROJECT_PATH + "/" + BEAN_PKG_NAME.replace(".", "/") + "/"

    /**
     * 生成的dao.java存放路径
     */
    val DAO_PATH = PROJECT_PATH + "/" + DAO_PKG_NAME.replace(".", "/") + "/"

    /**
     * 生成的service.java存放路径
     */
    val SERVICE_PATH = PROJECT_PATH + "/" + DAO_SERVICE_PKG_NAME.replace(".", "/") + "/"

    /**
     * 生成的impl.java存放路径
     */
    val IMPL_PATH = PROJECT_PATH + "/" + IMPL_PKG_NAME.replace(".", "/") + "/"

    /**
     * xml文件存放路径
     */
    val XML_PATH = PROJECT + "/resources/" + DAO_PKG_NAME.replace(".", "/") + "/"

    @JvmStatic
    fun main(args: Array<String>) {
        val dt = DbTool()

        //得到指定数据库所有表名
        dt.tables.forEach {

            // 只要指定表的字段
            if (it.name != TABLE_NAME) {
                return@forEach
            }

            //得到表所有的参数及类型
            val fields = dt.getTableFields(it.name!!)

            //制作Dto
            MakeDto(it, fields).start()

            //生成XML
            MakeXml(it, fields).start()

            //生成Dao
            MakeDao(it).start()
        }
        dt.close()
    }
}