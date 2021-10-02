import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.kubicz.mavenexecutor"
version = "2.1.1-SNAPSHOT"

buildscript {
    repositories { mavenCentral() }
}

plugins {
    id("org.jetbrains.intellij") version "0.6.5"
    kotlin("jvm") version "1.5.31"
}

intellij {
    updateSinceUntilBuild = false
    instrumentCode = true
    version = "2021.2"

    setPlugins("java", "maven")
}


repositories {
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
        languageVersion = "1.5"
        apiVersion = "1.5"
    }
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}