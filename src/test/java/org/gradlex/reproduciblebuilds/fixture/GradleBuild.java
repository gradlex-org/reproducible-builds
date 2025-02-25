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

package org.gradlex.reproduciblebuilds.fixture;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class GradleBuild implements AutoCloseable {

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
                .withDebug(ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0);
        if (!environment.isEmpty()) {
            runner.withEnvironment(environment);
        }
        return runner;
    }

    public void close() {
        projectDir.delete();
    }
}
