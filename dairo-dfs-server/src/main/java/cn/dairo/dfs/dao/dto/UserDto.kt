package cn.dairo.dfs.dao.dto

import java.util.*

class UserDto {

    /**
     * 主键
     */
    var id: Long? = null

    /**
     * 用户名
     */
    var name: String? = null

    /**
     * 登陆密码
     */
    var pwd: String? = null

    /**
     * 用户电子邮箱
     */
    var email: String? = null

    /**
     * 用户文件访问路径前缀
     */
    var urlPath: String? = null

    /**
     * API操作TOKEN
     */
    var apiToken: String? = null

    /**
     * 端对端加密密钥
     */
    var encryptionKey: String? = null

    /**
     * 用户状态
     */
    var state: Int? = null

    /**
     * 创建日期
     */
    var date: Date? = null
}
