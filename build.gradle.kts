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
    api("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.10.2")
    api("com.github.shynixn.mccoroutine", "mccoroutine-minestom-api", "2.22.0")
    api("com.github.shynixn.mccoroutine", "mccoroutine-minestom-core", "2.22.0")
    api("org.slf4j", "slf4j-api", "2.0.17")
    implementation("ch.qos.logback", "logback-classic", "1.5.18")
    implementation("org.fusesource.jansi", "jansi", "2.4.2")

    implementation("net.scrayos", "agones-client-sdk", "5.1.2")
    runtimeOnly("io.grpc", "grpc-netty", "1.75.0")

    // Ktor for Open Match HTTP endpoints
    implementation("io.ktor", "ktor-server-core", "3.0.3")
    implementation("io.ktor", "ktor-server-netty", "3.0.3")
    implementation("io.ktor", "ktor-server-content-negotiation", "3.0.3")
    implementation("io.ktor", "ktor-serialization-kotlinx-json", "3.0.3")
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

    /*
    repositories {
        maven {
            url = uri("https://gl.einfachgustaf.live/api/v4/projects/14/packages/maven")

            credentials(HttpHeaderCredentials::class) {
                name = "egl-gitlab_maven"
                value =
                    findProperty("egl-gitlab_maven") as String? // the variable resides in $GRADLE_USER_HOME/gradle.properties
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }
        }
    }
     */
}