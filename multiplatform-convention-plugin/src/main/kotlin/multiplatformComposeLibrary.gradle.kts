plugins {
    id("multiplatformLibrary")
    id("org.jetbrains.compose")
}

val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("compose-runtime").get())
            implementation(libs.findLibrary("compose-foundation").get())
            implementation(libs.findLibrary("compose-material3").get())
            implementation(libs.findLibrary("compose-ui").get())
            implementation(libs.findLibrary("compose-components-resources").get())
            implementation(libs.findLibrary("compose-uiToolingPreview").get())
            implementation(libs.findLibrary("androidx-lifecycle-viewmodelCompose").get())
            implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
        }
        androidMain.dependencies {
            implementation(libs.findLibrary("compose-uiToolingPreview").get())
        }
    }
}
