// SPDX-License-Identifier: Apache-2.0
package org.gradlex.reproduciblebuilds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Enumeration;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.gradlex.reproduciblebuilds.fixture.GradleBuild;
import org.gradlex.reproduciblebuilds.fixture.TestProject;
import org.gradlex.reproduciblebuilds.fixture.WritableFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

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
                    mainClass = 'org.example.App'
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
    @DisabledOnOs(OS.WINDOWS)
    void file_permissions_in_archives_do_not_rely_on_underlying_file_system(@TestProject GradleBuild build) {
        // Gradle 8: This test makes sure that the 'reproducible-builds' plugin provides the tested functionality
        // Gradle 9: This test makes sure that the Gradle itself provides the tested functionality
        WritableFile archive = build.getProjectDir().file("build/distributions/test-project.zip");
        build.getBuildFile().appendText("""
                interface InjectedExecOps {
                    @Inject //@javax.inject.Inject
                    ExecOperations getExecOps()
                }
                tasks.distZip {
                    def injected = project.objects.newInstance(InjectedExecOps)

                    doFirst {
                        // Simulate that Gradle gets an unexpected (or no) value for permissions from the
                        // underlying file system by changing the permission of a file on file system level.
                        injected.execOps.exec { commandLine 'chmod', '0444', 'build/libs/test-project.jar' }
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
                    assertEquals(
                            16877,
                            entry.getUnixMode(),
                            String.format("Directory '%s' has wrong mode!", entry.getName()));
                } else if (entry.getName().contains("/bin/")) {
                    assertEquals(
                            33261,
                            entry.getUnixMode(),
                            String.format("Directory '%s' has wrong mode!", entry.getName()));
                } else {
                    assertEquals(
                            33188, entry.getUnixMode(), String.format("File '%s' has wrong mode!", entry.getName()));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
