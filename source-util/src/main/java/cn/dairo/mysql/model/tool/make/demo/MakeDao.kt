package cn.dairo.mysql.model.tool.make.demo

import cn.dairo.mysql.model.tool.cls.bean.TableBean
import cn.dairo.mysql.model.tool.code.tool.FileUtil
import cn.dairo.mysql.model.tool.code.tool.Tool.toClassName
import java.io.File

/**
 * 生成Dao文件类
 */
class MakeDao(private val table: TableBean) {

    fun start() {//生成DAO
        val className = this.table.name.toClassName()
        val filename = MakeDaoCode.DAO_PATH + className + "Dao.kt"
        if (File(filename).exists()) {
            return
        }

        // DAO部分的代码
        val daoCode = """
        package ${MakeDaoCode.DAO_PKG_NAME}

        import ${MakeDaoCode.DAO_PKG_NAME}.dto.${className}Dto
        import org.apache.ibatis.annotations.Param
        import org.springframework.stereotype.Service

        /**
         * [${table.comment}]数据操作Dao
         */
        @Service
        interface ${className}Dao {
        
            /**
             * 添加一条数据
             * @param dto 要插入的Dto
             */
            fun add(dto: ${className}Dto?)

            /**
             * 通过id获取一条数据
             * @param id 要查询的ID
             */
            fun selectOne(id: Int): ${className}Dto?

            /**
             * 更新发送状态
             *
             * @param dto 要更新的Dto
             */
            fun update(dto: ${className}Dto)

            /**
             * 通过id删除一条数据
             * @param id 要删除的ID
             */
            fun delete(id: Int)
        }
    """.trimIndent()
        FileUtil.savaStr(filename, daoCode)
        println(filename)
    }
}