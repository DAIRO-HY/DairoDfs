package cn.dairo.dfs.util.vedio

/**
 * 视频信息
 */
class VedioInfo {

    //宽
    var width: Int = 0

    //高
    var height: Int = 0

    //帧数
    var fps: Float = 0F

    //视频比特率
    var bitrate: Int? = null

    //视频时长（毫秒）
    var duration: Long? = null

    //视频创建时间戳
    var date: Long? = null

    //音频比特率
    var audioBitrate: Int? = null

    //音频采样率（HZ）
    var audioSampleRate: Int? = null

    //音频格式
    var audioFormat: String? = null
}