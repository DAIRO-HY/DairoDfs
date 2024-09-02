package cn.dairo.dfs.dao

import cn.dairo.dfs.dao.dto.ShareDto
import org.apache.ibatis.annotations.Param
import org.springframework.stereotype.Service

@Service
interface ShareDao {

    /**
     * 添加一条数据
     */
    fun add(dto: ShareDto)

    /**
     * 通过ID获取一条数据
     */
    fun getOne(id: String): ShareDto?

    /**
     * 获取所有分享列表
     */
    fun getByUser(userId: Long): List<ShareDto>

    /**
     * 删除分享
     * @param userId 用户ID
     * @param ids 要删除的分享id列表
     */
    fun delete(@Param("userId") userId: Long, @Param("ids") ids: List<Long>)
}
