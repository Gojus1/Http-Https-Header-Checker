module com.example.httpserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.httpserver to javafx.fxml;
    exports com.example.httpserver;
}