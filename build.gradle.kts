

plugins {
    kotlin("jvm") version "1.5.31"
}

group = "ch.fhnw"
version = "1.0.0"

allprojects {
	repositories {
		mavenCentral()
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
		maven ("https://jitpack.io")

	}
}