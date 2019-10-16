package com.github.arsegg.controls;

import com.github.arsegg.App;
import com.github.arsegg.tasks.IndexTask;
import com.github.arsegg.tasks.ReadLineTask;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

public final class FileViewTab extends Tab implements Initializable {
    private final ObjectProperty<Path> file = new SimpleObjectProperty<>(this, "file");
    private final IndexService indexService = new IndexService();
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

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        listView.setCellFactory(param -> new ListCell<>() {
            private ReadLineService readLineService;

            @Override
            protected void updateItem(final Long item, final boolean empty) {
                super.updateItem(item, empty);

                if (item != null) {
                    if (readLineService != null) { // cancel previous task
                        readLineService = null;
                    }
                    readLineService = new ReadLineService(item);
                    readLineService.setOnSucceeded(event -> setText(readLineService.getValue()));
                    readLineService.setOnFailed(event -> readLineService.getException().printStackTrace());
                    readLineService.start();
                }
            }
        });
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

    private final class ReadLineService extends Service<String> {
        private final long position;

        public ReadLineService(final long position) {
            this.position = position;
        }

        @Override
        protected Task<String> createTask() {
            return new ReadLineTask(getFile(), position);
        }
    }
}
