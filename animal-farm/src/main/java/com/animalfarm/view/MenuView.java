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
    private Scene[] rulesScenes = new Scene[8];
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
        createRulesScenes();
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

    private void setupMenuBackground(StackPane root) {
        try {
            java.io.InputStream bgIs = getClass().getResourceAsStream("/ui/menu-background.png");
            if (bgIs != null) {
                ImageView bgView = new ImageView(new Image(bgIs));
                bgView.setFitWidth(GameConfig.WINDOW_WIDTH);
                bgView.setFitHeight(GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
                bgView.setPreserveRatio(false);
                root.getChildren().add(bgView);
            }

            java.io.InputStream raysIs = getClass().getResourceAsStream("/ui/menu-rays.png");
            if (raysIs != null) {
                ImageView raysView = new ImageView(new Image(raysIs));
                raysView.setPreserveRatio(true);
                double size = Math.max(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT) * 1.5;
                raysView.setFitWidth(size);
                raysView.setFitHeight(size);

                javafx.animation.RotateTransition rt = new javafx.animation.RotateTransition(
                        javafx.util.Duration.seconds(40), raysView);
                rt.setByAngle(360);
                rt.setCycleCount(javafx.animation.Animation.INDEFINITE);
                rt.setInterpolator(javafx.animation.Interpolator.LINEAR);
                rt.play();

                root.getChildren().add(raysView);
            }

            java.io.InputStream fgIs = getClass().getResourceAsStream("/ui/menu-foreground.png");
            if (fgIs != null) {
                ImageView fgView = new ImageView(new Image(fgIs));
                fgView.setFitWidth(GameConfig.WINDOW_WIDTH);
                fgView.setFitHeight(GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
                fgView.setPreserveRatio(false);
                root.getChildren().add(fgView);
            }
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #55b4ff;");
        }
    }

    private void setupBackground(StackPane root, boolean showGrass) {
        setupBackground(root, showGrass, showGrass, "/ui/background.png");
    }

    private void setupBackground(StackPane root, boolean showGrass, boolean showClouds) {
        setupBackground(root, showGrass, showClouds, "/ui/background.png");
    }

    private void setupBackground(StackPane root, boolean showGrass, boolean showClouds, String bgImagePath) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream(bgImagePath);
            if (is != null) {
                Image bgImage = new Image(is);
                BackgroundImage backgroundImage = new BackgroundImage(bgImage,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true));
                root.setBackground(new Background(backgroundImage));
            } else {
                root.setStyle("-fx-background-color: #55b4ff;");
            }
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #55b4ff;");
        }
    }

    private void createMainMenuScene() {
        StackPane root = new StackPane();
        setupMenuBackground(root);

        ImageView titleView = new ImageView();
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/ui/menu-title.png");
            if (is != null) {
                titleView.setImage(new Image(is));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        titleView.setPreserveRatio(true);
        titleView.setFitWidth(650);
        titleView.setTranslateY(-150);
        StackPane.setAlignment(titleView, Pos.CENTER);

        javafx.animation.TranslateTransition ttTitle = new javafx.animation.TranslateTransition(
                javafx.util.Duration.seconds(1.8), titleView);
        ttTitle.setByY(20);
        ttTitle.setAutoReverse(true);
        ttTitle.setCycleCount(javafx.animation.Animation.INDEFINITE);
        ttTitle.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);
        ttTitle.play();

        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.BOTTOM_CENTER);
        buttonBox.setPadding(new Insets(0, 0, 45, 0));

        Button startBtn = createImageButton("/ui/menu-start.png", "/ui/menu-start-selected.png", () -> {
            if (onStartGame != null)
                onStartGame.run();
        });

        Button onlineBtn = createImageButton("/ui/menu-online.png", "/ui/menu-online-selected.png", () -> {
            if (onPlayOnline != null)
                onPlayOnline.run();
        });

        Button rulesBtn = createImageButton("/ui/menu-tutorial.png", "/ui/menu-tutorial-selected.png", () -> {
            if (onSetScene != null)
                onSetScene.accept(rulesScenes[0]);
        });

        Button creditsBtn = createImageButton("/ui/menu-credits.png", "/ui/menu-credits-selected.png", () -> {
            if (onSetScene != null)
                onSetScene.accept(creditsScene1);
        });

        buttonBox.getChildren().addAll(startBtn, onlineBtn, rulesBtn, creditsBtn);
        StackPane.setAlignment(buttonBox, Pos.BOTTOM_CENTER);

        root.getChildren().addAll(titleView, buttonBox);
        mainMenuScene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        mainMenuScene.setOnMousePressed(e -> AudioManager.getInstance().playClick());
    }

    private void createCreditsScenes() {
        String page1Text =
            "Sa Kabukiran is inspired by Khan Kluay (2006), Thailand's first 3D computer-animated film.\n\n" +
            "This game was developed by Jeoff Nathaniel M. Conde, John Michael Magpantay, " +
            "and Kristan Louie Escarilla, created as a final project for CMSC 137 — Data Communications and Networking, " +
            "Second Semester, A.Y. 2025–2026. Built using Java 21 and JavaFX 21 (OpenJFX).";

        creditsScene1 = createPagedScene("credits", page1Text, null, null, null, null);
    }

    private Scene createPagedScene(String titleText, String contentText, String leftBtnText, Runnable leftBtnAction,
            String rightBtnText, Runnable rightBtnAction) {
        StackPane root = new StackPane();
        setupBackground(root, true, true, "/ui/credits-background.png");

        VBox contentBox = new VBox(40);
        contentBox.setAlignment(Pos.TOP_CENTER);
        contentBox.setPadding(new Insets(250, 50, 0, 50));

        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(javafx.scene.paint.Color.web("#1c5a8a"));
        shadow.setOffsetX(4);
        shadow.setOffsetY(4);
        shadow.setRadius(0);

        javafx.scene.Node titleNode;
        if (titleText.equalsIgnoreCase("credits")) {
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/ui/menu-credits.png")));
            iv.setPreserveRatio(true);
            iv.setFitHeight(40);
            titleNode = iv;
        } else {
            Label title = new Label(titleText.toLowerCase());
            title.setStyle("-fx-font-family: " + fontFam + "; -fx-font-size: 50px; -fx-text-fill: white;");
            title.setEffect(shadow);
            titleNode = title;
        }

        Label content = new Label(contentText);
        content.setStyle(
                "-fx-font-family: " + fontFam + "; -fx-font-size: 14px; -fx-text-fill: white; -fx-line-spacing: 8px;");
        content.setWrapText(true);
        content.setTextAlignment(javafx.scene.text.TextAlignment.JUSTIFY);
        content.setMaxWidth(780);
        content.setEffect(shadow);

        contentBox.getChildren().addAll(titleNode, content);

        StackPane buttonLayout = new StackPane();
        buttonLayout.setPadding(new Insets(0, 50, 40, 50));

        if (leftBtnText != null) {
            Button leftBtn = createImageButton("/ui/previous-button.png", "/ui/previous-button-selected.png", () -> leftBtnAction.run());
            StackPane.setAlignment(leftBtn, Pos.BOTTOM_LEFT);
            buttonLayout.getChildren().add(leftBtn);
        }

        Button mainMenuBtn = createImageButton("/ui/return-to-menu-button.png", "/ui/return-to-menu-button-selected.png", () -> {
            if (onSetScene != null)
                onSetScene.accept(mainMenuScene);
        });
        StackPane.setAlignment(mainMenuBtn, Pos.BOTTOM_CENTER);
        buttonLayout.getChildren().add(mainMenuBtn);

        if (rightBtnText != null) {
            Button rightBtn = createImageButton("/ui/next-button.png", "/ui/next-button-selected.png", () -> rightBtnAction.run());
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

    private void createRulesScenes() {
        String p1 =
            "SA KABUKIRAN (4-PLAYER)\n\n" +
            "OBJECTIVE\n" +
            "Score the most points before the 100-second timer runs out. " +
            "Earn 1 point every time your animal successfully crosses into an opponent's barn.\n\n" +
            "CONTROLS (ALL PLAYERS)\n" +
            "↑ / ↓ — Switch Lanes\n" +
            "SPACE — Deploy Animal";

        String p2 =
            "Fast and cheap — your most nimble unit.\n" +
            "Low HP and no armor, but its speed lets it slip past battles quickly.\n" +
            "Best used to sneak a point when lanes are open.\n\n" +
            "HP: 50  |  Speed: 80  |  Armor: 0  |  Damage: 5";

        String p3 =
            "A balanced all-rounder with decent HP and a solid punch.\n" +
            "Moves at a moderate pace — reliable for general pushing.\n\n" +
            "HP: 100  |  Speed: 50  |  Armor: 2  |  Damage: 10";

        String p4 =
            "Heavy hitter with high HP and strong damage.\n" +
            "Slow to move but dominates head-to-head fights.\n" +
            "Best for breaking through enemy pushes.\n\n" +
            "HP: 200  |  Speed: 30  |  Armor: 5  |  Damage: 20";

        String p5 =
            "The tankiest unit — highest HP and best armor in the game.\n" +
            "Moves slowly but is extremely hard to stop once it gets going.\n" +
            "Low damage, but it absorbs hits for other units behind it.\n\n" +
            "HP: 250  |  Speed: 25  |  Armor: 10  |  Damage: 15";

        String p6 =
            "Maximum armor, making it resistant to damage — a wall on legs.\n" +
            "Very slow and low damage, but nearly impossible to push back.\n\n" +
            "HP: 300  |  Speed: 20  |  Armor: 15  |  Damage: 8";

        String p7 =
            "LOBBY SYSTEM\n" +
            "The host creates a room code; joiners enter the code to connect.\n\n" +
            "THE QUEUE\n" +
            "You have a visible queue of 3 randomly assigned animals.\n\n" +
            "COMBAT\n" +
            "Animals automatically move forward. When they collide, the side with higher total HP pushes the other back.\n\n" +
            "COOLDOWN\n" +
            "There is a 2-second cooldown between animal deployments.";

        String p8 =
            "Triggers if scores are tied at 0 seconds.\n\n" +
            "The deploy cooldown is slashed to 1 second.\n\n" +
            "The first player to score 1 point wins!";

        rulesScenes[0] = buildRulesPage("rules",       null,      buildRulesTextNode(p1),                                       0);
        rulesScenes[1] = buildRulesPage("chicken",  "animals", buildRulesAnimalNode("/chicken/right_1_chicken_walk.png", p2), 1);
        rulesScenes[2] = buildRulesPage("pig",         null,      buildRulesAnimalNode("/pig/right_1_pig_walk.png",       p3),   2);
        rulesScenes[3] = buildRulesPage("cow",         null,      buildRulesAnimalNode("/cow/right_1_cow_walk.png",       p4),   3);
        rulesScenes[4] = buildRulesPage("sheep",       null,      buildRulesAnimalNode("/sheep/right_1_sheep_walk.png",   p5),   4);
        rulesScenes[5] = buildRulesPage("llama",       null,      buildRulesAnimalNode("/llama/right_1_llama_walk.png",   p6),   5);
        rulesScenes[6] = buildRulesPage("how to play", null,      buildRulesTextNode(p7),                                       6);
        rulesScenes[7] = buildRulesPage("sudden death", null,     buildRulesTextNode(p8),                                       7);
    }

    private Scene buildRulesPage(String titleText, String subtitle, javafx.scene.Node contentNode, int idx) {
        StackPane root = new StackPane();
        setupBackground(root, true, false, "/ui/tutorial-background.png");

        VBox contentBox = new VBox(20);
        contentBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setPadding(new Insets(250, 60, 0, 60));

        javafx.scene.effect.DropShadow shadow = makeDropShadow();

        javafx.scene.Node titleNode;
        if (titleText.equalsIgnoreCase("rules")) {
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/ui/menu-tutorial.png")));
            iv.setPreserveRatio(true);
            iv.setFitHeight(40);
            titleNode = iv;
        } else {
            Label title = new Label(titleText.toLowerCase());
            title.setStyle("-fx-font-family: " + fontFam + "; -fx-font-size: 50px; -fx-text-fill: white;");
            title.setEffect(shadow);
            titleNode = title;
        }
        contentBox.getChildren().add(titleNode);

        if (subtitle != null) {
            Label sub = new Label(subtitle.toUpperCase());
            sub.setStyle("-fx-font-family: " + fontFam + "; -fx-font-size: 12px; -fx-text-fill: #ffee88;");
            sub.setEffect(shadow);
            contentBox.getChildren().add(sub);
        }

        contentBox.getChildren().add(contentNode);

        StackPane buttonLayout = new StackPane();
        buttonLayout.setPadding(new Insets(0, 50, 40, 50));

        if (idx > 0) {
            Button prevBtn = createImageButton("/ui/previous-button.png", "/ui/previous-button-selected.png", () -> { if (onSetScene != null) onSetScene.accept(rulesScenes[idx - 1]); });
            StackPane.setAlignment(prevBtn, Pos.BOTTOM_LEFT);
            buttonLayout.getChildren().add(prevBtn);
        }

        Button mainMenuBtn = createImageButton("/ui/return-to-menu-button.png", "/ui/return-to-menu-button-selected.png", () -> { if (onSetScene != null) onSetScene.accept(mainMenuScene); });
        StackPane.setAlignment(mainMenuBtn, Pos.BOTTOM_CENTER);
        buttonLayout.getChildren().add(mainMenuBtn);

        if (idx < rulesScenes.length - 1) {
            Button nextBtn = createImageButton("/ui/next-button.png", "/ui/next-button-selected.png", () -> { if (onSetScene != null) onSetScene.accept(rulesScenes[idx + 1]); });
            StackPane.setAlignment(nextBtn, Pos.BOTTOM_RIGHT);
            buttonLayout.getChildren().add(nextBtn);
        }

        StackPane.setAlignment(buttonLayout, Pos.BOTTOM_CENTER);
        root.getChildren().addAll(contentBox, buttonLayout);

        Scene scene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        scene.setOnMousePressed(e -> AudioManager.getInstance().playClick());
        return scene;
    }

    private javafx.scene.Node buildRulesTextNode(String text) {
        Label content = new Label(text);
        content.setStyle("-fx-font-family: " + fontFam + "; -fx-font-size: 14px; -fx-text-fill: white; -fx-line-spacing: 8px;");
        content.setWrapText(true);
        content.setMaxWidth(780);
        content.setEffect(makeDropShadow());
        return content;
    }

    private javafx.scene.Node buildRulesAnimalNode(String imagePath, String desc) {
        ImageView iv = new ImageView();
        try {
            java.io.InputStream is = getClass().getResourceAsStream(imagePath);
            if (is != null) {
                iv.setImage(new Image(is));
                iv.setFitHeight(150);
                iv.setPreserveRatio(true);
            }
        } catch (Exception ignored) {}

        Label textLbl = new Label(desc);
        textLbl.setStyle("-fx-font-family: " + fontFam + "; -fx-font-size: 14px; -fx-text-fill: white; -fx-line-spacing: 8px;");
        textLbl.setWrapText(true);
        textLbl.setMaxWidth(580);
        textLbl.setEffect(makeDropShadow());

        HBox hbox = new HBox(40, iv, textLbl);
        hbox.setAlignment(Pos.CENTER_LEFT);
        return hbox;
    }

    private javafx.scene.effect.DropShadow makeDropShadow() {
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(javafx.scene.paint.Color.web("#1c5a8a"));
        shadow.setOffsetX(4);
        shadow.setOffsetY(4);
        shadow.setRadius(0);
        return shadow;
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

        btn.setOnMousePressed(e -> AudioManager.getInstance().playClick());

        return btn;
    }

    private Button createImageButton(String normalPath, String selectedPath, Runnable action) {
        Button btn = new Button();
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0;");

        try {
            java.io.InputStream normalIs = getClass().getResourceAsStream(normalPath);
            java.io.InputStream selectedIs = getClass().getResourceAsStream(selectedPath);

            if (normalIs != null && selectedIs != null) {
                Image normalImg = new Image(normalIs);
                Image selectedImg = new Image(selectedIs);

                ImageView iv = new ImageView(normalImg);
                iv.setPreserveRatio(true);
                iv.setFitHeight(50);

                btn.setGraphic(iv);

                btn.setOnMouseEntered(e -> {
                    iv.setImage(selectedImg);
                    iv.setScaleX(1.05);
                    iv.setScaleY(1.05);
                });

                btn.setOnMouseExited(e -> {
                    iv.setImage(normalImg);
                    iv.setScaleX(1.0);
                    iv.setScaleY(1.0);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        btn.setOnAction(e -> {
            if (action != null) {
                action.run();
            }
        });

        return btn;
    }
}