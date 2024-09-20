package cn.dairo.dfs.interceptor

import cn.dairo.dfs.code.ErrorCode
import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.dao.UserDao
import cn.dairo.dfs.dao.UserTokenDao
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

    /**
     * 用户登录票据
     */
    @Autowired
    private lateinit var userTokenDao: UserTokenDao

    /**
     * 用户Dao
     */
    @Autowired
    private lateinit var userDao: UserDao

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) {//不是mapping内容,没必要继续执行
            return false
        }

        //获取APP登录票据
        var token = request.getParameter("_token")
        if (token == null) {//判断cookie中是否有值
            token = request.cookies?.find { it.name == "token" }?.value
        }
        var userId: Long? = null
        if (token != null) {//如果APP或网页token不为空
            userId = this.userTokenDao.getByUserIdByToken(token)
        }
        if (userId == null) {//尝试通过ApiToken获取
            val apiToken = request.getParameter("api_token")
            if (apiToken != null) {
                userId = this.userDao.selectIdByApiToken(apiToken)
            }
        }
        if (userId != null) {//用户登录成功
            request.setAttribute(Constant.REQUEST_USER_ID, userId)

            //验证是否管理员
            val isAdmin = userId == this.userDao.selectAdminId()
            request.setAttribute(Constant.REQUEST_IS_ADMIN, isAdmin)
            return true
        }
        if (request.method == HttpMethod.POST.name()) {//Post请求时
            response.status = 500

            //设置contentType
            response.contentType = "text/json;charset=utf-8"

            val bizError = ErrorCode.NO_LOGIN
            response.outputStream.write("""{"code":${bizError.code},"msg":"${bizError.message}"}""".toByteArray())
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
