plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "ZigBrains"

include("core")
project(":core").projectDir = file("modules/core")