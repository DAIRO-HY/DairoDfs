package cn.dairo.dfs.exception

import cn.dairo.dfs.code.ErrorCode

//程序逻辑异常,用来区分系统异常时使用
class BusinessException : RuntimeException {
    var code = -1
    var data: Any? = null

    constructor()
    constructor(msg: String) : super(msg)
    constructor(code: Int, msg: String, data: Any? = null) : super(msg) {
        this.code = code
        this.data = data
    }

    companion object {
        fun fail(msg: String, data: Any? = null): BusinessException {
            return BusinessException(1, msg, data)
        }

        /**
         * 添加但项目验证失败消息
         */
        fun addFieldError(field: String, msg: String): BusinessException {
            val error = ErrorCode.PARAM_ERROR
            error.data = mapOf(field to listOf(msg))
            return error
        }
    }
}