package cn.dairo.lib.uc.auto.page

/**
 * 分页请求表单基类
 */
open class PageBaseForm {

    /**
     * 排序
     */
    var orderBy: String? = null

    /**
     * 当前页码
     */
    var page = 1

    /**
     * 每页显示的条数
     */
    var pageSize = 10

    /**
     * 是否初始化
     */
    var init = true
}
