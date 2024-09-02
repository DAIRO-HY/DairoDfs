package cn.dairo.dfs.controller.app.sync.form

import cn.dairo.lib.uc.auto.annotations.UCTable

/**
 * sql数据库日志
 */
@UCTable("sql_log")
class SqlLogErrorForm {

    /**
     * 主键
     */
    var id: Long? = null

    /**
     * 日志时间
     */
    var date: Long? = null

    /**
     * sql文
     */
    var sql: String? = null

    /**
     * 参数Json
     */
    var param: String? = null

    /**
     * 状态 0：待执行 1：执行完成 2：执行失败
     */
    var state: Int? = null

    /**
     * 日志来源IP
     */
    var source: String? = null

    /**
     * 错误消息
     */
    var err: String? = null
}
