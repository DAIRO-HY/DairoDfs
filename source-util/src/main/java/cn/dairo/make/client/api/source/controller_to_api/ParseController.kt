package cn.dairo.make.client.api.source.controller_to_api

import cn.dairo.make.client.api.source.ClassUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.javaType
import kotlin.reflect.jvm.javaType

object ParseController {

    private const val CONTROLLER_PACKAGE = "cn.dairo.dfs.controller.app"

    /**
     * 排除的类
     */
//    private val EXCLUDE_LIST = setOf(MusicUploadClientController::class.qualifiedName)
    private val EXCLUDE_LIST = setOf("nothing")

    fun start() {
        val classNameToApiInfo = LinkedHashMap<String, List<ApiInfo>>()
        ClassUtil.findClass(CONTROLLER_PACKAGE).forEach { className ->
            if (!className.endsWith("Controller")) {
                return@forEach
            }
            if (EXCLUDE_LIST.contains(className)) {//排除的类
                return@forEach
            }

            //内部api情报列表
            val apiList = ArrayList<ApiInfo>()
            val clsKotlin = Class.forName(className).kotlin
            val deprecated = clsKotlin.annotations.find { it is Deprecated }
            if (deprecated != null) {//该类已经过时
                return@forEach
            }

            //得到类上的注解RequestMapping，获取上级url路径
            val requestMapping = clsKotlin.annotations.find { it is RequestMapping }!! as RequestMapping

            if (requestMapping.value.isEmpty()) {//类上的RequestMapping没有标记path
                return@forEach
            }

            //得到类注解伤的URL
            val path1 = requestMapping.value[0]
            clsKotlin.declaredMemberFunctions.forEach { method ->

                val annotations = method.annotations
                if (annotations.find { it is Deprecated } != null) {//该方法已经被标记过时
                    return@forEach
                }

                //得到函数名上的PostMapping注解
                val postMapping = annotations.find { it is PostMapping } as PostMapping? ?: return@forEach

                //函数注解上配置路径列表
                val methodPathList = postMapping.value

                //函数注解上配置路径
                val path2 = if (methodPathList.isEmpty()) {//方法上没有设置路径,使用类的路径
                    ""
                } else {
                    methodPathList[0]
                }
                if (path2.contains("{")) {//过滤掉@PathVariable的函数
                    return@forEach
                }

                //得到函数名上的Operation注解，从该注解上获取说明文档
                val summaryAnno = method.annotations.find { it is Operation } as Operation?
                val summary = summaryAnno?.summary

                val api = ApiInfo()

                //函数名
                api.name = method.name

                //api路径
                api.path = path1 + path2

                //api说明
                api.summary = summary

                //返回类型
                val typeField = method.returnType::class.java.declaredFields.find { it.name == "type" }!!
                typeField.isAccessible = true
                val returnType = typeField.get(method.returnType).toString()

                //将form替换成Model
                api.returnType = returnType.replace("Form", "Model").replace("?", "")

                //是否标记返回类型允许为空
                api.returnTypeIsMarkedNullable = method.returnType.isMarkedNullable

                //api参数列表
                val paramList = ArrayList<ApiParam>()
                method.parameters.forEach {

                    val typeField = it.type::class.java.declaredFields.find { it.name == "type" }!!
                    typeField.isAccessible = true
                    val paramType = typeField.get(it.type).toString().replace("?", "")

                    if (it.name == null || it.name!!.startsWith("_") || paramType == "MultipartFile") {
                        return@forEach
                    }

                    if (paramType.endsWith("Form")) {//这是一个Form类
                        val formClass = Class.forName(it.type.toString())
                        formClass.declaredFields.map { formField ->
                            var type =
                                formClass.kotlin.declaredMemberProperties.find { it.name == formField.name }!!.returnType.toString()
                            type = type.replace("kotlin.", "").replace("?", "")
                            val lastDotIndex = type.lastIndexOf(".")
                            if (lastDotIndex != -1) {
                                type = type.substring(lastDotIndex + 1)
                            }
                            val apiParam = ApiParam()
                            apiParam.name = formField.name
                            apiParam.isRequired = formField.annotations.any { it is NotBlank || it is NotEmpty }
                            apiParam.type = type

                            //注释
                            apiParam.description =
                                (formField.annotations.find { it is Parameter } as Parameter?)?.description
                            paramList.add(apiParam)
                        }
                    } else {
                        val requestParamAnn = it.annotations.find { it is RequestParam } as RequestParam?

                        //参数key
                        val name = requestParamAnn?.value ?: return@forEach
                        if (name.startsWith("_")) {//_开头代表公共参数，无需生成到API中
                            return@forEach
                        }

                        //是否必须参数
                        val isRequired = requestParamAnn?.required ?: false

                        //参数说明
                        val parameterAnn = it.annotations.find { it is Parameter } as Parameter?
                        val description = parameterAnn?.description
                        val apiParam = ApiParam()
                        apiParam.name = name
                        apiParam.isRequired = isRequired
                        apiParam.type = paramType
                        apiParam.description = description
                        paramList.add(apiParam)
                    }
                }

                //api参数列表
                api.paramList = paramList
                apiList.add(api)
            }

            //类名
            val className = clsKotlin.simpleName!!
            classNameToApiInfo[className] = apiList
        }

        //生成Swift代码
        ControllerToClientHttpApiToDartUtil(classNameToApiInfo)
    }
}

/**
 * Api接口信息
 */
class ApiInfo {

    /**
     * 函数名
     */
    var name: String? = null

    /**
     * Api路径
     */
    var path: String? = null

    /**
     * API说明
     */
    var summary: String? = null

    /**
     * API需要的参数
     */
    var paramList: List<ApiParam>? = null

    /**
     * API返回数据
     */
    var returnType = ""

    /**
     * API返回数据
     */
    var returnTypeIsMarkedNullable = false

    /**
     * API路径静态变量
     */
    val constVar get() = this.path!!.replace("/app/", "").replace("/", "_").uppercase().replace("-", "_")
}

class ApiParam {

    /**
     * 参数类型
     */
    var type: String? = null

    /**
     * 参数名
     */
    var name: String? = null

    /**
     * 参数说明
     */
    var description: String? = null

    /**
     * 是否
     */
    var isRequired = false
}
