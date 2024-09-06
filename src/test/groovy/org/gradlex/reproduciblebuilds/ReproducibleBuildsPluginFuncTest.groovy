/*
 * Copyright 2022 the GradleX team.
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

package org.gradlex.reproduciblebuilds

import org.gradlex.reproduciblebuilds.fixture.GradleBuild
import spock.lang.AutoCleanup
import spock.lang.Specification

class ReproducibleBuildsPluginFuncTest extends Specification {

    @Delegate
    @AutoCleanup
    GradleBuild build = new GradleBuild();

    def "plugin can be applied to a project"() {
        given:
        buildFile << """
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
        """

        and:
        build.file("src/main/java/JavaClass.java") << """
            public class JavaClass {}
        """
        build.file("src/main/groovy/GroovyClass.groovy") << """
            class GroovyClass {}
        """
        build.file("src/main/scala/ScalaClass.java") << """
            class ScalaClass {}
        """

        expect:
        build("build", "javadoc")
    }
}
