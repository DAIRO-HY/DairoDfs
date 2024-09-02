package cn.dairo.make.client.api.source

import java.io.File
import java.io.IOException
import java.util.*
import java.net.URL

/**
 * 查找类工具类
 */
object ClassUtil {

    /**
     * 获取某个包下所有类列表
     * @param packageName 包名
     * @return 类全名列表
     */
    fun findClass(packageName: String): List<String> {
        val classNames = ArrayList<String>()
        val path = packageName.replace('.', '/')
        try {
            val resources: Enumeration<URL> = Thread.currentThread().contextClassLoader.getResources(path)
            while (resources.hasMoreElements()) {
                val resource: URL = resources.nextElement()
                classNames.addAll(this.getClasses(resource, packageName))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return classNames
    }

    /**
     * 通过资源URL获取所有类全名
     */
    private fun getClasses(resource: URL, packageName: String): List<String> {
        val classNames = ArrayList<String>()
        val directory = File(resource.file)
        if (directory.exists()) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        classNames.addAll(this.findClass(packageName + "." + file.name))
                        //classNames.addAll(getClasses(file.toURI().resolve(packageName).toURL(), packageName + "." + file.name))
                    } else if (file.name.endsWith(".class")) {
                        val className = packageName + '.' + file.name.substring(0, file.name.length - 6)
                        classNames.add(className)
                    }
                }
            }
        }
        return classNames
    }
}