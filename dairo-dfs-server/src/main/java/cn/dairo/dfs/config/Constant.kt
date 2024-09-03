package cn.dairo.dfs.config

import cn.dairo.dfs.boot.Boot
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
     * 初始化密码前缀
     */
    const val NO_SET_PWD_PRE = "@@-"

    /**
     * 用户登录票据
     */
    const val REQUEST_TOKEN = "TOKEN"

    /**
     * 用户登录票据
     */
    const val SESSION_TOKEN = "SESSION_TOKEN"

    /**
     * 登录名
     */
    const val SESSION_LOGIN_NAME = "LOGIN_NAME"

    /**
     * 是否管理员
     */
    const val SESSION_IS_ADMIN = "IS_ADMIN"

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
    val FFMPEG_PATH = "${Boot.service.dataPath}/lib/ffmpeg"

    /**
     * ffprobe安装目录
     */
    val FFPROBE_PATH = "${Boot.service.dataPath}/lib/ffprobe"

    /**
     * libraw安装目录
     */
    val LIBRAW_PATH = "${Boot.service.dataPath}/lib/libraw"

    /**
     * dcraw安装目录
     */
    val LIBRAW_BIN = "$LIBRAW_PATH/LibRaw-0.21.2/bin"


}