package cn.dairo.dfs.extension

import java.text.SimpleDateFormat
import java.util.*

/**
 * 当前日期格式化
 */
fun Date.format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern).format(this)
}