package soft.divan.financemanager

import Const
import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

fun BaseExtension.configureBaseAndroid(project: Project) {
    namespace = generateNamespace(project)
    setCompileSdkVersion(Const.COMPILE_SKD)

    defaultConfig {
        minSdk = Const.MIN_SKD
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(Const.JAVA_VERSION)
        targetCompatibility = JavaVersion.toVersion(Const.JAVA_VERSION)
    }
}
