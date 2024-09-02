package cn.dairo.dfs.dao

import cn.dairo.dfs.dao.dto.DfsFileDto
import org.apache.ibatis.annotations.Param
import org.springframework.stereotype.Service

@Service
interface DfsFileDao {

    /**
     * 添加一条数据
     */
    fun add(dto: DfsFileDto)

    /**
     * 通过id获取一条数据
     * @param id 文件ID
     */
    fun getOne(id: Long): DfsFileDto?

    /**
     * 通过文件夹ID和文件名获取文件信息
     * @param parentId 文件夹ID
     * @param name 文件名
     * @return 文件信息
     */
    fun getByParentIdAndName(
        @Param("userId") userId: Long,
        @Param("parentId") parentId: Long,
        @Param("name") name: String
    ): DfsFileDto?

    /**
     * 通过上级Id和文件名列表获取文件列表
     * @param parentId 文件夹ID
     * @param names 文件名列表
     * @return 文件列表
     */
    fun getByParentIdAndNames(
        @Param("userId") userId: Long,
        @Param("parentId") parentId: Long,
        @Param("names") names: List<String>
    ): List<DfsFileDto>

    /**
     * 通过文件夹ID和文件名获取文件Id
     * @param parentId 文件夹ID
     * @param name 文件名
     * @return 文件信息
     */
    fun getIdByParentIdAndName(
        @Param("userId") userId: Long,
        @Param("parentId") parentId: Long,
        @Param("name") name: String
    ): Long?

    /**
     * 通过路径获取文件ID
     * @param names 文件名列表
     * @return 文件ID
     */
    fun getIdByPath(@Param("userId") userId: Long, @Param("names") names: List<String>): Long?

    /**
     * 获取子文件数量
     * @param parentId 文件夹id
     * @return 子文件数量
     */
    fun getSubFileCount(@Param("userId") userId: Long, @Param("parentId") parentId: Long): Int

    /**
     * 获取子文件id和文件名
     * @param parentId 文件夹id
     * @return 子文件列表
     */
    fun getSubFileIdAndName(@Param("userId") userId: Long, @Param("parentId") parentId: Long): List<DfsFileDto>

    /**
     * 获取子文件信息,客户端显示用
     * @param parentId 文件夹id
     * @return 子文件列表
     */
    fun getSubFile(@Param("userId") userId: Long, @Param("parentId") parentId: Long): List<DfsFileDto>

    /**
     * 获取子文件ID(物理删除用)
     * @param parentId 文件夹id
     * @return 子文件ID列表
     */
    fun getSubIdListToLogicDelete(@Param("userId") userId: Long, @Param("parentId") parentId: Long): List<Long>

    /**
     * 获取全部已经删除的文件
     * @param userId 用户ID
     * @return 已删除的文件
     */
    fun getDeleteList(userId: Long): List<DfsFileDto>

    /**
     * 获取文件历史版本
     * @param userId 用户ID
     * @param id 文件id
     * @return 历史版本列表
     */
    fun getHistory(@Param("userId") userId: Long, @Param("id") id: Long): List<DfsFileDto>

    /**
     * 获取尚未处理的数据
     */
    fun selectNoHandle(): List<DfsFileDto>

    /**
     * 将文件标记为历史版本
     * @param id 文件ID
     */
    fun setHistory(id: Long)

    /**
     * 将文件标记为删除
     * @param id 文件ID
     * @param time 时间戳
     */
    fun setDelete(id: Long, time: Long)

    /**
     * 将标记为删除文件还原
     * @param id 文件ID
     */
    fun setNotDelete(id: Long)

    /**
     * 修改文件类型
     * @param id 文件ID
     */
    fun setContentType(@Param("id") id: Long, @Param("contentType") contentType: String)

    /**
     * 彻底删除文件(适用于删除文件夹下所有的文件)
     * @param id 文件ID
     */
    fun logicDelete(id: Long)

    /**
     * 删除文件及文件所有历史版本(适用于删除单的文件)
     * @param id 文件ID
     */
    fun logicDeleteFile(id: Long)

    /**
     * 文件移动
     * @param dto 移动文件信息
     */
    fun move(dto: DfsFileDto)

    /**
     * 设置文件缩略图
     */
    fun setThumb(@Param("id") id: Long, @Param("thumbLocalId") localId: Long)

    /**
     * 设置文件属性
     */
    fun setProperty(@Param("id") id: Long, @Param("property") property: String)

    /**
     * 设置文件处理状态
     */
    fun setState(@Param("id") id: Long, @Param("state") state: Byte, @Param("stateMsg") stateMsg: String?)

    /**
     * 验证文件存储ID权限
     */
    fun validLocalId(userId: Long, localId: Long): Boolean

    /**
     * 获取附属文件
     * @param parentId dfs文件ID
     * @param name 附属文件标题
     * @return 附属文件信息
     */
    fun selectExtra(@Param("parentId") parentId: Long, @Param("name") name: String): DfsFileDto?

    /**
     * 获取扩展文件的所有key值
     * @param id dfs文件ID
     * @return 附属文件信息
     */
    fun selectExtraNames(id: Long): List<String>

    /**
     * 通过本地存储ID查询缩略图
     * @param localId 本地存储id
     * @return 缩略图
     */
    fun selectThumbByLocalId(localId: Long): Long?

    /**
     * 通过本地存储ID查询文件属性
     * @param localId 本地存储id
     * @return 属性
     */
    fun selectPropertyByLocalId(localId: Long): String?

    /**
     * 通过本地存储ID查询文件附属文件
     * @param localId 本地存储id
     * @return 属性
     */
    fun selectExtraFileByLocalId(localId: Long): List<DfsFileDto>
}
