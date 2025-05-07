pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.google.protobuf") version "0.8.19"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://repo.platform.here.com/artifactory/maven/")
        }
        maven {
            url = uri("https://packages.map4d.vn/repository/maven-public")
        }
    }
}

rootProject.name = "MapApplication"
include(":app")
 