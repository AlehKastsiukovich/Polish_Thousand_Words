plugins {
    id("multiplatformComposeLibrary")
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidLibrary {
        namespace = "com.polish.thousand.core.designsystem"
    }
}
