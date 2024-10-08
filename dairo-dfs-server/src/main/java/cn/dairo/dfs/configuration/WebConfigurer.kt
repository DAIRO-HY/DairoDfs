package cn.dairo.dfs.configuration

import cn.dairo.dfs.interceptor.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.multipart.MultipartResolver
import org.springframework.web.multipart.support.StandardServletMultipartResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfigurer : WebMvcConfigurer {

    /**
     * 不拦截的静态资源文件
     */
    private val STATIC_FILES = listOf(
        "/plugins/**",
        "/app/res/**",
    )

    @Bean
    fun defaultInterceptor(): DefaultInterceptor {
        return DefaultInterceptor()
    }

    @Bean
    fun loginInterceptor(): LoginInterceptor {
        return LoginInterceptor()
    }

    @Bean
    fun adminInterceptor(): AdminInterceptor {
        return AdminInterceptor()
    }

    @Bean
    fun downloadInterceptor(): DownloadInterceptor {
        return DownloadInterceptor()
    }

    @Bean
    fun syncInterceptor(): SyncInterceptor {
        return SyncInterceptor()
    }

    @Bean
    fun shareInterceptor(): ShareInterceptor {
        return ShareInterceptor()
    }

    override fun addInterceptors(registry: InterceptorRegistry) {

        //全局拦截器
        registry.addInterceptor(defaultInterceptor())
            .excludePathPatterns(STATIC_FILES)
            .excludePathPatterns("/d/**")
            .addPathPatterns("/**")

        //登录拦截器
        registry.addInterceptor(loginInterceptor())
            .addPathPatterns("/app/**")//过滤拦截的请求
            .excludePathPatterns(STATIC_FILES)
            .excludePathPatterns(//不拦截的请求
                "/app/install/**",
                "/app/login/**",
                "/app/share/**"
            )

        //管理员拦截器
        registry.addInterceptor(adminInterceptor())
            .addPathPatterns(
                "/app/profile/**",
                "/app/user_list/**",
                "/app/user_edit/**",
                "/app/advanced/**",
            )//过滤拦截的请求

        //文件下载拦截器
        registry.addInterceptor(downloadInterceptor())
            .addPathPatterns("/d/**")

        //分布式同步
        registry.addInterceptor(syncInterceptor())
            .addPathPatterns("/distributed/**")

        //分享页面拦截器
        registry.addInterceptor(shareInterceptor())
            .addPathPatterns("/app/share/**")
    }
}
