package cn.dairo.dfs.dao

import cn.dairo.dfs.dao.dto.DfsFileDto
import cn.dairo.dfs.dao.dto.DfsFileThumbDto
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
    fun selectOne(id: Long): DfsFileDto?

    /**
     * 通过文件夹ID和文件名获取文件信息
     * @param parentId 文件夹ID
     * @param name 文件名
     * @return 文件信息
     */
    fun selectByParentIdAndName(
        @Param("userId") userId: Long,
        @Param("parentId") parentId: Long,
        @Param("name") name: String
    ): DfsFileDto?

    /**
     * 通过文件夹ID和文件名获取文件Id
     * @param parentId 文件夹ID
     * @param name 文件名
     * @return 文件信息
     */
    fun selectIdByParentIdAndName(
        @Param("userId") userId: Long,
        @Param("parentId") parentId: Long,
        @Param("name") name: String
    ): Long?

    /**
     * 通过路径获取文件ID
     * @param names 文件名列表
     * @return 文件ID
     */
    fun selectIdByPath(@Param("userId") userId: Long, @Param("names") names: List<String>): Long?

    /**
     * 获取子文件id和文件名
     * @param parentId 文件夹id
     * @return 子文件列表
     */
    fun selectSubFileIdAndName(@Param("userId") userId: Long, @Param("parentId") parentId: Long): List<DfsFileDto>

    /**
     * 获取子文件信息,客户端显示用
     * @param parentId 文件夹id
     * @return 子文件列表
     */
    fun selectSubFile(@Param("userId") userId: Long, @Param("parentId") parentId: Long): List<DfsFileThumbDto>

    /**
     * 获取全部已经删除的文件
     * @param userId 用户ID
     * @return 已删除的文件
     */
    fun selectDelete(userId: Long): List<DfsFileThumbDto>

    /**
     * 获取所有回收站超时的数据
     * @return 已删除的文件
     */
    fun selectIdsByDeleteAndTimeout(time: Long): List<Long>

    /**
     * 获取文件历史版本
     * @param userId 用户ID
     * @param id 文件id
     * @return 历史版本列表
     */
    fun selectHistory(@Param("userId") userId: Long, @Param("id") id: Long): List<DfsFileDto>

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
     * 删除
     * @param id 文件ID
     */
    fun delete(id: Long)

    /**
     * 文件移动
     * @param dto 移动文件信息
     */
    fun move(dto: DfsFileDto)

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
     * 通过本地存储ID查询文件属性
     * @param localId 本地存储id
     * @return 属性
     */
    fun selectPropertyByLocalId(localId: Long): String?

    /**
     * 通过本地存储ID查询文件附属文件
     * @param localId 本地存储id
     * @return 附属文件列表
     */
    fun selectExtraFileByLocalId(localId: Long): List<DfsFileDto>

    /**
     * 获取某个文件附属文件
     * @param id 文件id
     * @return 附属文件列表
     */
    fun selectExtraListById(id: Long): List<DfsFileDto>

    /**
     * 获取某个文件夹下的所有文件及文件夹，包括历史文件，已删除文件
     * @param id 文件id
     * @return 文件夹下的所有文件及文件夹，包括历史文件，已删除文件
     */
    fun selectAllChildList(id: Long): List<DfsFileDto>

    /**
     * 文件是否正在使用中
     * @param id 本地文件id
     */
    fun isFileUsing(id: Long): Boolean
}
