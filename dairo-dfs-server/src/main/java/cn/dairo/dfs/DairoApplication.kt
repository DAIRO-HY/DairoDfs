package cn.dairo.dfs

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

//@OpenAPIDefinition(info = Info(title = "API文档", version = "1.0", description = "API接口文档"))
@MapperScan("cn.dairo.dfs.dao")
@SpringBootApplication
class DairoApplication

fun main(args: Array<String>) {
    runApplication<DairoApplication>(*args)
}
