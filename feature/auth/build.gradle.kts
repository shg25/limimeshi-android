plugins {
    id("limimeshi.android.feature")
}

android {
    namespace = "com.shg25.limimeshi.feature.auth"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))

    // Google Sign-In (Credential Manager)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)
}
