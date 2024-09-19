package cn.dairo.dfs.dao

import org.springframework.stereotype.Service

@Service
interface ShareFileDao {

    /**
     * 添加一条数据
     */
    fun batchAdd(insertKeysStr: String)

    /**
     * 获取分享文件中的第一个有缩略图的缩略图
     */
    fun selectFirstThumb(id: Long): Long?
}
