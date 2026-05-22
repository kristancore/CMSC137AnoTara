package com.animalfarm.view;

import com.animalfarm.model.GameConfig;
import com.animalfarm.net.GameClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LobbyView {

    private final String pf;
    private final Runnable onBack;
    private final Consumer<Integer> onCreateLobby;
    private final Consumer<String> onJoinLobby;
    private final Runnable onStartGame;
    private final Runnable onLeave;
    private Consumer<Integer> onTeamSelect;

    private final StackPane root = new StackPane();
    private final StackPane contentPane = new StackPane();
    private final Scene scene;

    private final TextArea chatArea = new TextArea();
    private final TextField chatInput = new TextField();
    private GameClient currentClient;

    private final TextField nicknameField = new TextField();

    // Roster panel — shared and updated in-place
    private final VBox rosterSection = new VBox(6);

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
        this.pf            = fontFam;
        this.onBack        = onBack;
        this.onCreateLobby = onCreateLobby;
        this.onJoinLobby   = onJoinLobby;
        this.onStartGame   = onStartGame;
        this.onLeave       = onLeave;

        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(130);
        chatArea.setStyle(
                "-fx-font-family: " + pf + "; -fx-font-size: 7px;" +
                "-fx-background-color: #0a1a06; -fx-text-fill: #ccffcc;" +
                "-fx-control-inner-background: #0a1a06;");

        chatInput.setPromptText("Type a message...");
        chatInput.setStyle(
                "-fx-font-family: " + pf + "; -fx-font-size: 8px;" +
                "-fx-background-color: #0a1a06; -fx-text-fill: white;" +
                "-fx-prompt-text-fill: #669966;");

        nicknameField.setPromptText("Enter your nickname");
        nicknameField.setMaxWidth(340);
        nicknameField.setStyle(
                "-fx-font-family: " + pf + "; -fx-font-size: 8px;" +
                "-fx-background-color: #0a1a06; -fx-text-fill: white;" +
                "-fx-prompt-text-fill: #669966;");

        // Roster section: initially hidden, shown for 4P lobbies
        rosterSection.setVisible(false);
        rosterSection.setManaged(false);
        rosterSection.setStyle("-fx-background-color: #0a1a06; -fx-padding: 10;");

        // Background
        root.setStyle("-fx-background-color: #0e1e08;");
        try {
            java.io.InputStream bgIs = getClass().getResourceAsStream("/ui/menu-background.png");
            if (bgIs != null) {
                javafx.scene.image.ImageView bgView = new javafx.scene.image.ImageView(
                        new javafx.scene.image.Image(bgIs));
                bgView.setFitWidth(GameConfig.WINDOW_WIDTH);
                bgView.setFitHeight(GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
                bgView.setPreserveRatio(false);
                root.getChildren().add(bgView);
            }
            java.io.InputStream raysIs = getClass().getResourceAsStream("/ui/menu-rays.png");
            if (raysIs != null) {
                javafx.scene.image.ImageView raysView = new javafx.scene.image.ImageView(
                        new javafx.scene.image.Image(raysIs));
                raysView.setPreserveRatio(true);
                double size = Math.max(GameConfig.WINDOW_WIDTH,
                        GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT) * 1.5;
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
        } catch (Exception ignored) {}
        root.getChildren().add(contentPane);

        scene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT + GameConfig.HUD_HEIGHT);
        showSelectScreen();
    }

    public Scene getLobbyScene() { return scene; }

    public void setOnTeamSelect(Consumer<Integer> handler) { this.onTeamSelect = handler; }

    // -----------------------------------------------------------------------
    // Panel 1 — Lobby select
    // -----------------------------------------------------------------------

    public void showSelectScreen() {
        chatArea.clear();
        currentClient = null;
        contentPane.getChildren().setAll(buildSelectPanel());
    }

    private VBox buildSelectPanel() {
        VBox panel = centeredPanel(420);

        Label title = styled("PLAY ONLINE", 14);
        title.setStyle(title.getStyle() + " -fx-text-fill: #ffee44;");

        Label sub = styled("Choose game mode:", 7);

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton rb2 = styledRadio("2 Players  (1 vs 1)", modeGroup);
        RadioButton rb4 = styledRadio("4 Players  (2 vs 2)", modeGroup);
        rb2.setSelected(true);

        VBox modeBox = new VBox(8, rb2, rb4);
        modeBox.setAlignment(Pos.CENTER_LEFT);
        modeBox.setPadding(new Insets(0, 0, 0, 20));

        Label nickLabel = styled("Nickname (required):", 7);
        nickLabel.setStyle(nickLabel.getStyle() + " -fx-text-fill: #ffee88;");

        Label nickErrorLabel = styled("Nickname cannot be empty!", 7);
        nickErrorLabel.setStyle(nickErrorLabel.getStyle() + " -fx-text-fill: #ff5555;");
        nickErrorLabel.setVisible(false);
        nickErrorLabel.setManaged(false);

        Button createBtn = menuBtn("CREATE LOBBY");
        Button joinBtn   = menuBtn("JOIN LOBBY");
        Button backBtn   = menuBtn("BACK");

        createBtn.setOnAction(e -> {
            if (nicknameField.getText().trim().isEmpty()) {
                nickErrorLabel.setVisible(true);
                nickErrorLabel.setManaged(true);
                return;
            }
            nickErrorLabel.setVisible(false);
            nickErrorLabel.setManaged(false);
            int count = rb4.isSelected() ? 4 : 2;
            if (onCreateLobby != null) onCreateLobby.accept(count);
        });
        joinBtn.setOnAction(e -> {
            if (nicknameField.getText().trim().isEmpty()) {
                nickErrorLabel.setVisible(true);
                nickErrorLabel.setManaged(true);
                return;
            }
            nickErrorLabel.setVisible(false);
            nickErrorLabel.setManaged(false);
            showJoinInputPanel();
        });
        backBtn.setOnAction(e -> { if (onBack != null) onBack.run(); });

        HBox btnRow = new HBox(20, createBtn, joinBtn, backBtn);
        btnRow.setAlignment(Pos.CENTER);

        panel.getChildren().addAll(title, sub, modeBox, nickLabel, nicknameField, nickErrorLabel, btnRow);
        return panel;
    }

    public String getNickname() {
        String s = nicknameField.getText().trim();
        if (s.isEmpty()) return "Player";
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    // -----------------------------------------------------------------------
    // Panel 1b — Join: enter code
    // -----------------------------------------------------------------------

    private void showJoinInputPanel() {
        joinCodeField = styledField("Lobby Code (" + GameConfig.LOBBY_CODE_LENGTH + " chars)");
        joinStatusLabel = styled("Enter the lobby code.", 7);
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
        panel.getChildren().addAll(styled("JOIN LOBBY", 13), joinStatusLabel, joinCodeField, btnRow);
        contentPane.getChildren().setAll(panel);
    }

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
        cancelBtn.setOnAction(e -> { if (onLeave != null) onLeave.run(); });

        Label codeLbl = styled("Code: " + code, 11);
        codeLbl.setStyle(codeLbl.getStyle() + " -fx-text-fill: #ffee44;");

        Label ipLabel = styled(ips, 7);
        ipLabel.setWrapText(true);
        ipLabel.setTextAlignment(TextAlignment.CENTER);
        ipLabel.setStyle(ipLabel.getStyle() + " -fx-text-fill: #aaccff;");

        Label ipHint = styled("Share your IP with players on the same network", 6);
        ipHint.setStyle(ipHint.getStyle() + " -fx-text-fill: #668844;");
        ipHint.setWrapText(true);
        ipHint.setTextAlignment(TextAlignment.CENTER);

        HBox btnRow = new HBox(20, cancelBtn, startBtn);
        btnRow.setAlignment(Pos.CENTER);

        VBox panel = centeredPanel(520);
        panel.getChildren().addAll(
                styled("LOBBY CREATED", 13),
                codeLbl, ipLabel, ipHint,
                playerCountLabel,
                rosterSection,
                buildChatBox(),
                btnRow);

        contentPane.getChildren().setAll(panel);
    }

    // -----------------------------------------------------------------------
    // Panel 3 — Join waiting room
    // -----------------------------------------------------------------------

    public void showJoinWaiting(GameClient client) {
        this.currentClient = client;
        chatArea.clear();
        playerCountLabel = new Label("Players: —");
        playerCountLabel.setStyle(baseStyle(8) + " -fx-text-fill: #aaffaa;");

        Label statusLbl = styled("Connected! Waiting for host to start...", 8);
        statusLbl.setStyle(statusLbl.getStyle() + " -fx-text-fill: #ffee88;");

        Button leaveBtn = menuBtn("LEAVE");
        leaveBtn.setOnAction(e -> { if (onLeave != null) onLeave.run(); });

        VBox panel = centeredPanel(520);
        panel.getChildren().addAll(
                styled("IN LOBBY", 13),
                statusLbl,
                playerCountLabel,
                rosterSection,
                buildChatBox(),
                leaveBtn);

        contentPane.getChildren().setAll(panel);
    }

    // -----------------------------------------------------------------------
    // External update methods
    // -----------------------------------------------------------------------

    public void updatePlayerCount(String countStr) {
        if (playerCountLabel != null)
            playerCountLabel.setText("Players: " + countStr);
    }

    public void enableStart() {
        if (startBtn != null) {
            startBtn.setDisable(false);
            startBtn.setStyle(startBtn.getStyle().replace("-fx-opacity: 0.5;", ""));
        }
    }

    public void disableStart() {
        if (startBtn != null) {
            startBtn.setDisable(true);
            if (!startBtn.getStyle().contains("-fx-opacity: 0.5;"))
                startBtn.setStyle(startBtn.getStyle() + " -fx-opacity: 0.5;");
        }
    }

    public void appendChat(String line) {
        chatArea.appendText(line + "\n");
    }

    /**
     * Rebuilds the team roster panel.
     * Only visible for 4P lobbies (required == 4).
     *
     * @param mySlot   local player's connection slot (1-4)
     * @param names    slot-indexed nicknames (index 1-4)
     * @param teams    slot-indexed team choices: 0=unset, 1=Team1, 2=Team2
     * @param required total players needed (roster shown only when == 4)
     */
    public void updateRoster(int mySlot, String[] names, int[] teams, int required) {
        if (required != 4) {
            rosterSection.setVisible(false);
            rosterSection.setManaged(false);
            return;
        }
        rosterSection.setVisible(true);
        rosterSection.setManaged(true);
        rosterSection.getChildren().setAll(buildRosterRows(mySlot, names, teams));
    }

    private List<javafx.scene.Node> buildRosterRows(int mySlot, String[] names, int[] teams) {
        List<javafx.scene.Node> rows = new ArrayList<>();

        Label header = styled("TEAM SELECTION", 8);
        header.setStyle(header.getStyle() + " -fx-text-fill: #ffee44;");
        rows.add(header);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #334a22;");
        rows.add(sep);

        for (int slot = 1; slot <= 4; slot++) {
            String name  = (names != null && names.length > slot && names[slot] != null)
                           ? names[slot] : "(waiting...)";
            int team     = (teams != null && teams.length > slot) ? teams[slot] : 0;
            boolean isMe = (slot == mySlot);

            // Name label — green if Team 1, red if Team 2, grey if unset
            String nameColor = team == 1 ? "#88ff88" : team == 2 ? "#ff8888" : "#aaaaaa";
            Label nameLbl = styled("#" + slot + "  " + name, 8);
            nameLbl.setMinWidth(200);
            nameLbl.setStyle(nameLbl.getStyle() + " -fx-text-fill: " + nameColor + ";");

            javafx.scene.Node teamWidget;
            if (isMe) {
                Button t1Btn = menuBtn("TEAM 1");
                Button t2Btn = menuBtn("TEAM 2");
                // Highlight selected team button
                if (team == 1) t1Btn.setStyle(t1Btn.getStyle() + " -fx-text-fill: #88ff88;");
                if (team == 2) t2Btn.setStyle(t2Btn.getStyle() + " -fx-text-fill: #ff8888;");
                t1Btn.setOnAction(e -> { if (onTeamSelect != null) onTeamSelect.accept(1); });
                t2Btn.setOnAction(e -> { if (onTeamSelect != null) onTeamSelect.accept(2); });
                HBox btns = new HBox(8, t1Btn, t2Btn);
                btns.setAlignment(Pos.CENTER_LEFT);
                teamWidget = btns;
            } else {
                String teamStr  = team == 1 ? "TEAM 1" : team == 2 ? "TEAM 2" : "—";
                String teamColor = team == 1 ? "#88ff88" : team == 2 ? "#ff8888" : "#666666";
                Label tl = styled(teamStr, 8);
                tl.setStyle(tl.getStyle() + " -fx-text-fill: " + teamColor + ";");
                teamWidget = tl;
            }

            HBox row = new HBox(16, nameLbl, teamWidget);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(2, 0, 2, 0));
            rows.add(row);
        }

        return rows;
    }

    // -----------------------------------------------------------------------
    // Chat
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
        VBox panel = new VBox(14);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxWidth(maxW);
        panel.setPadding(new Insets(28, 36, 28, 36));
        panel.setFillWidth(true);
        panel.setStyle(
                "-fx-background-color: rgba(8, 20, 4, 0.88);" +
                "-fx-border-color: #ffee44;" +
                "-fx-border-width: 0 0 0 3;");
        return panel;
    }

    private Label styled(String text, int sizePx) {
        Label l = new Label(text);
        l.setStyle(baseStyle(sizePx));
        if (sizePx >= 10) {
            javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
            ds.setColor(javafx.scene.paint.Color.web("#1c5a1a"));
            ds.setOffsetX(3); ds.setOffsetY(3); ds.setRadius(0);
            l.setEffect(ds);
        }
        return l;
    }

    private String baseStyle(int sizePx) {
        return "-fx-font-family: " + pf + "; -fx-font-size: " + sizePx + "px; -fx-text-fill: white;";
    }

    private Button menuBtn(String text) {
        Button b = new Button(text);
        String base = "-fx-font-family: " + pf + "; -fx-font-size: 10px; -fx-text-fill: white;" +
                      "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 8 16 8 16;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> b.setStyle(base.replace("-fx-text-fill: white;", "-fx-text-fill: #ffee44;")));
        b.setOnMouseExited(e  -> b.setStyle(base));
        b.setOnMousePressed(e -> AudioManager.getInstance().playClick());
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
                "-fx-background-color: #0a1a06; -fx-text-fill: white;" +
                "-fx-prompt-text-fill: #669966;");
        return tf;
    }
}
