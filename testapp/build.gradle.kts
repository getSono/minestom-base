plugins {
    kotlin("jvm") version "2.1.20"
    id("com.gradleup.shadow") version "9.0.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
    implementation("io.github.juliarn", "npc-lib-minestom", "3.0.0-beta13")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    shadowJar {
        // set main class
        manifest {
            attributes(
                "Main-Class" to "live.einfachgustaf.minestom.base.testapp.EntrypointKt"
            )
        }
    }

    build {
        dependsOn(shadowJar)
    }
}