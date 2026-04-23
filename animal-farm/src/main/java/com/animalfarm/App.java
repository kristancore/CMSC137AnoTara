package com.animalfarm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(960, 540);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, 960, 540);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(32));
        gc.fillText("Animal Farm — JavaFX 21 OK", 280, 285);

        StackPane root = new StackPane(canvas);
        stage.setScene(new Scene(root, 960, 540));
        stage.setTitle("Animal Farm");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
