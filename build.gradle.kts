

plugins {
    kotlin("jvm") version "1.6.10"
}

group = "ch.fhnw"
version = "1.0.0"

allprojects {
	repositories {
		google()
		mavenCentral()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
		maven ("https://jitpack.io")
	}
}