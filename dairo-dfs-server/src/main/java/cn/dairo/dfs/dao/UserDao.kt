package cn.dairo.dfs.dao

import cn.dairo.dfs.dao.dto.UserDto
import org.springframework.stereotype.Service

@Service
interface UserDao {

    /**
     * 添加一条数据
     * @param dto 用户信息
     */
    fun add(dto: UserDto)

    /**
     * 通过id获取一条数据
     * @param id 用户ID
     * @return 用户信息
     */
    fun getOne(id: Long): UserDto?

    /**
     * 获取管理员账户
     * @return 用户信息
     */
    fun getAdmin(): UserDto?

    /**
     * 通过邮箱获取用户信息
     * @param email 邮箱
     * @return 用户信息
     */
    fun getByEmail(email: String): UserDto?

    /**
     * 通过用户名获取用户信息
     * @param name 用户名
     * @return 用户信息
     */
    fun getByName(name: String): UserDto?

    /**
     * 通过ApiToken获取用户信息
     * @param apiToken 用户ApiToken
     * @return 用户信息
     */
    fun getByApiToken(apiToken: String): UserDto?

    /**
     * 通过ApiToken获取用户ID
     * @param apiToken 用户ApiToken
     * @return 用户ID
     */
    fun getIdByApiToken(apiToken: String): Long?

    /**
     * 通过Token获取用户信息
     * @param token 用户登录token
     * @return 用户信息
     */
    fun getByToken(token: String): UserDto?

    /**
     * 通过urlPath获取用户ID
     * @param urlPath 文件访问前缀
     * @return 用户ID
     */
    fun getIdByUrlPath(urlPath: String): Long?

    /**
     * 获取所有用户
     * @return 所有用户列表
     */
    fun getAll(): List<UserDto>

    /**
     * 判断是否已经初始化
     */
    fun isInit(): Boolean

    /**
     * 更新用户信息
     * @param dto 用户信息
     */
    fun update(dto: UserDto)

    /**
     * 设置URL路径前缀
     * @param id 用户ID
     * @param urlPath URL路径前缀
     */
    fun setUrlPath(id: Long, urlPath: String?)

    /**
     * 设置API票据
     * @param id 用户ID
     * @param apiToken URL路径前缀
     */
    fun setApiToken(id: Long, apiToken: String?)

    /**
     * 设置端对端加密
     * @param id 用户ID
     * @param encryptionKey URL路径前缀
     */
    fun setEncryptionKey(id: Long, encryptionKey: String?)

    /**
     * 设置密码
     * @param id 用户ID
     * @param pwd 密码
     */
    fun setPwd(id: Long, pwd: String)
}
