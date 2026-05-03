package com.animalfarm;

import com.animalfarm.controller.GameLoop;
import com.animalfarm.controller.InputHandler;
import com.animalfarm.model.GameConfig;
import com.animalfarm.model.GameState;
import com.animalfarm.view.GameRenderer;
import com.animalfarm.view.HUDRenderer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        com.animalfarm.view.SpriteManager.loadSprites();
        GameState state = new GameState();

        Canvas canvas = new Canvas(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        Label feedLabel = new Label("Feed: 20");
        Label barnHpLabel = new Label("Enemy Barn HP: 500");
        feedLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");
        barnHpLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox hud = new VBox(10, feedLabel, barnHpLabel);
        hud.setPadding(new Insets(10));
        hud.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        StackPane root = new StackPane(canvas, hud);
        StackPane.setAlignment(hud, Pos.TOP_LEFT);
        StackPane.setMargin(hud, new Insets(10));

        GraphicsContext gc = canvas.getGraphicsContext2D();
        GameRenderer renderer = new GameRenderer(gc);
        HUDRenderer hudRenderer = new HUDRenderer(feedLabel, barnHpLabel, gc);
        InputHandler inputHandler = new InputHandler();
        GameLoop gameLoop = new GameLoop(state, renderer, hudRenderer);

        Scene scene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        scene.setOnKeyPressed(e -> inputHandler.handleSpawnRequest(e.getCode(), state));

        stage.setTitle("Animal Farm");
        stage.setScene(scene);
        stage.show();
        gameLoop.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
