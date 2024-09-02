package cn.dairo.dfs.extension

import cn.dairo.dfs.configuration.ApplicationContextProvider
import cn.dairo.lib.Json
import kotlin.reflect.KClass


/**
 * 获取一个类的bean实列
 */
val <T : Any> KClass<T>.bean: T
    get() {
        val key = this.simpleName!!.replaceFirstChar { it.lowercase() }
        return ApplicationContextProvider.applicationContext.getBean(key) as T
    }

/**
 * 从json读取到对象
 */
fun <T : Any> KClass<T>.fromJson(json: String): T {
    return Json.readValue(json, this.java)
}

