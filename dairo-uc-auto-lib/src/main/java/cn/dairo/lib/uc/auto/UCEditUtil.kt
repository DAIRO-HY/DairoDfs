package cn.dairo.lib.uc.auto

import cn.dairo.lib.server.dbtool.DBBase
import cn.dairo.lib.uc.auto.annotations.*
import cn.dairo.lib.uc.auto.exception.UCException

/**
 * 自定义控件编辑工具
 */
object UCEditUtil {

    /**
     * 通过form获取一条数据
     */
    fun selectOne(form: Any): Any {
        val editFormCls = form::class.java
        val fields = editFormCls.declaredFields//得到表单所有字段

        //得到查询条件字段
        val keyFields = fields.filter { it.annotations.find { it is UCEditKey } != null }

        //得到查询关键字的参数
        val whereParam = keyFields.map {
            it.isAccessible = true

            //如果查询关键字为NULL，直接返回
            val value = it.get(form) ?: return editFormCls.getDeclaredConstructor().newInstance()
            value
        }.toTypedArray()

        //得到where条件
        val where = keyFields.joinToString(separator = " and ") { it.name + " = ?" }

        val queryField = fields.joinToString(separator = ",") { it.name }//得到要查询的字段

        //从表单class中获取目标表名
        val table = (editFormCls.annotations.find { it is UCTable } as UCTable?)?.table
            ?: throw UCException("${editFormCls.name}中没有@${UCTable::class.java.simpleName}注解")

        //查询数据sql语句
        val sql = "select $queryField from $table where $where"
        UCInit.databaseConnection.use { conn ->
            val dbBase = DBBase()
            dbBase.connection = conn

            //得到查询结果集
            val rs = dbBase.getStatement(sql, *whereParam).executeQuery()

            //转换成FORM表单
            val dataList = UCFormConvert.convertResultSetToForm(editFormCls, rs)
            if (dataList.isEmpty()) {
                throw UCException("没有查询到数据")
            }
            if (dataList.size > 1) {
                throw UCException("查询到了多条(${dataList.size})数据")
            }
            return dataList[0]
        }
    }

    /**
     * 通过Form表单，编辑一条数据
     */
    fun edit(form: Any) {
        val editFormCls = form::class.java
        val fields = editFormCls.declaredFields//得到表单所有字段
        fields.forEach { it.isAccessible = true }
        val nullUCEditKey = fields.find { field ->
            field.annotations.find { it is UCEditKey } ?: return@find false
            field.get(form) == null
        }
        if (nullUCEditKey != null) {//表单里的主键值为null，则为insert模式

            //插入数据
            insert(form)
            return
        }
        val setParamList = ArrayList<Any>()//要更新的字段参数列表
        val whereParamList = ArrayList<Any>()//更新的where参数列表
        val setSqlSb = StringBuilder()//要设置的字段sql语句
        val whereSqlSb = StringBuilder()//更新where条件的sql语句
        fields.forEach { field ->
            val value = field.get(form)
            field.annotations.forEach {
                if (it is UCEditInsertUpdate || it is UCEditUpdate) {//允许更新的字段
                    setSqlSb.append("${field.name} = ?,")
                    setParamList.add(value)
                    return@forEach
                }
                if (it is UCEditKey) {//更新where条件字段
                    whereSqlSb.append("${field.name} = ? and ")
                    whereParamList.add(value)
                }
            }
        }
        if (setSqlSb.isEmpty()) {
            throw UCException("${editFormCls.name}没有设置（@${UCEditInsertUpdate::class.java.simpleName}/@${UCEditUpdate::class.java.simpleName}）注解")
        }
        if (whereSqlSb.isEmpty()) {
            throw UCException("${editFormCls.name}没有设置（@${UCEditKey::class.java.simpleName}）注解")
        }
        setSqlSb.setLength(setSqlSb.length - 1)//去掉最后一个逗号
        whereSqlSb.append("1=1")//补全where条件

        //整合参数
        val whereParam = ArrayList<Any>()
        whereParam.addAll(setParamList)
        whereParam.addAll(whereParamList)

        //从表单class中获取目标表名
        val table = (editFormCls.annotations.find { it is UCTable } as UCTable?)?.table
            ?: throw UCException("${editFormCls.name}中没有@${UCTable::class.java.simpleName}注解")

        //sql语句
        val sql = "update $table set $setSqlSb where $whereSqlSb"
        UCInit.databaseConnection.use { conn ->
            val dbBase = DBBase()
            dbBase.connection = conn
            dbBase.exec(sql, *whereParam.toArray())
        }
    }

    /**
     * 通过Form表单，编辑一条数据
     */
    private fun insert(form: Any) {
        val editFormCls = form::class.java
        val insertFields = ArrayList<String>()//要插入的字段列表
        val params = ArrayList<Any>()//要插入的参数
        editFormCls.declaredFields.forEach { field ->
            field.annotations.find { it is UCEditInsertUpdate || it is UCEditInsert } ?: return@forEach
            field.isAccessible = true
            val value = field.get(form) ?: return@forEach
            insertFields.add(field.name)
            params.add(value)
        }
        if (insertFields.isEmpty()) {
            throw UCException("${editFormCls.name}没有设置（@${UCEditInsertUpdate::class.java.simpleName}/@${UCEditInsert::class.java.simpleName}）注解")
        }

        //从表单class中获取目标表名
        val table = (editFormCls.annotations.find { it is UCTable } as UCTable?)?.table
            ?: throw UCException("${editFormCls.name}中没有@${UCTable::class.java.simpleName}注解")

        //要插入的sql字段
        val insertFieldStr = insertFields.joinToString(separator = ",") { it }

        //要插入的参数占位符
        val insertFieldParamStr = insertFields.joinToString(separator = ",") { "?" }

        //sql语句
        val sql = "insert into $table ($insertFieldStr) values($insertFieldParamStr)"
        UCInit.databaseConnection.use { conn ->
            val dbBase = DBBase()
            dbBase.connection = conn
            dbBase.exec(sql, *params.toArray())
        }
    }
}