/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.7.1/userguide/building_java_projects.html
 */

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    application
    java
    kotlin("jvm") version "1.4.31" // 确保添加 Kotlin
    kotlin("plugin.serialization") version "1.4.31"
}

repositories {
    mavenLocal()
    // Use JCenter for resolving dependencies.
    maven("https://maven.aliyun.com/repository/public") // 阿里云国内代理仓库
    //maven { url = uri("https://dl.bintray.com/karlatemp/misc") }
    mavenCentral()
    jcenter()
}

dependencies {
    val miraiVersion = "2.6.7"
    api("net.mamoe", "mirai-core-api", miraiVersion)     // 编译代码使用
    runtimeOnly("net.mamoe", "mirai-core", miraiVersion) // 运行时使用
    //api("net.mamoe:mirai-login-solver-selenium:1.0-dev-16")
    //implementation(fileTree(mapOf("dir" to "libs","include" to listOf("*.jar"))))
    implementation("mysql:mysql-connector-java:8.0.22")
    implementation("com.alibaba:fastjson:1.2.53")
}

application {
    // Define the main class for the application.
    mainClass.set("bot.mirai.App")
}
