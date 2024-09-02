// 仿java
String.prototype.startWith = function (str) {
    if (str == null || str == "" || this.length == 0
        || str.length > this.length)
        return false;
    if (this.substr(0, str.length) == str)
        return true;
    else
        return false;
    return true;
};

// 仿java
String.prototype.endWith = function (str) {
    if (str == null || str == "" || this.length == 0
        || str.length > this.length)
        return false;
    if (this.substring(this.length - str.length) == str)
        return true;
    else
        return false;
    return true;
};

/**
 * 数据流量单位换算
 */
Number.prototype.toDataSize = function (fraction = 2) {
    if (this == null) {
        return "0B"
    }
    const value = this
    if (value >= 1024 * 1024 * 1024 * 1024) {
        return (this / (1024 * 1024 * 1024 * 1024)).toFixed(fraction) + "TB"
    }
    if (value >= 1024 * 1024 * 1024) {
        return (this / (1024 * 1024 * 1024)).toFixed(fraction) + "GB"
    }
    if (value >= 1024 * 1024) {
        return (this / (1024 * 1024)).toFixed(fraction) + "MB"
    }
    if (value >= 1024) {
        return (this / (1024)).toFixed(fraction) + "KB"
    }
    return this.toFixed(fraction) + "B"
}

$(function () {
    if ($(".navbar").length > 0) {
        initTopBar();
    }
});


/**
 * 退出登录
 */
function logout() {
    $.ajaxByData({
        url: "/app/login/logout",
        type: "POST"
    }, (data) => {
        window.location.href = "/app/login"
    })
}

/**
 * 重置账户
 */
function reinit() {
    $.ajaxByData({
        url: "/app/index/reinit",
        type: "POST"
    }, () => {
        window.location.href = "/app/login"
    })
}