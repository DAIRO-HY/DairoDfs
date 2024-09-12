package cn.dairo.dfs.sync.sync_handle

import com.fasterxml.jackson.databind.node.ObjectNode
import cn.dairo.dfs.boot.Boot
import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.dao.LocalFileDao
import cn.dairo.dfs.extension.bean
import cn.dairo.dfs.extension.md5
import cn.dairo.dfs.sync.SyncFileUtil
import cn.dairo.dfs.sync.bean.SyncInfo
import cn.dairo.dfs.util.DfsFileUtil
import java.io.File

/**
 * 本地文件数据表同步操作
 */
object LocalFileSyncHandle {

    /**
     * 全量同步时的特殊处理
     */
    fun bySyncAll(info: SyncInfo, item: ObjectNode) {
        val md5 = item.path("md5").textValue()

        //从本地数据库查找该文件
        val existsLocalFile = LocalFileDao::class.bean.selectByFileMd5(md5)
        if (existsLocalFile == null) {
            val tmpFilePath = SyncFileUtil.download(info, md5)
            val tempFileMd5 = File(tmpFilePath).md5
            if (md5 != tempFileMd5) {
                throw RuntimeException("同步的文件数据不完整，目标文件MD5:$md5，实际文件MD5:$tempFileMd5")
            }
            val saveLocalPath = DfsFileUtil.localPath
            val saveLocalFile = File(saveLocalPath)

            //移动文件
            File(tmpFilePath).renameTo(saveLocalFile)
            item.put("path", saveLocalPath)
        } else {//本机存在同样的文件,将本地记录删除，然后改用主机端同步过来的id
            val id = item.path("id").longValue()

            //删除本地的数据
            Constant.dbService.exec("delete from local_file where id = ?", existsLocalFile.id)

            //更换ID
            Constant.dbService.exec("update dfs_file set localId = ? where localId = ?", id, existsLocalFile.id)
            item.put("path", existsLocalFile.path)
        }
    }

    /**
     * 日志同步时的特殊处理
     */
    fun bySyncLog(info: SyncInfo, params: ArrayList<Any>) {

        //得到文件的md5
        val md5 = params[2] as String

        //从本地数据库查找该文件
        val existsLocalFile = Boot.service.localFileDao.selectByFileMd5(md5)
        if (existsLocalFile == null) {
            val tmpFilePath = SyncFileUtil.download(info, md5)
            val tempFileMd5 = File(tmpFilePath).md5
            if (md5 != tempFileMd5) {
                throw RuntimeException("同步的文件数据不完整，目标文件MD5:$md5，实际文件MD5:$tempFileMd5")
            }
            val saveLocalPath = DfsFileUtil.localPath
            val saveLocalFile = File(saveLocalPath)

            //移动文件
            File(tmpFilePath).renameTo(saveLocalFile)
            params[1] = saveLocalPath
        } else {//本机存在同样的文件,直接使用
            val id = params[0] as Long

            //删除本地的数据
            Constant.dbService.exec("delete from local_file where id = ?", existsLocalFile.id)

            //更换ID
            Constant.dbService.exec("update dfs_file set localId = ? where localId = ?", id, existsLocalFile.id)
            params[1] = existsLocalFile.path!!
        }
    }
}