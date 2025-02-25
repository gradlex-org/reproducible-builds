/*
 * Copyright the GradleX team.
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

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.gradlex.reproduciblebuilds.fixture.GradleBuild;
import org.gradlex.reproduciblebuilds.fixture.TestProject;
import org.gradlex.reproduciblebuilds.fixture.WritableFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ArchiveFilePermissionsFuncTest {

    @BeforeEach
    void beforeEach(@TestProject GradleBuild build) {
        build.getBuildFile().writeText("""
                plugins {
                    id 'application'
                    id 'org.gradlex.reproducible-builds'
                }
                application {
                    mainClassName = 'org.example.App'
                }
                """);
        build.getProjectDir().file("src/main/java/org/example/App.java").writeText("""
                package org.example;
                public class App {
                    public static void main(String[] args) {}
                }
                """);
    }

    // https://github.com/gradlex-org/reproducible-builds/issues/7
    @Test
    void plugin_does_not_override_permissions_set_by_application_plugin(@TestProject GradleBuild build) {
        WritableFile archive = build.getProjectDir().file("build/distributions/test-project.zip");

        build.run("build");

        assertTrue(archive.exists());
        assertPermissions(archive);
    }

    @Test
    void plugin_sets_all_file_permissions_in_archives_to_not_rely_on_underlying_file_system(@TestProject GradleBuild build) {
        WritableFile archive = build.getProjectDir().file("build/distributions/test-project.zip");
        build.getBuildFile().appendText("""
                tasks.distZip {
                    doFirst {
                        // Simulate that Gradle gets an unexpected (or no) value for permissions from the
                        // underlying file system by changing the permission of a file on file system level.
                        exec { commandLine 'chmod', '0444', 'build/libs/test-project.jar' }
                    }
                }
                """);

        build.run("build");

        assertTrue(archive.exists());
        assertPermissions(archive);
    }

    private static void assertPermissions(WritableFile archive) {
        ZipFile.Builder zipBuilder = new ZipFile.Builder();
        zipBuilder.setFile(archive.getAsPath().toFile());
        try {
            ZipFile zipFile = zipBuilder.get();
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                if (entry.getName().endsWith("/")) {
                    assertEquals(16877, entry.getUnixMode(), String.format("Directory '%s' has wrong mode!", entry.getName()));
                } else if (entry.getName().contains("/bin/")) {
                    assertEquals(33261, entry.getUnixMode(), String.format("Directory '%s' has wrong mode!", entry.getName()));
                } else {
                    assertEquals(33188, entry.getUnixMode(), String.format("File '%s' has wrong mode!", entry.getName()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
