import org.gradle.api.file.DuplicatesStrategy

plugins {
    kotlin("jvm") version "2.4.21"
    id("com.gradleup.shadow") version "9.6.1"
}

group = "com.ququla89"
version = "1.0.0"

val pluginVersion = version.toString()

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.21") {
        exclude(group = "org.bukkit")
        exclude(group = "org.spigotmc")
    }
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.mockito:mockito-core:5.24.0")
    testImplementation("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveBaseName.set("ZoneAmbience")
        archiveClassifier.set("")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        relocate("kotlin", "com.ququla89.zoneambience.lib.kotlin")
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        inputs.property("version", pluginVersion)
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("plugin.yml") {
            expand("version" to pluginVersion)
        }
    }

    test {
        useJUnitPlatform()
    }

    register("deployTest") {
        group = "deployment"
        description = "Build shadowJar and deploy to the test server via MC_DEPLOY_SCRIPT."
        dependsOn(shadowJar)
        val jarProvider = shadowJar.flatMap { it.archiveFile }
        val scriptProvider = providers.gradleProperty("deployScript")
            .orElse(providers.environmentVariable("MC_DEPLOY_SCRIPT"))
        doLast {
            val scriptPath = scriptProvider.orNull
                ?: error("Set MC_DEPLOY_SCRIPT environment variable or pass -PdeployScript=<path>.")
            val jarFile = jarProvider.get().asFile
            val shell = if (System.getProperty("os.name").lowercase().contains("win")) "powershell" else "pwsh"
            val exitCode = ProcessBuilder(
                shell,
                "-NoProfile",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                scriptPath,
                "-JarPath",
                jarFile.absolutePath,
                "-Target",
                "plugin",
            )
                .inheritIO()
                .start()
                .waitFor()
            if (exitCode != 0) {
                error("Deploy script failed with exit code $exitCode")
            }
        }
    }
}
