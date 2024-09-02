package cn.dairo.mysql.model.tool.make.demo

import cn.dairo.mysql.model.tool.cls.bean.TableBean
import cn.dairo.mysql.model.tool.cls.bean.TableField
import cn.dairo.mysql.model.tool.code.tool.FileUtil
import cn.dairo.mysql.model.tool.code.tool.MakeStr
import cn.dairo.mysql.model.tool.code.tool.Tool
import cn.dairo.mysql.model.tool.code.tool.Tool.toClassName

/**
 * 生成Dao文件类
 */
class MakeDto(private val table: TableBean, private val fields: List<TableField>) {

    fun start() {
        val modelClass = Tool.makeJavaBeanClassByTableParams(fields)
        modelClass.name = table.name.toClassName() + "Dto"
        modelClass.pkg = MakeDaoCode.BEAN_PKG_NAME
        val modelFileName = MakeDaoCode.BEAN_PATH + modelClass.name + ".kt"

        //生成model
        val modelStr = MakeStr.makeJavaStr(modelClass)
        FileUtil.savaStr(modelFileName, modelStr, true)
        println(modelFileName)
    }
}