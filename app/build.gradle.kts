plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.dagger.hilt.android")
    id("androidx.room")
}

android {
    namespace = "com.xtremeiptv"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.xtremeiptv"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug") // Replace with release signing
        }
        
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Product flavors for different device types
    flavorDimensions += "device"
    productFlavors {
        create("mobile") {
            dimension = "device"
            versionNameSuffix = "-mobile"
        }
        create("tv") {
            dimension = "device"
            versionNameSuffix = "-tv"
            minSdk = 21
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:preferences"))
    implementation(project(":core:designsystem"))
    
    implementation(project(":feature:auth"))
    implementation(project(":feature:home"))
    implementation(project(":feature:live"))
    implementation(project(":feature:movies"))
    implementation(project(":feature:series"))
    implementation(project(":feature:epg"))
    implementation(project(":feature:player"))
    implementation(project(":feature:download"))
    implementation(project(":feature:recording"))
    implementation(project(":feature:profile"))
    
    implementation(project(":service:sync"))
    implementation(project(":service:download"))
    implementation(project(":service:recording"))

    // AndroidX
    implementation(Deps.AndroidX.core)
    implementation(Deps.AndroidX.lifecycle)
    implementation(Deps.AndroidX.activity)
    
    // Compose
    implementation(platform(Deps.AndroidX.Compose.bom))
    implementation(Deps.AndroidX.Compose.ui)
    implementation(Deps.AndroidX.Compose.graphics)
    implementation(Deps.AndroidX.Compose.material3)
    implementation(Deps.AndroidX.Compose.materialWindow)
    implementation(Deps.AndroidX.Compose.navigation)
    implementation(Deps.AndroidX.Compose.hilt)
    implementation(Deps.AndroidX.Compose.viewModel)
    
    // Hilt
    implementation(Deps.Google.hilt)
    kapt(Deps.Google.hiltCompiler)
    
    // Room
    implementation(Deps.AndroidX.Room.runtime)
    implementation(Deps.AndroidX.Room.ktx)
    kapt(Deps.AndroidX.Room.compiler)
    
    // Network
    implementation(Deps.Square.retrofit)
    implementation(Deps.Square.gson)
    implementation(Deps.Square.okhttp)
    implementation(Deps.Square.logging)
    
    // Media3
    implementation(Deps.Media3.exo)
    implementation(Deps.Media3.hls)
    implementation(Deps.Media3.dash)
    implementation(Deps.Media3.ui)
    implementation(Deps.Media3.session)
    implementation(Deps.Media3.download)
    implementation(Deps.Media3.cast)
    
    // Kotlin
    implementation(Deps.Kotlin.coroutines)
    implementation(Deps.Kotlin.coroutinesCore)
    
    // Google
    implementation(Deps.Google.ads)
    implementation(Deps.Google.cast)
    
    // Third Party
    implementation(Deps.ThirdParty.coil)
    implementation(Deps.ThirdParty.timber)
    debugImplementation(Deps.ThirdParty.leakcanary)
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
