package com.lms;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class LmsApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1500, 800);

        String css = Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Library Management System");
        stage.setScene(scene);
        stage.setMinHeight(700);
        stage.setMinWidth(1200);

        try {
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
        } catch (Exception e) {
            // Icon not found, continue without it
            System.out.println("Application icon not found, using default");
        }
        stage.show();

        System.out.println("===========================================");
        System.out.println("Library Management System Started");
        System.out.println("Backend URL: http://localhost:8080");
        System.out.println("===========================================");
    }
}
