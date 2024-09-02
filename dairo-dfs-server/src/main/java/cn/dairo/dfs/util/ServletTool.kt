package cn.dairo.dfs.util

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

//servlet工具类
object ServletTool {

    //获取request
    val request
        get() = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request

    //获取response
    val response
        get() = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).response!!

    //获取context
    val context
        get() = request.servletContext

    //获取session
    val session
        get() = request.session

    /**
     * 获取客户端ip
     */
    fun getClientIp(): String? {
        val request = this.request
        var ip = request.getHeader("x-forwarded-for")
        if (ip.isNullOrEmpty()) {
            ip = request.getHeader("Proxy-Client-IP")
        }
        if (ip.isNullOrEmpty()) {
            ip = request.getHeader("WL-Proxy-Client-IP")
        }
        if (ip.isNullOrEmpty()) {
            ip = request.getHeader("HTTP_CLIENT_IP")
        }
        if (ip.isNullOrEmpty()) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR")
        }
        if (ip.isNullOrEmpty()) {
            ip = request.remoteAddr
        }
        return ip
    }
}