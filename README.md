# Reproducible Builds Gradle plugin

[![CI Build](https://github.com/gradlex-org/reproducible-builds/actions/workflows/ci-build.yml/badge.svg)](https://github.com/gradlex-org/reproducible-builds/actions/workflows/ci-build.yml)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/org.gradlex.reproducible-builds?label=Plugin%20Portal)](https://plugins.gradle.org/plugin/org.gradlex.reproducible-builds)
[![Revved up by Develocity](https://img.shields.io/badge/Revved%20up%20by-Develocity-06A0CE?logo=Gradle&labelColor=02303A)](https://community.develocity.cloud/scans)

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
