package cn.dairo.lib.uc.auto.page

import cn.dairo.lib.uc.auto.bean.UCFormBean
import cn.dairo.lib.uc.auto.bean.UCColumnBean

/**
 * 分页输出数据
 */
class PageDataEntity {

    /**
     * 当前页码
     */
    var page = 1

    /**
     * 每页显示的条数
     */
    var pageSize = 10

    /**
     * 数据总条数
     */
    var itemCount = 0L

    /**
     * 总页数
     */
    val pageCount: Long
        get() {
            return (this.itemCount + this.pageSize - 1) / this.pageSize
        }

    /**
     * 数据
     */
    lateinit var data: List<Any>

    /**
     * 表头数据
     */
    var columns: List<UCColumnBean>? = null

    /**
     * 搜索表单字段
     */
    var searchFields: List<UCFormBean>? = null
}
