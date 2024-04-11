pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven ("https://devrepo.kakao.com/nexus/repository/kakaomap-releases/")
        maven("https://naver.jfrog.io/artifactory/maven/")
    }
}

rootProject.name = "WhereAreU"
include(":app")
