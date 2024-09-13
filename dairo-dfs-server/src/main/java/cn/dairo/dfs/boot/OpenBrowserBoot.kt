package cn.dairo.dfs.boot

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component


/**
 * 项目启动打开浏览器
 */
@Order(Int.MAX_VALUE)//值越小越先执行
@Component
class OpenBrowserBoot : ApplicationRunner {

    /**
     * 运行端口
     */
    @Value("\${server.port}")
    private var port = 0

    override fun run(args: ApplicationArguments) {
    }
}