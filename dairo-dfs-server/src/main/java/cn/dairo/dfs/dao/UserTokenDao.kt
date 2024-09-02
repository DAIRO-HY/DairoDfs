package cn.dairo.dfs.dao

import cn.dairo.dfs.dao.dto.UserTokenDto
import org.apache.ibatis.annotations.Param
import org.springframework.stereotype.Service

@Service
interface UserTokenDao {

    /**
     * 添加一条数据
     * @param dto 用户信息
     */
    fun add(dto: UserTokenDto)

    /**
     * 通过登录Token获取会员ID
     * @param token 登录Token
     */
    fun getByUserIdByToken(token: String): Long?

    /**
     * 获取某个用户的登录记录
     * @param userId 用户ID
     */
    fun listByUserId(userId: Long): List<UserTokenDto>

    /**
     * 更新会员登录记录
     * @param dto 用户信息
     */
    fun update(dto: UserTokenDto)

    /**
     * 通过会员ID和客户端标识删除一条记录
     * @param userId 用户ID
     * @param clientFlag 客户端标志
     */
    fun deleteByUserIdAndClientFlag(userId: Long, clientFlag: Int)

    /**
     * 通过会员ID和客户端标识删除一条记录
     * @param userId 用户ID
     * @param deviceId 设备唯一标识
     */
    fun deleteByUserIdAndDeviceId(userId: Long, deviceId: String)

    /**
     * 删除某个会员的所有登录token
     * @param userId 用户ID
     */
    fun deleteByUserId(userId: Long)

    /**
     * 通过token删除
     * @param token 用户登录票据
     */
    fun deleteByToken(token: String)
}
