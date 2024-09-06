package cn.dairo.dfs.service

import cn.dairo.dfs.code.ErrorCode
import cn.dairo.dfs.dao.DfsFileDao
import cn.dairo.dfs.dao.LocalFileDao
import cn.dairo.dfs.dao.dto.DfsFileDto
import cn.dairo.dfs.exception.BusinessException
import cn.dairo.dfs.extension.isFolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File

/**
 * 文件操作Service
 */
@Service
class DfsFileDeleteService {

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
     * @param userId 用户ID
     * @param ids 要删除的文件ID
     */
    fun delete(userId: Long, ids: List<Long>) {
        ids.forEach {
            val fileDto = this.dfsFileDao.getOne(it)!!
            if (fileDto.userId != userId) {//非自己的文件，无法删除
                throw ErrorCode.NOT_ALLOW
            }
            if (fileDto.deleteDate == null) {//该文件未标记为删除
                throw ErrorCode.NOT_ALLOW
            }
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

        //获取附属文件
        val extraList = this.dfsFileDao.selectExtraListById(fileDto.id!!)
        extraList.forEach {//删除文件所有附属文件
            this.delete(it)
        }
        this.delete(fileDto)
    }

    /**
     * 彻底删除文件
     * @TODO: 这里不应该立即删除文件，应该先用一个字段标记，过一段时间再彻底删除，防止误操作还有挽回的余地
     */
    private fun delete(fileDto: DfsFileDto) {
        val localDto = this.localFileDao.selectOne(fileDto.localId!!) ?: return
        if (!File(localDto.path!!).delete()) {//文件删除失败，可能文件正在被使用
            throw BusinessException("文件[${localDto.path}]删除失败")
        }

        //删除本地文件表数据
        this.localFileDao.delete(localDto.id!!)
        this.dfsFileDao.deleteByFile(fileDto.id!!)
    }
}
