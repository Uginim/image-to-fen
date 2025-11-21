plugins {
    kotlin("jvm")
}

dependencies {
    // OpenCV for board detection and image processing
    implementation("org.openpnp:opencv:4.7.0-0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
