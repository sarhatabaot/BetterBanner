import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "8.3.6"
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly(libs.spigot.api)
    implementation(libs.bstats.bukkit)
    compileOnly(libs.jetbrains.annotations)
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release.set(8)
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveVersion.set(project.version.toString())
        relocate("org.bstats", "com.netrust.betterbanner.bstats")
    }

    build {
        dependsOn(shadowJar)
    }
}
