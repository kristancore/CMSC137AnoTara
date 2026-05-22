package com.animalfarm.view;

import com.animalfarm.model.GameConfig;
import com.animalfarm.net.GameClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.util.function.Consumer;

/**
 * Lobby UI — a single Scene whose root is swapped between three panels:
 *   1. Lobby select (Create / Join, 2P / 4P)
 *   2. Host waiting room (code, IPs, player count, chat, START)
 *   3. Join waiting room (status, player count, chat, LEAVE)
 *
 * App.java creates the GameServer/GameClient and calls the showXxx() methods.
 */
public class LobbyView {

    private final String pf; // font-family CSS string
    private final Runnable onBack;
    private final Consumer<Integer> onCreateLobby;   // playerCount
    private final Consumer<String> onJoinLobby;      // code only — IP discovered automatically
    private final Runnable onStartGame;              // host presses START
    private final Runnable onLeave;                  // joiner leaves

    private final StackPane root = new StackPane();
    private final StackPane contentPane = new StackPane();
    private final Scene scene;

    // Shared chat widgets (swapped into each panel)
    private final TextArea chatArea = new TextArea();
    private final TextField chatInput = new TextField();
    private GameClient currentClient; // for sendChat()

    // Nickname field — lives on the select screen, persists for the session
    private final TextField nicknameField = new TextField();

    // Labels updated from outside
    private Label playerCountLabel = new Label();
    private Label joinStatusLabel;
    private Button joinConnectBtn;
    private TextField joinCodeField;
    private Button startBtn;

    public LobbyView(String fontFam, Runnable onBack,
                     Consumer<Integer> onCreateLobby,
                     Consumer<String> onJoinLobby,
                     Runnable onStartGame,
                     Runnable onLeave) {
        this.pf             = fontFam;
        this.onBack         = onBack;
        this.onCreateLobby  = onCreateLobby;
        this.onJoinLobby    = onJoinLobby;
        this.onStartGame    = onStartGame;
        this.onLeave        = onLeave;

        // Chat area style (shared across panels)
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(160);
        chatArea.setStyle(
                "-fx-font-family: " + pf + "; -fx-font-size: 7px;" +
                "-fx-background-color: #0d2208; -fx-text-fill: #ccffcc;" +
                "-fx-control-inner-background: #0d2208;");

        chatInput.setPromptText("Type a message...");
        chatInput.setStyle(
                "-fx-font-family: " + pf + "; -fx-font-size: 8px;" +
                "-fx-background-color: #0d2208; -fx-text-fill: white;" +
                "-fx-prompt-text-fill: #4a7744;");

        nicknameField.setPromptText("Your nickname (optional)");
        nicknameField.setMaxWidth(340);
        nicknameField.setStyle(
                "-fx-font-family: " + pf + "; -fx-font-size: 8px;" +
                "-fx-background-color: #0d2208; -fx-text-fill: white;" +
                "-fx-prompt-text-fill: #4a7744;");

        root.setStyle("-fx-background-color: #0e1e08;");
        try {
            java.io.InputStream bgIs = getClass().getResourceAsStream("/ui/menu-background.png");
            if (bgIs != null) {
                javafx.scene.image.ImageView bgView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(bgIs));
                bgView.setFitWidth(GameConfig.WINDOW_WIDTH);
                bgView.setFitHeight(GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
                bgView.setPreserveRatio(false);
                root.getChildren().add(bgView);
            }

            java.io.InputStream raysIs = getClass().getResourceAsStream("/ui/menu-rays.png");
            if (raysIs != null) {
                javafx.scene.image.ImageView raysView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(raysIs));
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
        } catch (Exception e) {}
        root.getChildren().add(contentPane);
        scene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        showSelectScreen();
    }

    public Scene getLobbyScene() { return scene; }

    // -----------------------------------------------------------------------
    // Panel 1 — Lobby select
    // -----------------------------------------------------------------------

    public void showSelectScreen() {
        chatArea.clear();
        currentClient = null;
        contentPane.getChildren().setAll(buildSelectPanel());
    }

    private VBox buildSelectPanel() {
        VBox panel = centeredPanel(380);

        Label title = styled("PLAY ONLINE", 14);
        title.setStyle(title.getStyle() + " -fx-text-fill: #ffee44;");

        Label sub = styled("Choose game mode:", 7);

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton rb2 = styledRadio("2 Players  (1 vs 1)", modeGroup);
        RadioButton rb4 = styledRadio("4 Players  (2 vs 2, doubled cooldown)", modeGroup);
        rb2.setSelected(true);

        VBox modeBox = new VBox(8, rb2, rb4);
        modeBox.setAlignment(Pos.CENTER_LEFT);
        modeBox.setPadding(new Insets(0, 0, 0, 20));

        Button createBtn = menuBtn("CREATE LOBBY");
        Button joinBtn   = menuBtn("JOIN LOBBY");
        Button backBtn   = menuBtn("BACK");

        createBtn.setOnAction(e -> {
            int count = rb4.isSelected() ? 4 : 2;
            if (onCreateLobby != null) onCreateLobby.accept(count);
        });
        joinBtn.setOnAction(e -> showJoinInputPanel());
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        Label nickLabel = styled("Your nickname:", 7);
        nickLabel.setStyle(nickLabel.getStyle() + " -fx-text-fill: #aaffaa;");

        HBox btnRow = new HBox(20, createBtn, joinBtn, backBtn);
        btnRow.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(title, sub, modeBox, nickLabel, nicknameField, btnRow);
        return panel;
    }

    /** Returns the trimmed nickname, capped at 16 chars, defaulting to "Player". */
    public String getNickname() {
        String s = nicknameField.getText().trim();
        if (s.isEmpty()) return "Player";
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    // -----------------------------------------------------------------------
    // Panel 1b — Join: enter code only (IP auto-discovered via LAN broadcast)
    // -----------------------------------------------------------------------

    private void showJoinInputPanel() {
        joinCodeField = styledField("Lobby Code (" + GameConfig.LOBBY_CODE_LENGTH + " chars)");
        joinStatusLabel = styled("Enter the 6-character lobby code.", 7);
        joinStatusLabel.setStyle(joinStatusLabel.getStyle() + " -fx-text-fill: #aaffaa;");

        joinConnectBtn = menuBtn("CONNECT");
        Button cancelBtn = menuBtn("CANCEL");
        cancelBtn.setOnAction(e -> showSelectScreen());

        joinConnectBtn.setOnAction(e -> {
            String code = joinCodeField.getText().trim().toUpperCase();
            if (code.length() != GameConfig.LOBBY_CODE_LENGTH) {
                joinStatusLabel.setText("Code must be " + GameConfig.LOBBY_CODE_LENGTH + " characters.");
                return;
            }
            joinStatusLabel.setText("Searching for lobby on your network...");
            joinConnectBtn.setDisable(true);
            joinCodeField.setDisable(true);
            if (onJoinLobby != null) onJoinLobby.accept(code);
        });

        HBox btnRow = new HBox(16, joinConnectBtn, cancelBtn);
        btnRow.setAlignment(Pos.CENTER);

        VBox panel = centeredPanel(400);
        panel.getChildren().addAll(
                styled("JOIN LOBBY", 13),
                joinStatusLabel,
                joinCodeField,
                btnRow);

        contentPane.getChildren().setAll(panel);
    }

    /** Update the join panel status (e.g. on discovery failure). Re-enables input on error. */
    public void setJoinStatus(String msg) {
        if (joinStatusLabel == null) return;
        joinStatusLabel.setText(msg);
        String lower = msg.toLowerCase();
        if (lower.contains("not found") || lower.contains("error") || lower.contains("fail")) {
            if (joinConnectBtn != null) joinConnectBtn.setDisable(false);
            if (joinCodeField  != null) joinCodeField.setDisable(false);
        }
    }

    // -----------------------------------------------------------------------
    // Panel 2 — Host waiting room
    // -----------------------------------------------------------------------

    /**
     * Called by App after the server is started and the host's GameClient is connected.
     *
     * @param code       lobby code to display
     * @param ips        comma-separated LAN IPs to display
     * @param required   total players needed
     * @param hostClient used for sending chat messages
     */
    public void showHostLobby(String code, String ips, int required, GameClient hostClient) {
        this.currentClient = hostClient;
        chatArea.clear();
        playerCountLabel = new Label("Players: 1 / " + required);
        playerCountLabel.setStyle(baseStyle(8) + " -fx-text-fill: #aaffaa;");

        startBtn = menuBtn("START  ▶");
        startBtn.setDisable(true);
        startBtn.setStyle(startBtn.getStyle() + " -fx-opacity: 0.5;");
        startBtn.setOnAction(e -> { if (onStartGame != null) onStartGame.run(); });

        Button cancelBtn = menuBtn("CANCEL");
        cancelBtn.setOnAction(e -> {
            if (onLeave != null) onLeave.run();
        });

        // Code + IP display
        Label codeLbl = styled("Code: " + code, 11);
        codeLbl.setStyle(codeLbl.getStyle() + " -fx-text-fill: #ffee44;");

        Label ipLabel = styled(ips, 7);
        ipLabel.setWrapText(true);
        ipLabel.setTextAlignment(TextAlignment.CENTER);
        ipLabel.setStyle(ipLabel.getStyle() + " -fx-text-fill: #aaccff;");

        Label ipHint = styled("Share one of these IPs with players on your network", 6);
        ipHint.setStyle(ipHint.getStyle() + " -fx-text-fill: #668844;");
        ipHint.setWrapText(true);
        ipHint.setTextAlignment(TextAlignment.CENTER);

        HBox btnRow = new HBox(20, cancelBtn, startBtn);
        btnRow.setAlignment(Pos.CENTER);

        VBox panel = centeredPanel(500);
        panel.getChildren().addAll(
                styled("LOBBY CREATED", 13),
                codeLbl, ipLabel, ipHint,
                playerCountLabel,
                buildChatBox(),
                btnRow);

        contentPane.getChildren().setAll(panel);
    }

    // -----------------------------------------------------------------------
    // Panel 3 — Join waiting room
    // -----------------------------------------------------------------------

    /**
     * Called by App after the joiner's GameClient has connected (WAITING status).
     *
     * @param client the connected client, used for sending chat
     */
    public void showJoinWaiting(GameClient client) {
        this.currentClient = client;
        chatArea.clear();
        playerCountLabel = new Label("Players: —");
        playerCountLabel.setStyle(baseStyle(8) + " -fx-text-fill: #aaffaa;");

        Label statusLbl = styled("Connected! Waiting for host to start...", 8);
        statusLbl.setStyle(statusLbl.getStyle() + " -fx-text-fill: #ffee88;");

        Button leaveBtn = menuBtn("LEAVE");
        leaveBtn.setOnAction(e -> { if (onLeave != null) onLeave.run(); });

        VBox panel = centeredPanel(500);
        panel.getChildren().addAll(
                styled("IN LOBBY", 13),
                statusLbl,
                playerCountLabel,
                buildChatBox(),
                leaveBtn);

        contentPane.getChildren().setAll(panel);
    }

    // -----------------------------------------------------------------------
    // External update methods (called from App via Platform.runLater)
    // -----------------------------------------------------------------------

    /** Update the player count label, e.g. "2/4". */
    public void updatePlayerCount(String countStr) {
        if (playerCountLabel != null)
            playerCountLabel.setText("Players: " + countStr);
    }

    /** Enable the START button once all players are connected (host only). */
    public void enableStart() {
        if (startBtn != null) {
            startBtn.setDisable(false);
            startBtn.setStyle(startBtn.getStyle().replace("-fx-opacity: 0.5;", ""));
        }
    }

    /** Append a line to the chat area. */
    public void appendChat(String line) {
        chatArea.appendText(line + "\n");
    }

    // -----------------------------------------------------------------------
    // Chat widget
    // -----------------------------------------------------------------------

    private VBox buildChatBox() {
        Label chatTitle = styled("CHAT", 7);
        chatTitle.setStyle(chatTitle.getStyle() + " -fx-text-fill: #88cc88;");

        Button sendBtn = menuBtn("SEND");
        sendBtn.setStyle(sendBtn.getStyle() + " -fx-padding: 6 12 6 12;");
        sendBtn.setOnAction(e -> sendChat());
        chatInput.setOnAction(e -> sendChat());

        HBox inputRow = new HBox(8, chatInput, sendBtn);
        HBox.setHgrow(chatInput, Priority.ALWAYS);
        inputRow.setAlignment(Pos.CENTER);

        VBox box = new VBox(6, chatTitle, chatArea, inputRow);
        box.setFillWidth(true);
        return box;
    }

    private void sendChat() {
        String msg = chatInput.getText().trim();
        if (msg.isEmpty() || currentClient == null) return;
        currentClient.sendChat(msg);
        chatInput.clear();
    }

    // -----------------------------------------------------------------------
    // Style helpers
    // -----------------------------------------------------------------------

    private VBox centeredPanel(double maxW) {
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(maxW);
        panel.setPadding(new Insets(28, 32, 28, 32));
        panel.setFillWidth(true);
        panel.setStyle(
                "-fx-background-color: #1a3a0a;" +
                "-fx-border-color: #ffee88 #aa8800 #aa8800 #ffee88;" +
                "-fx-border-width: 3;");
        return panel;
    }

    private Label styled(String text, int sizePx) {
        Label l = new Label(text);
        l.setStyle(baseStyle(sizePx));
        return l;
    }

    private String baseStyle(int sizePx) {
        return "-fx-font-family: " + pf + "; -fx-font-size: " + sizePx + "px; -fx-text-fill: white;";
    }

    private Button menuBtn(String text) {
        Button b = new Button(text);
        String base = "-fx-font-family: " + pf + "; -fx-font-size: 8px; -fx-text-fill: white;" +
                      "-fx-background-color: #2a5a1a; -fx-cursor: hand; -fx-padding: 8 16 8 16;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(base.replace("#2a5a1a", "#3a8a2a")));
        b.setOnMouseExited(e  -> b.setStyle(base));
        return b;
    }

    private RadioButton styledRadio(String text, ToggleGroup g) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(g);
        rb.setStyle("-fx-font-family: " + pf + "; -fx-font-size: 7px; -fx-text-fill: white;");
        return rb;
    }

    private TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setMaxWidth(340);
        tf.setStyle(
                "-fx-font-family: " + pf + "; -fx-font-size: 8px;" +
                "-fx-background-color: #0d2208; -fx-text-fill: white;" +
                "-fx-prompt-text-fill: #4a7744;");
        return tf;
    }
}
