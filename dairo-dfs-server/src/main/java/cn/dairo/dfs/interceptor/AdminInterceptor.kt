package cn.dairo.dfs.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import cn.dairo.dfs.code.ErrorCode
import cn.dairo.dfs.config.Constant
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor

/**
 * @author Badboy
 * 后台管理员权限验证拦截器
 */
class AdminInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.session.getAttribute(Constant.SESSION_IS_ADMIN) as Boolean) {
            return true
        }
        response.status = 500
        //设置contentType
        response.contentType = "text/json;charset=utf-8"

        val bizError = ErrorCode.NOT_ALLOW
        val result = mapOf("code" to bizError.code, "msg" to bizError.message)
        ObjectMapper().writeValue(response.outputStream, result)

        return false
    }
}
