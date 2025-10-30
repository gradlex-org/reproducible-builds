// SPDX-License-Identifier: Apache-2.0
package org.gradlex.reproduciblebuilds.fixture;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

public class GradleBuild implements AutoCloseable {
    public static final String GRADLE_VERSION_UNDER_TEST = System.getProperty("gradleVersionUnderTest");

    final Directory projectDir;
    final WritableFile buildFile;
    final WritableFile settingsFile;
    final WritableFile gradleProperties;
    final Map<String, String> environment = new HashMap<>();

    private GradleBuild(Path projectDirectory) {
        this.projectDir = new Directory(projectDirectory);
        this.buildFile = new WritableFile(projectDirectory, "build.gradle");
        this.settingsFile = new WritableFile(projectDirectory, "settings.gradle");
        this.gradleProperties = new WritableFile(projectDirectory, "gradle.properties");

        this.settingsFile.writeText("rootProject.name = 'test-project'\n");
    }

    public static GradleBuild create() {
        return create(Io.unchecked(() -> Files.createTempDirectory("gradle-build")));
    }

    public static GradleBuild create(Path projectDirectory) {
        return new GradleBuild(projectDirectory);
    }

    public Directory getProjectDir() {
        return projectDir;
    }

    public WritableFile getBuildFile() {
        return buildFile;
    }

    public WritableFile getSettingsFile() {
        return settingsFile;
    }

    public WritableFile getGradleProperties() {
        return gradleProperties;
    }

    public BuildResult run(String... args) {
        return runner(args).build();
    }

    public BuildResult runAndFail(String... args) {
        return runner(args).buildAndFail();
    }

    public GradleRunner runner(String... args) {
        GradleRunner runner = GradleRunner.create()
                .withProjectDir(projectDir.getAsPath().toFile())
                .withPluginClasspath()
                .withArguments(args)
                .forwardOutput()
                .withDebug(ManagementFactory.getRuntimeMXBean()
                                .getInputArguments()
                                .toString()
                                .indexOf("-agentlib:jdwp")
                        > 0);
        if (!environment.isEmpty()) {
            runner.withEnvironment(environment);
        }
        if (GRADLE_VERSION_UNDER_TEST != null) {
            runner.withGradleVersion(GRADLE_VERSION_UNDER_TEST);
        }
        return runner;
    }

    public String output(String path) {
        return Io.unchecked(
                () -> Files.readString(projectDir.getAsPath().resolve("build").resolve(path)));
    }

    public void close() {
        projectDir.delete();
    }
}
