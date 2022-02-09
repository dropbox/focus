import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.dokka") version "1.6.10"
    id("com.vanniktech.maven.publish") version "0.18.0"
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.8.0"
}

buildscript {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "1.8"
    // Because Gradle's Kotlin handling is stupid, this falls out of date quickly
    apiVersion = "1.5"
    languageVersion = "1.5"

    // We use class SAM conversions because lambdas compiled into invokedynamic are not
    // Serializable, which causes accidental headaches with Gradle configuration caching. It's
    // easier for us to just use the previous anonymous classes behavior
    @Suppress("SuspiciousCollectionReassignment")
    freeCompilerArgs += "-Xsam-conversion=class"
  }
}

gradlePlugin {
  plugins {
    plugins.create("focus") {
      id = "com.dropbox.focus"
      implementationClass = "com.dropbox.focus.FocusPlugin"
    }
  }
}

tasks.register("printVersionName") {
  doLast {
    println VERSION_NAME
  }
}
