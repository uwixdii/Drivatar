plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0") // Убедитесь, что версия актуальна
        classpath("com.google.gms:google-services:4.4.0") // Используйте одну и ту же версию плагина
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.8")
    }
}
