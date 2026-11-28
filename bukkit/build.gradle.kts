plugins { `java-library` }

dependencies {
    implementation(project(":common"))
    compileOnly("io.papermc.paper:paper-api:26.3.build.259-stable")
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")
    runtimeOnly("org.xerial:sqlite-jdbc:3.54.1.0")
    runtimeOnly("mysql:mysql-connector-java:8.0.33")
    implementation("com.sun.mail:javax.mail:1.6.2")
    implementation("org.apache.commons:commons-email:1.6.0")
    implementation("space.arim.morepaperlib:morepaperlib:0.5.3")
}
