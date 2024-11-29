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

import org.apache.commons.compress.archivers.zip.ZipFile
import org.gradlex.reproduciblebuilds.fixture.GradleBuild
import spock.lang.AutoCleanup
import spock.lang.Issue
import spock.lang.Specification

class ArchiveFilePermissionsFuncTest extends Specification {

    @Delegate
    @AutoCleanup
    GradleBuild build = new GradleBuild();

    def setup() {
        buildFile << """
            plugins {
                id 'application'
                id 'org.gradlex.reproducible-builds'
            }
            application {
                mainClassName = 'org.example.App'
            }
        """
        projectDir.file("src/main/java/org/example/App.java") << """
            package org.example;
            public class App {
                public static void main(String[] args) {}
            }
        """
    }

    @Issue('https://github.com/gradlex-org/reproducible-builds/issues/7')
    def "plugin does not override permissions set by application plugin"() {
        given:
        def archive = projectDir.file("build/distributions/test-project.zip")

        when:
        build 'build'

        then:
        archive.exists()
        assertPermissions(archive)
    }

    def "plugin sets all file permissions in archives to not rely on underlying file system"() {
        given:
        def archive = projectDir.file("build/distributions/test-project.zip")
        buildFile << """
        tasks.distZip {
                doFirst {
                    // Simulate that Gradle gets an unexpected (or no) value for permissions from the
                    // underlying file system by changing the permission of a file on file system level.
                    exec { commandLine 'chmod', '0444', 'build/libs/test-project.jar' }
                }
            }
        """

        when:
        build 'build'

        then:
        archive.exists()
        assertPermissions(archive)
    }

    private static boolean assertPermissions(File archive) {
        def zipBuilder = new ZipFile.Builder()
        zipBuilder.file = archive
        zipBuilder.get().entries.each { entry ->
            println("$entry.name -> $entry.unixMode")
            if (entry.name.endsWith('/')) {
                assert entry.unixMode == 16877
            } else if (entry.name.contains('/bin/')) {
                assert entry.unixMode == 33261 // explicitly configured by 'application' plugin
            } else {
                assert entry.unixMode == 33188
            }
        }
        return true
    }
}
