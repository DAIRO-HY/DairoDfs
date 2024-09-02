package cn.dairo.dfs.controller.base

import cn.dairo.dfs.code.ErrorCode
import cn.dairo.dfs.exception.BusinessException
import cn.dairo.dfs.util.ServletTool
import cn.dairo.lib.Json
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.PrintWriter

/**
 * Controller的异常处理
 */
open class AjaxBase {

    /**
     * 子类全局异常处理
     *
     * @param e
     */
    @ExceptionHandler
    fun exceptionHandle(e: Exception) {
        if (e is BusinessException) {//逻辑错误,直接提示
            jsonOut(e)
        } else if (e is BindException) {
            val fieldError = e.bindingResult.allErrors
                .groupBy({ (it as FieldError).field }, { (it as FieldError).defaultMessage })
            val bizError = ErrorCode.PARAM_ERROR
            bizError.data = fieldError
            this.jsonOut(bizError)
        } else {
            //addErrorLog(e)
            throw e
        }
    }

    /**
     * @param e
     * @作者 周龙权
     * @创建时间 2017年2月7日 下午8:33:01
     * @描述 未处理的异常, 添加到错误日志
     */
    private fun addErrorLog(e: Exception) {
        var flag = false
        val sb = StringBuilder()
        try {
            sb.append("错误信息:").append(e.message).append("\t")

            sb.append("错误类型:").append(e.javaClass).append("\t")
            sb.append("出错的类:").append(e.stackTrace[0]).append("\t")

            // 获取ServletRequest对象
            val request = ServletTool.request

            // 当前请求完整路劲 不包括参数部分
            val url = request.requestURL.toString()

            // 访问服务器所带有的参数信息
            val query = request.queryString

            sb.append("当前访问的URL:").append(url)
            if (query != null) {
                sb.append("?").append(query)
            }
            sb.append("\t")

            sb.append("所有参数:")
            val paramMap = request.parameterMap
            for (key in paramMap.keys) {
                val value = paramMap[key]
                sb.append(key).append("=").append(value).append(",")
            }

            sb.append("\t")
            sb.append("提交方式:").append(request.method).append("\t")

//            val m = GlLogcatBean()
//            m.type = "error"
//            m.content = sb.toString()
//
//            // 保存错误到日志
//            BootService.service.glLogcatService.add(m)
            flag = true
        } catch (e2: Exception) {
            //这里应该保存文件
            e2.printStackTrace()
        }
        val bizError: BusinessException
        if (flag) {
            bizError = ErrorCode.SYSTEM_ERROR
        } else {
            bizError = ErrorCode.SYSTEM_ERROR_NO_LOG
        }
        bizError.data = sb.toString()
        jsonOut(bizError)
    }

    /**
     * json输出
     *
     * @param code 结果代码
     * @param msg  消息
     * @param data 数据
     */
    private fun jsonOut(bizError: BusinessException) {
        val errorResult = mapOf(
            "code" to bizError.code,
            "msg" to bizError.message,
            "data" to bizError.data
        )
        val request = ServletTool.request
        val method = request.method
        if (method == "GET") {//get请求时发生异常
            request.setAttribute("error", errorResult)
            request.getRequestDispatcher("/error").forward(request, ServletTool.response)
            return
        }
        val rs = Json.writeValueAsString(errorResult)
        textOut(rs)
    }

    /**
     * 文本输出
     *
     * @param rs
     */
    private fun textOut(rs: String, status: Int = 500) {
        val response = ServletTool.response

        //设置contentType
        response.contentType = "text/json;charset=utf-8"
        response.status = status
        var out: PrintWriter? = null
        try {
            out = response.writer
            out!!.print(rs)
            saveLog(rs)
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
//            if (out != null) {
//                try {
//                    out.flush()
//                } catch (e2: Exception) {
//                }
//
//                try {
//                    out.close()
//                } catch (e2: Exception) {
//                }
//            }
        }
    }

    /**
     * 保存日志
     */
    private fun saveLog(rs: String) {
//        val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request
//        var url = request.requestURL.toString()
//        if (url.contains("/SqliteDb/SqliteDbReview/query")) {
//            return
//        }
//        val startTime = request.getAttribute("_startTime") as Long
//        val sc = System.currentTimeMillis() - startTime//时间差
//        val queryString = request.queryString//得到参数部分
//        url += if (queryString.isNullOrEmpty()) "" else "?$queryString"
//        val params = request.parameterMap

//        val bean = ApiResultLogDao.Bean()
//        bean.url = url
//        bean.param = Json.writeValueAsString(params)
//        bean.rs = rs
//        bean.start_time = GL.getFormatDate(startTime)
//        bean.use_time = sc.toInt()
//        ApiResultLogDao.add(bean)
    }
}