package cn.dairo.dfs.extension

import cn.dairo.dfs.config.Constant
import cn.dairo.lib.Arith
import java.lang.StringBuilder

/**
 * 加
 */
fun Number.jia(target: Number): Double {
    return Arith.add(this, target)
}

/**
 * 减
 */
fun Number.jian(target: Number): Double {
    return Arith.sub(this, target)
}

/**
 * 乘
 */
fun Number.cheng(target: Number): Double {
    return Arith.mul(this, target)
}

/**
 * 除
 */
fun Number.chu(target: Number, scale: Int = 2): Double {
    return Arith.div(this, target, scale)
}

/**
 * 数据流量单位换算
 */
val Number?.toDataSize: String
    get() {
        if (this == null) {
            return "0B"
        }
        val value = this.toLong()
        if (value >= 1024L * 1024 * 1024 * 1024) {
            return this.chu(1024L * 1024 * 1024 * 1024, 2).toString() + "TB"
        }
        if (value >= 1024L * 1024 * 1024) {
            return this.chu(1024 * 1024 * 1024, 2).toString() + "GB"
        }
        if (value >= 1024L * 1024) {
            return this.chu(1024 * 1024, 2).toString() + "MB"
        }
        if (value >= 1024L) {
            return this.chu(1024, 2).toString() + "KB"
        }
        return this.toString() + "B"
    }

/**
 * 将数字转换成较短的字母组合
 */
val Number.toShortString: String
    get() {
        var target = this.toLong()
        target -= 1
        val charLen = Constant.SHORT_CHAR.length
        val shortSB = StringBuilder()
        while (true) {
            val index = (target % charLen).toInt()
            shortSB.insert(0, Constant.SHORT_CHAR[index])
            target /= charLen
            if (target == 0L) {
                break
            }
        }
        return shortSB.toString()
    }
