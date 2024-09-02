package cn.dairo.mysql.model.tool.code.tool

import cn.dairo.mysql.model.tool.cls.bean.TableBean
import cn.dairo.mysql.model.tool.cls.bean.TableField
import cn.dairo.mysql.model.tool.make.demo.MakeDaoCode
import java.io.Closeable
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DbTool : Closeable {
    companion object {
        init {
            Class.forName("com.mysql.cj.jdbc.Driver")
        }
    }

    /**
     * 数据库连接
     */
    private var conn: Connection = DriverManager.getConnection(
        "jdbc:mysql://" + MakeDaoCode.URL + "/" + MakeDaoCode.DB_NAME,
        MakeDaoCode.USER,
        MakeDaoCode.PWD
    )

    /**
     * 获取表信息
     */
    val tables: List<TableBean>
        get() {

            // 查询数据库所有表的表名
            val sql =
                "SELECT TABLE_NAME,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + MakeDaoCode.DB_NAME + "'"
            val stmt = conn.createStatement()
            val res = stmt.executeQuery(sql)
            val list = ArrayList<TableBean>()
            while (res.next()) {
                val table = TableBean()
                table.name = res.getString(1)
                table.comment = res.getString(2)
                list.add(table)
            }
            return list
        }

    /**
     * 获取某个表里所有的字段名称及类型
     *
     * @param tableName:表名
     * @return
     */
    fun getTableFields(tableName: String): List<TableField> {

        // 查询数据库所有表的表名
        val sql = """
                select column_name,column_type,column_comment,column_key from Information_schema.columns
                    where TABLE_SCHEMA = '${MakeDaoCode.DB_NAME}' and TABLE_NAME = '${tableName}'
                    """.trimMargin()
        val stmt = conn!!.createStatement()
        val res = stmt.executeQuery(sql)
        val list = ArrayList<TableField>()
        while (res.next()) {
            val p = TableField()
            p.comment = res.getString("column_comment")
            p.name = res.getString("column_name")
            p.type = res.getString("column_type")
            p.isPrimaryKey = res.getString("column_key") == "PRI"
            list.add(p)
        }
        return list
    }

    override fun close() {
        try {
            conn.close()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        }
    }
}
