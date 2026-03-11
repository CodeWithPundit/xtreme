object Versions {
    const val compileSdk = 34
    const val minSdk = 24
    const val targetSdk = 34
    const val versionCode = 1
    const val versionName = "1.0.0"
    
    const val kotlin = "1.9.20"
    const val compose = "2024.01.00"
    const val composeCompiler = "1.5.7"
    const val hilt = "2.50"
    const val room = "2.6.1"
    const val retrofit = "2.9.0"
    const val okhttp = "4.12.0"
    const val media3 = "1.2.0"
}

object Deps {
    object AndroidX {
        const val core = "androidx.core:core-ktx:1.12.0"
        const val lifecycle = "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
        const val activity = "androidx.activity:activity-compose:1.8.2"
        
        object Compose {
            const val bom = "androidx.compose:compose-bom:${Versions.compose}"
            const val ui = "androidx.compose.ui:ui"
            const val graphics = "androidx.compose.ui:ui-graphics"
            const val material3 = "androidx.compose.material3:material3"
            const val materialWindow = "androidx.compose.material3:material3-window-size-class"
            const val navigation = "androidx.navigation:navigation-compose:2.7.6"
            const val hilt = "androidx.hilt:hilt-navigation-compose:1.1.0"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0"
        }
        
        object Room {
            const val runtime = "androidx.room:room-runtime:${Versions.room}"
            const val ktx = "androidx.room:room-ktx:${Versions.room}"
            const val compiler = "androidx.room:room-compiler:${Versions.room}"
        }
        
        const val dataStore = "androidx.datastore:datastore-preferences:1.0.0"
        const val work = "androidx.work:work-runtime-ktx:2.9.0"
        const val workHilt = "androidx.hilt:hilt-work:1.1.0"
    }
    
    object Google {
        const val hilt = "com.google.dagger:hilt-android:${Versions.hilt}"
        const val hiltCompiler = "com.google.dagger:hilt-compiler:${Versions.hilt}"
        const val ads = "com.google.android.gms:play-services-ads:22.6.0"
        const val cast = "com.google.android.gms:play-services-cast-framework:21.4.0"
    }
    
    object Square {
        const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
        const val gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
        const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
        const val logging = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    }
    
    object Media3 {
        const val exo = "androidx.media3:media3-exoplayer:${Versions.media3}"
        const val hls = "androidx.media3:media3-exoplayer-hls:${Versions.media3}"
        const val dash = "androidx.media3:media3-exoplayer-dash:${Versions.media3}"
        const val ui = "androidx.media3:media3-ui:${Versions.media3}"
        const val session = "androidx.media3:media3-session:${Versions.media3}"
        const val download = "androidx.media3:media3-exoplayer-workmanager:${Versions.media3}"
        const val cast = "androidx.media3:media3-cast:${Versions.media3}"
    }
    
    object Kotlin {
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
        const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3"
    }
    
    object ThirdParty {
        const val coil = "io.coil-kt:coil-compose:2.5.0"
        const val timber = "com.jakewharton.timber:timber:5.0.1"
        const val leakcanary = "com.squareup.leakcanary:leakcanary-android:2.12"
    }
}
