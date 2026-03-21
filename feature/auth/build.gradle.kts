plugins {
    id("limimeshi.android.feature")
}

android {
    namespace = "com.shg25.limimeshi.feature.auth"
}

dependencies {
    // Firebase Auth
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    // Google Sign-In (Credential Manager)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)
}
