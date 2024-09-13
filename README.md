# Reproducible Builds Gradle plugin

[![Build Status](https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Fgradlex-org%2Freproducible-builds%2Fbadge%3Fref%3Dmain&style=flat)](https://actions-badge.atrox.dev/gradlex-org/build-parameters/goto?ref=main)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v?label=Plugin%20Portal&metadataUrl=https%3A%2F%2Fplugins.gradle.org%2Fm2%2Forg%2Fgradlex%2Freproducible-builds%2Forg.gradlex.reproducible-builds.gradle.plugin%2Fmaven-metadata.xml)](https://plugins.gradle.org/plugin/org.gradlex.build-parameters)

Reproducibility settings applied to some of Gradle's built-in tasks, that should really be the default.
Compatible with Java 8 and Gradle 8.3 or later.

# Usage

Apply this plugin to all projects in your build, and it configures archive and compilation tasks for reproducibility.

```kotlin
plugins {
    id("org.gradlex.reproducible-builds")
}
```

See the plugin's [documentation page](https://gradlex.org/reproducible-builds) for more details about what exactly the plugin configures and why.

# Disclaimer

Gradle and the Gradle logo are trademarks of Gradle, Inc.
The GradleX project is not endorsed by, affiliated with, or associated with Gradle or Gradle, Inc. in any way.
