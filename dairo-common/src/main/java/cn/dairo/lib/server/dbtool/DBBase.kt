package cn.dairo.lib.server.dbtool

import java.math.BigDecimal
import java.sql.*
import java.util.Date
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

/**
 * sqlite数据库操作
 */
open class DBBase : DBService {

    /**
     * 设置连接
     */
    /**
     * 数据库连接
     */
    var connection: Connection? = null

    /**
     * 执行sql语句,返回影响的行数
     *
     * @param sql
     * @return
     */
    override fun exec(sql: String, vararg param: Any?): Int {
        this.getStatement(sql, *param).use {
            return it.executeUpdate()
        }
    }

    /**
     * 查询一个数据,只返回一个结果
     */
    override fun selectSingleOne(sql: String, vararg param: Any?): Any? {
        val statement = getStatement(sql, *param)
        statement.use {
            val rs = statement.executeQuery()
            if (rs.next()) {
                return rs.getObject(1)
            } else {
                return null
            }
        }
    }

    /**
     * 讲查询结果保存到list返回
     */
    override fun selectList(sql: String, vararg params: Any?): List<Map<String, Any?>> {
        val statement = getStatement(sql, *params)
        statement.use {
            val list = ArrayList<Map<String, Any?>>()
            val rs = statement.executeQuery()
            val md = rs.metaData //获取所有字段
            val colNum = md.columnCount //获取列数

            // 展开结果集数据库
            while (rs.next()) {
                val data = HashMap<String, Any?>() //声明Map
                for (i in 1..colNum) {
                    data[md.getColumnName(i)] = rs.getObject(i) //获取键名及值
                }
                list.add(data)
            }
            return list
        }
    }

    /**
     * 讲查询结果保存到list返回
     */
    override fun <T : Any> selectList(cls: KClass<T>, sql: String, vararg params: Any?): List<T> {
        val statement = getStatement(sql, *params)
        statement.use {
            val list = ArrayList<T>()
            val rs = statement.executeQuery()

            val columnSet = HashSet<String>(rs.metaData.columnCount)
            repeat(rs.metaData.columnCount) {
                columnSet.add(rs.metaData.getColumnName(it + 1))
            }

            // 展开结果集数据库
            while (rs.next()) {
                when (cls) {
                    String::class -> list.add(rs.getString(1) as T)
                    Long::class -> list.add(rs.getLong(1) as T)
                    Double::class -> list.add(rs.getDouble(1) as T)
                    Float::class -> list.add(rs.getFloat(1) as T)
                    Int::class -> list.add(rs.getInt(1) as T)
                    Short::class -> list.add(rs.getShort(1) as T)
                    Byte::class -> list.add(rs.getByte(1) as T)
                    Boolean::class -> list.add(rs.getBoolean(1) as T)
                    BigDecimal::class -> list.add(rs.getBigDecimal(1) as T)
                    else -> {
                        val bean = cls.createInstance()
                        cls.memberProperties.forEach {
                            if (!columnSet.contains(it.name)) {//该字段不存在的话
                                return@forEach
                            }
                            this.setBeanValue(bean, it, rs)
                        }
                        list.add(bean)
                    }
                }
            }
            return list
        }
    }

    /**
     * 返回一条查询记录
     */
    override fun selectOne(sql: String, vararg params: Any?): Map<String, Any?>? {
        val statement = getStatement(sql, *params)
        statement.use {
            val rs = statement.executeQuery()
            val md = rs.metaData //获取所有字段
            val colNum = md.columnCount //获取列数

            // 展开结果集数据库
            while (rs.next()) {
                val data = HashMap<String, Any?>() //声明Map
                for (i in 1..colNum) {
                    data[md.getColumnName(i)] = rs.getObject(i) //获取键名及值
                }
                return data
            }
            return null
        }
    }

    /**
     * 将查询结果返回到Bean对象
     */
    override fun <T : Any> selectOne(cls: KClass<T>, sql: String, vararg params: Any?): T? {
        val statement = getStatement(sql, *params)
        statement.use {
            val rs = statement.executeQuery()
            val columnSet = HashSet<String>(rs.metaData.columnCount)
            repeat(rs.metaData.columnCount) {
                columnSet.add(rs.metaData.getColumnName(it + 1))
            }

            // 展开结果集数据库
            while (rs.next()) {
                val bean = cls.createInstance()
                cls.memberProperties.forEach {
                    if (!columnSet.contains(it.name)) {//该字段不存在的话
                        return@forEach
                    }
                    this.setBeanValue(bean, it, rs)
                }
                return bean
            }
            return null
        }
    }

    /**
     * 查询
     */
    override fun selectResult(sql: String, vararg param: Any?, callback: (rs: ResultSet) -> Unit) {
        val statement = getStatement(sql, *param)
        statement.use {
            val rs = statement.executeQuery()
            while (rs.next()) {// 展开结果集数据库
                callback(rs)
            }
        }
    }

    /**
     * 获取PreparedStatement
     * @param sql
     * @return
     * @throws SQLException
     */
    override fun getStatement(sql: String): PreparedStatement {
        return this.getStatement(sql, *arrayOf())
    }

    /**
     * 获取PreparedStatement
     * @param sql
     * @param params
     * @return
     * @throws SQLException
     */
    override fun getStatement(sql: String, vararg params: Any?): PreparedStatement {
        val statement = this.connection!!.prepareStatement(sql)
        for (i in params.indices) {
            val value = params[i]
            if (value is Int) {
                statement.setInt(i + 1, value)
            } else if (value is Long) {
                statement.setLong(i + 1, value)
            } else if (value is Short) {
                statement.setShort(i + 1, value)
            } else if (value is Byte) {
                statement.setByte(i + 1, value)
            } else if (value is Double) {
                statement.setDouble(i + 1, value)
            } else if (value is Boolean) {
                statement.setBoolean(i + 1, value)
            } else if (value is Float) {
                statement.setFloat(i + 1, value)
            } else if (value is String) {
                statement.setString(i + 1, value)
            } else {
                statement.setObject(i + 1, value)
            }
            statement.setObject(i + 1, params[i])
        }
        return statement
    }

    /**
     * 关闭资源
     */
    override fun close() {
        this.connection!!.close()
    }

    /**
     * 将结果集设置到bean对象
     */
    private fun setBeanValue(bean: Any, field: KProperty<*>, rs: ResultSet) {
        field.isAccessible = true
        field as KMutableProperty<*>

        val value = rs.getObject(field.name)
        if (value == null) {//值为null的情况
            field.setter.call(bean, value)
            return
        }
        val fieldType = field.returnType.javaType
        if (fieldType == value::class.javaObjectType || fieldType == Any::class.javaObjectType) {//如果获取到的值类型和当前对象值类型一致时
            field.setter.call(bean, value)
            return
        }
        if (fieldType == Date::class.javaObjectType) {
            field.setter.call(bean, Date(rs.getTimestamp(field.name).time))
            return
        }
        if (fieldType == String::class.javaObjectType) {
            field.setter.call(bean, rs.getString(field.name))
            return
        }
        if (fieldType == Double::class.javaObjectType) {
            field.setter.call(bean, rs.getDouble(field.name))
            return
        }
        if (fieldType == Long::class.javaObjectType) {
            field.setter.call(bean, rs.getLong(field.name))
            return
        }
        if (fieldType == Float::class.javaObjectType) {
            field.setter.call(bean, rs.getFloat(field.name))
            return
        }
        if (fieldType == Int::class.javaObjectType) {
            field.setter.call(bean, rs.getInt(field.name))
            return
        }
        if (fieldType == Short::class.javaObjectType) {
            field.setter.call(bean, rs.getShort(field.name))
            return
        }
        if (fieldType == Byte::class.javaObjectType) {
            field.setter.call(bean, rs.getByte(field.name))
            return
        }
        if (fieldType == Boolean::class.javaObjectType) {
            field.setter.call(bean, rs.getBoolean(field.name))
            return
        }
        if (fieldType == BigDecimal::class.javaObjectType) {
            field.setter.call(bean, rs.getBigDecimal(field.name))
            return
        }
        if (fieldType == Time::class.javaObjectType) {
            field.setter.call(bean, rs.getTime(field.name))
            return
        }
        if (fieldType == Timestamp::class.javaObjectType) {
            field.setter.call(bean, rs.getTimestamp(field.name))
            return
        }
    }
}