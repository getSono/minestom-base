plugins {
    kotlin("jvm") version "2.1.20"
    id("com.gradleup.shadow") version "9.0.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
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