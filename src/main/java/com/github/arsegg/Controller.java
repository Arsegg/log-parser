package com.github.arsegg;

import com.github.arsegg.controls.FileViewTab;
import com.github.arsegg.tasks.FindPatternTask;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class Controller {
    private final List<Reference<FindPatternTask>> findPatternTaskList = new ArrayList<>();
    @FXML
    private TreeView<Path> treeView;
    @FXML
    private TabPane tabPane;
    @FXML
    private TextField patternTextField;
    @FXML
    private TextField formatTextField;

    @FXML
    public void openFileDialog() {
        final var window = tabPane.getScene().getWindow();
        final var fileChooser = new FileChooser();
        final var fileList = fileChooser.showOpenMultipleDialog(window);

        if (fileList == null) { // if files are not chosen
            return;
        }
        fileList.forEach(file -> {
            final var tab = new FileViewTab(file.toString());

            tab.setFile(file.toPath());
            tab.loadFile();

            tabPane.getTabs().add(tab);
        });
        tabPane.getSelectionModel().selectLast();
    }

    @FXML
    public void openDirectoryDialog() {
        final var window = treeView.getScene().getWindow();
        final var pattern = patternTextField.getText();
        final var format = formatTextField.getText();
        final var directoryChooser = new DirectoryChooser();
        final var directory = directoryChooser.showDialog(window);

        if (directory == null) { // directory is not chosen
            return;
        }
        if (!findPatternTaskList.isEmpty()) { // cancel previous tasks
            findPatternTaskList.forEach(findPatternTaskReference -> {
                final var task = findPatternTaskReference.get();
                if (task != null) {
                    task.cancel();
                }
            });
            findPatternTaskList.clear();
        }
        try (final var walk = Files.walk(directory.toPath())) {
            final var root = new TreeItem<>(directory.toPath());
            treeView.setRoot(root);
            walk.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(format)) // filter regular files of certain format
                    .forEach(file -> {
                        final var task = new FindPatternTask(file, Pattern.compile(pattern));
                        task.setOnSucceeded(event -> {
                            System.out.println("Done!");
                            if (task.getValue()) {
                                addLeaf(root, file);
                            }
                        });
                        task.setOnFailed(event -> task.getException().printStackTrace());
                        findPatternTaskList.add(new WeakReference<>(task));
                        App.getExecutorService().execute(task);
                    });
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void addLeaf(final TreeItem<Path> root, final Path file) {
        root.getChildren().add(new TreeItem<>(file));
    }
}
