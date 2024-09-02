package cn.dairo.dfs.util.image

import com.gl.lib.imaging.*
import com.gl.lib.imaging.imageoptions.JpegOptions
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

object ImageUtil {

    /**
     * 生成图片缩略图
     */
    fun thumb(path: String, maxWidth: Int, maxHeight: Int): ByteArray {
        return thumb(File(path).inputStream(), maxWidth, maxHeight)
    }

    /**
     * 生成图片缩略图
     */
    fun thumb(iStream: InputStream, maxWidth: Int, maxHeight: Int): ByteArray {

        //加载
        val image = Image.load(iStream)

        //目标图片宽高比
        val whTargetScale = maxWidth.toDouble() / maxHeight

        //输入图片宽高比
        val whInputtScale = image.width.toDouble() / image.height

        //裁剪宽度
        val cutWidth: Int

        //裁剪宽度
        val cutHeight: Int

        //裁剪坐标
        val x: Int
        val y: Int
        if (whTargetScale > whInputtScale) {
            cutWidth = image.width
            cutHeight = (image.width / whTargetScale).toInt()

            x = 0
            y = (image.height - cutHeight) / 2
        } else {
            cutWidth = (image.height * whTargetScale).toInt()
            cutHeight = image.height

            x = (image.width - cutWidth) / 2
            y = 0
        }

        //按比例裁切
        val rect = Rectangle(Point(x, y), Size(cutWidth, cutHeight))
        (image as RasterImage).crop(rect)
        if (image.width > maxWidth) {

            //重新设置图片尺寸
            image.resize(maxWidth, maxHeight)
        }

        val oStream = ByteArrayOutputStream()
        val jpegOptions = JpegOptions()
        jpegOptions.quality = 85//保存质量
        image.save(oStream, jpegOptions)
        return oStream.toByteArray()
    }

    /**
     * 获取图片信息
     */
    fun getInfo(path: String): ImageInfo {
        val image = Image.load(path)
        val info = ImageInfo()
        info.width = image.width
        info.height = image.height
        return info
    }

    @JvmStatic
    fun main(args: Array<String>) {
        getInfo("C:\\Users\\user\\Desktop\\test\\tt.cr3.tiff")
    }
}