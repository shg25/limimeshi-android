plugins {
    id("limimeshi.android.feature")
}

android {
    namespace = "com.shg25.limimeshi.feature.auth"

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:model"))

    // Google Sign-In (Credential Manager)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)

    // Testing - JUnit5
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
}
