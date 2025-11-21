import com.asarkar.gradle.buildtimetracker.BarPosition
import com.asarkar.gradle.buildtimetracker.BuildTimeTrackerPluginExtension
import com.asarkar.gradle.buildtimetracker.Output
import com.asarkar.gradle.buildtimetracker.Sort
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import soft.divan.finansemanager.libs
import java.time.Duration

class BuildTimeTrackerConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.findPlugin("build-time-tracker").get().get().pluginId)

            extensions.configure<BuildTimeTrackerPluginExtension> {
                barPosition.set(BarPosition.TRAILING)
                sortBy.set(Sort.ASC)
                output.set(Output.CSV)
                minTaskDuration.set(Duration.ofSeconds(1))
                reportsDir.set(file("${layout.buildDirectory.get()}/reports/buildTimeTracker"))
            }
        }
    }
}