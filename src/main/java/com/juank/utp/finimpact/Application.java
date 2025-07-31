package com.juank.utp.finimpact;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/com/juank/utp/finimpact/main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("FinImpact - Sistema de Seguimiento de Iniciativas e Impactos Financieros");
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(400);

        // Set full screen using screen bounds
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}