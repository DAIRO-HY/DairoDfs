package cn.dairo.dfs.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor


/**
 * 最顶层拦截器
 *
 * @author Badboy 默认拦截器
 */
class DefaultInterceptor : HandlerInterceptor {

    /**
     * 数据存放文件夹
     */
    @Value("\${data.path}")
    lateinit var dataPath: String

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        // 此处配置的是允许任意域名跨域请求，可根据需求指定
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("origin"))
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS")
        response.setHeader("Access-Control-Allow-Headers", "*")

        // 如果是OPTIONS则结束请求
        // 跨域请求时用到
        if (HttpMethod.OPTIONS.toString() == request.method) {
            response.status = HttpStatus.NO_CONTENT.value()
            return false
        }

        if (handler !is HandlerMethod) {//不是mapping内容,没必要继续执行
            response.status = 404
            //response.sendRedirect("/404.html")
            return false
        }

        return true
    }
}