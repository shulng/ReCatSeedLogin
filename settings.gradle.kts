pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.opencollab.dev/main/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://mvn-repo.arim.space/lesser-gpl3/")
    }
}

rootProject.name = "CatSeedLogin"
include("common", "bukkit", "bungee", "velocity")
