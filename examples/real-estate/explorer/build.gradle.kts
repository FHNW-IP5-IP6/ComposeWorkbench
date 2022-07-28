
import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "ch.fhnw"
version = "1.0.0"

val sqliteVersion: String by project

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material:material-icons-extended:1.1.1")

    implementation("org.xerial:sqlite-jdbc:$sqliteVersion")
    implementation("org.junit.jupiter:junit-jupiter:5.8.2")

    implementation(project(":examples:real-estate:db"))
    implementation(project(":examples:real-estate:AllPurpose"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

