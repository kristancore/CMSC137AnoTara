package com.animalfarm.view;

import com.animalfarm.model.GameConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class MenuView {

    private Scene mainMenuScene;
    private Scene emptyScene;
    private Scene creditsScene1;
    private Scene creditsScene2;
    private String fontFam = "'Courier New', monospace";

    private final Runnable onStartGame;
    private final Runnable onPlayOnline;
    private final java.util.function.Consumer<Scene> onSetScene;

    /** Legacy constructor — no online button. */
    public MenuView(Runnable onStartGame, java.util.function.Consumer<Scene> onSetScene) {
        this(onStartGame, null, onSetScene);
    }

    public MenuView(Runnable onStartGame, Runnable onPlayOnline, java.util.function.Consumer<Scene> onSetScene) {
        this.onStartGame  = onStartGame;
        this.onPlayOnline = onPlayOnline;
        this.onSetScene   = onSetScene;

        loadFont();
        createEmptyScene();
        createCreditsScenes();
        createMainMenuScene();
    }

    private void loadFont() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/fonts/PressStart2P-Regular.ttf");
            if (is != null) {
                javafx.scene.text.Font pixelFont = javafx.scene.text.Font.loadFont(is, 10);
                if (pixelFont != null) {
                    fontFam = "'" + pixelFont.getFamily() + "', monospace";
                }
            }
        } catch (Exception e) {
        }
    }

    public Scene getMainMenuScene() {
        return mainMenuScene;
    }

    public Scene getEmptyScene() {
        return emptyScene;
    }

    public String getFontFam() {
        return fontFam;
    }

    public void updateEmptySceneTitle(String title) {
        StackPane root = (StackPane) emptyScene.getRoot();
        VBox layout = (VBox) root.getChildren().get(1);
        Label label = (Label) layout.getChildren().get(0);
        label.setText(title);
    }

    private void setupBackground(StackPane root, boolean showGrass) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/ui/background.png");
            if (is != null) {
                Image bgImage = new Image(is);
                BackgroundImage backgroundImage = new BackgroundImage(bgImage,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(100, 100, true, true, false, false));
                root.setBackground(new Background(backgroundImage));
            } else {
                root.setStyle("-fx-background-color: #55b4ff;");
            }
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #55b4ff;");
        }

        Pane animPane = new Pane();
        root.getChildren().add(animPane);

        addGlitters(animPane);

        if (showGrass) {
            try {
                java.io.InputStream is1 = getClass().getResourceAsStream("/ui/clouds.gif");
                java.io.InputStream is2 = getClass().getResourceAsStream("/ui/clouds.gif");
                if (is1 != null && is2 != null) {
                    Image cloudsImg1 = new Image(is1);
                    Image cloudsImg2 = new Image(is2);

                    ImageView cloudsView1 = new ImageView(cloudsImg1);
                    ImageView cloudsView2 = new ImageView(cloudsImg2);

                    // Keep original size — preserve ratio, fit to full window width
                    double cloudW = GameConfig.WINDOW_WIDTH;
                    for (ImageView cv : new ImageView[] { cloudsView1, cloudsView2 }) {
                        cv.setPreserveRatio(true);
                        cv.fitWidthProperty().bind(root.widthProperty());
                    }

                    // Place the two copies side by side
                    cloudsView1.setTranslateX(0);
                    cloudsView2.setTranslateX(cloudW);

                    StackPane.setAlignment(cloudsView1, Pos.TOP_LEFT);
                    StackPane.setAlignment(cloudsView2, Pos.TOP_LEFT);
                    root.getChildren().addAll(cloudsView1, cloudsView2);

                    // Slow pan: scroll leftward at 20px/sec
                    double[] offset = { 0 };
                    double speed = 0.50; // pixels per second - lower = slower
                    long[] lastTime = { -1 };

                    javafx.animation.AnimationTimer cloudTimer = new javafx.animation.AnimationTimer() {
                        @Override
                        public void handle(long now) {
                            if (lastTime[0] < 0) {
                                lastTime[0] = now;
                                return;
                            }
                            double delta = (now - lastTime[0]) / 1_000_000_000.0;
                            lastTime[0] = now;
                            offset[0] -= speed * delta;
                            if (offset[0] <= -cloudW)
                                offset[0] += cloudW;
                            cloudsView1.setTranslateX(offset[0]);
                            cloudsView2.setTranslateX(offset[0] + cloudW);
                        }
                    };
                    cloudTimer.start();
                }
            } catch (Exception e) {
            }

            ImageView grasslandView = null;
            try {
                java.io.InputStream is = getClass().getResourceAsStream("/ui/grassland.png");
                if (is != null) {
                    Image grasslandImg = new Image(is);
                    grasslandView = new ImageView(grasslandImg);
                    grasslandView.setPreserveRatio(true);
                    grasslandView.fitWidthProperty().bind(root.widthProperty());

                    StackPane.setAlignment(grasslandView, Pos.BOTTOM_CENTER);
                    root.getChildren().add(grasslandView);
                }
            } catch (Exception e) {
            }

            try {
                java.io.InputStream is = getClass().getResourceAsStream("/ui/grass.png");
                if (is != null) {
                    Image grassImg = new Image(is);

                    ImageView bush1 = new ImageView(grassImg);
                    bush1.setPreserveRatio(true);
                    bush1.setFitWidth(250);
                    StackPane.setAlignment(bush1, Pos.BOTTOM_LEFT);

                    ImageView bush2 = new ImageView(grassImg);
                    bush2.setPreserveRatio(true);
                    bush2.setFitWidth(200);
                    StackPane.setAlignment(bush2, Pos.BOTTOM_RIGHT);

                    root.widthProperty().addListener((obs, oldVal, newVal) -> {
                        double currentGroundHeight = 161.0 * (newVal.doubleValue() / 834.0);
                        StackPane.setMargin(bush1, new Insets(0, 0, currentGroundHeight - 10, 50));
                        StackPane.setMargin(bush2, new Insets(0, 50, currentGroundHeight - 10, 0));
                    });

                    double initialGroundHeight = 161.0 * (GameConfig.WINDOW_WIDTH / 834.0);
                    StackPane.setMargin(bush1, new Insets(0, 0, initialGroundHeight - 10, 50));
                    StackPane.setMargin(bush2, new Insets(0, 50, initialGroundHeight - 10, 0));

                    root.getChildren().addAll(bush1, bush2);
                }
            } catch (Exception e) {
            }
        }
    }

    private void createMainMenuScene() {
        StackPane root = new StackPane();
        setupBackground(root, true);

        VBox titleBox = new VBox(15);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setTranslateY(-80);

        Label title = new Label("Sa Kabukiran");
        title.setStyle("-fx-font-family: " + fontFam + "; -fx-font-size: 60px; -fx-text-fill: white;");
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(javafx.scene.paint.Color.web("#1c5a8a"));
        shadow.setOffsetX(6);
        shadow.setOffsetY(6);
        shadow.setRadius(0);
        title.setEffect(shadow);

        Label subtitle = new Label("A Khan Kluay-Inspired Game");
        subtitle.setStyle("-fx-font-family: " + fontFam + "; -fx-font-size: 14px; -fx-text-fill: white;");
        subtitle.setEffect(shadow);

        titleBox.getChildren().addAll(title, subtitle);

        javafx.animation.TranslateTransition ttTitle = new javafx.animation.TranslateTransition(
                javafx.util.Duration.seconds(1.5), titleBox);
        ttTitle.setByY(16);
        ttTitle.setAutoReverse(true);
        ttTitle.setCycleCount(javafx.animation.Animation.INDEFINITE);
        ttTitle.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        ttTitle.play();

        HBox buttonBox = new HBox(50);
        buttonBox.setAlignment(Pos.BOTTOM_CENTER);
        buttonBox.setPadding(new Insets(0, 0, 40, 0));

        Button creditsBtn = createMenuButton("CREDITS");
        Button startBtn   = createMenuButton("LOCAL");
        Button onlineBtn  = createMenuButton("ONLINE");
        Button rulesBtn   = createMenuButton("RULES");

        startBtn.setOnAction(e -> {
            if (onStartGame != null)
                onStartGame.run();
        });
        onlineBtn.setOnAction(e -> {
            if (onPlayOnline != null)
                onPlayOnline.run();
        });
        creditsBtn.setOnAction(e -> {
            if (onSetScene != null)
                onSetScene.accept(creditsScene1);
        });
        rulesBtn.setOnAction(e -> {
            updateEmptySceneTitle("RULES");
            if (onSetScene != null)
                onSetScene.accept(emptyScene);
        });

        buttonBox.getChildren().addAll(creditsBtn, startBtn, onlineBtn, rulesBtn);
        StackPane.setAlignment(buttonBox, Pos.BOTTOM_CENTER);

        root.getChildren().addAll(titleBox, buttonBox);
        mainMenuScene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        mainMenuScene.setOnMousePressed(e -> AudioManager.getInstance().playClick());
    }

    private void createCreditsScenes() {
        String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum";

        creditsScene1 = createPagedScene("credits", "PART 1 " + lorem.toUpperCase(), null, null, "NEXT", () -> {
            if (onSetScene != null)
                onSetScene.accept(creditsScene2);
        });

        creditsScene2 = createPagedScene("credits", "PART 2 " + lorem.toUpperCase(), "PREVIOUS", () -> {
            if (onSetScene != null)
                onSetScene.accept(creditsScene1);
        }, null, null);
    }

    private Scene createPagedScene(String titleText, String contentText, String leftBtnText, Runnable leftBtnAction,
            String rightBtnText, Runnable rightBtnAction) {
        StackPane root = new StackPane();
        setupBackground(root, true);

        VBox contentBox = new VBox(40);
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.setPadding(new Insets(60, 50, 0, 50));

        Label title = new Label(titleText.toLowerCase());
        title.setStyle("-fx-font-family: " + fontFam + "; -fx-font-size: 50px; -fx-text-fill: white;");
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(javafx.scene.paint.Color.web("#1c5a8a"));
        shadow.setOffsetX(4);
        shadow.setOffsetY(4);
        shadow.setRadius(0);
        title.setEffect(shadow);

        Label content = new Label(contentText);
        content.setStyle(
                "-fx-font-family: " + fontFam + "; -fx-font-size: 14px; -fx-text-fill: white; -fx-line-spacing: 8px;");
        content.setWrapText(true);
        content.setTextAlignment(javafx.scene.text.TextAlignment.JUSTIFY);
        content.setMaxWidth(550);
        content.setEffect(shadow);

        contentBox.getChildren().addAll(title, content);

        StackPane buttonLayout = new StackPane();
        buttonLayout.setPadding(new Insets(0, 50, 40, 50));

        if (leftBtnText != null) {
            Button leftBtn = createMenuButton(leftBtnText);
            leftBtn.setOnAction(e -> leftBtnAction.run());
            StackPane.setAlignment(leftBtn, Pos.BOTTOM_LEFT);
            buttonLayout.getChildren().add(leftBtn);
        }

        Button mainMenuBtn = createMenuButton("MAIN MENU");
        mainMenuBtn.setOnAction(e -> {
            if (onSetScene != null)
                onSetScene.accept(mainMenuScene);
        });
        StackPane.setAlignment(mainMenuBtn, Pos.BOTTOM_CENTER);
        buttonLayout.getChildren().add(mainMenuBtn);

        if (rightBtnText != null) {
            Button rightBtn = createMenuButton(rightBtnText);
            rightBtn.setOnAction(e -> rightBtnAction.run());
            StackPane.setAlignment(rightBtn, Pos.BOTTOM_RIGHT);
            buttonLayout.getChildren().add(rightBtn);
        }

        StackPane.setAlignment(buttonLayout, Pos.BOTTOM_CENTER);
        root.getChildren().addAll(contentBox, buttonLayout);

        Scene scene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        scene.setOnMousePressed(e -> AudioManager.getInstance().playClick());
        return scene;
    }

    private void createEmptyScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #55b4ff;");

        Pane animPane = new Pane();
        addGlitters(animPane);
        root.getChildren().add(animPane);

        Label titleLabel = new Label("EMPTY PAGE");
        titleLabel.setStyle("-fx-font-family: " + fontFam + "; -fx-font-size: 40px; -fx-text-fill: white;");

        Button backBtn = createMenuButton("BACK TO MENU");
        backBtn.setOnAction(e -> {
            if (onSetScene != null)
                onSetScene.accept(mainMenuScene);
        });

        VBox layout = new VBox(50, titleLabel, backBtn);
        layout.setAlignment(Pos.CENTER);

        root.getChildren().add(layout);
        emptyScene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        emptyScene.setOnMousePressed(e -> AudioManager.getInstance().playClick());
    }

    private void addGlitters(Pane root) {
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 40; i++) {
            int pSize = rand.nextInt(3) * 4 + 4;
            javafx.scene.shape.Rectangle glitter = new javafx.scene.shape.Rectangle(pSize, pSize,
                    javafx.scene.paint.Color.WHITE);
            glitter.setOpacity(0);

            glitter.setLayoutX(rand.nextInt(GameConfig.WINDOW_WIDTH / 4) * 4);
            glitter.setLayoutY(rand.nextInt((GameConfig.WINDOW_HEIGHT) / 4) * 4);

            javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                    javafx.util.Duration.seconds(rand.nextDouble() * 1.5 + 0.5), glitter);
            ft.setFromValue(0.0);
            ft.setToValue(0.8);
            ft.setAutoReverse(true);
            ft.setCycleCount(javafx.animation.Animation.INDEFINITE);
            ft.setDelay(javafx.util.Duration.seconds(rand.nextDouble() * 2));
            ft.play();
            root.getChildren().add(glitter);
        }
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-family: " + fontFam + "; " +
                "-fx-font-size: 18px; " +
                "-fx-text-fill: white; " +
                "-fx-background-color: transparent; " +
                "-fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-font-family: " + fontFam
                        + "; -fx-font-size: 20px; -fx-text-fill: yellow; -fx-background-color: transparent; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-font-family: " + fontFam
                        + "; -fx-font-size: 18px; -fx-text-fill: white; -fx-background-color: transparent; -fx-cursor: hand;"));

        // Play click SFX on every button press
        btn.setOnMousePressed(e -> AudioManager.getInstance().playClick());

        return btn;
    }
}
