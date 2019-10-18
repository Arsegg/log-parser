package com.github.arsegg.tasks;

import javafx.concurrent.Task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class FirstMatchTask extends Task<Boolean> {
    private final Path file;
    private final Pattern pattern;

    public FirstMatchTask(final Path file, final Pattern pattern) {
        this.file = file;
        this.pattern = pattern;
    }

    @Override
    protected Boolean call() throws Exception {
        try (final var reader = Files.newBufferedReader(file)) {
            var s = "";
            while ((s = reader.readLine()) != null) {
                if (pattern.matcher(s).find()) {
                    return true;
                }
            }
        }

        return false;
    }
}
