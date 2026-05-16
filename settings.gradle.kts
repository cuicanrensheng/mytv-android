pluginManagement {
    repositories {
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    // 改为优先使用项目仓库，不再强制用settings仓库
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    // 删除全局repositories块，完全交给app模块自己配置
}

rootProject.name = "myTV-Android"
include(":app")
