import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "ch.fhnw"
version = "1.0.0"

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(project(":lib"))
    implementation("org.jetbrains.compose.material:material-icons-extended:1.1.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "ch.fhnw.composeWorkbench.examples.hello-workbench.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ch.fhnw.composeWorkbench.examples.hello-workbench"
            packageVersion = "1.0.0"
        }
    }
}