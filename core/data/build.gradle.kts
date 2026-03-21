plugins {
    id("limimeshi.android.library")
    id("limimeshi.android.hilt")
}

android {
    namespace = "com.shg25.limimeshi.core.data"

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:common"))

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)

    implementation(libs.kotlinx.coroutines.test)

    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
