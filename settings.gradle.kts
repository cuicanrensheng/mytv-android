pluginManagement {
    repositories {
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    // 关键：强制优先使用 app 项目内的仓库
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    // 彻底删除全局 repositories 块，不再提供全局仓库
}

rootProject.name = "myTV-Android"
include(":app")
