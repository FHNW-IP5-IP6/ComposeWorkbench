rootProject.name = "ComposeWorkbench"
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }

    plugins {
        kotlin("jvm").version(extra["kotlin.version"] as String)
        kotlin("multiplatform").version(extra["kotlin.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
    }
}

include(":lib")
include(":examples:hello-workbench:workbench")
include(":examples:cities:explorer")
include(":examples:cities:editor")
include(":examples:cities:workbench")
include(":examples:cities:db")
include(":examples:real-estate:workbench")
include(":examples:real-estate:editor")
include(":examples:real-estate:explorer")
include(":examples:real-estate:db")
findProject(":examples:real-estate:db")?.name = "db"
include(":examples:real-estate:AllPurpose")
findProject(":examples:real-estate:AllPurpose")?.name = "AllPurpose"
