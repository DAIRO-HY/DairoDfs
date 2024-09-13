package cn.dairo.dfs.sync.bean

class SyncInfo {

    /**
     * 编号
     */
    var no = 0

    /**
     * 主机域名
     */
    var domain = ""

    /**
     * 同步状态 0：待机中   1：同步中  2：同步错误
     */
    var state = 0

    /**
     * 同步消息
     */
    var msg = ""

    /**
     * 同步日志数
     */
    var syncCount = 0

    /**
     * 最后一次同步完成时间
     */
    var lastTime = 0L

    /**
     * 最后一次心跳时间
     */
    var lastHeartTime = 0L
}