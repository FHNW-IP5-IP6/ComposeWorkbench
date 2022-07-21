import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "ch.fhnw"
version = "1.0.0"

val sqliteVersion: String by project

dependencies {
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}


tasks.withType<Test> {
    useJUnitPlatform()
}
