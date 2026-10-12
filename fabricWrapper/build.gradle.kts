@file:Suppress("UnstableApiUsage")

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import java.io.File

plugins {
    id("java-library")
    id("maven-publish")
    id("mod-plugin")
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

group = modMavenGroup
version = fullProjectVersion

base {
    archivesName.set(modArchivesBaseName)
}

val fabricSubprojects = rootProject.subprojects.filter { it.name != "fabricWrapper" }

fabricSubprojects.forEach {
    evaluationDependsOn(":${it.name}")
}

tasks {
    register("collectSubModules") {
        description = "Collect JAR files from all subprojects"
        outputs.upToDateWhen { false }

        val embeddedJarsDir = layout.buildDirectory.dir("tmp/submods/META-INF/jars")
        val standaloneJarsDir = layout.buildDirectory.dir("libs/jars")

        dependsOn(fabricSubprojects.map { it.tasks.named("buildAndCollect") })

        doFirst {
            delete(embeddedJarsDir)
            delete(standaloneJarsDir)

            copy {
                from(fabricSubprojects.map { sub ->
                    sub.tasks.named("buildAndCollect").get().outputs.files
                })
                into(embeddedJarsDir)
            }

            copy {
                from(fabricSubprojects.map { sub ->
                    sub.tasks.named("buildAndCollect").get().outputs.files
                })
                into(standaloneJarsDir)
            }
        }
    }

    named<Jar>("jar") {
        outputs.upToDateWhen { false }
        from(rootProject.file("LICENSE"))
        from(layout.buildDirectory.dir("tmp/submods"))
    }

    named<ProcessResources>("processResources") {
        outputs.upToDateWhen { false }
        dependsOn("collectSubModules")
        inputs.properties(placeholderProps)
        filesMatching("fabric.mod.json") {
            expand(placeholderProps)
        }

        val rootIcon = rootProject.file("src/main/resources/assets/$modId/icon.png")
        val buildIconFile = layout.buildDirectory.file("resources/main/assets/$wrapperModId/icon.png").get().asFile

        doLast {
            if (rootIcon.exists()) {
                buildIconFile.parentFile.mkdirs()
                rootIcon.copyTo(buildIconFile, overwrite = true)
            }

            val jars = ArrayList<Map<String, String>>()
            val jarsDir = layout.buildDirectory.dir("tmp/submods/META-INF/jars").get().asFile

            if (jarsDir.exists() && jarsDir.isDirectory) {
                val jarFiles = jarsDir.listFiles { file ->
                    file.isFile && file.name.endsWith(".jar") &&
                            !file.name.contains("-dev.jar") &&
                            !file.name.contains("-sources.jar") &&
                            !file.name.contains("-shadow.jar")
                }
                jarFiles?.sortedBy { it.name }?.forEach { jarFile ->
                    jars.add(mapOf("file" to "META-INF/jars/${jarFile.name}"))
                }
            }

            val minecraftVersions = mutableListOf<String>()
            fabricSubprojects.forEach { subproject ->
                try {
                    val minecraftVersion = subproject.property("minecraft_dependency") as String
                    if (minecraftVersion.isNotBlank()) {
                        minecraftVersions.add(minecraftVersion)
                    }
                } catch (_: Exception) {
                }
            }

            val jsonFile = layout.buildDirectory.file("resources/main/fabric.mod.json").get().asFile
            if (jsonFile.exists()) {
                val slurper = JsonSlurper()
                @Suppress("UNCHECKED_CAST")
                val jsonContent = slurper.parse(jsonFile) as MutableMap<String, Any>
                jsonContent["jars"] = jars
                @Suppress("UNCHECKED_CAST")
                val depends = jsonContent["depends"] as? MutableMap<String, Any>
                depends?.put("minecraft", minecraftVersions)
                val builder = JsonBuilder(jsonContent)
                jsonFile.bufferedWriter().use { writer ->
                    writer.write(builder.toPrettyString())
                }
            }
        }
    }
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = wrapperModId
            version = fullProjectVersion
        }
    }
    repositories {
        mavenLocal()
    }
}
