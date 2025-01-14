plugins {
    id("gradlexbuild.build-parameters")
    id("gradlexbuild.documentation-conventions")
    id("org.gradlex.internal.plugin-publish-conventions") version "0.6"
}

group = "org.gradlex"
version = "1.0"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(8)
}

pluginPublishConventions {
    id("${project.group}.${project.name}")
    implementationClass("org.gradlex.reproduciblebuilds.ReproducibleBuildsPlugin")
    displayName("Reproducible Builds Gradle Plugin")
    description("Reproducibility settings applied to Gradle's built-in tasks.")
    tags("gradlex", "reproducible builds")
    gitHub("https://github.com/gradlex-org/reproducible-builds")
    website("https://gradlex.org/reproducible-builds")
    developer {
        id = "britter"
        name = "Benedikt Ritter"
        email = "benedikt@gradlex.org"
    }
    developer {
        id = "jjohannes"
        name = "Jendrik Johannes"
        email = "jendrik@gradlex.org"
    }
    developer {
        id = "ljacomet"
        name = "Louis Jacomet"
        email = "louis@gradlex.org"
    }
}

tasks.compileTestJava {
    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
    dependencies {
        implementation("org.apache.commons:commons-compress:1.27.1") {
            because("For asserting file permissions in zip files")
        }
    }
    targets.all {
        testTask {
            maxParallelForks = 4
            javaLauncher = project.javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(17)
            }
        }
    }
}

tasks.publishPlugins {
    dependsOn(tasks.check)
}
