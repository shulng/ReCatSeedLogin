plugins {
    java
    id("com.gradleup.shadow") version "9.6.1"
}

group = "cc.baka9"
version = "2.1.1"
description = "A comprehensive login plugin for Minecraft servers supporting Bukkit, BungeeCord, and Velocity"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.257-stable")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("net.md-5:bungeecord-api:1.21-R0.4")
    compileOnly("com.velocitypowered:velocity-api:4.3.1")
    compileOnly("org.projectlombok:lombok:1.18.48")
    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")
    annotationProcessor("org.projectlombok:lombok:1.18.48")

    implementation("org.xerial:sqlite-jdbc:3.54.1.0")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.yaml:snakeyaml:2.7")
    implementation("com.sun.mail:javax.mail:1.6.2")
    implementation("org.apache.commons:commons-email:1.6.0")
    implementation("org.apache.commons:commons-lang3:3.21.0")
    implementation("commons-net:commons-net:3.13.0")
    implementation("space.arim.morepaperlib:morepaperlib:0.5.3")
    implementation("de.mkammerer:argon2-jvm:2.12")
}

configurations.compileClasspath {
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(8)
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
}

tasks.processResources {
    val properties = mapOf("version" to project.version.toString())
    filteringCharset = "UTF-8"
    inputs.properties(properties)
    filesMatching(listOf("plugin.yml", "bungee.yml", "velocity-plugin.json")) {
        expand(properties)
    }
}

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
