package cn.dairo.dfs.interceptor

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
        val isAdmin = request.getAttribute(Constant.REQUEST_IS_ADMIN) as Boolean
        if (isAdmin) {//验证是否管理员
            return true
        }
        response.status = 500

        //设置contentType
        response.contentType = "text/json;charset=utf-8"

        val bizError = ErrorCode.NOT_ALLOW
        response.outputStream.write("""{"code":${bizError.code},"msg":"${bizError.message}"}""".toByteArray())
        return false
    }
}
