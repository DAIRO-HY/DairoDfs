package cn.dairo.lib.server.dbtool

import java.sql.DriverManager

/**
 * sqlite数据库操作
 * @param dbPath 数据库文件路径
 */
class SqliteTool(private val dbPath: String) : DBBase() {

    init {
        val url = "jdbc:sqlite:${this.dbPath}" //定义连接数据库的url(url:访问数据库的URL路径),test为数据库名称
        Class.forName("org.sqlite.JDBC") //加载数据库驱动
        val conn = DriverManager.getConnection(url) //获取数据库连接
        super.connection = conn
    }
}