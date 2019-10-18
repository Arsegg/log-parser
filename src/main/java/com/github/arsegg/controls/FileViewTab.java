package com.github.arsegg.controls;

import com.github.arsegg.App;
import com.github.arsegg.tasks.FindPatternTask;
import com.github.arsegg.tasks.IndexTask;
import com.github.arsegg.tasks.ReadLineTask;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ListIterator;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public final class FileViewTab extends Tab implements Initializable {
    private final ObjectProperty<Path> file = new SimpleObjectProperty<>(this, "file");
    private final IndexService indexService = new IndexService();
    private final FindPatternService findPatternService = new FindPatternService();
    @FXML
    private ListView<Long> listView;

    public FileViewTab() {
        this(null);
    }

    public FileViewTab(final String text) {
        super(text);

        final var fxmlLoader = new FXMLLoader(App.class.getResource("tab.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);
        try {
            fxmlLoader.load();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public ListView<Long> getListView() {
        return listView;
    }

    public Path getFile() {
        return file.get();
    }

    public void setFile(final Path file) {
        this.file.set(file);
    }

    public ObjectProperty<Path> fileProperty() {
        return file;
    }

    public void loadFile() {
        indexService.start();
    }

    public void setPattern(final Pattern pattern) {
        findPatternService.setPattern(pattern);
    }

    public void find() {
        findPatternService.reset();
        findPatternService.start();
    }

    public void findNext() {
        if (findPatternService.getState() != Worker.State.SUCCEEDED) {
            find();
        }
        findPatternService.focusNext();
    }

    public void findPrevious() {
        if (findPatternService.getState() != Worker.State.SUCCEEDED) {
            find();
        }
        findPatternService.focusPrevious();
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        listView.setCellFactory(param -> new ListCell<>() {
            private ReadLineTask readLineTask;

            @Override
            protected void updateItem(final Long item, final boolean empty) {
                super.updateItem(item, empty);

                if (item != null) {
                    if (readLineTask != null) { // cancel previous task
                        readLineTask.cancel();
                        readLineTask = null;
                    }
                    readLineTask = new ReadLineTask(getFile(), item);
                    readLineTask.setOnSucceeded(event -> setText(readLineTask.getValue()));
                    readLineTask.setOnFailed(event -> readLineTask.getException().printStackTrace());
                    App.getExecutorService().execute(readLineTask);
                }
            }
        });
        final var selectionModel = listView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE); // make multiple selection
    }

    private final class IndexService extends Service<ObservableList<Long>> {
        @Override
        protected void succeeded() {
            super.succeeded();

            listView.setItems(getValue());
        }

        @Override
        protected Task<ObservableList<Long>> createTask() {
            final var _file = getFile();
            return new IndexTask(_file);
        }
    }

    private final class FindPatternService extends Service<ObservableList<Integer>> {
        private final ObjectProperty<Pattern> pattern = new SimpleObjectProperty<>(this, "pattern");
        private ListIterator<Integer> iterator;

        public Pattern getPattern() {
            return pattern.get();
        }

        public void setPattern(final Pattern pattern) {
            this.pattern.set(pattern);
        }

        public ObjectProperty<Pattern> patternProperty() {
            return pattern;
        }

        @Override
        protected void succeeded() {
            super.succeeded();

            selectFound();
            iterator = getValue().listIterator();
            focusNext();
        }

        @Override
        protected void failed() {
            super.failed();

            getException().printStackTrace();
        }

        @Override
        protected Task<ObservableList<Integer>> createTask() {
            return new FindPatternTask(getFile(), getPattern());
        }

        private void selectFound() {
            final var selectionModel = listView.getSelectionModel();
            selectionModel.clearSelection();
            getValue().forEach(selectionModel::select);
        }

        private void focusNext() {
            if (iterator != null) {
                final var index = iterator.next();
                listView.getFocusModel().focus(index);
                listView.scrollTo(index);
            }
        }


        private void focusPrevious() {
            if (iterator != null) {
                final var index = iterator.previous();
                listView.getFocusModel().focus(index);
                listView.scrollTo(index);
            }
        }
    }
}
