package com.github.arsegg.tasks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class FindPatternTask extends Task<ObservableList<Integer>> {
    private final Path file;
    private final Pattern pattern;

    public FindPatternTask(final Path file, final Pattern pattern) {
        this.file = file;
        this.pattern = pattern;
    }

    @Override
    protected ObservableList<Integer> call() throws Exception {
        final var result = FXCollections.<Integer>observableArrayList();

        try (final var reader = Files.newBufferedReader(file)) {
            var s = "";
            var line = 0;
            while ((s = reader.readLine()) != null) {
                if (pattern.matcher(s).find()) {
                    result.add(line);
                }
                line++;
            }
        }

        return result;
    }
}
