package cn.dairo.lib

/**
 * @author BadBoy
 * 字符串相关处理类
 */
object StringUtil {

    /**
     * 获取一个随机数字字符串
     *
     * @param num
     * @return
     */
    fun getRandomNum(num: Int): String {
        val sb = StringBuilder()
        for (i in 0 until num) {
            sb.append((Math.random() * 10).toInt())
        }
        return sb.toString()
    }

    /**
     * 生成随机字符
     *
     * @param num
     * @return
     */
    fun getRandomChar(num: Int): String {
        val sb = StringBuilder()
        for (i in 0 until num) {
            val r = (Math.random() * 62).toInt()
            var asii = 0
            asii = if (r < 10) {
                48 + r
            } else if (r < 36) {
                65 + r - 10
            } else {
                97 + r - 36
            }
            sb.append(asii.toChar())
        }
        return sb.toString()
    }
}
