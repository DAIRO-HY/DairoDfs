package cn.dairo.lib.server.dbtool

import java.sql.DriverManager

/**
 * mysql连接工具
 */
class MysqlTool(
    user: String,
    pwd: String,
    url: String,
    driver: String = "com.mysql.cj.jdbc.Driver"
) : DBBase() {
    init {

        // 注册 JDBC 驱动
        Class.forName(driver)
        val conn = DriverManager.getConnection(url, user, pwd)
        super.connection = conn
    }
}