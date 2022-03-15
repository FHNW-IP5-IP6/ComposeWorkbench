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
                implementation(project(":demo:editor"))
                implementation(project(":demo:explorer"))
            }
        }
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "ch.fhnw.composeWorkbench.demo.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ch.fhnw.composeWorkbench.demo"
            packageVersion = "1.0.0"
        }
    }
}