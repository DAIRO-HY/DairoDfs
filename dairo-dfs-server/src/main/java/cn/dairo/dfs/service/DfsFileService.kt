package cn.dairo.dfs.service

import cn.dairo.dfs.code.ErrorCode
import cn.dairo.dfs.dao.DfsFileDao
import cn.dairo.dfs.dao.LocalFileDao
import cn.dairo.dfs.dao.dto.DfsFileDto
import cn.dairo.dfs.dao.dto.LocalFileDto
import cn.dairo.dfs.exception.BusinessException
import cn.dairo.dfs.extension.*
import cn.dairo.dfs.util.DBID
import cn.dairo.dfs.util.DfsFileUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

/**
 * 文件操作Service
 */
@Service
class DfsFileService {

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
     * 添加一个文件或文件夹
     */
    fun addFile(fileDto: DfsFileDto, isOverWrite: Boolean = false) = synchronized("" + fileDto.userId) {
        if (fileDto.localId == 0L) {
            throw BusinessException("本地存储文件ID不能为空")
        }
        val existDto = this.dfsFileDao.getByParentIdAndName(fileDto.userId!!, fileDto.parentId!!, fileDto.name!!)
        if (existDto != null) {
            if (existDto.isFolder) {
                throw BusinessException("已存在同名文件夹:${fileDto.name}")
            }
            if (existDto.localId == fileDto.localId) {//同一个文件,直接成功
                return
            }
            if (!isOverWrite) {//文件已经存在,在不允许覆盖的情况下,直接报错义务错误
                val error = ErrorCode.EXISTS_FILE
                throw BusinessException(error.code, "文件[${fileDto.name}]已存在")
            }
        }
        fileDto.date = Date()
        fileDto.id = DBID.id

        //添加文件
        this.dfsFileDao.add(fileDto)
        if (existDto != null && isOverWrite) {//将已经存在的文件标记为历史版本
            this.dfsFileDao.setHistory(existDto.id!!)
        }
    }

    /**
     * 添加文件夹
     */
    fun addFolder(folderDto: DfsFileDto) = synchronized("" + folderDto.userId) {
        val existDto = this.dfsFileDao.getByParentIdAndName(folderDto.userId!!, folderDto.parentId!!, folderDto.name!!)
        if (existDto != null) {
            throw BusinessException("文件或文件夹已经存在")
        }
        folderDto.localId = 0
        folderDto.date = Date()
        folderDto.id = DBID.id
        this.dfsFileDao.add(folderDto)
    }

    /**
     * 通过路径获取文件夹ID
     * @param userId 用户ID
     * @param folder 文件夹路径
     * @param isCreate 文件夹不存在时是否创建
     * @return 文件夹ID
     */
    fun getIdByFolder(userId: Long, folder: String, isCreate: Boolean = false): Long? {
//        if (folder == "" || folder == "/") {//空字符串默认为根目录
//            return 0
//        }
        var folderId = this.dfsFileDao.getIdByPath(userId, folder.toDfsFileNameList)
        if (folderId != null) {
            return folderId
        }
        if (isCreate) {
            folderId = this.mkdirs(userId, folder)
        }
        return folderId
    }

    /**
     * 复制目录
     * @param userId 用户ID
     * @param sourcePaths 要复制的目录数组
     * @param targetFolder 要复制到的目标文件夹目录
     */
    fun copy(userId: Long, sourcePaths: List<String>, targetFolder: String, isOverWrite: Boolean = false) {
        val sourceToTargetMap = LinkedHashMap<String, String>()
        sourcePaths.forEach {

            //复制的源路径
            val sourcePath = it

            //复制的目标路径
            var targetPath = targetFolder + "/" + it.fileName

            if (sourcePath == targetPath) {//源路径和目标路径一样时,在目标文件名加上编号
                val newName = this.makeNameNo(userId, targetPath)
                targetPath = "$targetFolder/$newName"
            }
            recursionMakeSourceToTargetMap(userId, sourcePath, targetPath, sourceToTargetMap)
        }
        sourceToTargetMap.forEach { (sourcePath, targetPath) ->
            val fileId = this.dfsFileDao.getIdByPath(userId, sourcePath.toDfsFileNameList)!!
            val fileDto = this.dfsFileDao.getOne(fileId)!!
            if (fileDto.isFolder) {//源目录是一个文件夹
                this.mkdirs(userId, targetPath)
            } else {
                val createFileDto = DfsFileDto()
                val folderId = this.getIdByFolder(userId, targetPath.fileParent, true)
                createFileDto.parentId = folderId
                createFileDto.name = targetPath.fileName
                createFileDto.localId = fileDto.localId
                createFileDto.size = fileDto.size
                createFileDto.contentType = fileDto.contentType
                createFileDto.userId = fileDto.userId
                createFileDto.date = fileDto.date
                this.addFile(createFileDto, isOverWrite)
            }
        }
    }

    /**
     * 同一个文件夹下复制时,为新的文件或文件夹加上编号
     * 例: test.zip  ==>  test(1).zip
     * @param userId 用户id
     * @param targetPath 目标目录
     * @return 新的文件名
     */
    private fun makeNameNo(userId: Long, targetPath: String): String {

        //得到父级文件夹id
        val parentId = this.getIdByFolder(userId, targetPath.fileParent)!!

        val name = targetPath.fileName
        val startName: String
        val endNameName: String
        val lastDotIndex = name.lastIndexOf('.')
        if (lastDotIndex != -1) {//路径包含点
            val existFileDto = this.dfsFileDao.getByParentIdAndName(userId, parentId, name)!!
            if (existFileDto.isFile) {
                startName = name.substring(0, lastDotIndex)
                endNameName = name.substring(lastDotIndex)
            } else {
                startName = name
                endNameName = ""
            }
        } else {
            startName = name
            endNameName = ""
        }
        for (i in 1..10000) {
            val newName = "$startName($i)$endNameName"
            this.dfsFileDao.getIdByParentIdAndName(userId, parentId, newName) ?: return newName
        }
        throw BusinessException("目录${targetPath}已存在")
    }

    /**
     * 移动目录
     * @param userId 用户ID
     * @param sourcePaths 要复制的目录数组
     * @param targetFolder 要复制到的目标文件夹目录
     */
    fun move(userId: Long, sourcePaths: List<String>, targetFolder: String, isOverWrite: Boolean = false) {
        val sourceToTargetMap = LinkedHashMap<String, String>()
        sourcePaths.forEach {

            //复制的源路径
            val sourcePath = it

            //复制的目标路径
            val targetPath = targetFolder + "/" + it.fileName
            if (("$targetFolder/").startsWith("$it/")) {
                throw BusinessException("不能移动文件夹到子文件夹下")
            }
            if (sourcePath == targetPath) {//同一个文件路径无需操作
                return@forEach
            }
            recursionMakeSourceToTargetMap(userId, sourcePath, targetPath, sourceToTargetMap)
        }

        //用来记录移动完成后要删除的文件夹
        val afterDeleteFolderList = ArrayList<DfsFileDto>()
        sourceToTargetMap.forEach { (sourcePath, targetPath) ->
            val fileId = this.dfsFileDao.getIdByPath(userId, sourcePath.toDfsFileNameList)!!
            val sourceFileDto = this.dfsFileDao.getOne(fileId)!!
            if (sourceFileDto.isFolder) {//源目录是一个文件夹
                this.mkdirs(userId, targetPath)
                afterDeleteFolderList.add(sourceFileDto)
            } else {
                val folderId = this.getIdByFolder(userId, targetPath.fileParent, true)!!
                val existFileDto = this.dfsFileDao.getByParentIdAndName(userId, folderId, sourceFileDto.name!!)
                if (existFileDto == null) {//文件不存在时,移动文件包括版本记录
                    sourceFileDto.parentId = folderId
                    sourceFileDto.name = targetPath.fileName
                    this.dfsFileDao.move(sourceFileDto)
                } else {//目标文件已经存在
                    if (existFileDto.isFolder) {//移动的对象一个时文件一个是文件夹,禁止移动
                        val error = ErrorCode.EXISTS_FILE
                        throw BusinessException(error.code, "文件或文件夹[${targetPath}]已存在")
                    }
                    if (!isOverWrite) {
                        val error = ErrorCode.EXISTS_FILE
                        throw BusinessException(error.code, "文件或文件夹[${targetPath}]已存在")
                    }
                    sourceFileDto.parentId = folderId
                    sourceFileDto.name = targetPath.fileName
                    this.dfsFileDao.move(sourceFileDto)

                    //将已经存在的文件标记为历史版本
                    this.dfsFileDao.setHistory(existFileDto.id!!)
                }
            }
        }
        afterDeleteFolderList.forEach {//删除源文件夹,不能在移动的途中删除文件夹,否则导致无法找到要移动的文件
            this.dfsFileDao.logicDelete(it.id!!)
        }
    }

    /**
     * 文件重命名
     */
    fun rename(userId: Long, sourcePath: String, name: String) {

        //获取源目录文件id
        val fileId = this.dfsFileDao.getIdByPath(userId, sourcePath.toDfsFileNameList)
            ?: throw BusinessException("移动源目录不存在")
        val fileDto = this.dfsFileDao.getOne(fileId)!!
        val existFileDto = this.dfsFileDao.getByParentIdAndName(userId, fileDto.parentId!!, name)
        if (existFileDto != null && existFileDto.id != fileId) {//existFileDto.id != fileId判断是否同一个文件,有可能仅仅时将文件名大小写转换的可能
            throw BusinessException("路径[${sourcePath.fileParent}/${name}]已存在")
        }
        fileDto.parentId = fileDto.parentId
        fileDto.name = name
        this.dfsFileDao.move(fileDto)
    }

    /**
     * 递归整理所有要复制或移动的源路径对应的目标路径
     * @param userId 用户ID
     * @param sourcePath 复制的源目录
     * @param targetPath 复制到的目标目录
     */
    private fun recursionMakeSourceToTargetMap(
        userId: Long,
        sourcePath: String,
        targetPath: String,
        sourceToTargetMap: LinkedHashMap<String, String>
    ) {
        sourceToTargetMap[sourcePath] = targetPath
        val fileId = this.dfsFileDao.getIdByPath(userId, sourcePath.toDfsFileNameList)
            ?: throw BusinessException("文件路径:[${sourcePath}]不存在")
        val fileDto = this.dfsFileDao.getOne(fileId)!!
        if (fileDto.isFolder) {//这是一个文件夹
            this.dfsFileDao.getSubFileIdAndName(userId, fileId).forEach {
                val subSourcePath = sourcePath + "/" + it.name
                val subTargetPath = targetPath + "/" + it.name
                recursionMakeSourceToTargetMap(userId, subSourcePath, subTargetPath, sourceToTargetMap)
            }
        }
    }

    /**
     * 删除文件夹或者文件
     * @param userId 用户ID
     * @param path 文件夹路径
     */
    fun setDelete(userId: Long, path: String) {
        val dfsFileId = this.dfsFileDao.getIdByPath(userId, path.toDfsFileNameList)
            ?: throw BusinessException("找不到指定目录:${path}")
        val dsfFileDto = this.dfsFileDao.getOne(dfsFileId)!!
        this.setDelete(dsfFileDto.id!!)
    }

    /**
     * 彻底删除文件
     * @param userId 用户ID
     * @param ids 要删除的文件ID
     */
    fun logicDelete(userId: Long, ids: List<Long>) {
        ids.forEach {
            val fileDto = this.dfsFileDao.getOne(it)!!
            if (fileDto.userId != userId) {
                throw ErrorCode.NOT_ALLOW
            }
            if (fileDto.deleteDate == null) {
                throw ErrorCode.NOT_ALLOW
            }
            if (fileDto.isFolder) {//如果是文件夹
                recursionLogicDelete(userId, it)
            } else {
                this.dfsFileDao.logicDeleteFile(it)
            }
        }
    }

    /**
     * 递归删除文件夹所有类容
     * @param fileId 要删除的文件ID
     */
    private fun recursionLogicDelete(userId: Long, fileId: Long) {
        this.dfsFileDao.getSubIdListToLogicDelete(userId, fileId).forEach {
            this.recursionLogicDelete(userId, it)
        }

        //彻底删除
        this.dfsFileDao.logicDelete(fileId)
    }

    /**
     * 创建文件夹
     * @param userId 用户ID
     * @param path 文件夹路径
     */
    fun mkdirs(userId: Long, path: String): Long {
        var lastFolderId = 0L

        //用来标记,以后所有文件夹都需要创建
        var isCreatModel = false

        //记录当前文件路径
        val curPathSB = StringBuilder()
        path.toDfsFileNameList.forEach {
            curPathSB.append('/').append(it)
            if (!isCreatModel) {
                val folderDto = this.dfsFileDao.getByParentIdAndName(userId, lastFolderId, it)
                if (folderDto != null) {//父级文件夹不存在,创建文件夹
                    if (folderDto.isFile) {
                        throw BusinessException("${curPathSB}是一个文件,不允许创建文件夹")
                    }
                    lastFolderId = folderDto.id!!
                    return@forEach
                }
                isCreatModel = true
            }
            val createFolderDto = DfsFileDto()
            createFolderDto.userId = userId
            createFolderDto.name = it
            createFolderDto.parentId = lastFolderId
            createFolderDto.size = 0
            this.addFolder(createFolderDto)
            lastFolderId = createFolderDto.id!!
        }
        return lastFolderId
    }

    /**
     * 将文件标记为删除状态
     */
    fun setDelete(id: Long) {
        this.dfsFileDao.setDelete(id, System.currentTimeMillis())
    }

    /**
     * 从垃圾箱还原文件
     * @param userId 用户ID
     * @param ids 要删除的文件ID
     */
    fun trashRecover(userId: Long, ids: List<Long>, isOverWrite: Boolean = false) {
        ids.forEach {
            val fileDto = this.dfsFileDao.getOne(it)!!
            if (fileDto.userId != userId) {
                throw ErrorCode.NOT_ALLOW
            }
            val existsDto = this.dfsFileDao.getByParentIdAndName(userId, fileDto.parentId!!, fileDto.name!!)
            if (existsDto == null) {//目标文件不存在,直接将文件的删除日期即可
                this.dfsFileDao.setNotDelete(it)
            } else {//目标路径已经存在
                val path = this.getPathById(it)
                throw BusinessException("目录:${path}已存在")
            }
        }
    }

    /**
     * 通过文件ID获取文件全路径
     * @param id 文件id
     * @return 文件全路径
     */
    fun getPathById(id: Long): String {
        val pathSB = StringBuilder()
        while (true) {
            val fileDto = this.dfsFileDao.getOne(id) ?: throw ErrorCode.NO_EXISTS
            pathSB.insert(0, "/" + fileDto.name)
            if (fileDto.parentId == 0L) {
                break
            }
        }
        return pathSB.toString()
    }

    /**
     * 分享文件转存
     * @param shareUserId 分享用户ID
     * @param userId 用户ID
     * @param sourcePaths 要转存的路径列表
     * @param targetFolder 要复制到的目标文件夹目录
     */
    fun shareSaveTo(
        shareUserId: Long,
        userId: Long,
        sourcePaths: List<String>,
        targetFolder: String,
        isOverWrite: Boolean = false
    ) {
        val sourceToTargetMap = LinkedHashMap<String, String>()
        sourcePaths.forEach {

            //复制的目标路径
            val targetPath = targetFolder + "/" + it.fileName
            recursionMakeSourceToTargetMap(userId, it, targetPath, sourceToTargetMap)
        }
        sourceToTargetMap.forEach { (sourcePath, targetPath) ->

            //获取分享文件的ID
            val fileId = this.dfsFileDao.getIdByPath(shareUserId, sourcePath.toDfsFileNameList)!!
            val fileDto = this.dfsFileDao.getOne(fileId)!!
            if (fileDto.isFolder) {//源目录是一个文件夹
                this.mkdirs(userId, targetPath)
            } else {
                val createFileDto = DfsFileDto()
                val folderId = this.getIdByFolder(userId, targetPath.fileParent, true)
                createFileDto.parentId = folderId
                createFileDto.name = targetPath.fileName
                createFileDto.localId = fileDto.localId
                createFileDto.size = fileDto.size
                createFileDto.contentType = fileDto.contentType
                createFileDto.userId = fileDto.userId
                createFileDto.date = fileDto.date
                this.addFile(createFileDto, isOverWrite)
            }
        }
    }


    /**
     * 保存文件到本地磁盘
     * @param md5 文件md5
     * @param iStream 文件流
     */
    fun saveToLocalFile(md5: String, iStream: InputStream): LocalFileDto {
        val exitsLocalFileDto = this.localFileDao.selectByFileMd5(md5)
        if (exitsLocalFileDto != null) {//该文件已经存在,删除本次上传的文件并返回
            return exitsLocalFileDto
        }

        //获取本地文件存储路径
        val localPath = DfsFileUtil.localPath
        val localFile = File(localPath)

        //将文件保存到指定目录
        localFile.outputStream().use { oStream ->
            iStream.use {
                it.transferTo(oStream)
            }
        }

        val addLocalFileDto = LocalFileDto()
        addLocalFileDto.path = localPath
        addLocalFileDto.md5 = md5
        addLocalFileDto.id = DBID.id
        this.localFileDao.add(addLocalFileDto)
        return addLocalFileDto
    }
}
