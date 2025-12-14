import com.android.build.gradle.LibraryExtension
import com.shg25.limimeshi.configureKotlinAndroid
import com.shg25.limimeshi.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * 基本的なAndroid Libraryモジュール用のConvention Plugin
 * 適用対象: core:model, core:common など
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
            }

            dependencies {
                add("testImplementation", libs.findLibrary("junit5-api").get())
                add("testRuntimeOnly", libs.findLibrary("junit5-engine").get())
            }
        }
    }
}
