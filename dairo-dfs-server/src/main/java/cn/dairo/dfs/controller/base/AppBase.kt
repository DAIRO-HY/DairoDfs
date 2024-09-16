package cn.dairo.dfs.controller.base

import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.util.ServletTool

/**
 * Web端Controller基类
 */
open class AppBase : AjaxBase() {

    /**
     * 当前登录用户ID
     */
    val loginId: Long
        get() = ServletTool.request.getAttribute(Constant.REQUEST_USER_ID) as Long
}