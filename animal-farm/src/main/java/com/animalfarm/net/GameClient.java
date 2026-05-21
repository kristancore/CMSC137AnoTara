package com.animalfarm.net;

import com.animalfarm.model.*;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.function.Consumer;

/**
 * TCP client for online play.
 *
 * Connects to the host's GameServer, verifies the lobby code,
 * receives lobby updates and STATE snapshots, and sends input commands.
 * All network I/O runs on a background virtual thread.
 */
public class GameClient {

    public enum Status { CONNECTING, WAITING, CAN_START, IN_GAME, GAME_OVER, ERROR }

    private final String host;
    private final String expectedCode;
    private final String nickname;
    private final Consumer<Status> onStatus;
    /** Fires with "N/M" strings on player count changes. */
    private final Consumer<String> onLobbyUpdate;
    /** Fires with formatted chat lines on messages from any player. */
    private final Consumer<String> onChatMessage;

    private volatile int myPlayerId = -1;
    private volatile GameState state;
    private volatile Status status = Status.CONNECTING;
    private final Map<Integer, String> knownNicks = new HashMap<>();

    private PrintWriter out;
    private Thread ioThread;
    private volatile boolean running = false;

    public GameClient(String host, String lobbyCode, String nickname,
                      Consumer<Status> onStatus,
                      Consumer<String> onLobbyUpdate,
                      Consumer<String> onChatMessage) {
        this.host           = host;
        this.expectedCode   = lobbyCode.trim().toUpperCase();
        this.nickname       = nickname;
        this.onStatus       = onStatus;
        this.onLobbyUpdate  = onLobbyUpdate;
        this.onChatMessage  = onChatMessage;
    }

    // -----------------------------------------------------------------------
    // Connect
    // -----------------------------------------------------------------------

    public void connect() {
        running = true;
        ioThread = Thread.ofVirtual().start(this::ioLoop);
    }

    private void ioLoop() {
        try (Socket sock = new Socket(host, GameConfig.SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()))) {

            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);

            String line;
            while (running && (line = in.readLine()) != null) {
                handleLine(line.trim());
            }
        } catch (IOException e) {
            if (running) setStatus(Status.ERROR);
        }
    }

    private void handleLine(String line) {
        String verb = Protocol.verb(line);
        String[] args = Protocol.args(line);

        switch (verb) {
            case Protocol.MSG_LOBBY_OK -> {
                if (args.length >= 2) {
                    String serverCode = args[1].trim().toUpperCase();
                    if (!serverCode.equals(expectedCode)) {
                        if (out != null) out.println(Protocol.error("wrong code"));
                        setStatus(Status.ERROR);
                        return;
                    }
                    myPlayerId = parseInt(args[0], -1);
                    if (out != null) out.println(Protocol.nick(nickname));
                    setStatus(Status.WAITING);
                }
            }
            case Protocol.MSG_PLAYER_JOINED -> {
                // args[0]=connected, args[1]=required
                String countStr = (args.length >= 2) ? args[0] + "/" + args[1] : "?/?";
                setStatus(Status.WAITING);
                if (onLobbyUpdate != null) onLobbyUpdate.accept(countStr);
            }
            case Protocol.MSG_CAN_START -> setStatus(Status.CAN_START);
            case Protocol.MSG_NICK_UPDATE -> {
                // NICK_UPDATE <pid> <name>
                if (args.length >= 2) {
                    int pid = parseInt(args[0], 0);
                    String name = line.substring(Protocol.MSG_NICK_UPDATE.length() + 1 + args[0].length() + 1);
                    if (pid > 0) knownNicks.put(pid, name);
                }
            }
            case Protocol.MSG_CHAT -> {
                // CHAT <pid> <message...>  — pid=0 means system event
                if (args.length >= 1 && onChatMessage != null) {
                    int pid = parseInt(args[0], 0);
                    String msg = args.length >= 2
                            ? line.substring(Protocol.MSG_CHAT.length() + 1 + args[0].length() + 1)
                            : "";
                    if (pid == 0) {
                        onChatMessage.accept(msg);
                    } else {
                        String name = knownNicks.getOrDefault(pid, "P" + pid);
                        onChatMessage.accept(name + ": " + msg);
                    }
                }
            }
            case Protocol.MSG_START -> {
                int playerCount = args.length > 0 ? parseInt(args[0], 2) : 2;
                state = new GameState();
                if (playerCount == 4) state.activate4P();
                else state.activateOnline2P();
                setStatus(Status.IN_GAME);
            }
            case Protocol.MSG_STATE -> {
                if (state != null) applySnapshot(Protocol.stateJson(line));
            }
            case Protocol.MSG_GAME_OVER -> {
                if (state != null) state.setGameOver(true);
                setStatus(Status.GAME_OVER);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Snapshot application (manual JSON parsing — no external dependencies)
    // -----------------------------------------------------------------------

    private void applySnapshot(String json) {
        if (json == null || json.isBlank() || state == null) return;
        try {
            state.setTimeRemaining(readDouble(json, "\"t\":", state.getTimeRemaining()));

            double[] scores = readDoubleArray(json, "\"s\":");
            if (scores.length >= 2) {
                state.setScoreP1((int) scores[0]);
                state.setScoreP2((int) scores[1]);
            }

            double[] cds = readDoubleArray(json, "\"cd\":");
            for (int i = 0; i < cds.length; i++) {
                Player p = state.getPlayer(i + 1);
                if (p != null) p.setSpawnCooldown(cds[i]);
            }

            double[] nxts = readDoubleArray(json, "\"nxt\":");
            for (int i = 0; i < nxts.length && i < nextUnitOrdinal.length; i++) {
                nextUnitOrdinal[i] = (int) nxts[i];
            }

            double[] rows = readDoubleArray(json, "\"rows\":");
            boolean is4P = state.getMode() == com.animalfarm.model.GameState.GameMode.ONLINE_4P;
            // In 4P, teammates share a barn — only the local player drives their team's barn,
            // and the first enemy pid drives the enemy barn; skip all other teammates.
            int enemyRepPid = is4P ? (myPlayerId <= 2 ? 3 : 1) : -1;
            for (int i = 0; i < rows.length; i++) {
                int pid = i + 1;
                if (state.getPlayer(pid) == null) continue; // skip inactive players
                int row = (int) rows[i];
                state.setSelectedRow(pid, row);
                int teamId = is4P ? (pid <= 2 ? 1 : 2) : (pid == 1 ? 1 : 2);
                if (!is4P || pid == myPlayerId || pid == enemyRepPid) {
                    state.getLane().getBarn(teamId).setY(com.animalfarm.model.GameConfig.laneY(row) - 27.5);
                }
            }

            applyBarns(json);
            applyUnits(json);

        } catch (Exception ignored) {
            // Malformed snapshot — skip this frame
        }
    }

    private final int[] nextUnitOrdinal = {-1, -1, -1, -1};

    public int getNextUnitOrdinal(int playerIndex) { return nextUnitOrdinal[playerIndex]; }

    private void applyBarns(String json) {
        int idx = json.indexOf("\"barns\":[");
        if (idx < 0) return;
        int start = json.indexOf('[', idx);
        int end   = json.indexOf(']', start);
        if (start < 0 || end < 0) return;
        for (String entry : splitObjects(json.substring(start + 1, end))) {
            int team   = (int) readDouble(entry, "\"team\":", -1);
            double hp  = readDouble(entry, "\"hp\":", -1);
            if (team > 0 && hp >= 0) state.getLane().getBarn(team).setHp(hp);
        }
    }

    private void applyUnits(String json) {
        int idx = json.indexOf("\"units\":[");
        if (idx < 0) return;
        int start = json.indexOf('[', idx);
        int end   = findMatchingBracket(json, start);
        if (start < 0 || end < 0) return;
        String arr = json.substring(start + 1, end);

        List<Unit> newUnits = new ArrayList<>();
        if (!arr.isBlank()) {
            for (String entry : splitObjects(arr)) {
                try {
                    int pid     = (int) readDouble(entry, "\"pid\":", 1);
                    int t       = (int) readDouble(entry, "\"t\":", 0);
                    double x    = readDouble(entry, "\"x\":", 0);
                    double y    = readDouble(entry, "\"y\":", 0);
                    double hp   = readDouble(entry, "\"hp\":", 0);
                    int dir     = (int) readDouble(entry, "\"dir\":", 1);
                    double anim = readDouble(entry, "\"anim\":", 0);
                    UnitType[] types = UnitType.values();
                    if (t < 0 || t >= types.length) continue;
                    Unit u = new Unit(types[t], x, y);
                    u.setHp(hp);
                    u.setDirection(dir);
                    u.setPlayerId(pid);
                    u.setAnimationTimer(anim);
                    newUnits.add(u);
                } catch (Exception ignored) {}
            }
        }
        state.getLane().setUnits(newUnits);
    }

    // -----------------------------------------------------------------------
    // Commands
    // -----------------------------------------------------------------------

    public void sendSpawn(int lane) {
        if (out != null) out.println(Protocol.spawn(lane));
    }

    public void sendLaneSelect(int row) {
        if (out != null) out.println(Protocol.laneSelect(row));
    }

    public void sendChat(String msg) {
        if (out != null && !msg.isBlank()) out.println(Protocol.CMD_CHAT + " " + msg);
    }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------

    public int getMyPlayerId() { return myPlayerId; }
    public GameState getState() { return state; }
    public Status getStatus()   { return status; }

    public void disconnect() {
        running = false;
        if (ioThread != null) ioThread.interrupt();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void setStatus(Status s) {
        status = s;
        if (onStatus != null) onStatus.accept(s);
    }

    private static double readDouble(String json, String key, double fallback) {
        int idx = json.indexOf(key);
        if (idx < 0) return fallback;
        int start = idx + key.length();
        while (start < json.length() && json.charAt(start) == ' ') start++;
        int end = start;
        while (end < json.length() && "-0123456789.eE".indexOf(json.charAt(end)) >= 0) end++;
        try { return Double.parseDouble(json.substring(start, end)); }
        catch (NumberFormatException e) { return fallback; }
    }

    private static double[] readDoubleArray(String json, String key) {
        int idx = json.indexOf(key);
        if (idx < 0) return new double[0];
        int start = json.indexOf('[', idx + key.length());
        int end   = json.indexOf(']', start);
        if (start < 0 || end < 0) return new double[0];
        String[] parts = json.substring(start + 1, end).split(",");
        double[] result = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try { result[i] = Double.parseDouble(parts[i].trim()); }
            catch (NumberFormatException e) { result[i] = 0; }
        }
        return result;
    }

    private static String[] splitObjects(String arr) {
        List<String> results = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < arr.length(); i++) {
            char c = arr.charAt(i);
            if (c == '{') { if (depth == 0) start = i; depth++; }
            else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) { results.add(arr.substring(start, i + 1)); start = -1; }
            }
        }
        return results.toArray(new String[0]);
    }

    private static int findMatchingBracket(String s, int openIdx) {
        int depth = 0;
        for (int i = openIdx; i < s.length(); i++) {
            if (s.charAt(i) == '[') depth++;
            else if (s.charAt(i) == ']') { depth--; if (depth == 0) return i; }
        }
        return -1;
    }

    private static int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return fallback; }
    }

}
