package cn.dairo.lib.server.dbtool

import java.sql.DriverManager

/**
 * 需要引用的jar包 'net.sf.ucanaccess:ucanaccess:5.0.0'
 * Access数据库操作
 * @param dbPath 数据库文件路径
 */
class AccessTool(dbPath: String) : DBBase() {
    init {

        // url表示需要连接的数据源的位置，此时使用的是JDBC-ODBC桥的连接方式，url是"jdbc:odbc:数据源名"
        val url = "jdbc:ucanaccess://${dbPath}"
        Class.forName("net.ucanaccess.jdbc.UcanaccessDriver")
        val conn = DriverManager.getConnection(url)
        super.connection = conn
    }
}