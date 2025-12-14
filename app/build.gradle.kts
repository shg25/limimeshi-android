import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.detekt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    jacoco
}

android {
    namespace = "com.shg25.limimeshi"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.shg25.limimeshi"
        minSdk = 31
        targetSdk = 36
        // CI/CD: VERSION_CODE環境変数から読み込み、ローカル: デフォルト1
        versionCode = (System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1)
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // CI/CD: 環境変数から読み込み
            // ローカル: keystore.propertiesから読み込み
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(keystorePropertiesFile.inputStream())
                storeFile = rootProject.file(keystoreProperties["storeFile"].toString())
                storePassword = keystoreProperties["storePassword"].toString()
                keyAlias = keystoreProperties["keyAlias"].toString()
                keyPassword = keystoreProperties["keyPassword"].toString()
            } else {
                // CI/CD環境では環境変数から読み込み
                storeFile = file(System.getenv("KEYSTORE_FILE") ?: "upload-keystore.jks")
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: ""
                keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "リミDEV")
        }
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "リミメシ")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        // XML設定ファイル
        lintConfig = file("lint.xml")
        // CI/CDではエラーで失敗
        abortOnError = true
        // 警告をエラーとして扱う（厳格モード）
        warningsAsErrors = false
        // HTMLレポート生成
        htmlReport = true
        htmlOutput = file("${layout.buildDirectory.get()}/reports/lint-results.html")
        // XMLレポート生成（CI連携用）
        xmlReport = true
        xmlOutput = file("${layout.buildDirectory.get()}/reports/lint-results.xml")
        // ベースライン（既存の警告を無視）
        // baseline = file("lint-baseline.xml")
    }
}

// Detekt設定
detekt {
    // 設定ファイル
    config.setFrom("${rootProject.projectDir}/config/detekt/detekt.yml")
    // ベースライン（既存の警告を無視）
    // baseline = file("detekt-baseline.xml")
    // 並列実行
    parallel = true
    // ビルド失敗条件
    buildUponDefaultConfig = true
}

// JUnit5対応
tasks.withType<Test> {
    useJUnitPlatform()
    // JaCoCoのためのJVM引数設定
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

// JaCoCo設定
jacoco {
    toolVersion = "0.8.12"
}

// カバレッジレポート生成タスク
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDevDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        // Android生成コード
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        // Hilt生成コード
        "**/*_Hilt*.class",
        "**/Hilt_*.class",
        "**/*_Factory.class",
        "**/*_MembersInjector.class",
        // Compose生成コード
        "**/*ComposableSingletons*.class"
    )

    val devDebugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/devDebug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(devDebugTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include("jacoco/testDevDebugUnitTest.exec")
    })
}

dependencies {
    // Feature modules
    implementation(project(":feature:chainlist"))
    implementation(project(":feature:favorites"))

    // Core modules
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navigation
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Logging
    implementation(libs.timber)

    // Hilt (DI)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Unit Test - JUnit5
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.junit5.params)

    // Unit Test - MockK
    testImplementation(libs.mockk)

    // Unit Test - Turbine (Flow testing)
    testImplementation(libs.turbine)

    // Unit Test - Coroutines
    testImplementation(libs.kotlinx.coroutines.test)

    // Android Test
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}