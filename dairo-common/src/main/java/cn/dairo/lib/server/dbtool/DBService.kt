package cn.dairo.lib.server.dbtool

import java.io.Closeable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.reflect.KClass

interface DBService : Closeable {

    /**
     * @param sql
     * @return
     */
    fun exec(sql: String, vararg param: Any?): Int

    /**
     * 查询一个数据,只返回一个结果
     */
    fun selectSingleOne(sql: String, vararg param: Any?): Any?

    /**
     * 讲查询结果保存到list返回
     */
    fun selectList(sql: String, vararg param: Any?): List<Map<String, Any?>>

    /**
     * 讲查询结果保存到list返回
     */
    fun <T : Any> selectList(cls: KClass<T>, sql: String, vararg params: Any?): List<T>

    /**
     * 返回一条查询记录
     */
    fun selectOne(sql: String, vararg params: Any?): Map<String, Any?>?

    /**
     * 将查询结果返回到Bean对象
     */
    fun <T : Any> selectOne(cls: KClass<T>, sql: String, vararg params: Any?): T?

    /**
     * 查询
     */
    fun selectResult(sql: String, vararg param: Any?, callback: (rs: ResultSet) -> Unit)

    /**
     * 获取PreparedStatement
     * @param sql
     * @return
     * @throws SQLException
     */
    fun getStatement(sql: String): PreparedStatement

    /**
     * 获取PreparedStatement
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     */
    fun getStatement(sql: String, vararg params: Any?): PreparedStatement
}