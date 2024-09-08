$(function () {

    /**
     * 绑定表单数据扩展函数
     * @param data
     */
    $.fn.bindValue = function (data) {
        this.find(":input[name]").each((_, obj) => {
            const $obj = $(obj)
            const name = $obj.attr("name")
            let value = data[name]
            if (value == null) {
                value = ""
            } else {
                value = value.toString()
            }
            $obj.val(value)
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

    $.http = function (url) {
        return new ApiHttp(url)
    }

    /**
     * Form表单发起ajax请求
     */
    $.fn.ajaxByForm123 = function (url) {


        this.find("span[error-valid]").remove()
        this.find(".is-invalid").removeClass("is-invalid")
        if (url === undefined) {
            url = this.attr("action")
        }
        const data = this.formData(this)
        const http = new ApiHttp(url)
        http.addAll(data)
        return http
    }


    /**
     * 获取表单数据
     */
    $.fn.formData = function () {
        const data = {}
        $.each(this.find(":input"), (i, obj) => {
            if (obj.name === "") {
                return true
            }
            if ((obj.type === "radio" || obj.type === "checkbox") && !obj.checked) {
                return true
            }
            if (obj.disabled) {
                return true
            }
            data[obj.name] = obj.value
        })
        return data
    }
})