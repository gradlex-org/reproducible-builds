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

import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.scala.ScalaCompile;

import java.nio.charset.StandardCharsets;

@NonNullApi
public abstract class ReproducibleBuildsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().withType(AbstractArchiveTask.class).configureEach(task -> {
            task.setPreserveFileTimestamps(false);
            task.setReproducibleFileOrder(true);
            task.dirPermissions(p -> p.unix("755"));
            task.filePermissions(p -> p.unix("644"));
        });

        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            task.getOptions().setEncoding(StandardCharsets.UTF_8.displayName());
        });
        project.getTasks().withType(Javadoc.class).configureEach(task -> {
            task.getOptions().setEncoding(StandardCharsets.UTF_8.displayName());
        });
        project.getTasks().withType(GroovyCompile.class).configureEach(task -> {
            task.getOptions().setEncoding(StandardCharsets.UTF_8.displayName());
        });
        project.getTasks().withType(ScalaCompile.class).configureEach(task -> {
            task.getOptions().setEncoding(StandardCharsets.UTF_8.displayName());
        });
    }
}
