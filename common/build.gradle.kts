@file:Suppress("OPT_IN_USAGE")

plugins {
    kotlin("multiplatform")
    id("kotlinx-serialization")
    id("com.android.library")
    id("app.cash.sqldelight")
    id("com.google.devtools.ksp")
    id("com.rickclephas.kmp.nativecoroutines")
    id("io.github.luca992.multiplatform-swiftpackage") version "2.1.1"
}

// CocoaPods requires the podspec to have a version.
version = "1.0"

android {
    compileSdk = AndroidSdk.compile
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "com.surrus.common"
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),

        watchosArm32(),
        watchosArm64(),
        watchosSimulatorArm64()

    ).forEach {
        it.binaries.framework {
            baseName = "common"
        }
    }


    targetHierarchy.default()

    //macosX64("macOS")
    macosArm64("macOS") {
        binaries.framework {
            baseName = "common"
        }
    }
    androidTarget()
    jvm()

    js(IR) {
        useCommonJs()
        browser()
    }

//    wasm {
//        browser()
//    }


    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.bundles.ktor.common)
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization)

                with(Deps.SqlDelight) {
                    implementation(runtime)
                    implementation(coroutineExtensions)
                }

                api(libs.koin.core)
                implementation(libs.koin.test)

                with(Deps.Log) {
                    api(kermit)
                }
            }
        }

        commonTest {
            dependencies {
                implementation(libs.koin.test)
                implementation(Deps.Kotlinx.coroutinesTest)
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(Deps.Ktor.clientAndroid)
                implementation(Deps.SqlDelight.androidDriver)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(Deps.Ktor.clientJava)
                implementation(Deps.SqlDelight.sqliteDriver)
                implementation(Deps.Log.slf4j)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by getting {
            dependsOn(commonMain.get())
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(Deps.Ktor.clientDarwin)
                implementation(Deps.SqlDelight.nativeDriver)
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by getting {
            dependsOn(commonTest.get())
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }

        val watchosArm32Main by getting
        val watchosArm64Main by getting
        val watchosSimulatorArm64Main by getting
        val watchosMain by getting {
            watchosArm32Main.dependsOn(this)
            watchosArm64Main.dependsOn(this)
            watchosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(Deps.Ktor.clientDarwin)
                implementation(Deps.SqlDelight.nativeDriver)
            }
        }

        val macOSMain by getting {
            dependencies {
                implementation(Deps.Ktor.clientDarwin)
                implementation(Deps.SqlDelight.nativeDriverMacos)
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(Deps.Ktor.clientJs)
            }
        }

    }
}

sqldelight {
    databases {
        create("PeopleInSpaceDatabase") {
            packageName.set("com.surrus.peopleinspace.db")
        }
    }
}

multiplatformSwiftPackage {
    packageName("PeopleInSpaceKit")
    swiftToolsVersion("5.3")
    targetPlatforms {
        iOS { v("14") }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
}

