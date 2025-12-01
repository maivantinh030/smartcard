import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile // Import cần thiết cho tasks.withType

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

// GỘP TẤT CẢ VÀO MỘT KHỐI kotlin
kotlin {
    // Đảm bảo chỉ định toolchain
    jvmToolchain(21)

    jvm() { // Đặt tên cho JVM target nếu bạn muốn
        // Cấu hình jvm() và sourceSets ở đây
    }

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
            implementation(compose.materialIconsExtended)  // ← Icons extended
            implementation(compose.material3AdaptiveNavigationSuite)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    compilerOptions.freeCompilerArgs.addAll(
        listOf(
            "-Xadd-modules=java.smartcardio"
        )
    )
}


compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        // Cấu hình Runtime (Fix Run Time)
        jvmArgs += listOf("--add-modules", "java.smartcardio")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = "1.0.0"
            // Cấu hình Distribution (Fix Packaging)
            modules("java.smartcardio")
        }
    }
}