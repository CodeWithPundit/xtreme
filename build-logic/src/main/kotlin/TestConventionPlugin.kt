import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

class TestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            tasks.withType<Test> {
                useJUnitPlatform()
                testLogging {
                    events("passed", "skipped", "failed")
                }
            }
            
            dependencies {
                add("testImplementation", "junit:junit:4.13.2")
                add("testImplementation", "org.junit.jupiter:junit-jupiter:5.9.2")
                add("testImplementation", "org.mockito:mockito-core:5.6.0")
                add("testImplementation", "org.mockito.kotlin:mockito-kotlin:5.1.0")
                add("testImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
                add("testImplementation", "app.cash.turbine:turbine:1.0.0")
                add("testImplementation", "io.mockk:mockk:1.13.8")
                
                add("androidTestImplementation", "androidx.test.ext:junit:1.1.5")
                add("androidTestImplementation", "androidx.test.espresso:espresso-core:3.5.1")
                add("androidTestImplementation", "androidx.compose.ui:ui-test-junit4")
            }
        }
    }
}
