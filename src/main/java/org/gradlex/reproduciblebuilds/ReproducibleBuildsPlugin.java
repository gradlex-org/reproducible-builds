// SPDX-License-Identifier: Apache-2.0
package org.gradlex.reproduciblebuilds;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;

import java.util.List;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.scala.ScalaCompile;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.gradle.process.CommandLineArgumentProvider;
import org.gradle.util.GradleVersion;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class ReproducibleBuildsPlugin implements Plugin<Project> {

    private static final GradleVersion MINIMUM_SUPPORTED_VERSION = GradleVersion.version("8.3");
    private static final boolean MIN_GRADLE_8_14 =
            GradleVersion.current().compareTo(GradleVersion.version("8.14")) >= 0;
    private static final boolean MIN_GRADLE_9_0 =
            GradleVersion.current().compareTo(GradleVersion.version("9.0.0")) >= 0;

    @Override
    public void apply(Project project) {
        if (GradleVersion.current().compareTo(MINIMUM_SUPPORTED_VERSION) < 0) {
            throw new IllegalStateException(
                    "Plugin requires at least Gradle " + MINIMUM_SUPPORTED_VERSION.getVersion());
        }

        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            @SuppressWarnings("UnstableApiUsage")
            List<CommandLineArgumentProvider> argumentProviders =
                    task.getOptions().getForkOptions().getJvmArgumentProviders();
            task.getOptions().setEncoding(UTF_8.name());
            task.getOptions().setFork(true);
            if (MIN_GRADLE_8_14) {
                // https://github.com/gradle/gradle/issues/27385
                argumentProviders.add(() -> singletonList("-Dline.separator=\n"));
            }
        });
        project.getTasks().withType(Javadoc.class).configureEach(task -> {
            task.getOptions().setEncoding(UTF_8.name());
            if (task.getOptions() instanceof StandardJavadocDocletOptions) {
                StandardJavadocDocletOptions docletOptions = (StandardJavadocDocletOptions) task.getOptions();
                docletOptions.setCharSet(UTF_8.name());
                docletOptions.setDocEncoding(UTF_8.name());
            }
        });
        project.getTasks().withType(GroovyCompile.class).configureEach(task -> {
            task.getOptions().setEncoding(UTF_8.name());
            task.getGroovyOptions().setEncoding(UTF_8.name());
        });
        project.getTasks().withType(ScalaCompile.class).configureEach(task -> {
            task.getOptions().setEncoding(UTF_8.name());
            task.getScalaCompileOptions().setEncoding(UTF_8.name());
        });

        if (!MIN_GRADLE_9_0) {
            applyGradle8SpecificDefaults(project);
        }
    }

    private void applyGradle8SpecificDefaults(Project project) {
        project.getTasks().withType(AbstractArchiveTask.class).configureEach(task -> {
            task.setPreserveFileTimestamps(false);
            task.setReproducibleFileOrder(true);
            task.dirPermissions(p -> p.unix("755"));
            task.filePermissions(p -> p.unix("644"));
        });
    }
}
