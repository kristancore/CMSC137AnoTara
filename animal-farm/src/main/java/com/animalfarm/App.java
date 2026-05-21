package com.animalfarm;

import com.animalfarm.controller.GameLoop;
import com.animalfarm.controller.InputHandler;
import com.animalfarm.model.GameConfig;
import com.animalfarm.model.GameState;
import com.animalfarm.net.GameClient;
import com.animalfarm.net.GameServer;
import com.animalfarm.net.LobbyDiscovery;
import com.animalfarm.view.AudioManager;
import com.animalfarm.view.GameRenderer;
import com.animalfarm.view.HUDRenderer;
import com.animalfarm.view.LobbyView;
import com.animalfarm.view.MenuView;
import com.animalfarm.view.SpriteManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class App extends Application {

    private Stage primaryStage;
    private Scene gameScene;
    private GameLoop gameLoop;
    private MenuView menuView;
    private LobbyView lobbyView;
    private GameServer activeServer;
    private GameClient activeClient;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        SpriteManager.loadSprites();

        menuView = new MenuView(this::startGame, this::openLobby, this::showScene);

        stage.setTitle("Sa Kabukiran");
        stage.setScene(menuView.getMainMenuScene());
        stage.show();

        AudioManager.getInstance().playLanding();
    }

    private void showScene(Scene scene) {
        primaryStage.setScene(scene);
    }

    private void showMainMenu() {
        primaryStage.setScene(menuView.getMainMenuScene());
    }

    private StackPane buildGameOverCard(String pf, int s1, int s2, boolean isSD, Runnable onReplay, Runnable onMenu) {
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.72);");

        VBox card = new VBox(18);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(460);
        card.setMaxHeight(400);
        card.setPadding(new Insets(32, 40, 32, 40));
        card.setStyle(
                "-fx-background-color: #1a3a0a;" +
                        "-fx-border-color: #ffee88 #aa8800 #aa8800 #ffee88;" +
                        "-fx-border-width: 4;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.9), 20, 0, 0, 6);");

        try {
            java.io.InputStream is = getClass().getResourceAsStream("/ui/grassland.png");
            if (is != null) {
                Image grass = new Image(is);
                ImageView gv = new ImageView(grass);
                gv.setFitWidth(460);
                gv.setFitHeight(28);
                gv.setPreserveRatio(false);
                card.getChildren().add(gv);
            }
        } catch (Exception ignored) {
        }

        Label timesUp = new Label(isSD ? "FINISH!" : "TIME'S UP!");
        timesUp.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 20px; -fx-text-fill: #ffee44;");

        String winnerStr;
        if (s1 > s2)
            winnerStr = "PLAYER 1";
        else if (s2 > s1)
            winnerStr = "PLAYER 2";
        else
            winnerStr = "TIE GAME";

        Label winnerLabel = new Label(winnerStr);
        winnerLabel.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 16px; -fx-text-fill: #88ff88;");

        Label winsLabel = new Label(isSD ? "WINS SUDDEN DEATH!" : "WINS THE ROUND!");
        winsLabel.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 10px; -fx-text-fill: #ccffcc;");

        Label scoreLine = new Label(String.format("P1: %d  |  P2: %d", s1, s2));
        scoreLine.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 12px; -fx-text-fill: white;");

        Button actionBtn = buildCardButton(pf, "PLAY AGAIN", "#1a7a1a", "#aaffaa");
        actionBtn.setOnAction(e -> {
            AudioManager.getInstance().playClick();
            onReplay.run();
        });

        Button menuBtn = buildCardButton(pf, "MAIN MENU", "#7a1a1a", "#ffaaaa");
        menuBtn.setOnAction(e -> {
            AudioManager.getInstance().playClick();
            onMenu.run();
        });

        HBox btnRow = new HBox(20, actionBtn, menuBtn);
        btnRow.setAlignment(Pos.CENTER);

        card.getChildren().addAll(timesUp, winnerLabel, winsLabel, scoreLine, btnRow);
        backdrop.getChildren().add(card);
        return backdrop;
    }

    private void showSuddenDeathAnnouncement(StackPane root, String pf, GameState state) {
        StackPane backdrop = new StackPane();
        backdrop.setStyle("-fx-background-color: rgba(0,0,0,0.78);");

        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);

        Label title = new Label("SUDDEN DEATH!");
        title.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 48px; -fx-text-fill: #ff4444;");
        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
        glow.setColor(javafx.scene.paint.Color.web("#ff0000"));
        glow.setRadius(20);
        glow.setSpread(0.4);
        title.setEffect(glow);

        Label sub = new Label("First to score wins!");
        sub.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 14px; -fx-text-fill: #ffee88;");

        box.getChildren().addAll(title, sub);

        javafx.animation.ScaleTransition pop = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(350), box);
        pop.setFromX(0.4); pop.setFromY(0.4);
        pop.setToX(1.0);   pop.setToY(1.0);
        pop.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        pop.play();

        backdrop.getChildren().add(box);
        root.getChildren().add(backdrop);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2.5));
        pause.setOnFinished(e -> {
            root.getChildren().remove(backdrop);
            state.startSuddenDeath();
            gameLoop.start();
        });
        pause.play();
    }

    private Button buildCardButton(String pf, String text, String bgColor, String textColor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 10px; -fx-background-color: " + bgColor
                + "; -fx-text-fill: " + textColor + "; -fx-cursor: hand; -fx-padding: 10 20 10 20;");
        return btn;
    }

    private void openLobby() {
        lobbyView = new LobbyView(
                menuView.getFontFam(),
                this::showMainMenu,
                this::createLobby,
                this::joinLobby,
                this::startGameOnServer,
                this::cancelLobby);
        primaryStage.setScene(lobbyView.getLobbyScene());
    }

    /**
     * Creates the game server and immediately connects the host as player 1.
     * Called when host selects player count and clicks CREATE LOBBY.
     */
    private void createLobby(int playerCount) {
        try {
            GameServer server = new GameServer(playerCount,
                    () -> Platform.runLater(this::showMainMenu));
            server.startAccepting();
            activeServer = server;

            // Host connects to their own server as player 1
            GameClient hostClient = new GameClient(
                    "127.0.0.1",
                    server.getLobbyCode(),
                    lobbyView.getNickname(),
                    status -> Platform.runLater(() -> handleClientStatus(status)),
                    count  -> Platform.runLater(() -> { if (lobbyView != null) lobbyView.updatePlayerCount(count); }),
                    chat   -> Platform.runLater(() -> { if (lobbyView != null) lobbyView.appendChat(chat); }));
            activeClient = hostClient;
            hostClient.connect();

            lobbyView.showHostLobby(server.getLobbyCode(), server.getHostIps(),
                    playerCount, hostClient);

        } catch (java.io.IOException e) {
            // TODO: surface error in lobby UI
            e.printStackTrace();
        }
    }

    /**
     * Discovers the host IP via UDP LAN broadcast, then connects a GameClient.
     * Called when joiner enters only the 6-char code and clicks CONNECT.
     */
    private void joinLobby(String code) {
        String nick = lobbyView != null ? lobbyView.getNickname() : "Player";
        LobbyDiscovery.discover(code, 5000,
            ip -> Platform.runLater(() -> {
                GameClient client = new GameClient(
                        ip, code, nick,
                        status -> Platform.runLater(() -> {
                            handleClientStatus(status);
                            if (status == GameClient.Status.WAITING && lobbyView != null)
                                lobbyView.showJoinWaiting(activeClient);
                        }),
                        count -> Platform.runLater(() -> { if (lobbyView != null) lobbyView.updatePlayerCount(count); }),
                        chat  -> Platform.runLater(() -> { if (lobbyView != null) lobbyView.appendChat(chat); }));
                activeClient = client;
                client.connect();
            }),
            () -> Platform.runLater(() -> {
                if (lobbyView != null)
                    lobbyView.setJoinStatus("Lobby not found on your network. Check the code and try again.");
            }));
    }

    /** Host presses START — tells the server to begin the game. */
    private void startGameOnServer() {
        if (activeServer != null) activeServer.startGame();
    }

    /** Cancel lobby (host or joiner) — clean up and return to main menu. */
    private void cancelLobby() {
        if (activeClient != null) { activeClient.disconnect(); activeClient = null; }
        if (activeServer != null) { activeServer.shutdown(); activeServer = null; }
        showMainMenu();
    }

    /** Handles GameClient status changes from both host and joining clients. */
    private void handleClientStatus(GameClient.Status status) {
        switch (status) {
            case CAN_START -> { if (lobbyView != null) lobbyView.enableStart(); }
            case IN_GAME   -> startOnlineGame(activeClient);
            case ERROR     -> stopOnlineGameAndReturnToMenu();
            default        -> {} // CONNECTING, WAITING handled by LobbyView callbacks
        }
    }

    private void startOnlineGame(GameClient client) {
        GameState state = client.getState();
        if (state == null) return;
        setupOnlineGame(state, client);
    }

    private void setupOnlineGame(GameState state, GameClient client) {
        int myPid = client.getMyPlayerId();
        // 2P: P1=team1, P2=team2.  4P: P1+P2=team1, P3+P4=team2.
        boolean is4P = state.getMode() == GameState.GameMode.ONLINE_4P;
        int myTeamId = is4P ? (myPid <= 2 ? 1 : 2) : (myPid == 1 ? 1 : 2);
        // Right-panel player: in 4P show an ENEMY player so both team scores are visible;
        // in 2P show the only enemy.
        int otherPid = is4P
                ? (myPid <= 2 ? myPid + 2 : myPid - 2)   // 1→3, 2→4, 3→1, 4→2
                : (myPid == 1 ? 2 : 1);

        Canvas canvas = new Canvas(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        String pf = menuView.getFontFam();
        String base = "-fx-font-family: " + pf + "; -fx-font-size: 8px; -fx-text-fill: white;";

        // Build HUD — same layout as local mode; server keeps P1/P2 data up to date
        Label p1Score = new Label("Score: 0");   p1Score.setStyle(base);
        Label p1CD    = new Label("READY");      p1CD.setStyle(base + " -fx-text-fill: #88ff88;");
        ImageView p1NextV = new ImageView();     p1NextV.setFitWidth(40); p1NextV.setFitHeight(40); p1NextV.setPreserveRatio(true);
        Label p1NextN = new Label("-");          p1NextN.setStyle(base);
        Label p1Hint  = new Label("SPACE");      p1Hint.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 6px; -fx-text-fill: #ffee88;");
        VBox p1NextInfo = new VBox(2, p1NextN, p1Hint);
        p1NextInfo.setAlignment(Pos.CENTER_LEFT);
        HBox p1HUD = new HBox(10, p1NextV, p1NextInfo, new VBox(4, p1Score, p1CD));
        p1HUD.setAlignment(Pos.CENTER_LEFT);
        p1HUD.setPadding(new Insets(10));

        Label p2Score = new Label("Score: 0");   p2Score.setStyle(base);
        Label p2CD    = new Label("READY");      p2CD.setStyle(base + " -fx-text-fill: #88ff88;");
        ImageView p2NextV = new ImageView();     p2NextV.setFitWidth(40); p2NextV.setFitHeight(40); p2NextV.setPreserveRatio(true);
        Label p2NextN = new Label("-");          p2NextN.setStyle(base);
        Label p2Hint  = new Label("SPACE");      p2Hint.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 6px; -fx-text-fill: #ffee88;");
        VBox p2NextInfo = new VBox(2, p2NextN, p2Hint);
        p2NextInfo.setAlignment(Pos.CENTER_RIGHT);
        HBox p2HUD = new HBox(10, new VBox(4, p2Score, p2CD), p2NextInfo, p2NextV);
        p2HUD.setAlignment(Pos.CENTER_RIGHT);
        p2HUD.setPadding(new Insets(10));

        Label timerLabel = new Label("1:40");
        timerLabel.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 14px; -fx-text-fill: white;");
        Button backBtn = new Button("MENU");
        backBtn.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 9px; -fx-background-color: #cc3333; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(e -> stopOnlineGameAndReturnToMenu());
        VBox centerPanel = new VBox(4, timerLabel, backBtn);
        centerPanel.setAlignment(Pos.CENTER);

        HBox hudContent = new HBox();
        hudContent.setAlignment(Pos.CENTER);
        Region sp1 = new Region(); HBox.setHgrow(sp1, Priority.ALWAYS);
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
        hudContent.getChildren().addAll(p1HUD, sp1, centerPanel, sp2, p2HUD);

        StackPane hudStack = new StackPane();
        hudStack.setPrefHeight(GameConfig.HUD_HEIGHT);
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/ui/grassland.png");
            if (is != null) {
                ImageView bg = new ImageView(new Image(is));
                bg.setFitWidth(GameConfig.WINDOW_WIDTH); bg.setFitHeight(GameConfig.HUD_HEIGHT); bg.setPreserveRatio(false);
                hudStack.getChildren().add(bg);
            }
        } catch (Exception ignored) {}
        hudStack.getChildren().add(hudContent);

        VBox gameColumn = new VBox(0, canvas, hudStack);
        StackPane root = new StackPane(gameColumn);

        GameRenderer renderer = new GameRenderer(gc, myTeamId);
        HUDRenderer hudRenderer = new HUDRenderer(
                p1CD, p1Score, p1NextV, p1NextN, p1Hint,
                p2CD, p2Score, p2NextV, p2NextN, p2Hint,
                timerLabel);
        final int finalMyPid = myPid;
        final int finalOtherPid = otherPid;
        InputHandler inputHandler = new InputHandler();

        gameScene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        gameScene.setOnKeyPressed(e -> inputHandler.handleNetworkInput(e.getCode(), client, client.getState()));
        gameScene.setOnMousePressed(e -> AudioManager.getInstance().playClick());
        root.setOnMouseClicked(e -> root.requestFocus());
        root.requestFocus();

        gameLoop = new GameLoop(client, renderer, hudRenderer, finalMyPid, finalOtherPid, () -> Platform.runLater(() -> {
            if (!state.isSuddenDeath() && state.getScoreP1() == state.getScoreP2()) {
                showSuddenDeathAnnouncement(root, pf, state);
                return;
            }
            AudioManager.getInstance().playLanding();
            StackPane overlay = buildGameOverCard(pf,
                    state.getScoreP1(), state.getScoreP2(), state.isSuddenDeath(),
                    () -> { root.getChildren().remove(root.getChildren().size() - 1); stopOnlineGameAndReturnToMenu(); },
                    this::stopOnlineGameAndReturnToMenu);
            root.getChildren().add(overlay);
        }));

        primaryStage.setScene(gameScene);
        AudioManager.getInstance().playBattle();
        gameLoop.start();
    }

    private void stopOnlineGameAndReturnToMenu() {
        if (gameLoop != null) gameLoop.stop();
        if (activeClient != null) { activeClient.disconnect(); activeClient = null; }
        if (activeServer != null) { activeServer.shutdown(); activeServer = null; }
        AudioManager.getInstance().playLanding();
        showMainMenu();
    }

    private void startGame() {
        GameState state = new GameState();
        setupGame(state);
    }

    private void setupGame(GameState state) {
        Canvas canvas = new Canvas(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        String pf = menuView.getFontFam();
        String base = "-fx-font-family: " + pf + "; -fx-font-size: 8px; -fx-text-fill: white;";

        // P1 Panel
        Label p1Score = new Label("Score: 0");
        p1Score.setStyle(base);
        Label p1CD = new Label("READY");
        p1CD.setStyle(base + " -fx-text-fill: #88ff88;");
        ImageView p1NextV = new ImageView();
        p1NextV.setFitWidth(40);
        p1NextV.setFitHeight(40);
        p1NextV.setPreserveRatio(true);
        Label p1NextN = new Label("-");
        p1NextN.setStyle(base);
        Label p1Hint = new Label("SPACE");
        p1Hint.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 6px; -fx-text-fill: #ffee88;");
        VBox p1NextInfo = new VBox(2, p1NextN, p1Hint);
        p1NextInfo.setAlignment(Pos.CENTER_LEFT);
        HBox p1HUD = new HBox(10, p1NextV, p1NextInfo, new VBox(4, p1Score, p1CD));
        p1HUD.setAlignment(Pos.CENTER_LEFT);
        p1HUD.setPadding(new Insets(10));

        // P2 Panel
        Label p2Score = new Label("Score: 0");
        p2Score.setStyle(base);
        Label p2CD = new Label("READY");
        p2CD.setStyle(base + " -fx-text-fill: #88ff88;");
        ImageView p2NextV = new ImageView();
        p2NextV.setFitWidth(40);
        p2NextV.setFitHeight(40);
        p2NextV.setPreserveRatio(true);
        Label p2NextN = new Label("-");
        p2NextN.setStyle(base);
        Label p2Hint = new Label("A KEY");
        p2Hint.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 6px; -fx-text-fill: #ffee88;");
        VBox p2NextInfo = new VBox(2, p2NextN, p2Hint);
        p2NextInfo.setAlignment(Pos.CENTER_RIGHT);
        HBox p2HUD = new HBox(10, new VBox(4, p2Score, p2CD), p2NextInfo, p2NextV);
        p2HUD.setAlignment(Pos.CENTER_RIGHT);
        p2HUD.setPadding(new Insets(10));

        // Center Panel
        Label timerLabel = new Label("1:40");
        timerLabel.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 14px; -fx-text-fill: white;");
        Button backBtn = new Button("MENU");
        backBtn.setStyle("-fx-font-family: " + pf
                + "; -fx-font-size: 9px; -fx-background-color: #cc3333; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(e -> stopGameAndReturnToMenu());
        VBox centerPanel = new VBox(4, timerLabel, backBtn);
        centerPanel.setAlignment(Pos.CENTER);

        HBox hudContent = new HBox();
        hudContent.setAlignment(Pos.CENTER);
        Region sp1 = new Region();
        HBox.setHgrow(sp1, Priority.ALWAYS);
        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);
        hudContent.getChildren().addAll(p1HUD, sp1, centerPanel, sp2, p2HUD);

        StackPane hudStack = new StackPane();
        hudStack.setPrefHeight(GameConfig.HUD_HEIGHT);
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/ui/grassland.png");
            if (is != null) {
                ImageView bg = new ImageView(new Image(is));
                bg.setFitWidth(GameConfig.WINDOW_WIDTH);
                bg.setFitHeight(GameConfig.HUD_HEIGHT);
                bg.setPreserveRatio(false);
                hudStack.getChildren().add(bg);
            }
        } catch (Exception e) {
        }
        hudStack.getChildren().add(hudContent);

        VBox gameColumn = new VBox(0, canvas, hudStack);
        StackPane root = new StackPane(gameColumn);

        GameRenderer renderer = new GameRenderer(gc);
        HUDRenderer hudRenderer = new HUDRenderer(p1CD, p1Score, p1NextV, p1NextN, p1Hint, p2CD, p2Score, p2NextV,
                p2NextN, p2Hint, timerLabel);
        InputHandler inputHandler = new InputHandler();

        gameScene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        gameScene.setOnKeyPressed(e -> inputHandler.handleInput(e.getCode(), state));
        gameScene.setOnMousePressed(e -> AudioManager.getInstance().playClick());
        root.setOnMouseClicked(e -> root.requestFocus());
        root.requestFocus();

        gameLoop = new GameLoop(state, renderer, hudRenderer, () -> Platform.runLater(() -> {
            if (!state.isSuddenDeath() && state.getScoreP1() == state.getScoreP2()) {
                showSuddenDeathAnnouncement(root, pf, state);
                return;
            }
            AudioManager.getInstance().playLanding();
            StackPane overlay = buildGameOverCard(pf, state.getScoreP1(), state.getScoreP2(), state.isSuddenDeath(),
                    () -> { // Replay
                        root.getChildren().remove(root.getChildren().size() - 1);
                        stopGameAndRestart();
                    },
                    () -> stopGameAndReturnToMenu());
            root.getChildren().add(overlay);
        }));

        primaryStage.setScene(gameScene);
        AudioManager.getInstance().playBattle();
        gameLoop.start();
    }

    private void stopGameAndRestart() {
        if (gameLoop != null)
            gameLoop.stop();
        startGame();
    }

    private void stopGameAndReturnToMenu() {
        if (gameLoop != null)
            gameLoop.stop();
        AudioManager.getInstance().playLanding();
        showMainMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
