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

package org.gradlex.reproduciblebuilds.fixture;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class Directory {

    private final Path directory;

    Directory(Path directory) {
        this.directory = directory;
        mkdirs(directory);
    }

    public WritableFile file(String path) {
        Path file = directory.resolve(path);
        mkdirs(file.getParent());
        return new WritableFile(file);
    }

    public Directory dir(String path) {
        Path dir = directory.resolve(path);
        mkdirs(dir);
        return new Directory(dir);
    }

    public void delete() {
        try (Stream<Path> walk = Files.walk(directory)) {
            walk
                    .sorted(Comparator.reverseOrder())
                    .forEach(Directory::delete);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Path getAsPath() {
        return directory;
    }

    private static void mkdirs(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void delete(Path f) {
        try {
            Files.delete(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
