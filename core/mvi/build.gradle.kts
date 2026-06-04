plugins {
    id("multiplatformLibrary")
}

kotlin {
    androidLibrary {
        namespace = "com.polish.thousand.core.mvi"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
