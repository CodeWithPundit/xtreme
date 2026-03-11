import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class FlavorConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<ApplicationExtension> {
                configureFlavors(this)
            }
            
            extensions.configure<LibraryExtension> {
                configureFlavors(this)
            }
        }
    }
    
    private fun configureFlavors(extension: CommonExtension<*, *, *, *, *>) {
        extension.apply {
            flavorDimensions += "device"
            
            productFlavors {
                create("mobile") {
                    dimension = "device"
                    versionNameSuffix = "-mobile"
                    buildConfigField("boolean", "IS_TV", "false")
                    buildConfigField("boolean", "IS_MOBILE", "true")
                    buildConfigField("boolean", "IS_TABLET", "false")
                }
                
                create("tv") {
                    dimension = "device"
                    versionNameSuffix = "-tv"
                    buildConfigField("boolean", "IS_TV", "true")
                    buildConfigField("boolean", "IS_MOBILE", "false")
                    buildConfigField("boolean", "IS_TABLET", "false")
                    
                    // TV specific configurations
                    minSdk = 21
                }
                
                create("tablet") {
                    dimension = "device"
                    versionNameSuffix = "-tablet"
                    buildConfigField("boolean", "IS_TV", "false")
                    buildConfigField("boolean", "IS_MOBILE", "false")
                    buildConfigField("boolean", "IS_TABLET", "true")
                    
                    // Tablet specific configurations
                    minSdk = 24
                }
            }
        }
    }
}
