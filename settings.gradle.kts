import buildparameters.BuildParametersExtension

rootProject.name = "reproducible-builds"

pluginManagement {
    includeBuild("gradle/plugins")
}

plugins {
    id("com.gradle.develocity") version "3.18"
    id("com.gradle.common-custom-user-data-gradle-plugin") version "2.0.2"
    id("gradlexbuild.build-parameters")
}

dependencyResolutionManagement {
    repositories.mavenCentral()
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/help/legal-terms-of-use"
        termsOfUseAgree = "yes"

        // required to bind this to a local variable for configuration cache compatibility
        val isCi = the<BuildParametersExtension>().ci
        publishing.onlyIf { isCi }
    }
}