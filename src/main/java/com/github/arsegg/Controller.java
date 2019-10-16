package com.github.arsegg;

import com.github.arsegg.controls.FileViewTab;
import javafx.fxml.FXML;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;

public final class Controller {
    @FXML
    private TabPane tabPane;


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
}
