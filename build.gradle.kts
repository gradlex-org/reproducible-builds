version = "1.1"

dependencies {
    testImplementation("org.apache.commons:commons-compress:1.28.0") {
        because("For asserting file permissions in zip files")
    }
}

publishingConventions {
    pluginPortal("${project.group}.${project.name}") {
        implementationClass("org.gradlex.reproduciblebuilds.ReproducibleBuildsPlugin")
        displayName("Reproducible Builds Gradle Plugin")
        description("Reproducibility settings applied to Gradle's built-in tasks.")
        tags("gradlex", "reproducible builds")
    }
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

testingConventions { testGradleVersions("8.3", "8.14.3", "9.0.0") }
