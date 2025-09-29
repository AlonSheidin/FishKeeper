pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")

        gradlePluginPortal()
    }

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") // for MPAndroidChart
    }
}

rootProject.name = "SmartAquarium"
include(":app")
 