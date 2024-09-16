package cn.dairo.dfs.interceptor

import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.dao.UserDao
import cn.dairo.dfs.dao.UserTokenDao
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

/**
 * @author Badboy
 * 后台管理员权限验证拦截器
 */
class ShareInterceptor : HandlerInterceptor {

    /**
     * 用户登录票据
     */
    @Autowired
    private lateinit var userTokenDao: UserTokenDao

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) {//不是mapping内容,没必要继续执行
            return false
        }
        var token = request.getParameter("_token")
        if (token == null) {//判断cookie中是否有值
            token = request.cookies?.find { it.name == "token" }?.value
        }
        if (token != null) {
            val userId = this.userTokenDao.getByUserIdByToken(token)
            if (userId != null) {
                request.setAttribute(Constant.REQUEST_USER_ID, userId)
            }
        }
        return true
    }
}
