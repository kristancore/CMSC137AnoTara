package com.animalfarm;

import com.animalfarm.controller.GameLoop;
import com.animalfarm.controller.InputHandler;
import com.animalfarm.model.GameConfig;
import com.animalfarm.model.GameState;
import com.animalfarm.view.GameRenderer;
import com.animalfarm.view.HUDRenderer;
import com.animalfarm.view.MenuView;
import com.animalfarm.view.SpriteManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class App extends Application {

    private Stage primaryStage;
    private Scene gameScene;
    private GameLoop gameLoop;
    private MenuView menuView;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        SpriteManager.loadSprites();
        
        menuView = new MenuView(
            this::startGame,
            this::showScene
        );

        stage.setTitle("Khan Kluay");
        stage.setScene(menuView.getMainMenuScene());
        stage.show();
    }

    private void showScene(Scene scene) {
        primaryStage.setScene(scene);
    }

    private void showMainMenu() {
        primaryStage.setScene(menuView.getMainMenuScene());
    }

    private void startGame() {
        GameState state = new GameState();

        Canvas canvas = new Canvas(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        Label feedLabel = new Label("Feed: 20");
        Label barnHpLabel = new Label("Enemy Barn HP: 500");
        feedLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");
        barnHpLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: white; -fx-font-weight: bold;");

        Button backBtn = new Button("Back to Menu");
        backBtn.setStyle("-fx-font-family: " + menuView.getFontFam() + "; -fx-background-color: #ff4c4c; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(e -> stopGameAndReturnToMenu());

        VBox hud = new VBox(10, feedLabel, barnHpLabel, backBtn);
        hud.setPadding(new Insets(10));
        hud.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

        StackPane root = new StackPane(canvas, hud);
        StackPane.setAlignment(hud, Pos.TOP_LEFT);
        StackPane.setMargin(hud, new Insets(10));

        GraphicsContext gc = canvas.getGraphicsContext2D();
        GameRenderer renderer = new GameRenderer(gc);
        HUDRenderer hudRenderer = new HUDRenderer(feedLabel, barnHpLabel, gc);
        InputHandler inputHandler = new InputHandler();
        
        gameLoop = new GameLoop(state, renderer, hudRenderer);

        gameScene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        gameScene.setOnKeyPressed(e -> inputHandler.handleSpawnRequest(e.getCode(), state));
        
        root.setOnMouseClicked(e -> root.requestFocus());
        root.requestFocus();

        primaryStage.setScene(gameScene);
        gameLoop.start();
    }

    private void stopGameAndReturnToMenu() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        showMainMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
