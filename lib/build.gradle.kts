plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
}

android {
    namespace = "dora.lifecycle.walletconnect"
    compileSdk = 34

    defaultConfig {
        minSdk = 23
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
        ndk {
            abiFilters.add("arm64-v8a") // 主流手机
            abiFilters.add("armeabi-v7a") // 电视盒子
//            abiFilters.add("x86")
//            abiFilters.add("x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isJniDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets {
        getByName("main") {
            jniLibs.srcDir("src/main/jniLibs")
        }
    }
//    externalNativeBuild {
//        cmake {
//            path = file("src/main/cpp/CMakeLists.txt")
//            version = "3.22.1"
//        }
//    }
}

dependencies {
    implementation("com.github.dora4:dora:1.2.51")
    api("com.github.dora4:dview-alert-dialog:1.20")
    // wallet connect
    api(platform("com.walletconnect:android-bom:1.31.4"))
    api("com.walletconnect:android-core")
    api("com.walletconnect:web3modal")
    api("org.web3j:core:4.1.0-android")
}

afterEvaluate {
    publishing {
        publications {
            register("release", MavenPublication::class) {
                from(components["release"])
                groupId = "com.github.dora4"
                artifactId = "dora-walletconnect-support"
                version = "1.65"
            }
        }
    }
}