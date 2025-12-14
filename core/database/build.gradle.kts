plugins {
    id("limimeshi.android.library")
    id("limimeshi.android.hilt")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.shg25.limimeshi.core.database"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.mockk)
}
