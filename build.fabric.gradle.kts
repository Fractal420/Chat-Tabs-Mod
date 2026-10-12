@file:Suppress("UnstableApiUsage")

plugins {
    id("mod-plugin")
    id("maven-publish")
    id("net.fabricmc.fabric-loom")
    id("com.replaymod.preprocess")
}

version = artifactVersion
group = modMavenGroup

repositories {
    mavenCentral()
    fun strictMaven(url: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) }
        filter {
            groups.forEach {
                includeGroupAndSubgroups(it)
                includeGroupAndSubgroups("$it.*")
            }
        }
    }
    strictMaven("https://maven.fabricmc.net")
    strictMaven("https://api.modrinth.com/maven", "maven.modrinth")
    strictMaven("https://maven.terraformersmc.com/releases", "com.terraformersmc")
    strictMaven("https://maven.nucleoid.xyz", "eu.pb4")
    strictMaven("https://jitpack.io")
}

val modMenuDependency = "maven.modrinth:modmenu:${prop("modmenu")}"

configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            substitute(module("com.terraformersmc:modmenu"))
                .using(module(modMenuDependency))
                .because("Use one Mod Menu coordinate when dependencies request the official Maven module")
        }
        force("net.fabricmc:fabric-loader:$fabricLoaderVersion")
        force(modMenuDependency)
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    implementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    implementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    implementation(modMenuDependency)
}

loom {
    val commonVmArgs = listOf("-Dmixin.debug.export=true", "-Dmixin.debug.verbose=true", "-Dmixin.env.remapRefMap=true")
    val programArgs = listOf("--width", "1280", "--height", "720", "--username", "ChatTabsTest")
    runs {
        named("client") {
            generateRunConfig = true
            jvmArguments.addAll(commonVmArgs)
            programArguments.addAll(programArgs)
            runDirectory = file("../../run/client")
        }
    }
}

tasks {
    register<Copy>("buildAndCollect") {
        group = "build"
        val collectedJarDir = rootProject.layout.buildDirectory.dir("libs/$modVersion")
        from(jar.map { it.archiveFile })
        into(collectedJarDir)
        dependsOn("build")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = modId
            version = modVersion
        }
    }
    repositories {
        mavenLocal()
        maven {
            url = uri("$rootDir/publish")
        }
    }
}
