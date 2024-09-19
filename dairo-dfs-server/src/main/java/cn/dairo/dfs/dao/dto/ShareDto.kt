package cn.dairo.dfs.dao.dto

import java.util.*

class ShareDto {

    /**
     * id
     */
    var id: Long? = null

    /**
     * 分享标题
     */
    var title: String? = null

    /**
     * 所属用户ID
     */
    var userId: Long? = null

    /**
     * 加密分享
     */
    var pwd: String? = null

    /**
     * 分享的文件夹
     */
    var folder: String? = null

    /**
     * 分享的文件夹或文件名,用|分割
     */
    var names: String? = null

    /**
     * 缩略图
     */
    var thumb: Long? = null

    /**
     * 是否是一个文件夹
     */
    var folderFlag: Boolean? = null

    /**
     * 文件数
     */
    var fileCount:Int? = null

    /**
     * 结束日期
     */
    var endDate: Long? = null

    /**
     * 创建日期
     */
    var date: Date? = null
}
