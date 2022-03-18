
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.compose.compose
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.0.0"
}

group = "ch.fhnw"
version = "1.0.0"

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        events.add(TestLogEvent.PASSED)
        events.add(TestLogEvent.FAILED)
        events.add(TestLogEvent.SKIPPED)
        showStandardStreams = true
    }
}
