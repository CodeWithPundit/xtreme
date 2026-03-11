// build.gradle.kts (Project level)
plugins {
    id("com.android.application") version "8.3.0" apply false
    id("com.android.library") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("org.jetbrains.kotlin.kapt") version "1.9.20" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("androidx.room") version "2.6.1" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
