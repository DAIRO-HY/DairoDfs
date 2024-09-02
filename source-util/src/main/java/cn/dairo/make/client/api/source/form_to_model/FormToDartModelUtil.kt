package cn.dairo.make.client.api.source.form_to_model

import cn.dairo.dfs.extension.getField
import java.io.File
import kotlin.reflect.full.declaredMembers

/**
 * 将代码转为Swift的Modal代码
 */
object FormToDartModelUtil {

    /**
     * model文件存放目录
     */
    private const val SWIFT_TARGET_FOLDER = "C:/develop/project/idea/dairo-dfs-app/lib/api/model"
//    private const val SWIFT_TARGET_FOLDER = "/Users/zhoulq/dev/java/idea/gl_dfs_app/lib/api/model"

    /**
     * 将Form文件转swift的Modal代码
     * @param formFile kotlin表单Form代码
     * @param saveFolder 要保存的文件夹
     */
    fun start(formFile: File) {
        val content = String(formFile.inputStream().readAllBytes())

        //Swift类名
        val swiftModelName = formFile.name.replace("Form.kt", "Model")
        this.toCode(formFile, swiftModelName, content)
    }

    /**
     * 将Form文件转swift的Modal代码
     * @param swiftModelName Swift类名
     * @param content Form表单代码类容
     * @param saveFolder 要保存的文件夹
     */
    private fun toCode(formFile: File, swiftModelName: String, content: String) {

        //将kotlin的null转为swift的nil
        var content = content.replace("null", "nil")
        content = content.replace("\r\n", "\n")

        //swift中没有包名这个概念,直接删除
        content = content.replace(Regex("package.*\n"), "")

        //List类型转换
        //content = content.replace("List<", "[").replace(">", "]")

        //删除import代码
        content = content.replace(Regex("import.*\n"), "")

        //删除注解代码
        content = content.replace(Regex("@.*\n"), "")

        //修改注释格式
        content = content.replace(Regex(".*/\\*\\*.*\n"), "")
        content = content.replace(Regex(".*\\*/.*\n"), "")
        content = content.replace(Regex(".*\\*"), "///")

        //修改类名.将Form更改成Modal,并继承Codable
        content = content.replace("Form {", "Model extends JsonSerialize{")

        //将成员变量的Form更改成Modal
        content = content.replace("Form", "Model")

        content = content.split("\n").map {
            if (!it.contains(" var ")) {
                it
            } else {
                var line = it.replace(" var ", "")
                line = line.replace(" ", "")
                line = line.replace(Regex("=.*"), "")

                val vararr = line.split(":")
                line = vararr[1]
                    .replace("Long", "int")
                    .replace("Int", "int")
                    .replace("Boolean", "bool")
                line += " " + vararr[0] + ";"
                line
            }
        }.joinToString(separator = "\n") { it }


        File(this.SWIFT_TARGET_FOLDER).mkdirs()
        val saveFile = "${this.SWIFT_TARGET_FOLDER}/$swiftModelName.dart"

        //添加泛型model
        content.split("List<").forEach {
            val closeIndex = it.indexOf(">")
            if (closeIndex == -1) return@forEach
            val listModel = it.substring(0, it.indexOf(">"))
            content = "import '$listModel.dart';\n$content"
        }

        //添加构造函数及JSON反序列化函数
        val constructorAndConvertJsonCode = this.makeConstructorAndConvertJsonCode(formFile, swiftModelName)
        content = content.substring(0, content.lastIndexOf("}")) + constructorAndConvertJsonCode + "}"


        //写入文件
        File(saveFile).outputStream().use {
            it.write("\n/*工具自动生成代码,请勿手动修改*/\n".toByteArray())
            it.write("\nimport 'dart:convert';\n".toByteArray())
            it.write("\nimport '../../util/JsonSerialize.dart';\n".toByteArray())
            it.write(content.toByteArray())
        }
        println(File(saveFile).absolutePath)
    }

    private fun makeConstructorAndConvertJsonCode(formFile: File, swiftModelName: String): String {
        var clsName =
            formFile.readLines().find { it.contains("package") }!!.replace("package", "") + "." + formFile.name.replace(
                ".kt",
                ""
            )
        clsName = clsName.replace(" ", "")

        val constructor = StringBuilder()
        val fromJson = StringBuilder()
        val toJson = StringBuilder()
        Class.forName(clsName).kotlin.declaredMembers.forEach {
            val name = it.name
            constructor.append("required this.$name,")

            val type = it.returnType.getField("type").toString()
            if (type.startsWith("List")) {
                val listType = type.substring(type.indexOf("<") + 1, type.indexOf(">"))
                val listModel = listType.replace("Form", "Model")
                fromJson.append("$name: map[\"$name\"] == null ? null : $listModel.fromMapList(map[\"$name\"]),")
            } else {
                fromJson.append("$name: map[\"$name\"],")
            }
            toJson.append("\"${name}\" : this.$name,\n")
        }
        constructor.setLength(constructor.length - 1)
        fromJson.setLength(fromJson.length - 1)
        toJson.setLength(toJson.length - 1)

        val constructorMethod = "$swiftModelName({$constructor});\n"
        val fromJsonMethod = """
            static $swiftModelName fromJson(String json){
                Map<String,dynamic> map = jsonDecode(json);
                return $swiftModelName.fromMap(map);
            }
        """.trimIndent()

        val fromMapMethod = """
            static $swiftModelName fromMap(Map<String, dynamic> map){
                return $swiftModelName($fromJson);
            }
        """.trimIndent()

        val fromJsonListMethod = """
            static List<$swiftModelName> fromJsonList(String json){
                List<dynamic> list = jsonDecode(json);
                return $swiftModelName.fromMapList(list);
            }
        """.trimIndent()

        val fromMapListMethod = """
            static List<$swiftModelName> fromMapList(List<dynamic> list){
                return list.map((map) => $swiftModelName.fromMap(map)).toList();
            }
        """.trimIndent()


        val toJsonMethod = """
              @override
              toJson()=> {
                $toJson
                };
        """.trimIndent()

        return constructorMethod + fromJsonMethod + fromMapMethod + fromJsonListMethod + fromMapListMethod + toJsonMethod
    }
}