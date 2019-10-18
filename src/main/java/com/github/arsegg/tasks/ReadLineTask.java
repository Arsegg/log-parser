package com.github.arsegg.tasks;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.file.Path;

public final class ReadLineTask extends Task<String> {
    private final Path file;
    private final long position;

    public ReadLineTask(final Path file, final long position) {
        this.file = file;
        this.position = position;
    }

    @Override
    protected String call() throws Exception {
        try (final var fileInputStream = new FileInputStream(file.toFile());
             final var channel = fileInputStream.getChannel().position(position);
             final var reader = new BufferedReader(Channels.newReader(channel, Charset.defaultCharset()))) {
            return reader.readLine();
        }
    }
}
