package cn.dairo.dfs

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

//@OpenAPIDefinition(info = Info(title = "API文档", version = "1.0", description = "API接口文档"))
@MapperScan("cn.dairo.dfs.dao")
@SpringBootApplication
@EnableScheduling//启动定时任务
class DairoApplication

fun main(args: Array<String>) {
    runApplication<DairoApplication>(*args)
}
