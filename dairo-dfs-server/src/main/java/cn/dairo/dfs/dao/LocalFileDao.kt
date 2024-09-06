package cn.dairo.dfs.dao

import cn.dairo.dfs.dao.dto.LocalFileDto
import org.springframework.stereotype.Service

@Service
interface LocalFileDao {

    /**
     * 添加一条数据
     */
    fun add(dto: LocalFileDto)

    /**
     * 通过id获取一条数据
     * @param id 文件ID
     */
    fun selectOne(id: Long): LocalFileDto?

    /**
     * 通过文件MD5获取一条数据
     * @param md5 文件MD5
     */
    fun selectByFileMd5(md5: String): LocalFileDto?

    /**
     * 通过id删除一条数据
     */
    fun delete(id: Long)
}
