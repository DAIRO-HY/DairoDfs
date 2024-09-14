package cn.dairo.dfs.controller.base

import cn.dairo.dfs.code.ErrorCode
import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.dao.UserTokenDao
import cn.dairo.dfs.util.ServletTool
import org.springframework.beans.factory.annotation.Autowired

/**
 * Web端Controller基类
 */
open class AppBase : AjaxBase() {

    /**
     * 当前登录用户ID
     */
    val loginId: Long
        get() = ServletTool.request.getAttribute(Constant.REQUEST_USER_ID) as Long

//
//    /**
//     * 是否管理员
//     */
//    val isAdmin: Boolean
//        get() = ServletTool.session.getAttribute(Constant.SESSION_IS_ADMIN) as Boolean


//    /**
//     * 当前登录用户ID(测试用)
//     */
//    val loginId: Long
//        get() = UserDao::class.bean.getAdmin()!!.id!!
//
//    /**
//     * 是否管理员(测试用)
//     */
//    val isAdmin: Boolean
//        get() = true
}