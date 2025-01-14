/*
 * Copyright 2024 the GradleX team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradlex.reproduciblebuilds;

import org.gradlex.reproduciblebuilds.fixture.GradleBuild;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReproducibleBuildsPluginFuncTest {

    GradleBuild build = GradleBuild.create();

    @AfterEach
    void afterEach() {
        build.close();
    }

    @Test
    void plugin_can_be_applied_to_a_project() {
        build.getBuildFile().writeText("""
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

        build.getProjectDir().file("src/main/java/JavaClass.java").writeText("""
                public class JavaClass {}
                """);
        build.getProjectDir().file("src/main/groovy/GroovyClass.groovy").writeText("""
                class GroovyClass {}
                """);
        build.getProjectDir().file("src/main/scala/ScalaClass.java").writeText("""
                class ScalaClass {}
                """);

        build.run("build", "javadoc");
    }
}
