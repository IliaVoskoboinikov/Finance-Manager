import com.android.build.gradle.LibraryExtension

plugins {
    id("android-base")
    id("com.android.library")
}

configure<LibraryExtension> {
    baseAndroidConfig(project)
}
