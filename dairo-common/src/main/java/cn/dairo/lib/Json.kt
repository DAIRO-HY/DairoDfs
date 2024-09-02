package cn.dairo.lib

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

object Json {

    /**
     * 实例化ObjectMapper需要时间 如果使用频繁可以使用静态实例化,
     * 这样速度会比较快,原因是:虽然ObjectMapper解析比较快,但是实例化的时间较慢
     * ,所以建议静态,经过测试了,内存开销很小,一个实例占用内存在4kb左右,不用担心内存开销
     *
     */
    val mapper: ObjectMapper = object : ObjectMapper() {
        init {

            //配置支持DB的LocalDateTime
            registerModule(JavaTimeModule())

            //配置将日期类型转换成:2021-12-30T13:11:17
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

            //有时候，返回的JSON字符串中含有我们并不需要的字段，那么当对应的实体类中不含有该字段时，会抛出一个异常，告诉你有些字段没有在实体类中找到。解决办法很简单，在声明ObjectMapper之后，加上上述代码：
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            //配置返回的数据中,值null的字段不要返回
            setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
        }
    }

    /**
     * 读取内容到JsonNode对象
     * @param content Json字符串
     * @return JsonNode对象
     */
    fun readValue(content: String): JsonNode {
        return mapper.readTree(content)
    }

    /**
     * 读取内容到object对象
     * @param content Json字符串
     * @param valueType 目标类型
     * @return 目标对象
     */
    fun <T> readValue(content: String, valueType: Class<T>): T {
        return mapper.readValue(content, valueType)
    }

    /**
     * 读取内容到object对象
     * @param content JsonNode对象
     * @param valueType 目标类型
     * @return 目标对象
     */
    fun <T> readValue(content: JsonNode, valueType: Class<T>): T {
        return mapper.treeToValue(content, valueType)
    }

    /**
     * 读取内容到List<Bean>
     * @param content Json字符串
     * @param valueType 目标类型
     * @return List对象
    </Bean> */
    fun <T> readList(content: String, valueType: Class<T>): List<T> {
        val javaType = mapper.typeFactory.constructParametricType(
            ArrayList::class.java, valueType
        )
        return mapper.readValue(content, javaType) // 这里不需要强制转换
    }

    /**
     * 读取内容到List<Bean>
     * @param content JsonNode对象
     * @param valueType 目标类型
     * @return List对象
    </Bean> */
    fun <T> readList(content: JsonNode, valueType: Class<T>): List<T> {
        val javaType = mapper.typeFactory.constructParametricType(
            ArrayList::class.java, valueType
        )
        return mapper.convertValue(content, javaType) // 这里不需要强制转换
    }

    /**
     * 将Object对象写入json字符串
     *
     * @param obj
     * @return
     */
    fun writeValueAsString(obj: Any): String {
        return mapper.writeValueAsString(obj)
    }
}
