package cn.dairo.dfs.controller.app.files.form

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size

class ShareForm {

    /**
     * 有效天数 -1代表自定义时间
     */
    var shareDays: Int? = null

    /**
     * 分享结束日期
     */
    var shareEndDate: String? = null

    /**
     * 分享密码
     */
    @Size(max = 32)
    var pwd: String? = null

    /**
     * 分享的文件夹
     */
    var folder: String? = null

    /**
     * 要分享的文件名或文件夹名列表
     */
    var names: List<String>? = null

    /**
     * 验证截止日期是否正确输入
     * 该函数必须以[is]开头才会被执行,而且is后面为要检查的字段名首字母大写
     */
    @AssertTrue(message = "请选择截止日期")
    fun isShareEndDate(): Boolean {
        if (this.shareDays == -1 && this.shareEndDate.isNullOrEmpty()) {
            return false
        }
        return true
    }
}