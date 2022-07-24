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

dependencies {
    implementation(compose.desktop.currentOs)

    implementation("org.jetbrains.compose.material:material-icons-extended:1.1.1")

    implementation("org.xerial:sqlite-jdbc:$sqliteVersion")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.0")

    implementation("org.junit.jupiter:junit-jupiter:5.8.2")

    implementation(project(":demo-dh:db"))
    implementation(project(":demo-dh:AllPurpose"))
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}


tasks.withType<Test> {
    useJUnitPlatform()
}
