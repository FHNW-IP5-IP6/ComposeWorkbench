import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "ch.fhnw"
version = "1.0.0"

val sqliteVersion: String by project
val exposedVersion: String by project
dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":examples:real-estate:editor"))
    implementation(project(":examples:real-estate:explorer"))
    implementation(project(":examples:real-estate:AllPurpose")) //dont't understand why this is necessary, should be recognized as transitive dependency

    implementation(project(":lib"))

    implementation("org.xerial:sqlite-jdbc:$sqliteVersion")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ch.fhnw.estateagentworkbench"
            packageVersion = "1.0.0"
        }
    }
}