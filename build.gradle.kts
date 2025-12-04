import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import io.netty.util.internal.PlatformDependent.javaVersion
import org.gradle.kotlin.dsl.withType
import kotlin.apply

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply true
}

private val detektConfigPath = "$rootDir/config/detekt/detekt.yml"

private val detektReportMergeHtml by tasks.registering(ReportMergeTask::class) {
    output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.html"))
}
private val detektVersion = libs.versions.detekt.asProvider().get()
private val detektFormattingPlugin = libs.detekt.formatting
private val detektLibrariesPlugin = libs.detekt.libraries
private val detektComposePlugin = libs.detekt.compose
subprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = javaVersion().toString()

        reports {
            xml.apply {
                required.set(true)
                outputLocation.set(file("reports/detekt.xml"))
            }
            html.apply {
                required.set(true)
                outputLocation.set(file("reports/detekt.html"))
            }
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }

        finalizedBy(detektReportMergeHtml)
    }

    detektReportMergeHtml {
        input.from(tasks.withType<Detekt>().map { it.htmlReportFile })
    }

    detekt {
        toolVersion = detektVersion
        source.setFrom(files("src"))
        config.setFrom(files(detektConfigPath))

        dependencies {
            detektPlugins(detektFormattingPlugin)
            detektPlugins(detektLibrariesPlugin)
            detektPlugins(detektComposePlugin)
        }
    }
}
