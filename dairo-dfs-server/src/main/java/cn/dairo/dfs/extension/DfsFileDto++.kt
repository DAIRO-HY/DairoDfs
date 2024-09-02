package cn.dairo.dfs.extension

import cn.dairo.dfs.dao.dto.DfsFileDto
import java.math.BigInteger
import java.security.MessageDigest

/**
 * 是否文件
 */
val DfsFileDto.isFile: Boolean
    get() {
        val localId = this.localId ?: throw NullPointerException()
        return localId > 0
    }

/**
 * 是否文件夹
 */
val DfsFileDto.isFolder: Boolean
    get() {
        val localId = this.localId ?: throw NullPointerException()
        return localId == 0L
    }

