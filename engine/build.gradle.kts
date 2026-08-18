plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

group = "com.footballmanager"
version = "0.1.0"

application {
    mainClass.set("com.footballmanager.MainKt")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    // kotlin-test-junit5 transitively brings junit-jupiter-api + engine 5.10.1
    // and junit-platform-launcher 1.10.1, so useJUnitPlatform() works out of the box.
    testImplementation(kotlin("test-junit5"))
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        events("passed", "failed")
    }
}
