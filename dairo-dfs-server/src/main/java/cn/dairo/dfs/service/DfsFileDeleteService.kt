package cn.dairo.dfs.service

import cn.dairo.dfs.code.ErrorCode
import cn.dairo.dfs.dao.DfsFileDao
import cn.dairo.dfs.dao.DfsFileDeleteDao
import cn.dairo.dfs.dao.LocalFileDao
import cn.dairo.dfs.dao.dto.DfsFileDto
import cn.dairo.dfs.extension.isFolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.io.File

/**
 * 文件操作Service
 */
@Service
class DfsFileDeleteService {

    /**
     * 文件删除数据操作Dao
     */
    @Autowired
    private lateinit var dfsFileDeleteDao: DfsFileDeleteDao

    /**
     * 文件数据操作Dao
     */
    @Autowired
    private lateinit var dfsFileDao: DfsFileDao

    /**
     * 本地文件数据操作Dao
     */
    @Autowired
    private lateinit var localFileDao: LocalFileDao

    /**
     * 彻底删除文件
     * @param ids 要删除的文件ID
     */
    fun addDelete(ids: List<Long>) {
        ids.forEach {
            val fileDto = this.dfsFileDao.getOne(it)!!
            if (fileDto.isFolder) {//如果是文件夹
                this.deleteFolder(fileDto)
            } else {
                //彻底删除文件
                this.deleteSelfAndExtra(fileDto)
            }
        }
    }

    /**
     * 递归删除文件夹所有类容
     * @param fileDto 要删除的文件
     */
    private fun deleteFolder(fileDto: DfsFileDto) {
        this.dfsFileDao.selectAllChildList(fileDto.id!!).forEach {
            if (it.isFolder) {
                this.deleteFolder(it)
            } else {
                this.deleteSelfAndExtra(it)
            }
        }

        //彻底删除文件夹
        this.dfsFileDao.deleteByFolder(fileDto.id!!)
    }

    /**
     * 删除文件本身和附属文件
     */
    private fun deleteSelfAndExtra(fileDto: DfsFileDto) {
        if (!fileDto.isExtra) {//如果这不是一个附属文件

            //获取附属文件
            val extraList = this.dfsFileDao.selectExtraListById(fileDto.id!!)
            extraList.forEach {//删除文件所有附属文件
                this.addDelete(it)
            }
        }
        this.addDelete(fileDto)
    }

    /**
     * 彻底删除文件
     */
    private fun addDelete(fileDto: DfsFileDto) {
        this.dfsFileDeleteDao.insert(fileDto.id!!)
        this.dfsFileDeleteDao.setDeleteDate(fileDto.id!!, System.currentTimeMillis())
        this.dfsFileDao.deleteByFile(fileDto.id!!)
    }

    /**
     * 彻底删除文件
     */
    fun deleteLocalFile(id: Long) {
        if (this.dfsFileDao.isFileUsing(id)) {//文件还在使用中
            return
        }
        if (this.dfsFileDeleteDao.isFileUsing(id)) {//文件还在使用中
            return
        }
        val localDto = this.localFileDao.selectOne(id) ?: return
        val file = File(localDto.path!!)
        if (file.exists()) {
            if (!file.delete()) {//文件删除不成功的话不做任何处理
                return
            }
        }

        //删除本地文件表数据
        this.localFileDao.delete(localDto.id!!)
    }
}
