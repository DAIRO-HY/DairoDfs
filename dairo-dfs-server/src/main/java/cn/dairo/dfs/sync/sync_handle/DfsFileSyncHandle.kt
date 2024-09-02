package cn.dairo.dfs.sync.sync_handle

import com.fasterxml.jackson.databind.node.ObjectNode
import cn.dairo.dfs.boot.Boot
import cn.dairo.dfs.config.Constant

/**
 * 用户文件数据表同步操作
 */
object DfsFileSyncHandle {
    fun handle(item: ObjectNode) {
        val deleteDate = item.path("deleteDate").asLong()
        if (deleteDate > 0) {//该文件已经被删除，不用做任何处理
            return
        }
        val isHistory = item.path("isHistory").asInt()
        if (isHistory == 1) {//这是一个历史文件
            return
        }

        //用户文件id
        val id = item.path("id").asLong()

        //用户id
        val userId = item.path("userId").asLong()

        //父级文件夹id
        val parentId = item.path("parentId").asLong()

        //文件（夹）名
        val name = item.path("name").asText()

        //本地存储文件id
        val localId = item.path("localId").asLong()
        val dfsFile = Boot.service.dfsFileDao.getByParentIdAndName(userId, parentId, name)
        if (dfsFile == null) {//文件不存在时，不做任何处理
            return
        }
        if (localId == 0L && dfsFile.localId == 0L) {//如果都是文件夹，则将本地的文件转移到同步主机的文件夹下，本删除本地问文件夹
            Constant.dbService.exec("update dfs_file set parentId = ? where parentId = ?", localId, dfsFile.id)
            Constant.dbService.exec("delete from dfs_file where id = ?", dfsFile.id)
        } else if (localId > 0 && dfsFile.localId!! > 0) {//如果都是文件,则保留最新的一个文件
            if (id > dfsFile.id!!) {//当前同步到的文件比较新，则将本地的文件设置为历史文件
                Constant.dbService.exec("update dfs_file set isHistory = 1 where id = ?", localId, dfsFile.id)
            } else {//本地的文件比较新，则将当前同步到的文件设置为历史文件
                item.put("isHistory", 1)
            }
        } else {
            throw RuntimeException("同步失败，同名文件或文件夹已经存在")
        }
    }
}