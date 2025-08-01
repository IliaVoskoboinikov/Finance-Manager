import com.android.build.gradle.LibraryExtension

plugins {
    id("com.android.library")
    id("android-base")
}

configure<LibraryExtension> {
    baseAndroidConfig(project)
}
