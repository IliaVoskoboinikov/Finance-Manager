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

    signingConfigs {
        create("release") {
            storeFile = project.layout.projectDirectory.file("release.jks").asFile
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(Const.JAVA_VERSION)
        targetCompatibility = JavaVersion.toVersion(Const.JAVA_VERSION)
    }
}
