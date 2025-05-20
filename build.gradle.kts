// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    configurations.getByName("classpath") {
        resolutionStrategy {
            force("org.apache.commons:commons-compress:1.27.1")
        }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}