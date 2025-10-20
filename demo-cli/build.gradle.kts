plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":core"))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.example.fenvision.demo.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
