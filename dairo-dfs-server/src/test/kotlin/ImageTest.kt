import java.awt.Image
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import java.io.IOException

fun cropCenterSquare(image: BufferedImage): BufferedImage {
    val width = image.width
    val height = image.height
    val newDimension = Math.min(width, height)

    val x = (width - newDimension) / 2
    val y = (height - newDimension) / 2

    return image.getSubimage(x, y, newDimension, newDimension)
}

fun createThumbnail(image: BufferedImage, size: Int): BufferedImage {
    val croppedImage = cropCenterSquare(image)
    val scaledImage = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val graphics2D = scaledImage.createGraphics()

    graphics2D.drawImage(croppedImage.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null)
    graphics2D.dispose()

    return scaledImage
}

fun main() {
    try {
        val inputFile = File("./dairo-dfs-server/IMG_0044.JPG")
        val originalImage = ImageIO.read(inputFile)
        val thumbnailSize = 100 // 你想要的缩略图尺寸
        val thumbnail = createThumbnail(originalImage, thumbnailSize)

        val outputFile = File("./dairo-dfs-server/IMG_0044_1.JPG")
        ImageIO.write(thumbnail, "jpg", outputFile)
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
