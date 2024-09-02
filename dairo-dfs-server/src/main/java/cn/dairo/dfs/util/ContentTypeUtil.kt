package cn.dairo.dfs.util

object ContentTypeUtil {

    /**
     * 通过文件路径匹配ContentType
     */
    fun getContentTypeByPath(path: String): String? {
        val path = path.lowercase()
        if (path.endsWith("jpg") || path.endsWith("jpeg")) {
            return "image/jpeg"
        } else {
            return null
        }
    }
}