package cn.dairo.make.client.api.source.controller_to_api

import java.io.File

class ControllerToClientHttpApiToDartUtil(private val apiInfoMap: Map<String, List<ApiInfo>>) {

    /**
     * model文件存放目录
     */
    private val DART_TARGET_FOLDER = "C:/develop/project/idea/dairo-dfs-app/lib/api"
//    private val DART_TARGET_FOLDER = "/Users/zhoulq/dev/java/idea/gl_dfs_app/lib/api"

    init {
        val apiConstCode = this.makeApiConst()

        //API接口地址静态文件路径
        val apiConstPath = "$DART_TARGET_FOLDER/API.dart"

        //保存到文件
        File(apiConstPath).outputStream().write(apiConstCode.toByteArray())
        println(apiConstPath)

        apiInfoMap.forEach { (k, v) ->
            println("----------------------------------------------------------")
            val className = k.replace("AppController", "Api")
            val httpUtilCode = this.makeApiUtilClass(className, v)

            //API请求工具类存放文件
            val apiUtilPath = "$DART_TARGET_FOLDER/$className.dart"

            //保存到文件
            File(apiUtilPath).outputStream().write(httpUtilCode.toByteArray())
            println(apiUtilPath)
        }
    }

    /**
     * 生成api常量类
     */
    private fun makeApiConst(): String {
        val constSb = StringBuilder()
//        constSb.append("import '../../Const.dart';\n\nclass Api{").append("\n").append("\n")
        constSb.append("class Api{").append("\n").append("\n")
        this.apiInfoMap.forEach { (_, v) ->
            v.forEach {
                if (it.constVar.contains("{")) {
                    return@forEach
                }
                it.summary?.also {
//                    constSb.append("/**").append("\n")
//                    constSb.append(" * ").append(it).append("\n")
//                    constSb.append(" */").append("\n")
                    constSb.append("///").append(it).append("\n")
                }
//                constSb.append("static const ${it.constVar} = \"\${Const.DOMAIN}${it.path}\";").append("\n")
                constSb.append("static const ${it.constVar} = \"${it.path}\";").append("\n")
                    .append("\n")
            }
        }
        constSb.append("}")
        return constSb.toString()
    }

    /**
     * 生成HTTP请求工具类
     */
    private fun makeApiUtilClass(className: String, apiList: List<ApiInfo>): String {
        val httpUtilSb =
            StringBuilder("class $className{").append("\n")
                .append("\n")

        val importSet = HashSet<String>()

        ////便利该类下面所有的api
        apiList.forEach { apiInfo ->
            httpUtilSb.append("///").append(apiInfo.summary).append("\n")
            apiInfo.paramList!!.forEach {//遍历该API所有的参数,用来生成注释
                httpUtilSb.append("/// [").append(it.name).append("] ").append(it.description).append("\n")
            }

            //返回值类型
//            val apiReturnType = if (apiInfo.returnType.startsWith("List<")) {
//                "[" + apiInfo.returnType.substring(5, apiInfo.returnType.length - 1) + "]"
//            } else {
//                apiInfo.returnType
//            }
            val apiReturnType = apiInfo.returnType
            var listModel = ""//返回的是一个列表对象时的form类型
            if (apiReturnType.startsWith("List")) {//抽出List的泛型类型
                listModel = apiInfo.returnType.substring(5, apiInfo.returnType.length - 1)
                if (listModel == "String") {//如果返回值是一个List<String>
                    importSet.add("import '../util/http/ApiHttp.dart';")
                } else {
                    importSet.add("import 'model/$listModel.dart';")
                }
            }

            //要返回的API请求类型
            var modelType: String
            if (apiReturnType == "Unit") {
                modelType = "String"
            } else if (apiInfo.returnTypeIsMarkedNullable) {//允许返回NULL
                modelType = "${apiReturnType}"
            } else {
                modelType = "${apiReturnType}"
            }

            if (modelType.endsWith("Model")) {
                importSet.add("import 'model/$modelType.dart';")
            } else {
                modelType = toDartType(modelType)
            }

            //接口返回类类型
            val returnClass: String
            if (apiReturnType == "Unit") {
                returnClass = "VoidApiHttp"
                importSet.add("import '../util/http/VoidApiHttp.dart';")
            } else if (apiInfo.returnTypeIsMarkedNullable) {//允许返回NULL
                returnClass = "NullApiHttp<${modelType}?>"
                importSet.add("import '../util/http/NullApiHttp.dart';")
            } else {
                returnClass = "NotNullApiHttp<${modelType}>"
                importSet.add("import '../util/http/NotNullApiHttp.dart';")
            }

            httpUtilSb.append("static $returnClass ").append(apiInfo.name).append("(")

            //参数部分的代码
            val paramCode = apiInfo.paramList!!.joinToString(separator = ", ") {
                var paramType = it.type!!
                if (paramType.startsWith("List")) {//参数是一个List类型
                    var paramListType = paramType.substring(5, paramType.length - 1)
                    paramListType = this.toDartType(paramListType)
                    paramType = "List<$paramListType>"
                } else {
                    paramType = this.toDartType(paramType)
                }
                val p = if (it.isRequired) {
                    "required $paramType ${it.name}"
                } else {
                    "$paramType? ${it.name}"
                }
                p
            }
            if (paramCode.isEmpty()) {
                httpUtilSb.append(paramCode)
            } else {
                httpUtilSb.append("{").append(paramCode).append("}")
            }

            httpUtilSb.append("){").append("\n")

            //Json转对象代码
            val fromJsonMethod = if (modelType.endsWith("Model")) {//返回的是一个form
                ",$modelType.fromJson"
            } else if (modelType.startsWith("List")) {//返回的是一个form列表
                if (listModel == "String") {//如果返回的是一个字符串数组
                    ",StringExt.fromJsonList"
                } else {
                    ",$listModel.fromJsonList"
                }
            } else {
                ""
            }
            httpUtilSb.append(" return $returnClass(Api.${apiInfo.constVar}$fromJsonMethod)")

            //遍历该API所有的参数,用来传递参数
            apiInfo.paramList!!.forEach {
                httpUtilSb.append(".add(\"").append(it.name).append("\",").append(it.name).append(")")
            }
            httpUtilSb.append(";\n")
            httpUtilSb.append("}").append("\n").append("\n")
        }
        httpUtilSb.append("}")

        importSet.forEach {
            httpUtilSb.insert(0, "$it\n")
        }
        httpUtilSb.insert(0, "import 'API.dart';\n")
        return httpUtilSb.toString()
    }


    /**
     * 将Kotlin数据类型转换成Dart数据类型
     */
    private fun toDartType(type: String) = when (type) {
        "Integer" -> "int"
        "Long" -> "int"
        "Int" -> "int"
        "Boolean" -> "bool"
        //"List<Long>" -> "List<int>"
        else -> type
    }
}