/**
 * 遮罩已经显示次数
 */
let maskShowTimes = 0
$(() => {

    //禁用表单自带提交
    $("form").submit(function () {
        return false
    })

    /**
     * 发起ajax请求
     * @param option 请求参数或者只是URL
     * @param success 成功之后的回调
     * @param fail 失败之后的回调
     * @param final 始终执行
     */
    $.ajaxByData = function (option, success, fail, final) {
        if (typeof option === "string") {
            option = {
                url: option
            }
        }
        ajax(option, success, fail, final)
    }

    /**
     * Form表单发起ajax请求
     * @param success 成功之后的回调
     * @param fail 失败之后的回调
     * @param final 始终执行
     */
    $.fn.ajaxByForm = function (success, fail, final) {
        this.ajaxByFormAndOption({}, success, fail, final)
    }

    /**
     * Form表单发起ajax请求
     * @param option 请求参数
     * @param success 成功之后的回调
     * @param fail 失败之后的回调
     * @param final 始终执行
     */
    $.fn.ajaxByFormAndOption = function (option, success, fail, final) {
        $("span[error-valid]").remove()
        $(".is-invalid").removeClass("is-invalid")
        if (option.url === undefined) {
            option.url = this.attr("action")
        }
        const data = getFormData(this)
        if (option.data === undefined) {
            option.data = data
        } else {
            option.data = $.extend(data, option.data)
        }
        ajax(option, success, fail, final)
    }

    /**
     * 获取表单数据
     * @param $form
     */
    function getFormData($form) {
        const data = {}
        $.each($form.find(":input"), (i, obj) => {
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

    /**
     * 发起ajax请求
     * @param option 请求参数
     * @param success 成功之后的回调
     * @param fail 失败之后的回调
     * @param final 始终执行
     */
    function ajax(option, success, fail, final) {
        addMask()
        let urlParam = "_clientFlag=0&_version=0&"
        const token = sessionStorage.getItem("token")
        if (token !== "") {
            urlParam += "_token=" + token + "&"
        }
        if (option.data) {
            for (let key in option.data) {
                const value = option.data[key]
                if (value == null || value === "") {
                    continue
                }
                if (typeof value === "object") {
                    value.forEach(item => {
                        urlParam += key + "=" + encodeURIComponent(item) + "&"
                    })
                } else {
                    urlParam += key + "=" + encodeURIComponent(value) + "&"
                }
            }
        }
        if (urlParam !== "") {//删除最后一个&
            urlParam = urlParam.substring(0, urlParam.length - 1)
        }

        $.ajax({
            url: option.url,
            method: "POST",
            data: urlParam,
            dataType: "TEXT",
            success: resText => {
                removeMask()
                let data = null
                try {
                    data = JSON.parse(resText)
                } catch {
                    data = resText
                }
                if (success) {
                    success(data)
                }
            },
            error: xhr => {
                removeMask()
                const resText = xhr.responseText
                if (resText === undefined) {
                    alert("网络连接失败")
                    return
                }
                let data = null
                try {
                    data = JSON.parse(resText)
                } catch {
                }
                if (data == null) {//数据解析失败
                    alert(resText)
                    return
                }
                if (data.code === undefined) {//非业务错误
                    alert(resText)
                    return
                }
                if (data.code === 5) {
                    window.location.href = "/app/login"
                    return
                }
                if (data.code === 2) {
                    const fieldError = data.data
                    addFiledError(fieldError)
                    return
                }
                if (fail) {
                    fail(data)
                } else {
                    alert(data.msg)
                }
            },
            complete: () => {
                if (final) {
                    final()
                }
            }
        })
    }

    /**
     * 添加验证失败消息
     * @param fieldError
     */
    function addFiledError(fieldError) {
        for (key in fieldError) {
            const messages = fieldError[key]
            const error = messages.join(";")
            const $input = $(`[name="${key}"]`)
            $input.addClass("is-invalid")
            const $parent = $input.parent()
            $parent.append(`<span class="text-danger" error-valid>${error}</span>`)
        }
        $(".is-invalid").first().focus()
    }

    function addMask() {
        maskShowTimes++
        if (maskShowTimes > 1) {
            return
        }
        const MASK_HTML =
            `<div class="ajax-mask">
            <div class="ajax-mask-animation"></div>
         </div>`
        $("body").append(MASK_HTML)
    }

    function removeMask() {
        maskShowTimes--
        if (maskShowTimes === 0) {
            $(".ajax-mask").remove()
        }
    }
})