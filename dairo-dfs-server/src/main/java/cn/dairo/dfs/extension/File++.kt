package cn.dairo.dfs.extension

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * 获取文件转md5
 */
val File.md5: String
    get() {
        this.inputStream().use { iStream ->
            val digest = MessageDigest.getInstance("MD5")
            val data = ByteArray(1 * 1024 * 1024)
            var len: Int
            while (iStream.read(data, 0, data.size).also { len = it } != -1) {
                digest.update(data, 0, len)
            }
            val bigInt = BigInteger(1, digest.digest())

            //这里有个BUG,toString(16)方法会生成一个不包含前导零的十六进制字符串
            //return bigInt.toString(16)
            return String.format("%032x", bigInt)
        }
    }

