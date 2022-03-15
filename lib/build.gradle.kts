import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.0.0"
}

group = "ch.fhnw"
version = "1.0.0"

kotlin {
    sourceSets {
        named("main") {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
