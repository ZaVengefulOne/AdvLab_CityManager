plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    afterEvaluate {
        configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            version.set(libs.versions.ktlint.get())
            debug.set(false)
            verbose.set(true)
            android.set(true)
            outputToConsole.set(true)
            ignoreFailures.set(true)
            enableExperimentalRules.set(true)
            filter {
                exclude("**/generated/**")
                exclude("**/build/**")
                exclude("**/.gradle/**")
                exclude("**/iosApp/**")
            }
        }
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        reports {
            html.required.set(true)
            html.outputLocation.set(file("$rootDir/build/reports/detekt/${project.name}.html"))
            xml.required.set(true)
            xml.outputLocation.set(file("$rootDir/build/reports/detekt/${project.name}.xml"))
            txt.required.set(false)
            sarif.required.set(true)
            sarif.outputLocation.set(file("$rootDir/build/reports/detekt/${project.name}.sarif"))
        }
        ignoreFailures = true
        buildUponDefaultConfig = true
        val configFile = rootProject.file("config/detekt/detekt.yml")
        val baselineFile = rootProject.file("config/detekt/baseline.xml")

        if (configFile.exists()) {
            config.setFrom(configFile)
        }

        if (baselineFile.exists()) {
            baseline.set(baselineFile)
        }
    }

    tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
        enabled = false
    }
}
