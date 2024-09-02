package cn.dairo.mysql.model.tool.code.tool

import java.util.ArrayList
import java.util.HashMap

import cn.dairo.mysql.model.tool.cls.bean.ClassBean
import cn.dairo.mysql.model.tool.cls.bean.ClassParamBean
import cn.dairo.mysql.model.tool.cls.bean.TableField

/**
 * @author Long-PC 生成类
 */
object Tool {
    private val typeKey = HashMap<String, String>()

    init {
//        typeKey["tinyint(1)"] = "boolean"
        typeKey["tinyint(1)"] = "Int"
        typeKey["tinyint"] = "Int"
        typeKey["smallint"] = "Int"
        typeKey["int"] = "Int"
        typeKey["mediumint"] = "Int"
        typeKey["bigint"] = "Long"
        typeKey["varchar"] = "String"
        typeKey["mediumtext"] = "String"
        typeKey["longtext"] = "String"
        typeKey["text"] = "String"
        typeKey["char"] = "String"
        typeKey["enum"] = "String"
        typeKey["datetime"] = "Date"
        // typeKey.put("date", "Date");
        typeKey["time"] = "Date"
        // typeKey.put("double", "Double");
        typeKey["decimal"] = "Double"
        // typeKey.put("float", "Double");
    }

    /**
     * @作者 周龙权
     * @创建时间 2017年2月13日 下午10:11:31
     * @描述 添加类型匹配
     * @param key
     * @param value
     */
    fun addTypeKey(key: String, value: String) {
        typeKey[key] = value
    }

    /**
     * 通过表字段列表生成class需要的属性
     *
     * @param tpb
     * @return
     */
    fun makeJavaBeanClassByTableParams(tpb: List<TableField>): ClassBean {

        // 类名没有加任何前缀和后缀
        //		String cName = tableName2ClassName(cb.getTbName());
        var hasDate = false// 是否有日期类型

        // 导入包
        val importList = ArrayList<String>()

        val params = ArrayList<ClassParamBean>()
        for (item in tpb) {
            val _type = item.type!!
            var type: String? = null

            for (key in typeKey.keys) {
                if (_type.startsWith(key)) {
                    type = typeKey[key]
                }
            }

            if (type == null) {
                type = _type
            }
            if (type == "Date") {
                hasDate = true
            }
            val name = item.name!!
            if (name == "class" || name == "switch" || name == "if"
                || name == "continue" || name.startsWith("0")
                || name.startsWith("1") || name.startsWith("2")
                || name.startsWith("3") || name.startsWith("4")
                || name.startsWith("5") || name.startsWith("6")
                || name.startsWith("7") || name.startsWith("8")
                || name.startsWith("9")
            ) {
                item.name = "_$name"
            }

            val pb = ClassParamBean()
            pb.permission = ""
            pb.name = item.name
            pb.isPrimaryKey = item.isPrimaryKey
            pb.type = type
            pb.comment = item.comment

            params.add(pb)
        }

        if (hasDate) {
            importList.add("java.util.Date")
        }
        // 生成了一个完整的JavaBean
        val classBean = ClassBean()
        classBean.params = params
//        classBean.methods = mbList
        classBean.impoertList = importList

        return classBean
    }

    /**
     * 表名生成类名
     */
    fun String?.toClassName(): String {
        this ?: return ""
        var name = ""
        val tns = this.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (s in tns) {

            // 以—分开首字母大写
            name += StringBuilder().append(Character.toUpperCase(s[0])).append(s.substring(1)).toString()
        }
        return name
    }

    /**
     * 首字母小写
     *
     * @param Str
     * @return
     */
    fun firstToLower(Str: String): String {
        return StringBuilder().append(Character.toLowerCase(Str[0])).append(Str.substring(1)).toString()
    }
}
