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

    apiVersion = "1.5"
    languageVersion = "1.5"

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
