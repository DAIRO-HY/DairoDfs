import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    kotlin("jvm") version "1.8.21"
}

//println(rootProject.extra["kotlin_version"])

group = "cn.dairo.common"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("mysql:mysql-connector-java:8.0.33")

    implementation("commons-io:commons-io:2.6")
    implementation("org.dom4j:dom4j:2.1.4")

    implementation("org.xerial:sqlite-jdbc:3.40.1.0")
    implementation("org.apache.httpcomponents:httpmime:4.5.12")
    implementation("javax.mail:javax.mail-api:1.6.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")
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
