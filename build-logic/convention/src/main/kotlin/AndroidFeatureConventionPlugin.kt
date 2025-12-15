import com.shg25.limimeshi.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

/**
 * Feature moduleモジュール用のConvention Plugin
 * 適用対象: feature:chainlist, feature:favorites など
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "limimeshi.android.library.compose")
            apply(plugin = "limimeshi.android.hilt")

            dependencies {
                // ViewModel
                add("implementation", libs.findLibrary("lifecycle-viewmodel-compose").get())

                // Navigation
                add("implementation", libs.findLibrary("navigation-compose").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())

                // Logging
                add("implementation", libs.findLibrary("timber").get())

                // Testing
                add("testImplementation", libs.findLibrary("mockk").get())
                add("testImplementation", libs.findLibrary("turbine").get())
                add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
            }
        }
    }
}
