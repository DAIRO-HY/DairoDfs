package cn.dairo.dfs.util.image

import cn.dairo.dfs.extension.toDataSize
import cn.dairo.dfs.util.ShellUtil
import com.gl.lib.imaging.Image
import com.gl.lib.imaging.imageoptions.JpegOptions
import com.gl.lib.imaging.imageoptions.PngOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat

/**
 * Raw图片解析工具类
 */
object RawUtil {

    /**
     * 生成缩略图
     * @param path 视频文件路径
     * @param ext 文件后最，raw图片处理时，必须携带文件后缀名
     * @param maxWidth 图片最大宽度
     * @param maxHeight 图片最大高度
     * @return 图片字节数组
     */
    fun thumb(path: String, ext: String, maxWidth: Int, maxHeight: Int): ByteArray {

        //重命名文件名
        val renameTo = path + "." + ext
        try {//获取视频第一帧作为缩略图
            File(path).renameTo(File(renameTo))
            val tiffData =
                ShellUtil.execToByteArray("""dcraw_emu -T -w -h -Z - -mem -mmap $renameTo""")
            return ImageUtil.thumb(tiffData.inputStream(), maxWidth, maxHeight)
        } finally {
            File(renameTo).renameTo(File(path))
        }
    }

    /**
     * 生成PNG图片
     * @param path 视频文件路径
     * @param ext 文件后最，raw图片处理时，必须携带文件后缀名
     * @return PNG图片字节数组
     */
    fun png(path: String, ext: String): ByteArray {

        //重命名文件名
        val renameTo = path + "." + ext
        try {//获取视频第一帧作为缩略图
            File(path).renameTo(File(renameTo))
            val tiffData =
                ShellUtil.execToByteArray("""dcraw_emu -T -w -Z - -mem -mmap $renameTo""")

            val tiff = Image.load(tiffData.inputStream())
            val pngOption = PngOptions()

            // 可以设置压缩级别等选项,压缩级别越高，占用空间越小，CPU处理的时间越长
            pngOption.compressionLevel = 9//0-9
            val pngStream = ByteArrayOutputStream()
            tiff.save(pngStream, pngOption)
            return pngStream.toByteArray()
        } finally {
            File(renameTo).renameTo(File(path))
        }
    }

    /**
     * 生成jpg图片
     * @param path 视频文件路径
     * @param ext 文件后最，raw图片处理时，必须携带文件后缀名
     * @return PNG图片字节数组
     */
    fun jpeg(path: String, ext: String): ByteArray {
        val now = System.currentTimeMillis()

        //重命名文件名
        val renameTo = path + "." + ext
        try {//获取视频第一帧作为缩略图
            File(path).renameTo(File(renameTo))
            val tiffData =
                ShellUtil.execToByteArray("""dcraw_emu -T -w -Z - -mem -mmap $renameTo""")
            val tiff = Image.load(tiffData.inputStream())
            val jpgOption = JpegOptions()
            jpgOption.quality = 85
            val jpgStream = ByteArrayOutputStream()
            tiff.save(jpgStream, jpgOption)
            println("-->耗时:${System.currentTimeMillis() - now} 数据大小:${jpgStream.size().toDataSize}")
            return jpgStream.toByteArray()
        } finally {
            File(renameTo).renameTo(File(path))
        }
    }

    /**
     * 获取图片信息
     */
    fun getInfo(path: String): ImageInfo {

        //获取视频第一帧作为缩略图
        val infoData = ShellUtil.execToByteArray("""raw-identify -v "$path"""")
        val infoStr = String(infoData)

        //拍摄时间
        var date: Long? = null
        try {
            var durationStr = Regex("Timestamp:.*").find(infoStr)!!.value
            durationStr = durationStr.substring(11)
            val durationArr = durationStr.split(" ")

            val montnMap = mapOf(
                "Jan" to "01",
                "Feb" to "02",
                "Mar" to "03",
                "Apr" to "04",
                "May" to "05",
                "Jun" to "06",
                "Jul" to "07",
                "Aug" to "08",
                "Sep" to "09",
                "Oct" to "10",
                "Nov" to "11",
                "Dec" to "12",
            )
            val month = montnMap[durationArr[1]]
            val day = durationArr[3]

            val dateStr = durationArr[5] + month + String.format("%02d", day.toInt()) + durationArr[4]
            date = SimpleDateFormat("yyyyMMddHH:mm:ss").parse(dateStr).time
        } catch (e: Exception) {
            e.printStackTrace()
        }


        var width: Int? = null//宽
        var height: Int? = null//高
        try {
            var imageSizeStr = Regex("Image size:.*").find(infoStr)!!.value
            imageSizeStr = imageSizeStr.substring(11).replace(" ", "")
            val imageSizeArr = imageSizeStr.split("x")
            width = imageSizeArr[0].toInt()
            height = imageSizeArr[1].toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //相机名
//        var camera: String? = null
//        try {
//            var cameraStr = Regex("Camera:.*").find(infoStr)!!.value
//            camera = cameraStr.substring(8)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
        val info = ImageInfo()
        info.width = width
        info.height = height
        info.date = date
        return info
    }
}