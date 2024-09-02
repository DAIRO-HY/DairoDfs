package cn.dairo.mysql.model.tool.make.demo

import cn.dairo.mysql.model.tool.cls.bean.TableBean
import cn.dairo.mysql.model.tool.cls.bean.TableField
import cn.dairo.mysql.model.tool.code.tool.FileUtil
import cn.dairo.mysql.model.tool.code.tool.Tool.toClassName
import java.io.File

/**
 * 生成Mybatis映射xml文件
 */
class MakeXml(private val table: TableBean, private val fields: List<TableField>) {
    private val DAO_XML =
        """<?xml version="1.0" encoding="UTF-8"?>
           <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
           <mapper namespace="{dao_name}">
               {content}
           </mapper>
""".trimIndent()

    fun start() {
        val daoFile = this.table.name.toClassName() + "Dao"
        val xalFn = MakeDaoCode.XML_PATH + daoFile + ".xml"
        if (File(xalFn).exists()) {
            return
        }
        val content = this.insert + this.selectOne + this.update + this.delete

        var xml = DAO_XML.replace("{dao_name}", MakeDaoCode.DAO_PKG_NAME + "." + daoFile)
        xml = xml.replace("{content}", content)
        FileUtil.savaStr(xalFn, xml)
        println(xalFn)
    }

    /**
     * 生成插入语句
     */
    private val insert: String
        get() {
            val keys = this.fields.map { it.name!! }.filter { it != "id" }
            val insertKeys = keys.joinToString { it }
            val insertParams = keys.joinToString("},#{", "#{", "}") {
                it
            }
            val insertDemo = """
                <!--添加-->
                <insert id="add" useGeneratedKeys="true" keyProperty="id">
                    insert into ${this.table.name}(${insertKeys}) values (${insertParams})
                </insert>
        """
            return insertDemo
        }

    /**
     * 生成查询语句
     */
    private val selectOne: String
        get() {
            val dto = this.table.name.toClassName() + "Dto"
            val selectOneDemo = """
                <!--通过ID获取一条数据-->
                <select id="selectOne" resultType="$dto">
                    select * from ${this.table.name} where id = #{0}
                </select>
        """
            return selectOneDemo
        }

    /**
     * 更新数据
     */
    private val update: String
        get() {
            val keys = this.fields.map { it.name }.filter { it != "id" }
            val updateStr = keys.joinToString { "$it=#{$it}" }
            val updateDemo = """
                <!--更新-->
                <update id="update">
                    update ${this.table.name} set ${updateStr} where id=#{id}
                </update>
        """
            return updateDemo
        }

    /**
     * 生成删除语句
     */
    private val delete: String
        get() {
            val selectOneDemo = """
                <!--通过ID删除一条数据-->
                <delete id="deleteOne">
                    delete from ${this.table.name} where id = #{0}
                </delete>
        """
            return selectOneDemo
        }
}