package cn.dairo.mysql.model.tool.cls.bean

/**
 * @author Long-PC 参数bean
 */
class TableField {

    /**
     * 参数名
     */
    var name: String? = null

    /**
     * 参数类型名
     */
    var type: String? = null

    /**
     * 默认值
     */
    var value: String? = null

    /**
     * 备注
     */
    var comment: String? = null

    /**
     * 是否主键
     */
    var isPrimaryKey = false
}
