package cn.dairo.dfs.configuration

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * 全局获取上下文工具类
 *
 * @author zhoulq
 * @date 2026/06/09
 */
@Component
class ApplicationContextProvider : ApplicationContextAware {
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    companion object {
        private lateinit var context: ApplicationContext

        val applicationContext: ApplicationContext
            get() = context
    }
}