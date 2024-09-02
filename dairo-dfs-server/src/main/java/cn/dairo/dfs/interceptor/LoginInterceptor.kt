package cn.dairo.dfs.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import cn.dairo.dfs.code.ErrorCode
import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.dao.UserDao
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

/**
 * @author Badboy
 * 后台管理员权限验证拦截器
 */
class LoginInterceptor : HandlerInterceptor {

    private val mapper = ObjectMapper()

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) {//不是mapping内容,没必要继续执行
            return false
        }
        var token = request.getParameter("_token")
        if (token == null) {
            token = request.session.getAttribute(Constant.SESSION_TOKEN) as String?
        }
        if (token != null) {
            request.setAttribute(Constant.REQUEST_TOKEN, token)
            return true
        }
        if (request.method == HttpMethod.POST.name()) {//Post请求时
            response.status = 500

            //设置contentType
            response.contentType = "text/json;charset=utf-8"

            val bizError = ErrorCode.NO_LOGIN
            val result = mapOf("code" to bizError.code, "msg" to bizError.message)
            this.mapper.writeValue(response.outputStream, result)
        } else {
            if (request.getHeader("range") != null) {//可能来自客户端下载
                response.status = 500
            } else {
                response.sendRedirect("/app/login")
            }
        }
        return false
    }
}
