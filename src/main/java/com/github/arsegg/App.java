package com.github.arsegg;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class App extends Application {
    public static void main(final String[] args) {
        launch();
    }

    @Override
    public void start(final Stage stage) throws IOException {
        final var fxmlLoader = new FXMLLoader(App.class.getResource("app.fxml"));
        final var root = fxmlLoader.<Parent>load();
        final var scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}