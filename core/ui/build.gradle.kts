plugins {
    id("limimeshi.android.library.compose")
}

android {
    namespace = "com.shg25.limimeshi.core.ui"
}

dependencies {
    implementation(project(":core:model"))
}
