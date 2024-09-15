import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21"
}

group = "cn.dairo.dfs"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    //maven("https://maven.aliyun.com/nexus/content/groups/public/")
    mavenCentral()
}

springBoot {
    mainClass.set("cn.dairo.dfs.DairoApplicationKt")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    //热部署
    //稍微解决与redis反序列化的冲突,暂时无法使用,后续跟进
    //引入该依赖会导致重启session不会丢失
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.1")
    implementation("org.springdoc:springdoc-openapi-ui:1.7.0")

// https://mvnrepository.com/artifact/org.apache.commons/commons-imaging
    implementation("org.apache.commons:commons-imaging:1.0.0-alpha5")

    implementation("commons-io:commons-io:2.7")
    implementation("org.dom4j:dom4j:2.1.4")

    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
    implementation(project(":dairo-common"))

    implementation(fileTree("libs") {
        include("*.jar")
    })

    //引入外部依赖,目的:减小git仓库体积
    implementation(files(downloadJar("https://github.com/DAIRO-HY/DairoDfsLib/raw/main/lib-psd-rebuild-24.6-all.jar")))
    implementation(files(downloadJar("https://github.com/DAIRO-HY/DairoDfsLib/raw/main/lib-imaging-rebuild-24.6-all.jar")))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

/**
 * 下载jar包
 */
fun downloadJar(jarUrl: String): String {
    val jarName = jarUrl.substring(jarUrl.lastIndexOf("/") + 1)
    val tempFolder = File(System.getProperty("java.io.tmpdir"))

    //jar存储文件
//    val jarFile = file("${rootDir.absolutePath}/.gradle/libs/$jarName")
    val jarFile = file(tempFolder.path + "/" + jarName)
    if (!jarFile.parentFile.exists()) {
        jarFile.parentFile.mkdirs()
    }
    if (jarFile.exists()) {//如果文件已经存在,直接返回
        println("----------->implementation $jarFile")
        return jarFile.absolutePath
    }

    //下载到临时文件
    val tempFile = file(jarFile.path + ".temp")
    println("----------->Downloading $jarFile")
    URL(jarUrl).openStream().use { iStream ->
        tempFile.outputStream().use {
            iStream.transferTo(it)
        }
    }
    println("----------->Downloaded $jarFile")
    tempFile.renameTo(jarFile)
    return jarFile.absolutePath
}