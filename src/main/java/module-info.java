module com.github.arsegg {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.github.arsegg to javafx.fxml;
    exports com.github.arsegg;
}