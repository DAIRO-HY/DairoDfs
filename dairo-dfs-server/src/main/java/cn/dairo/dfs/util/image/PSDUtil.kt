package cn.dairo.dfs.util.image

import com.gl.lib.psd.Image
import com.gl.lib.psd.imageoptions.JpegOptions
import com.gl.lib.psd.imageoptions.PngOptions
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object PSDUtil {

    /**
     * 生成图片缩略图
     */
    fun thumb(path: String, maxWidth: Int, maxHeight: Int): ByteArray {
        Image.load(path).use {
            val oStream = ByteArrayOutputStream()
            val jpgOption = JpegOptions()
            jpgOption.quality = 100
            it.save(oStream, jpgOption)
            val iStream = ByteArrayInputStream(oStream.toByteArray())
            return ImageUtil.thumb(iStream, maxWidth, maxHeight)
        }
    }

    /**
     * 生成PNG图片
     */
    fun toPng(path: String): ByteArray {
        Image.load(path).use {
            val oStream = ByteArrayOutputStream()
            val pngOption = PngOptions()
            it.save(oStream, pngOption)
            return oStream.toByteArray()
        }
    }

    /**
     * 获取图片信息
     */
    fun getInfo(path: String): ImageInfo {
        Image.load(path).use {
            val info = ImageInfo()
            info.width = it.width
            info.height = it.height
            return info
        }
    }
}