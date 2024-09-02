package cn.dairo.lib.uc.auto.bean

/**
 * 自定义分页列表标题数据
 */
class UCColumnBean {

    /**
     * 字段名
     */
    var key: String? = null

    /**
     * 显示标题
     */
    var title: String? = null

    /**
     * 是否允许排序
     */
    var orderable: Boolean? = null

    /**
     * 初始化排序方式 asc/desc
     */
    var defaultSort: String? = null

    /**
     * 显示类型
     * 参考 UCColumn中的静态变量
     */
    var type: String? = null
}
