plugins {
    id("limimeshi.android.library")
    id("limimeshi.android.hilt")
}

android {
    namespace = "com.shg25.limimeshi.core.domain"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:data"))

    implementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
