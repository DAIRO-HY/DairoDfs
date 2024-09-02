$(function () {

    /**
     * 绑定表单数据扩展函数
     * @param data
     */
    $.fn.bindValue = function (data) {
        this.find(":input[name]").each((_, obj) => {
            const $obj = $(obj)
            const name = $obj.attr("name")
            $obj.val(data[name])
        })
    }

    /**
     * 绑定表单数据扩展函数
     * @param data
     */
    $.fn.bindValue = function (data) {
        this.find(":input[name]").each((_, obj) => {
            const $obj = $(obj)
            const name = $obj.attr("name")
            $obj.val(data[name])
        })
    }

    /**
     * 初始化页面数据专用
     * @param success 成功回调
     */
    $.fn.initData = function (success) {
        $.ajaxByData({url: location.href}, data => {
            if (success) {
                success(data)
            } else {
                this.bindValue(data)
            }
        })
    }
})