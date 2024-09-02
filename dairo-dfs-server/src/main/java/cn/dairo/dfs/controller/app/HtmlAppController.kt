package cn.dairo.dfs.controller.app

import cn.dairo.dfs.controller.base.AppBase
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

/**
 * 管理员登录画面
 */
@Controller
@RequestMapping("/app")
class HtmlAppController : AppBase() {

    /**
     * 页面初始化
     */
    @GetMapping("/{html}")
    fun html(@PathVariable html: String): String {
        return "app/" + html
    }
}
