import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "org.kubicz.mavenexecutor"
version = "1.0.2-SNAPSHOT"

buildscript {
    repositories { mavenCentral() }
    dependencies { classpath(kotlin("gradle-plugin", "1.2.30")) }
}

plugins {
    id("org.jetbrains.intellij") version "0.3.12"
    kotlin("jvm") version "1.2.30"
}

intellij {
    updateSinceUntilBuild = false
    instrumentCode = true
    version = "2018.2.5"

    setPlugins("maven")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        languageVersion = "1.2"
        apiVersion = "1.2"
    }
}