package cn.dairo.dfs.extension

import cn.dairo.lib.Json

/**
 * 对象转Json字符串
 */
val Any.toJson: String
    get() = Json.writeValueAsString(this)

/**
 * 通过反射获取类内部变量
 */
fun Any.getField(name: String): Any? {
    val field = this.javaClass.getDeclaredField(name)
    field.isAccessible = true
    return field.get(this)
}

