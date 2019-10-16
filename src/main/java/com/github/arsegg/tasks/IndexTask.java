package com.github.arsegg.tasks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.nio.file.Files;
import java.nio.file.Path;

public final class IndexTask extends Task<ObservableList<Long>> {
    private final Path file;

    public IndexTask(final Path file) {
        this.file = file;
    }

    @Override
    protected ObservableList<Long> call() throws Exception {
        final var result = FXCollections.<Long>observableArrayList();
        try (final var reader = Files.newInputStream(file)) {
            final var buffer = new byte[8 * 1024];

            var position = 0L;
            result.add(position);

            var n = 0;
            while ((n = reader.read(buffer)) != -1) {
                if (isCancelled()) {
                    break;
                }

                for (int i = 0; i < n; i++) {
                    if (isCancelled()) {
                        break;
                    }

                    if (buffer[i] == '\n') {
                        final var currentPosition = position + i + 1;
                        result.add(currentPosition);
                    }
                }
                position += n;
            }
            result.remove(result.size() - 1); // remove last line
        }

        return result;
    }
}
