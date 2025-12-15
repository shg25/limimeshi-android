plugins {
    id("limimeshi.android.feature")
}

android {
    namespace = "com.shg25.limimeshi.feature.chainlist"
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
}
