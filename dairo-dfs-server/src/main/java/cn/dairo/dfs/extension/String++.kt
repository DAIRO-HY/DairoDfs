package cn.dairo.dfs.extension

import cn.dairo.dfs.exception.BusinessException
import cn.dairo.dfs.util.DfsFileUtil
import java.math.BigInteger
import java.security.MessageDigest
import java.util.regex.Pattern

/**
 * 字符串转md5
 */
val String.md5: String
    get() {
        val digest = MessageDigest.getInstance("MD5")
        digest.update(this.toByteArray())
        val bigInt = BigInteger(1, digest.digest())
        return bigInt.toString(16)
    }

/**
 * 验证是否是一个正确的邮箱地址
 */
val String.isEmail: Boolean
    get() {
        val emailMath =
            "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$"
        return match(emailMath, this)
    }

/**
 * 验证是否是一个正确的超链接
 */
val String.isUrl: Boolean
    get() {
        val urlMatch =
            "^((https|http|ftp|rtsp|mms)?://)?(([0-9a-zA-Z_!~*'().&=+$%-]+: )?[0-9a-zA-Z_!~*'().&=+$%-]+@)?(([0-9]{1,3}\\.){3}[0-9]{1,3}|([0-9a-zA-Z_!~*'()-]+\\.)*([0-9a-zA-Z][0-9a-zA-Z-]{0,61})?[0-9a-zA-Z]\\.[a-zA-Z]{2,6})(:[0-9]{1,4})?((/?)|(/[0-9a-zA-Z_!~*'().;?:@&=+$,%#-]+)+/?)$"
        return match(urlMatch, this)
    }

/**
 * 手机号验证
 */
val String.isMobile: Boolean
    get() {
        val mobileMatch = "^[1][3,4,5,7,8,9][0-9]{9}$"
        return match(mobileMatch, this)
    }

private fun match(regex: String, str: String): Boolean {
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(str)
    return matcher.matches()
}

/**
 * 将路径分割成列表
 */
val String.toDfsFileNameList: List<String>
    get() {
        DfsFileUtil.checkPath(this)
        var path = this
        if (this.isEmpty()) {
            return listOf("")
        }
        if (!this.startsWith("/")) {
            throw BusinessException("文件路径必须以/开头")
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length - 1)
        }
        return path.split("/")
    }

/**
 * 获取文件名
 */
val String.fileName: String
    get() {
        val splitIndex = this.lastIndexOf('/')
        if (splitIndex == -1) {
            return this
        }
        return this.substring(splitIndex + 1)
    }

/**
 * 获取文件后缀名
 */
val String.fileExt: String
    get() {
        val splitIndex = this.lastIndexOf('.')
        if (splitIndex == -1) {//根目录文件,没有父级文件夹
            return ""
        }
        return this.substring(splitIndex + 1)
    }

/**
 * 获取路径的父级文件夹路径
 */
val String.fileParent: String
    get() {
        val splitIndex = this.lastIndexOf('/')
        if (splitIndex == -1) {//根目录文件,没有父级文件夹
            return ""
        }
        return this.substring(0, splitIndex)
    }

/**
 * 加密字符串
 */
val String.encode: String
    get() {
        this.toByteArray()
        val splitIndex = this.lastIndexOf('/')
        if (splitIndex == -1) {//根目录文件,没有父级文件夹
            return ""
        }
        return this.substring(0, splitIndex)
    }


