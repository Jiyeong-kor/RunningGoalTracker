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
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://devrepo.kakao.com/nexus/content/groups/public/")
    }
}

rootProject.name = "RunningGoalTracker"
include(":app")
include(":domain")
include(":shared:designsystem")
include(":shared:navigation")
include(":data")
include(":feature")
include(":feature:home")
include(":feature:goal")
include(":feature:record")
include(":feature:reminder")
include(":feature:mypage")
include(":feature:ai-coach")
include(":feature:auth")
