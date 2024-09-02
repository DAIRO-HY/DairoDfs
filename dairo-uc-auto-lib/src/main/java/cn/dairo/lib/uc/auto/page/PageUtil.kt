package cn.dairo.lib.uc.auto.page

import cn.dairo.lib.server.dbtool.DBBase
import cn.dairo.lib.uc.auto.annotations.UCColumn
import cn.dairo.lib.uc.auto.annotations.UCTable
import cn.dairo.lib.uc.auto.annotations.UCWhere
import cn.dairo.lib.uc.auto.UCFormUtil
import cn.dairo.lib.uc.auto.bean.UCColumnBean
import cn.dairo.lib.uc.auto.UCFormConvert
import cn.dairo.lib.uc.auto.UCInit
import cn.dairo.lib.uc.auto.exception.UCException
import javax.sql.DataSource

/**
 * 分页查询数据工具类
 */
object PageUtil {

    /**
     * 获取分页数据
     * @param listFormClass 输出表单类
     * @param whereForm 查询条件表单
     * @return 分页数据信息
     */
    fun getPage(listFormClass: Class<*>, whereForm: PageBaseForm): PageDataEntity {
        if (whereForm.pageSize > 1000) {
            throw UCException("每页最大数据不得超过1000条")
        }

        //从输出输出表单类中查询的表名
        val table = getTableNameByClass(listFormClass);

        //从输出输出表单类中得到要查询的表字段
        val selectFields = getSelectFieldByClass(listFormClass)

        //where生成结果
        val whereSb = StringBuilder()

        //where参数
        val whereParam = ArrayList<Any>()

        //从查询条件表单中生成where语句和where参数
        makeWhereSqlAndParam(whereForm, whereSb, whereParam)

        //获取数据列表的sql语句
        var dataSql = "select $selectFields from $table where $whereSb"

        //获取数据条数的sql语句
        val countSql = "select count(*) from ($dataSql) as temp"

        //排序方式
        var orderBy = whereForm.orderBy
        if (orderBy == null) {//获取某人的排序方式
            listFormClass.declaredFields.forEach { field ->
                val ucPage = field.annotations.find { it is UCColumn && it.defaultSort.isNotEmpty() } as UCColumn?
                if (ucPage != null) {
                    orderBy = field.name + " " + ucPage.defaultSort
                }
            }
        }
        if (orderBy != null) {
            dataSql += " order by $orderBy"
        }

        dataSql += " limit ${(whereForm.page - 1) * whereForm.pageSize},${whereForm.pageSize}"

        val pageData = PageDataEntity()
        UCInit.databaseConnection.use { conn ->
            val dbBase = DBBase()
            dbBase.connection = conn
            val paramArr = whereParam.toArray()

            //数据总条数
            val count = dbBase.selectSingleOne(countSql, *paramArr) as Long
            pageData.itemCount = count
            if (count == 0L) {//没有数据
                pageData.data = ArrayList()
                return@use
            }

            //得到查询结果集
            val rs = dbBase.getStatement(dataSql, *paramArr).executeQuery()

            //转换成FORM表单
            val data = UCFormConvert.convertResultSetToForm(listFormClass, rs)
            pageData.data = data
        }

        //设置当前页面
        pageData.page = whereForm.page

        //设置每页显示条数
        pageData.pageSize = whereForm.pageSize

        if (whereForm.init) {//初始化加载时返回头部数据和搜索字段表单

            //生成表头数据
            pageData.columns = makeColumns(listFormClass)

            //搜索表单字段列表
            pageData.searchFields = UCFormUtil.make(whereForm)
        }
        return pageData
    }

    /**
     * 生成where语句和where参数
     * @param whereSb where生成结果
     * @param whereParam where参数
     */
    private fun makeWhereSqlAndParam(whereForm: PageBaseForm, whereSb: StringBuilder, whereParam: ArrayList<Any>) {

        //类注解上的where语句
        val clsUCWhere = whereForm::class.java.annotations.find { it is UCWhere } as UCWhere?
        if (clsUCWhere != null) {
            whereSb.append(clsUCWhere.where)
        } else {//若没有全局where条件，默认添加1=1
            whereSb.append("1=1")
        }
        whereForm::class.java.declaredFields.forEach { field ->
            field.isAccessible = true
            val value = field.get(whereForm)

            //自定义where条件
            val customWhereMethod =
                whereForm::class.java.declaredMethods.find { it.name == field.name + "Where" }
            if (customWhereMethod != null) {//优先自定义的where条件
                customWhereMethod.isAccessible = true

                //得到自定义的where条件
                val customWhere = customWhereMethod.invoke(whereForm, value) as String? ?: return@forEach
                whereSb.append(" ").append(customWhere)
                if (customWhere.contains("?")) {//需要参数
                    whereParam.add(value)
                }
            } else {//注解的where条件
                value ?: return@forEach

                //得到where注解
                val ucWhere = field.annotations.find { it is UCWhere } as UCWhere? ?: return@forEach
                whereSb.append(" ").append(ucWhere.concat).append(" ")
                    .append(ucWhere.where.replace("{FIELD}", field.name))
                if (ucWhere.where == UCWhere.LIKE || ucWhere.where == UCWhere.LIKE_START || ucWhere.where == UCWhere.LIKE_END) {//模糊匹配需要特殊处理
                    whereParam.add(value.toString().replace("%", "\\%").replace("_", "\\_"))
                } else {
                    whereParam.add(value)
                }
            }
        }
    }

    /**
     * 通过反射从cls文件中获取类名
     * @param cls 返回的表单类类型
     * @return 要查询的表名
     */
    private fun getTableNameByClass(cls: Class<*>): String {
        val ucPageTable = cls.annotations.find { it is UCTable } as UCTable?
            ?: throw UCException(cls.name + "中没有包含注解@UCPageTable")
        var table = ucPageTable.table
        if (table.contains(" ")) {//这是一个子查询
            table = "($table) as page_table"
        }
        return table
    }

    /**
     * 通过反射从cls文件中获取类名
     * @param cls 返回的表单类类型
     * @return 要从表里查询的字段
     */
    private fun getSelectFieldByClass(cls: Class<*>): String {
        return cls.declaredFields.joinToString(separator = ",") { it.name }
    }


    /**
     * 生成表头数据
     * @param cls 目标表单类
     * @return 表头数据列表
     */
    private fun makeColumns(cls: Class<*>): List<UCColumnBean> {
        val columns = ArrayList<UCColumnBean>()

        //遍历所有字段的注解
        cls.declaredFields.forEach { field ->
            val ucTable = field.annotations.find { it is UCColumn } as UCColumn? ?: return@forEach
            val column = UCColumnBean()
            column.key = field.name
            column.title = ucTable.title
            if (!ucTable.orderable) {//默认为true,所以为true时没必要设置数据
                column.orderable = false
            }
            if (ucTable.defaultSort.isNotEmpty()) {
                column.defaultSort = ucTable.defaultSort
            }
            if (ucTable.type.isNotEmpty()) {
                column.type = ucTable.type
            }
            columns.add(column)
        }
        return columns
    }
}