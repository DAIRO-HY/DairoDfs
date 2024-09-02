package cn.dairo.lib.uc.auto

import cn.dairo.lib.uc.auto.exception.UCException
import java.lang.reflect.Method
import java.sql.ResultSet

/**
 * 表单和表结果互相转换工具类
 */
object UCFormConvert {

    /**
     * 将查询结果集转换到Form表单类
     * @param cls 目标表单类
     * @param rs 结果集
     * @return 目标结果
     */
    fun <T> convertResultSetToForm(cls: Class<T>, rs: ResultSet): List<T> {

        //字段对应的set方法
        val fieldNameToSetter = HashMap<String, Method>()

        //类里的所有字段
        val fields = cls.declaredFields

        //得到类里的所有函数MAP映射
        val methodMap = cls.declaredMethods.associateBy({ it.name }, { it })
        fields.forEach { field ->

            //优先寻找转换器，如果转换器不存在，则使用Setter方法
            val method = methodMap[field.name + "Convert"]
                ?: methodMap["set" + field.name.replaceFirstChar { it.uppercase() }]
            method!!.isAccessible = true
            fieldNameToSetter[field.name] = method
        }
        val formList = ArrayList<T>()
        while (rs.next()) {//遍历结果集
            val form = cls.getDeclaredConstructor().newInstance()
            fields.forEach {
                val setter = fieldNameToSetter[it.name]!!
                val value = rs.getObject(it.name)
                try {
                    setter.invoke(form, value)
                } catch (e: IllegalArgumentException) {
                    throw UCException("类${cls.name}.${setter.name}(${setter.parameterTypes[0].name}) 不能接收${value.javaClass.name}:${value}")
                }
            }
            formList.add(form)
        }
        return formList
    }
}