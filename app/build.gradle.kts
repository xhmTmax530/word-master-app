plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.wordmaster.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wordmaster.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    // B-1 fix: 共用签名配置。debug 用默认 debug keystore,release 用发布 keystore。
    // 上架前需把 release keystore 放到 app/keystore/wordmaster-release.jks 并配置密码
    signingConfigs {
        // debug 用 Android SDK 自带的 ~/.android/debug.keystore,无需额外配置
        // release 配置示例(请自行放置 keystore 文件并替换密码):
        // create("release") {
        //     storeFile = file("keystore/wordmaster-release.jks")
        //     storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
        //     keyAlias = "wordmaster"
        //     keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        // }
    }

    buildTypes {
        release {
            // B-1 fix: 启用 R8/ProGuard 压缩 + 签名(签名见上方,本环境无 keystore 文件保留 isMinifyEnabled=true 但签名待补)
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // signingConfig = signingConfigs.getByName("release") // 上架前启用
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// D-1 fix: 输出 Room schema 到 app/schemas/,供版本升级时迁移参考
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}