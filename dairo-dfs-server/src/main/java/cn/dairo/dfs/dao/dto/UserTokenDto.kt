package cn.dairo.dfs.dao.dto

import java.util.*

class UserTokenDto {

    /**
     * 主键
     */
    var id: Long? = null

    /**
     * 登录Token
     */
    var token: String? = null

    /**
     * 用户ID
     */
    var userId: Long? = null

    /**
     * 客户端标识  0:WEB 1：Android  2：IOS  3：WINDOWS 4:MAC 5:LINUX
     */
    var clientFlag: Int? = null

    /**
     * 设备唯一标识
     */
    var deviceId: String? = null

    /**
     * 客户端IP地址
     */
    var ip: String? = null

    /**
     * 创建日期
     */
    var date: Date? = null

    /**
     * 客户端版本
     */
    var version: Int? = null
}
