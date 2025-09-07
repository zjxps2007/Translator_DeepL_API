import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlin.serialization)
}

val ktor = "3.2.0"

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(compose.materialIconsExtended)


            implementation("io.ktor:ktor-client-core:${ktor}")
            implementation("io.ktor:ktor-client-content-negotiation:${ktor}")
            implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor}")
            implementation("io.ktor:ktor-client-logging:${ktor}")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            implementation("io.ktor:ktor-client-cio:${ktor}")
            implementation("ch.qos.logback:logback-classic:1.5.6")


        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.inho.deepl_local.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "deepl_local"
            packageVersion = "1.0.0"

//            macOS {
//                iconFile.set(project.file("../icons/app.icns"))
//            }
            windows {
                iconFile.set(project.file("src/jvmMain/composeResources/drawable/icon_w.ico"))
            }
//            linux {
//                iconFile.set(project.file("../icons/app.png"))
//            }
        }
    }
}
