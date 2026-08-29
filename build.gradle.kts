import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.bundling.Jar

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "8.3.6"
}

// Read these once during configuration.
// Avoid referencing the Gradle script/project object from task actions.
val pluginGroup: String = providers.gradleProperty("group").get()
val pluginVersion: String = providers.gradleProperty("version").get()

group = pluginGroup
version = pluginVersion

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.md-5.net/content/repositories/snapshots/")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly(libs.spigot.api) {
        exclude(group = "net.md-5", module = "bungeecord-chat")
    }

    implementation(libs.bstats.bukkit)
    compileOnly(libs.jetbrains.annotations)
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
}

java {
    // Use a modern compiler...
    toolchain.languageVersion = JavaLanguageVersion.of(17)
}

tasks {
    withType<JavaCompile>().configureEach {
        // ...while producing Java 8-compatible bytecode/API usage.
        options.release.set(8)
    }

    processResources {
        inputs.property("pluginVersion", pluginVersion)

        filter(
            ReplaceTokens::class,
            "tokens" to mapOf(
                "version" to pluginVersion
            )
        )
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveVersion.set(pluginVersion)
        // Empty classifier produces a single, predictable jar named
        // BetterBanner-<version>.jar. The default "all" classifier is
        // only used when the regular `jar` task is also producing output;
        // with `jar` disabled (see below) we don't need it.
        archiveClassifier.set("")

        relocate(
            "org.bstats",
            "com.netrust.betterbanner.bstats"
        )
    }

    build {
        dependsOn(shadowJar)
    }
}