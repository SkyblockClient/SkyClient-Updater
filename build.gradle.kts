@file:Suppress("UnstableApiUsage", "PropertyName")

import org.polyfrost.gradle.util.noServerRunConfigs
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

// Adds support for kotlin, and adds the Polyfrost Gradle Toolkit
// which we use to prepare the environment.
plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
    kotlin("jvm")
    id("org.polyfrost.multi-version")
    id("org.polyfrost.defaults.repo")
    id("org.polyfrost.defaults.java")
    id("org.polyfrost.defaults.loom")
    id("com.github.johnrengelman.shadow")
    id("net.kyori.blossom") version "1.3.2"
    id("signing")
    java
}

// Gets the mod name, version and id from the `gradle.properties` file.
val mod_name: String by project
val mod_version: String by project
val mod_id: String by project
val mod_archives_name: String by project

// Sets up the variables for when we preprocess to other Minecraft versions.
preprocess {
    vars.put("MODERN", if (project.platform.mcMinor >= 16) 1 else 0)
}

// Replaces the variables in `ExampleMod.java` to the ones specified in `gradle.properties`.
blossom {
    replaceToken("@VER@", mod_version)
    replaceToken("@NAME@", mod_name)
    replaceToken("@ID@", mod_id)
}

// Sets the mod version to the one specified in `gradle.properties`. Make sure to change this following semver!
version = mod_version
// Sets the group, make sure to change this to your own. It can be a website you own backwards or your GitHub username.
// e.g. com.github.<your username> or com.<your domain>
group = "mynameisjeff"

// Sets the name of the output jar (the one you put in your mods folder and send to other people)
// It outputs all versions of the mod into the `build` directory.
base {
    archivesName.set("$mod_archives_name-$platform")
}

// Configures the Polyfrost Loom, our plugin fork to easily set up the programming environment.
loom {
    // Removes the server configs from IntelliJ IDEA, leaving only client runs.
    // If you're developing a server-side mod, you can remove this line.
    noServerRunConfigs()

    // Adds the tweak class if we are building legacy version of forge as per the documentation (https://docs.polyfrost.org)
    if (project.platform.isLegacyForge) {
        runConfigs {
            "client" {
                programArgs("--tweakClass", "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker")
                property("mixin.debug.export", "true")
            }
        }
    }
}

// Creates the shade/shadow configuration, so we can include libraries inside our mod, rather than having to add them separately.
val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}
val shadeMod: Configuration by configurations.creating {
    configurations.modImplementation.get().extendsFrom(this)
}

// Configures the output directory for when building from the `src/resources` directory.
sourceSets {
    main {
        output.setResourcesDir(java.classesDirectory)
    }
}

// Adds the Polyfrost maven repository so that we can get the libraries necessary to develop the mod.
repositories {
    maven("https://repo.polyfrost.org/releases")
    mavenCentral()
}

// Configures the libraries/dependencies for your mod.
dependencies {
    // Adds the OneConfig library, so we can develop with it.
    modCompileOnly("cc.polyfrost:oneconfig-$platform:0.2.2-alpha+")
    shadeMod("cc.polyfrost:elementa-$platform:560") {
        isTransitive = false
    }

    shade(ktor("serialization-kotlinx-json")) {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-stdlib-common")
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-jdk7")
        exclude(module = "kotlinx-coroutines-core")
        exclude(module = "kotlinx-coroutines-core-jvm")
        exclude(module = "kotlinx-coroutines-core-jdk8")
        exclude(module = "kotlinx-coroutines-jdk8")
        exclude(module = "kotlinx-serialization-core-jvm")
        exclude(module = "kotlinx-serialization-json-jvm")
    }

    shade(ktorClient("core")) {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-stdlib-common")
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-jdk7")
        exclude(module = "kotlinx-coroutines-core")
        exclude(module = "kotlinx-coroutines-core-jvm")
        exclude(module = "kotlinx-coroutines-core-jdk8")
        exclude(module = "kotlinx-coroutines-jdk8")
        exclude(module = "kotlinx-serialization-core-jvm")
        exclude(module = "kotlinx-serialization-json-jvm")
    }
    shade(ktorClient("cio")) {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-stdlib-common")
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-jdk7")
        exclude(module = "kotlinx-coroutines-core")
        exclude(module = "kotlinx-coroutines-core-jvm")
        exclude(module = "kotlinx-coroutines-core-jdk8")
        exclude(module = "kotlinx-coroutines-jdk8")
        exclude(module = "kotlinx-serialization-core-jvm")
        exclude(module = "kotlinx-serialization-json-jvm")
    }
    shade(ktorClient("content-negotiation")) {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-stdlib-common")
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-jdk7")
        exclude(module = "kotlinx-coroutines-core")
        exclude(module = "kotlinx-coroutines-core-jvm")
        exclude(module = "kotlinx-coroutines-core-jdk8")
        exclude(module = "kotlinx-coroutines-jdk8")
        exclude(module = "kotlinx-serialization-core-jvm")
        exclude(module = "kotlinx-serialization-json-jvm")
    }
    shade(ktorClient("encoding")) {
        exclude(module = "kotlin-reflect")
        exclude(module = "kotlin-stdlib")
        exclude(module = "kotlin-stdlib-common")
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-stdlib-jdk7")
        exclude(module = "kotlinx-coroutines-core")
        exclude(module = "kotlinx-coroutines-core-jvm")
        exclude(module = "kotlinx-coroutines-core-jdk8")
        exclude(module = "kotlinx-coroutines-jdk8")
        exclude(module = "kotlinx-serialization-core-jvm")
        exclude(module = "kotlinx-serialization-json-jvm")
    }

    //modRuntimeOnly("me.djtheredstoner:DevAuth-${if (platform.isFabric) "fabric" else if (platform.isLegacyForge) "forge-legacy" else "forge-latest"}:1.1.2")

    // If we are building for legacy forge, includes the launch wrapper with `shade` as we configured earlier.
    if (platform.isLegacyForge) {
        shade("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta17")
    }
}

tasks {
    // Processes the `src/resources/mcmod.info or fabric.mod.json` and replaces
    // the mod id, name and version with the ones in `gradle.properties`
    processResources {
        inputs.property("id", mod_id)
        inputs.property("name", mod_name)
        val java = if (project.platform.mcMinor >= 18) {
            17 // If we are playing on version 1.18, set the java version to 17
        } else {
            // Else if we are playing on version 1.17, use java 16.
            if (project.platform.mcMinor == 17)
                16
            else
                8 // For all previous versions, we **need** java 8 (for Forge support).
        }
        val compatLevel = "JAVA_${java}"
        inputs.property("java", java)
        inputs.property("java_level", compatLevel)
        inputs.property("version", mod_version)
        inputs.property("mcVersionStr", project.platform.mcVersionStr)
        filesMatching(listOf("mcmod.info", "mixins.${mod_id}.json", "mods.toml")) {
            expand(
                mapOf(
                    "id" to mod_id,
                    "name" to mod_name,
                    "java" to java,
                    "java_level" to compatLevel,
                    "version" to mod_version,
                    "mcVersionStr" to project.platform.mcVersionStr
                )
            )
        }
        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "id" to mod_id,
                    "name" to mod_name,
                    "java" to java,
                    "java_level" to compatLevel,
                    "version" to mod_version,
                    "mcVersionStr" to project.platform.mcVersionStr.substringBeforeLast(".") + ".x"
                )
            )
        }
    }

    // Configures the resources to include if we are building for forge or fabric.
    withType(Jar::class.java) {
        if (project.platform.isFabric) {
            exclude("mcmod.info", "mods.toml")
        } else {
            exclude("fabric.mod.json")
            if (project.platform.isLegacyForge) {
                exclude("mods.toml")
            } else {
                exclude("mcmod.info")
            }
        }
    }

    // Configures our shadow/shade configuration, so we can
    // include some dependencies within our mod jar file.
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("dev") // TODO: machete gets confused by the `dev` prefix.
        configurations = listOf(shade, shadeMod)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        archiveClassifier.set("")
    }

    jar {
        // Sets the jar manifest attributes.
        if (platform.isLegacyForge) {
            manifest.attributes += mapOf(
                "ModSide" to "CLIENT", // We aren't developing a server-side mod, so this is fine.
                "ForceLoadAsMod" to true, // We want to load this jar as a mod, so we force Forge to do so.
                "TweakOrder" to "0", // Makes sure that the OneConfig launch wrapper is loaded as soon as possible.
                "TweakClass" to "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker" // Loads the OneConfig launch wrapper.
            )
        }
        dependsOn(shadowJar)
        archiveClassifier.set("")
        enabled = false
    }
}

fun DependencyHandler.ktor(module: String, version: String? = "2.3.9", addSuffix: Boolean = true) =
    "io.ktor:ktor-$module${if (addSuffix) "-jvm" else ""}${version?.let { ":$version" } ?: ""}"

fun DependencyHandler.ktorClient(module: String, version: String? = "2.3.9") = ktor("client-${module}", version)