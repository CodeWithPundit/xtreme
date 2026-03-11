import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 34
            }

            dependencies {
                add("implementation", project(":core:designsystem"))
            }
        }
    }
}

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 34
            }
        }
    }
}

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<CommonExtension<*, *, *, *, *>> {
                buildFeatures {
                    compose = true
                }

                composeOptions {
                    kotlinCompilerExtensionVersion = "1.5.7"
                }
            }

            dependencies {
                val bom = platform("androidx.compose:compose-bom:2024.01.00")
                add("implementation", bom)
                add("androidTestImplementation", bom)
                
                add("implementation", "androidx.compose.ui:ui")
                add("implementation", "androidx.compose.ui:ui-graphics")
                add("implementation", "androidx.compose.ui:ui-tooling-preview")
                add("implementation", "androidx.compose.material3:material3")
                add("implementation", "androidx.compose.material3:material3-window-size-class")
                add("implementation", "androidx.activity:activity-compose:1.8.2")
                add("implementation", "androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
                add("implementation", "androidx.navigation:navigation-compose:2.7.6")
                add("implementation", "androidx.hilt:hilt-navigation-compose:1.1.0")
                
                add("debugImplementation", "androidx.compose.ui:ui-tooling")
                add("debugImplementation", "androidx.compose.ui:ui-test-manifest")
            }
        }
    }
}

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.dagger.hilt.android")
                apply("org.jetbrains.kotlin.kapt")
            }

            dependencies {
                add("implementation", "com.google.dagger:hilt-android:2.50")
                add("kapt", "com.google.dagger:hilt-compiler:2.50")
                add("implementation", "androidx.hilt:hilt-work:1.1.0")
                add("kapt", "androidx.hilt:hilt-compiler:1.1.0")
            }
        }
    }
}

class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("androidx.room")
            }

            dependencies {
                add("implementation", "androidx.room:room-runtime:2.6.1")
                add("implementation", "androidx.room:room-ktx:2.6.1")
                add("kapt", "androidx.room:room-compiler:2.6.1")
            }
        }
    }
}

fun CommonExtension<*, *, *, *, *>.configureKotlinAndroid(
    extension: CommonExtension<*, *, *, *, *>
) {
    extension.apply {
        compileSdk = 34

        defaultConfig {
            minSdk = 24
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        kotlinOptions {
            jvmTarget = "17"
        }
    }
}
