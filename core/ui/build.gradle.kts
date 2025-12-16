plugins {
    id("limimeshi.android.library.compose")
}

android {
    namespace = "com.shg25.limimeshi.core.ui"
}

dependencies {
    implementation(project(":core:model"))

    // Compose UI Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
