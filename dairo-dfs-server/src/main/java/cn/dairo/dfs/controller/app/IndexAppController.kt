package cn.dairo.dfs.controller.app

import cn.dairo.dfs.controller.base.AppBase
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

/**
 * 管理员登录画面
 */
@Controller
@RequestMapping
class IndexAppController : AppBase() {

    /**
     * 页面初始化
     */
    @GetMapping
    fun execute() = "redirect:/app/files"

    /**
     * 页面初始化
     */
    @GetMapping("/app")
    fun html() = "redirect:/app/files"
}
