// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

// 全局强制替换 xmlpull 版本，优先级最高，彻底屏蔽 1.1.3.1
allprojects {
    configurations.all {
        resolutionStrategy.force("org.xmlpull:xmlpull:1.1.3.4")
    }
}
