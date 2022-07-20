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
include(":demo:explorer")
include(":demo:editor")
include(":demo:demoApps")
include(":demo:database")
include(":demo-dh:workbench")
include(":demo-dh:editor")
include(":demo-dh:explorer")
include("demo-dh:db")
findProject(":demo-dh:db")?.name = "db"
