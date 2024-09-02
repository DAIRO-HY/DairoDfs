package cn.dairo.lib.uc.auto.bean

import cn.dairo.lib.uc.auto.annotations.UCOption


/**
 * 转换成Html表单数据
 */
class UCFormBean {

    /**
     * 表单名
     */
    var name: String? = null

    /**
     * 显示标题
     */
    var title: String? = null

    /**
     * 提示内容
     */
    var hint: String? = null

    /**
     * 值
     */
    var value: Any? = null

    /**
     * 显示类型
     */
    var type = "text"

    /**
     * 下拉框，单选框，复选框的可选值
     */
    var option: List<UCOption>? = null

    /**
     * 下拉框，单选框，复选框的可选值
     */
    var disabled: Boolean? = null

    /**
     * 显示条件
     */
    var showBy: String? = null
}