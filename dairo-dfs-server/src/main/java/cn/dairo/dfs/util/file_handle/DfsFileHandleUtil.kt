package cn.dairo.dfs.util.file_handle

import cn.dairo.dfs.boot.Boot
import cn.dairo.dfs.config.Constant
import cn.dairo.dfs.dao.DfsFileDao
import cn.dairo.dfs.dao.LocalFileDao
import cn.dairo.dfs.dao.dto.DfsFileDto
import cn.dairo.dfs.extension.*
import cn.dairo.dfs.service.DfsFileService
import cn.dairo.dfs.util.DBID
import cn.dairo.dfs.util.DfsFileUtil.dfsContentType
import cn.dairo.dfs.util.image.ImageUtil
import cn.dairo.dfs.util.image.PSDUtil
import cn.dairo.dfs.util.image.RawUtil
import cn.dairo.dfs.util.vedio.VideoUtil
import java.io.File
import kotlin.concurrent.thread
import kotlin.math.round
import kotlin.time.measureTime

object DfsFileHandleUtil {

    /**
     * 本地文件操作Dao
     */
    private val localFileDao = LocalFileDao::class.bean

    /**
     * dfs文件操作Dao
     */
    private val dfsFileDao = DfsFileDao::class.bean

    /**
     * 文件夹数据操作Service
     */
    private val dfsFileService = DfsFileService::class.bean
    private var isRuning = false

    fun start() {
        synchronized(DfsFileHandleUtil::class) {
            if (isRuning) {
                return
            }
            isRuning = true
        }
        thread {
            Constant.dbService.use { db ->
                while (true) {
                    val dfsList = this.dfsFileDao.selectNoHandle()
                    if (dfsList.isEmpty()) {
                        break
                    }
                    dfsList.forEach {
                        try {
                            val startTime = System.currentTimeMillis()

                            //设置文件属性
                            this.makeProperty(it)

                            //生成附属文件，如标清视频，高清视频，raw预览图片
                            this.makeExtra(it)

                            //耗时
                            val measureTime = System.currentTimeMillis() - startTime
                            this.dfsFileDao.setState(
                                it.id!!,
                                1,
                                "耗时:" + measureTime.chu(1000).chu(60, 2).toString() + "分"
                            )
                        } catch (e: Exception) {
                            this.dfsFileDao.setState(it.id!!, 2, e.toString())
                        }
                    }
                }
            }
            synchronized(DfsFileHandleUtil::class) {
                isRuning = false
            }
        }
    }

    /**
     * 生成缩略图
     */
    private fun makeThumb(dfsFileDto: DfsFileDto) {
        val localDto = this.localFileDao.selectOne(dfsFileDto.localId!!) ?: return
        val path = File(localDto.path!!).absolutePath

        var data: ByteArray? = null
        val lowerName = dfsFileDto.name!!.lowercase()
        val width = 300
        val height = 300
        if (
            lowerName.endsWith(".jpg")
            || lowerName.endsWith(".jpeg")
            || lowerName.endsWith(".png")
            || lowerName.endsWith(".bmp")
            || lowerName.endsWith(".gif")
            || lowerName.endsWith(".ico")
            || lowerName.endsWith(".svg")
            || lowerName.endsWith(".tiff")
            || lowerName.endsWith(".webp")
            || lowerName.endsWith(".wmf")
            || lowerName.endsWith(".wmz")
            || lowerName.endsWith(".jp2")
            || lowerName.endsWith(".eps")
            || lowerName.endsWith(".tga")
            || lowerName.endsWith(".jfif")
        ) {
            data = ImageUtil.thumb(path, width, height)
        } else if (lowerName.endsWith(".psd")
            || lowerName.endsWith(".psb")
            || lowerName.endsWith(".ai")
        ) {
            data = PSDUtil.thumb(path, width, height)
        } else if (lowerName.endsWith(".mp4")
            || lowerName.endsWith(".mov")
            || lowerName.endsWith(".avi")
            || lowerName.endsWith(".mkv")
            || lowerName.endsWith(".flv")
            || lowerName.endsWith(".rm")
            || lowerName.endsWith(".rmvb")
            || lowerName.endsWith(".3gp")
        ) {
            data = VideoUtil.thumb(path, width, height)
        } else if (lowerName.endsWith(".cr3")
            || lowerName.endsWith(".cr2")
        ) {

            //专业相机RAW图片
            data = RawUtil.thumb(path, lowerName.fileExt, width, height)
        } else {
        }
        data ?: return

        //计算缩略图的md5
        val md5 = data.md5

        //保存文件
        val localFileDto = this.dfsFileService.saveToLocalFile(md5, data.inputStream())

        //添加缩率图附属文件
        val extraDto = DfsFileDto()
        extraDto.id = DBID.id
        extraDto.name = "thumb"
        extraDto.size = data.size.toLong()
        extraDto.localId = localFileDto.id
        extraDto.isExtra = true
        extraDto.parentId = dfsFileDto.id
        extraDto.userId = dfsFileDto.userId
        extraDto.date = dfsFileDto.date
        extraDto.state = 1
        extraDto.contentType = "jpeg".dfsContentType
        this.dfsFileDao.add(extraDto)
    }

    /**
     * 生成文件属性
     */
    private fun makeProperty(dfsFileDto: DfsFileDto) {
        val exitsProperty = this.dfsFileDao.selectPropertyByLocalId(dfsFileDto.localId!!)
        if (exitsProperty != null) {//属性已经存在
            this.dfsFileDao.setProperty(dfsFileDto.id!!, exitsProperty)
            return
        }
        val localDto = this.localFileDao.selectOne(dfsFileDto.localId!!) ?: return
        val path = localDto.path!!

        val lowerName = dfsFileDto.name!!.lowercase()
        var property: Any? = null
        if (
            lowerName.endsWith(".jpg")
            || lowerName.endsWith(".jpeg")
            || lowerName.endsWith(".png")
            || lowerName.endsWith(".bmp")
            || lowerName.endsWith(".gif")
            || lowerName.endsWith(".ico")
            || lowerName.endsWith(".svg")
            || lowerName.endsWith(".tiff")
            || lowerName.endsWith(".webp")
            || lowerName.endsWith(".wmf")
            || lowerName.endsWith(".wmz")
            || lowerName.endsWith(".jp2")
            || lowerName.endsWith(".eps")
            || lowerName.endsWith(".tga")
            || lowerName.endsWith(".jfif")
        ) {
            property = ImageUtil.getInfo(path)
        } else if (lowerName.endsWith(".psd")
            || lowerName.endsWith(".psb")
            || lowerName.endsWith(".ai")
        ) {
            property = PSDUtil.getInfo(path)
        } else if (lowerName.endsWith(".mp4")
            || lowerName.endsWith(".mov")
            || lowerName.endsWith(".avi")
            || lowerName.endsWith(".mkv")
            || lowerName.endsWith(".flv")
            || lowerName.endsWith(".rm")
            || lowerName.endsWith(".rmvb")
            || lowerName.endsWith(".3gp")
        ) {
            property = VideoUtil.getInfo(path)
        } else if (lowerName.endsWith(".cr3")
            || lowerName.endsWith(".cr2")
        ) {

            //专业相机RAW图片
            property = RawUtil.getInfo(path)
        } else {
        }
        property ?: return
        this.dfsFileDao.setProperty(dfsFileDto.id!!, property.toJson)
    }

    /**
     * 生成附属文件，如标清视频，高清视频，raw预览图片
     */
    private fun makeExtra(dfsFileDto: DfsFileDto) {
        val existsExtraList = this.dfsFileDao.selectExtraFileByLocalId(dfsFileDto.localId!!)
        if (existsExtraList.isNotEmpty()) {//该文件已经存在了附属文件,直接使用
            existsExtraList.forEach {
                val extraDto = DfsFileDto()
                extraDto.id = DBID.id
                extraDto.name = it.name
                extraDto.size = it.size
                extraDto.localId = it.localId
                extraDto.isExtra = true
                extraDto.parentId = dfsFileDto.id
                extraDto.userId = it.userId
                extraDto.date = dfsFileDto.date
                extraDto.state = 1
                extraDto.contentType = it.contentType
                this.dfsFileDao.add(extraDto)
            }
            return
        }

        //获取缩略图
        this.makeThumb(dfsFileDto)
        val localDto = this.localFileDao.selectOne(dfsFileDto.localId!!) ?: return
        val path = localDto.path!!

        val lowerName = dfsFileDto.name!!.lowercase()
        if (
            lowerName.endsWith(".jpg")
            || lowerName.endsWith(".jpeg")
            || lowerName.endsWith(".png")
            || lowerName.endsWith(".bmp")
            || lowerName.endsWith(".gif")
            || lowerName.endsWith(".ico")
            || lowerName.endsWith(".svg")
            || lowerName.endsWith(".tiff")
            || lowerName.endsWith(".webp")
            || lowerName.endsWith(".wmf")
            || lowerName.endsWith(".wmz")
            || lowerName.endsWith(".jp2")
            || lowerName.endsWith(".eps")
            || lowerName.endsWith(".tga")
            || lowerName.endsWith(".jfif")
        ) {
            ;
        } else if (lowerName.endsWith(".psd")
            || lowerName.endsWith(".psb")
            || lowerName.endsWith(".ai")
        ) {
            val pngData = PSDUtil.toPng(path)
            val md5 = pngData.md5

            //保存文件
            val localFileDto = this.dfsFileService.saveToLocalFile(md5, pngData.inputStream())
            val extraDto = DfsFileDto()
            extraDto.id = DBID.id
            extraDto.name = "preview"
            extraDto.size = pngData.size.toLong()
            extraDto.localId = localFileDto.id!!
            extraDto.isExtra = true
            extraDto.parentId = dfsFileDto.id
            extraDto.userId = dfsFileDto.userId
            extraDto.date = dfsFileDto.date
            extraDto.state = 1
            extraDto.contentType = "image/png"
            this.dfsFileDao.add(extraDto)
        } else if (lowerName.endsWith(".mp4")
            || lowerName.endsWith(".mov")
            || lowerName.endsWith(".avi")
            || lowerName.endsWith(".mkv")
            || lowerName.endsWith(".flv")
            || lowerName.endsWith(".rm")
            || lowerName.endsWith(".rmvb")
            || lowerName.endsWith(".3gp")
        ) {
            val videoInfo = VideoUtil.getInfo(path)

            //要转换的目标尺寸
            val targetList = listOf("1920:30", "1280:25", "640:15")
            targetList.forEach {
                val targetArr = it.split(":")
                val targetSize = targetArr[0].toInt()//目标最大边
                var targetFps = targetArr[1].toFloat()//目标帧数

                //是否横向视频
                val isHorizontal = videoInfo.width > videoInfo.height

                //视频文件最最大边像素
                val maxSize = if (isHorizontal) videoInfo.width else videoInfo.height
                if (targetSize > maxSize) {
                    return@forEach
                }

                //当视频宽度相等时,如果目标视频帧数大于或者等于原视频帧数,则不需要处理
                if (targetSize == maxSize && targetFps >= videoInfo.fps) {
                    return@forEach
                }
                if (targetFps > videoInfo.fps) {
                    targetFps = videoInfo.fps
                }

                var targetW: Int//目标宽
                var targetH: Int//目标高
                if (isHorizontal) {//如果是横向视频
                    targetW = targetSize
                    targetH = round(targetW.toDouble() / videoInfo.width * videoInfo.height).toInt()
                    if (targetH % 2 == 1) {//视频像素不能时基数
                        targetH -= 1
                    }
                } else {//如果是竖向视频
                    targetH = targetSize
                    targetW = round(targetH.toDouble() * videoInfo.width / videoInfo.height).toInt()
                    if (targetW % 2 == 1) {//视频像素不能时基数
                        targetW -= 1
                    }
                }

                //转换之后的文件
                val targetFile = File(Boot.service.dataPath + "/temp/${System.currentTimeMillis()}")
                try {
                    VideoUtil.transfer(path, targetW, targetH, targetFps, targetFile.absolutePath)
                    val md5 = targetFile.md5

                    //保存到本地文件
                    val localFileDto = this.dfsFileService.saveToLocalFile(md5, targetFile.inputStream())

                    val extraDto = DfsFileDto()
                    extraDto.id = DBID.id
                    extraDto.name = targetSize.toString()
                    extraDto.size = targetFile.length()
                    extraDto.localId = localFileDto.id!!
                    extraDto.isExtra = true
                    extraDto.parentId = dfsFileDto.id
                    extraDto.userId = dfsFileDto.userId
                    extraDto.date = dfsFileDto.date
                    extraDto.contentType = "video/mp4"
                    extraDto.state = 1
                    this.dfsFileDao.add(extraDto)
                } finally {
                    targetFile.delete()
                }
            }
        } else if (lowerName.endsWith(".cr3")
            || lowerName.endsWith(".cr2")
        ) {
            val jpgData = RawUtil.jpeg(path, lowerName.fileExt)
            val md5 = jpgData.md5

            //保存文件
            val localFileDto = this.dfsFileService.saveToLocalFile(md5, jpgData.inputStream())
            val extraDto = DfsFileDto()
            extraDto.id = DBID.id
            extraDto.name = "preview"
            extraDto.size = jpgData.size.toLong()
            extraDto.localId = localFileDto.id!!
            extraDto.isExtra = true
            extraDto.parentId = dfsFileDto.id
            extraDto.userId = dfsFileDto.userId
            extraDto.date = dfsFileDto.date
            extraDto.contentType = "image/jpeg"
            extraDto.state = 1
            this.dfsFileDao.add(extraDto)
        } else {
        }
    }
}