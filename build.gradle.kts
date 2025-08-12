plugins {
    kotlin("jvm") version "2.1.20"
    id("maven-publish")
}

allprojects {
    group = "live.einfachgustaf"
    version = "1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    api("net.minestom", "minestom", "2025.08.12-1.21.8")
    api("org.slf4j", "slf4j-api", "2.0.17")
    implementation("ch.qos.logback", "logback-classic", "1.5.18")
    implementation("org.fusesource.jansi", "jansi", "2.4.2")
}

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}