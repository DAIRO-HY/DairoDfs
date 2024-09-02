package cn.dairo.dfs.dao

import cn.dairo.dfs.dao.dto.SqlLogDto
import org.springframework.stereotype.Service

@Service
interface SqlLogDao {

    /**
     * 获取错误的日志记录
     */
    fun getErrorLog(): SqlLogDto?
}
