plugins {
    id("groovy")
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
    description("Compile-safe access to parameters supplied to a Gradle build.")
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

testing.suites.named<JvmTestSuite>("test") {
    useJUnitJupiter()
    dependencies {
        implementation("org.spockframework:spock-core:2.3-groovy-3.0")
    }
    targets.all {
        testTask { maxParallelForks = 4 }
    }
}

tasks.publishPlugins {
    dependsOn(tasks.check)
}
