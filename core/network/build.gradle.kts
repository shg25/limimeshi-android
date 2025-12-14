plugins {
    id("limimeshi.android.library")
    id("limimeshi.android.hilt")
}

android {
    namespace = "com.shg25.limimeshi.core.network"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    implementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.mockk)
}
