package cn.dairo.dfs.dao

import cn.dairo.dfs.dao.dto.DfsFileDto
import org.apache.ibatis.annotations.Param
import org.springframework.stereotype.Service

@Service
interface DfsFileDeleteDao {

    /**
     * 添加一条数据
     */
    fun insert(id: Long)

    /**
     * 设置删除时间
     * @param id 文件ID
     * @param time 时间戳
     */
    fun setDeleteDate(id: Long, time: Long)

    /**
     * 获取所有超时的数据
     * @param time 时间戳
     */
    fun selectIdsByTimeout(time: Long): List<DfsFileDto>

    /**
     * 获取所有超时的数据
     */
    fun delete(ids: String)

    /**
     * 文件是否正在使用中
     * @param id 本地文件id
     */
    fun isFileUsing(id: Long): Boolean
}
