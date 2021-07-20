rootProject.name = "Sugarcane"
include("Sugarcane-API", "Sugarcane-MojangAPI", "Sugarcane-Server")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
        maven("https://mvn.thearcanebrony.net/repository/maven-public/")
        maven("https://jitpack.io/")
        mavenCentral()
    }
}