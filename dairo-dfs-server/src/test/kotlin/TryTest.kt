import java.io.File


object TryTest {
    @JvmStatic
    fun main(args: Array<String>) {
        File("C:\\tmp\\512MB.dat").outputStream().use { oStream ->
            repeat(512) {
                oStream.write(ByteArray(1024 * 1024) {
                    65
                })
            }
        }
    }
}