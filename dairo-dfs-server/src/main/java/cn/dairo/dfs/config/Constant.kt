package cn.dairo.dfs.config

import cn.dairo.dfs.boot.Boot
import cn.dairo.dfs.extension.bean
import cn.dairo.lib.server.dbtool.DBService
import cn.dairo.lib.server.dbtool.SqliteTool

object Constant {

    /**
     * 加密数据
     */
    val appEncodeKey = byteArrayOf(1)

    /**
     * 基准时间戳
     * 用当前时间戳减去此时间戳,得到唯一字符串,一旦数据库已经有数据,该值就不能改,否则可能导致重数据.
     */
    const val BASE_TIME = 1699177026571

    /**
     * 数字转换成短文本支持的字符串
     */
    const val SHORT_CHAR = "0Mkhc7EingwxJYtPdUmWGHeV3ND5KRACb4rBXlO6f91syvIuqoZQLa2FTS8zpj"

    /**
     * 账户信息存储路径
     */
    const val SYSTEM_JSON_PATH = "./data/system.json"

    /**
     * 登录用户ID
     */
    const val REQUEST_USER_ID = "USER_ID"

    /**
     * 是否管理员
     */
    const val REQUEST_IS_ADMIN = "IS_ADMIN"

    /**
     *  获取数据库服务
     */
    val dbService: DBService
        get() {
            return SqliteTool(Boot.service.dbPath)
        }

    /**
     * ffmpeg安装目录
     */
    val FFMPEG_PATH = Boot.service.ffmpegPath

    /**
     * ffprobe安装目录
     */
    val FFPROBE_PATH = Boot.service.ffprobePath

    /**
     * libraw安装目录
     */
    val LIBRAW_PATH = Boot.service.librawPath

    /**
     * dcraw安装目录
     */
    val LIBRAW_BIN = "$LIBRAW_PATH/LibRaw-0.21.2/bin"


}