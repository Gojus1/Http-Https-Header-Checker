package com.example.httpserver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class GUI extends Application {

    private final Map<String, CheckBox> headerCheckboxes = new LinkedHashMap<>();

    @Override
    public void start(Stage primaryStage) {
        TextField urlField = new TextField("https://");
        urlField.setPrefWidth(400);

        List<String> commonHeaders = List.of(
                "Accept", "Accept-Encoding", "Accept-Language", "Access-Control-Allow-Origin",
                "Cache-Control", "Connection", "Content-Disposition", "Content-Encoding", "Content-Language",
                "Content-Length", "Content-Security-Policy", "Content-Type", "Date", "ETag",
                "Expires", "Host", "Keep-Alive", "Last-Modified", "Location", "Pragma", "Referer",
                "Server", "Set-Cookie", "Strict-Transport-Security", "Transfer-Encoding",
                "Upgrade-Insecure-Requests", "User-Agent", "Vary", "Via", "WWW-Authenticate",
                "X-Content-Type-Options", "X-Frame-Options", "X-Powered-By", "X-XSS-Protection"
        );

        VBox checkboxContainer = new VBox(5);
        for (String header : commonHeaders) {
            CheckBox cb = new CheckBox(header);
            headerCheckboxes.put(header, cb);
            checkboxContainer.getChildren().add(cb);
        }

        ScrollPane checkboxScroll = new ScrollPane(checkboxContainer);
        checkboxScroll.setFitToWidth(true);
        checkboxScroll.setPrefHeight(600);

        Button toggleAllButton = new Button("Select All");
        Button fetchFilteredBtn = new Button("Fetch Headers");

        VBox leftPanel = new VBox(10,
                new Label("Select Headers to Include:"),
                checkboxScroll,
                toggleAllButton,
                fetchFilteredBtn
        );
        leftPanel.setPrefWidth(300);
        leftPanel.setPadding(new Insets(10));

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setWrapText(false);
        outputArea.setPrefWidth(600);
        outputArea.setPrefHeight(600);
        outputArea.setStyle("-fx-font-family: 'monospace'; -fx-font-size: 13px;");
        VBox outputBox = new VBox(outputArea);
        VBox.setVgrow(outputArea, Priority.ALWAYS);

        Label titleLabel = new Label("HTTP/HTTPS Header Viewer 2.0");
        titleLabel.getStyleClass().add("title");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.setAlignment(Pos.CENTER);

//        VBox leftPanel = new VBox(10,
//                new Label("Filter Type:"), filterTypeBox,
//                loadHeadersBtn,
//                new Label("Headers to Include/Exclude:"),
//                checkboxScroll,
//                fetchFilteredBtn);
//        leftPanel.setPrefWidth(300);

        HBox topBar = new HBox(10, new Label("URL:"), urlField);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(0, 0, 10, 0));
        HBox.setHgrow(urlField, Priority.ALWAYS);

        VBox topSection = new VBox(10);
        topSection.setAlignment(Pos.CENTER);
        topSection.setPadding(new Insets(10));
        topSection.getChildren().addAll(titleLabel, topBar);

        BorderPane root = new BorderPane();
        root.setTop(topSection);
        root.setLeft(leftPanel);
        root.setCenter(outputBox);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        checkboxScroll.setStyle("-fx-background-color: #1e1e1e;");
        checkboxContainer.setStyle("-fx-background-color: #1e1e1e;");
        primaryStage.setTitle("HTTP/HTTPS Header Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();

        toggleAllButton.setOnAction(e -> {
            boolean allSelected = headerCheckboxes.values().stream().allMatch(CheckBox::isSelected);
            for (CheckBox cb : headerCheckboxes.values()) {
                cb.setSelected(!allSelected);
            }
            toggleAllButton.setText(allSelected ? "Select All" : "Deselect All");
        });

        fetchFilteredBtn.setOnAction(e -> {
            String url = urlField.getText().trim();
            httpserverchecker.HeaderFilter filter = httpserverchecker.createHeaderFilter(httpserverchecker.FilterType.INCLUDE);

            for (Map.Entry<String, CheckBox> entry : headerCheckboxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    filter.addExactHeader(entry.getKey());
                }
            }

            outputArea.clear();
            outputArea.appendText("Connecting to: " + url + "\n\n");

            new Thread(() -> {
                String result = httpserverchecker.fetchHeaders(url, filter);
                Platform.runLater(() -> outputArea.appendText(result));
            }).start();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}

