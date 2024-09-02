package cn.dairo.dfs.controller.app.install.ffprobe.form

class FfprobeInstallProgressForm {


    /**
     * 是否正在下载
     */
    var hasRuning = false

    /**
     * 是否已经安装完成
     */
    var hasFinish = false

    /**
     * 文件总大小
     */
    var total = ""

    /**
     * 已经下载大小
     */
    var downloadedSize = ""

    /**
     * 下载速度
     */
    var speed = ""

    /**
     * 下载进度
     */
    var progress = 0

    /**
     * 下载url
     */
    var url: String? = null

    /**
     * 安装信息
     */
    var info: String? = null

    /**
     * 错误信息
     */
    var error: String? = null
}