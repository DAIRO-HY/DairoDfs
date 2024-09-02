import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object FileUploadTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val url = URL("http://127.0.0.1:8030/api/file_upload/-?path=${URLEncoder.encode("admin/js/test.js", "utf-8")}")
        val http = url.openConnection() as HttpURLConnection
        http.requestMethod = "POST"
        http.doOutput = true
        http.connect()
        File("/Users/zhoulq/dev/java/idea/dairo-dfs/dairo-dfs-server/src/main/resources/static/admin/js/test.js").inputStream()
            .transferTo(http.outputStream)
        println(http.responseCode)
        http.disconnect()
    }
}