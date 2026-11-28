plugins { `java-library` }

dependencies {
    implementation(project(":common"))
    compileOnly("com.velocitypowered:velocity-api:4.3.1")
}
