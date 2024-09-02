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
        val psd = Image.load(path)
        val oStream = ByteArrayOutputStream()
        val jpgOption = JpegOptions()
        jpgOption.quality = 100
        psd.save(oStream, jpgOption)
        val iStream = ByteArrayInputStream(oStream.toByteArray())
        return ImageUtil.thumb(iStream, maxWidth, maxHeight)
    }

    /**
     * 生成PNG图片
     */
    fun toPng(path: String): ByteArray {
        val psd = Image.load(path)
        val oStream = ByteArrayOutputStream()
        val pngOption = PngOptions()
        psd.save(oStream, pngOption)
        return oStream.toByteArray()
    }

    /**
     * 获取图片信息
     */
    fun getInfo(path: String): ImageInfo {
        val psd = Image.load(path)
        val info = ImageInfo()
        info.width = psd.width
        info.height = psd.height
        return info
    }
}