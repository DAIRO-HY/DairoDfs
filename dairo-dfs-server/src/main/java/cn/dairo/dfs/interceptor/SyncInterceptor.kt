package cn.dairo.dfs.interceptor

import cn.dairo.dfs.config.SystemConfig
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

/**
 * @author Badboy
 * 后台管理员权限验证拦截器
 */
class SyncInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) {//不是mapping内容,没必要继续执行
            return false
        }
        var token = request.servletPath.split("/")[2]
        if (token.length >= 32) {
            token = token.substring(0, 32)
        } else {
            token = ""
        }
        if (token != SystemConfig.instance.token) {//token验证失败
            response.status = 500

            //设置contentType
            response.contentType = "text/plain;charset=utf-8"
            response.outputStream.write("token验证失败".toByteArray())
            return false
        }
        return true
    }
}
