package cn.dairo.dfs.dao.dto

import java.util.*

open class DfsFileDto {

    /**
     * id
     */
    var id: Long? = null

    /**
     * 所属用户ID
     */
    var userId: Long? = null

    /**
     * 目录ID
     */
    var parentId: Long? = null

    /**
     * 名称
     */
    var name: String? = null

    /**
     * 大小
     */
    var size: Long? = null

    /**
     * 文件类型(文件专用)
     */
    var contentType: String? = null

    /**
     * 本地文件存储id(文件专用)
     */
    var localId: Long? = null

    /**
     * 创建日期
     */
    var date: Date? = null

    /**
     * 文件属性，比如图片尺寸，视频分辨率等信息，JSON字符串
     */
    var property: String? = null

    /**
     * 是否附属文件，比如视频的标清文件，高清文件，PSD图片的预览图片，cr3的预览图片等
     */
    var isExtra = false

    /**
     * 是否历史版本(文件专用),1:历史版本 0:当前版本
     */
    var isHistory: Boolean? = null

    /**
     * 删除日期
     */
    var deleteDate: Long? = null

    /**
     * 文件处理状态，0：待处理 1：处理完成 2：处理出错，比如视频文件，需要转码；图片需要获取尺寸等信息
     */
    var state: Byte = 0

    /**
     * 文件处理出错信息
     */
    var stateMsg: String? = null
}
