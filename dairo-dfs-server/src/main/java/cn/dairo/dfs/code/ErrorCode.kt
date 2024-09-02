package cn.dairo.dfs.code

import cn.dairo.dfs.exception.BusinessException

/**
 * 业务错误消息配置
 */
object ErrorCode {
    val FAIL get() = BusinessException(1, "操作失败")
    val EXISTS_NAME get() = BusinessException(1, "该用户名已被注册")
    val EXISTS_EMAIL get() = BusinessException(1, "该邮箱已被其他用户注册")
    val PARAM_ERROR get() = BusinessException(2, "参数错误")
    val SYSTEM_ERROR get() = BusinessException(3, "系统错误,请查看错误日志")
    val SYSTEM_ERROR_NO_LOG get() = BusinessException(3, "系统错误,日志未记录")
    val NOT_ALLOW get() = BusinessException(4, "非法操作")
    val NO_LOGIN get() = BusinessException(5, "没有登录")
    val EXISTS_FILE get() = BusinessException(1001, "文件已存在")
    val NO_FOLDER get() = BusinessException(1002, "文件夹不存在")
    val EXISTS get() = BusinessException(1003, "文件或文件夹已存在")
    val NO_EXISTS get() = BusinessException(1004, "文件夹或文件不存在")
    val FILE_UPLOADING get() = BusinessException(1005, "文件服务繁忙，请稍后重试。")
    val SHARE_NOT_FOUND get() = BusinessException(2001, "分享链接不存在")
    val SHARE_IS_END get() = BusinessException(2002, "分享已过期")
    val SHARE_NEED_PWD get() = BusinessException(2003, "需要提取码")
}
