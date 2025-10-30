// SPDX-License-Identifier: Apache-2.0
package org.gradlex.reproduciblebuilds.fixture;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class WritableFile {

    private final Path file;

    public WritableFile(Path file) {
        this.file = file;
    }

    public WritableFile(Path parent, String fileName) {
        this.file = Io.unchecked(() -> Files.createDirectories(parent)).resolve(fileName);
    }

    public void writeText(String text) {
        Io.unchecked(() -> Files.writeString(file, text, StandardOpenOption.CREATE, StandardOpenOption.WRITE));
    }

    public void appendText(String text) {
        Io.unchecked(() -> Files.writeString(file, text, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
    }

    public boolean exists() {
        return Files.exists(file);
    }

    public Path getAsPath() {
        return file;
    }
}
