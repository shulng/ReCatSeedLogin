plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

group = "cc.baka9"
version = providers.gradleProperty("releaseVersion").getOrElse("2.1.1")
description = "A comprehensive login plugin for Minecraft servers supporting Bukkit, BungeeCord, and Velocity"

subprojects {
    apply(plugin = "java-library")
    group = rootProject.group
    version = rootProject.version

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
    }
    configurations.named("compileClasspath") {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
    }
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(8)
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    }
    tasks.named<ProcessResources>("processResources") {
        val properties = mapOf("version" to project.version.toString())
        filteringCharset = "UTF-8"
        inputs.properties(properties)
        filesMatching(listOf("plugin.yml", "bungee.yml", "velocity-plugin.json")) {
            expand(properties)
        }
    }
}

dependencies {
    implementation(project(":bukkit"))
    implementation(project(":bungee"))
    implementation(project(":velocity"))
}

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveClassifier.set("")
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    exclude("META-INF/LICENSE*", "LICENSE*", "NOTICE*")
    relocate("com.google.protobuf", "cc.baka9.catseedlogin.libs.protobuf")
}

tasks.register("printProjectInfo") {
    val projectName = project.name
    val projectVersion = project.version.toString()

    doLast {
        println("name=$projectName")
        println("version=$projectVersion")
    }
}
