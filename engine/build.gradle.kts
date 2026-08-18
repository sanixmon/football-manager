plugins {
    kotlin("jvm")
}

group = "com.footballmanager"
version = "0.1.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    // kotlin-test-junit5 transitively brings junit-jupiter-api + engine 5.10.1
    // and junit-platform-launcher 1.10.1, so useJUnitPlatform() works out of the box.
    testImplementation(kotlin("test-junit5"))
}

tasks.test {
    useJUnitPlatform()
}
