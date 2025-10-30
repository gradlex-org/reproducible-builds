// SPDX-License-Identifier: Apache-2.0
package org.gradlex.reproduciblebuilds;

import org.gradlex.reproduciblebuilds.fixture.GradleBuild;
import org.gradlex.reproduciblebuilds.fixture.TestProject;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReproducibleBuildsPluginFuncTest {

    @Test
    void plugin_can_be_applied_to_a_project(@TestProject GradleBuild build) {
        build.getBuildFile()
                .writeText(
                        """
                plugins {
                    id 'java'
                    id 'groovy'
                    id 'scala'
                    id 'org.gradlex.reproducible-builds'
                }

                repositories {
                    mavenCentral()
                }

                dependencies {
                    implementation(localGroovy())
                    implementation('org.scala-lang:scala-library:2.13.12')
                }
                """);

        build.getProjectDir()
                .file("src/main/java/JavaClass.java")
                .writeText("""
                public class JavaClass {}
                """);
        build.getProjectDir()
                .file("src/main/groovy/GroovyClass.groovy")
                .writeText("""
                class GroovyClass {}
                """);
        build.getProjectDir()
                .file("src/main/scala/ScalaClass.scala")
                .writeText("""
                class ScalaClass {}
                """);

        build.run("build", "javadoc");
    }
}
