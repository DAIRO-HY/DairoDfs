package cn.dairo.dfs.extension

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

/**
 * 将字节数组转base64
 */
val ByteArray.base64: String
    get() = Base64.getEncoder().encodeToString(this)


/**
 * 获取字节数组的md5
 */
val ByteArray.md5: String
    get() {
        val digest = MessageDigest.getInstance("MD5")
        digest.update(this)
        val bigInt = BigInteger(1, digest.digest())

        //这里有个BUG,toString(16)方法会生成一个不包含前导零的十六进制字符串
        //return bigInt.toString(16)
        return String.format("%032x", bigInt)
    }

/**
 * 将数据加密
 */
//val ByteArray.encode: ByteArray
//    get() {
//        this.map {
//
//        }
//    }

