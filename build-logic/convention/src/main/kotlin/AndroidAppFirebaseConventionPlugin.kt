import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import soft.divan.financemanager.Conf
import soft.divan.financemanager.FmBuildType
import soft.divan.financemanager.applyPlugin
import soft.divan.financemanager.lib
import soft.divan.financemanager.libs

class AndroidAppFirebaseConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            applyPlugin("gms")
            applyPlugin("firebase-crashlytics")

            dependencies {
                val bom = libs.findLibrary("firebase-bom").get()
                add(Conf.IMPLEMENTATION, platform(bom))
                add(Conf.IMPLEMENTATION, lib("crashlytics"))
            }
            extensions.configure<ApplicationExtension> {
                buildTypes {
                    debug {
                        applicationIdSuffix = FmBuildType.DEBUG.applicationIdSuffix
                        manifestPlaceholders["crashlyticsCollectionEnabled"] = "false"
                    }
                    release {
                        applicationIdSuffix = FmBuildType.RELEASE.applicationIdSuffix
                        manifestPlaceholders["crashlyticsCollectionEnabled"] = "true"
                    }
                }
            }
        }
    }
}
