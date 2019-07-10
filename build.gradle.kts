plugins {
    id("org.jetbrains.kotlin.jvm").version("1.3.20")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.20")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.9.8")

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.20")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.3.20")
    testImplementation("org.assertj:assertj-core:3.12.1")
}

application {
    mainClassName = "utterancegenerator.MainKt"
}
