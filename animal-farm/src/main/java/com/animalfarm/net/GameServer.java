package com.animalfarm.net;

import com.animalfarm.model.*;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * Listen server for online matches.
 *
 * Usage:
 *   GameServer srv = new GameServer(2, onGameOver);
 *   srv.startAccepting();   // begins accepting TCP connections
 *   // host connects as the first client, then other players join
 *   // when all connected, server broadcasts CAN_START
 *   // host calls srv.startGame() on button press
 */
public class GameServer {

    private static final int TICK_HZ = 20;
    private static final long TICK_MS = 1000 / TICK_HZ;

    private final int requiredPlayers;
    private final String lobbyCode;
    private final String hostIps;   // comma-separated list
    private final GameState state;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final BlockingQueue<ClientHandler.Command> inbound = new LinkedBlockingQueue<>();
    private final Map<Integer, String> nicknames = new ConcurrentHashMap<>();
    private final Runnable onGameOver;
    private final int[] teamPrefs = new int[5];                            // slot 1-4 → chosen team (0=unset,1,2)
    private final Map<Integer, Integer> slotToPid = new ConcurrentHashMap<>(); // resolved at startGame

    private ServerSocket serverSocket;
    private Thread acceptThread;
    private Thread beaconThread;
    private ScheduledExecutorService ticker;
    private volatile boolean gameRunning = false;
    private long lastTickNanos = -1;

    public GameServer(int requiredPlayers, Runnable onGameOver) {
        this.requiredPlayers = requiredPlayers;
        this.onGameOver      = onGameOver;
        this.lobbyCode       = generateCode();
        this.hostIps         = detectLocalIps();
        this.state           = new GameState();
        if (requiredPlayers == 4) state.activate4P();
        else state.activateOnline2P();
    }

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    public String getLobbyCode() { return lobbyCode; }
    /** Comma-separated list of LAN IPv4 addresses, e.g. "192.168.1.13" or "192.168.1.13, 10.0.0.5" */
    public String getHostIps()   { return hostIps; }
    public GameState getState()  { return state; }
    public int getRequiredPlayers() { return requiredPlayers; }
    public int getConnectedPlayers() { return clients.size(); }

    public void startAccepting() throws IOException {
        serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new java.net.InetSocketAddress(GameConfig.SERVER_PORT));
        acceptThread = Thread.ofVirtual().start(this::acceptLoop);
        Thread.ofVirtual().start(this::lobbyRelayLoop);
        beaconThread = Thread.ofVirtual().start(this::beaconLoop);
    }

    private void beaconLoop() {
        byte[] msg = ("ANIMALFARM " + lobbyCode).getBytes(StandardCharsets.UTF_8);
        try (DatagramSocket sock = new DatagramSocket()) {
            sock.setBroadcast(true);
            InetAddress broadcast = InetAddress.getByName("255.255.255.255");
            DatagramPacket pkt = new DatagramPacket(msg, msg.length, broadcast, GameConfig.DISCOVERY_PORT);
            while (!gameRunning && !serverSocket.isClosed()) {
                sock.send(pkt);
                Thread.sleep(1000);
            }
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            if (!serverSocket.isClosed()) e.printStackTrace();
        }
    }

    private void lobbyRelayLoop() {
        while (!gameRunning && !serverSocket.isClosed()) {
            List<ClientHandler.Command> batch = new ArrayList<>();
            inbound.drainTo(batch);
            for (ClientHandler.Command cmd : batch) {
                String verb = Protocol.verb(cmd.line());
                if (Protocol.CMD_CHAT.equals(verb)) {
                    String msg = cmd.line().length() > Protocol.CMD_CHAT.length() + 1
                            ? cmd.line().substring(Protocol.CMD_CHAT.length() + 1) : "";
                    broadcast(Protocol.chat(cmd.playerId(), msg));
                } else if (Protocol.CMD_NICK.equals(verb)) {
                    String name = sanitizeNick(cmd.line().length() > Protocol.CMD_NICK.length() + 1
                            ? cmd.line().substring(Protocol.CMD_NICK.length() + 1) : "");
                    nicknames.put(cmd.playerId(), name);
                    broadcast(Protocol.nickUpdate(cmd.playerId(), name));
                    broadcast(Protocol.chat(0, "** " + name + " joined the lobby"));
                } else if (Protocol.CMD_TEAM_SELECT.equals(verb) && requiredPlayers == 4) {
                    String[] a = Protocol.args(cmd.line());
                    if (a.length > 0) {
                        int team = parseInt(a[0], 0);
                        if (team == 1 || team == 2) {
                            teamPrefs[cmd.playerId()] = team;
                            broadcast(Protocol.teamRoster(teamPrefs));
                        }
                    }
                }
            }
            try { Thread.sleep(50); } catch (InterruptedException e) { break; }
        }
    }

    private void acceptLoop() {
        while (!serverSocket.isClosed() && !gameRunning) {
            try {
                Socket sock = serverSocket.accept();
                // Reject if lobby is already at capacity or game has started
                if (gameRunning || clients.size() >= requiredPlayers) {
                    try { sock.close(); } catch (IOException ignored) {}
                    continue;
                }
                java.util.Set<Integer> usedSlots = new java.util.HashSet<>();
                for (ClientHandler c : clients) usedSlots.add(c.getPlayerId());
                int nextPid = 1;
                while (usedSlots.contains(nextPid)) nextPid++;
                final int pid = nextPid;
                ClientHandler handler = new ClientHandler(pid, sock, inbound, () -> onClientDisconnect(pid));
                clients.add(handler);
                Thread.ofVirtual().start(handler);

                // Handshake: tell this client their slot ID and the lobby code
                handler.send(Protocol.lobbyOk(pid, lobbyCode));
                // Replay existing nicknames and team prefs so late joiners see the full state
                for (Map.Entry<Integer, String> e : nicknames.entrySet())
                    handler.send(Protocol.nickUpdate(e.getKey(), e.getValue()));
                handler.send(Protocol.teamRoster(teamPrefs));
                // Tell everyone (including the new client) current player count
                broadcast(Protocol.playerJoined(clients.size(), requiredPlayers));

                // If roster is now full, tell all clients they can start
                if (clients.size() == requiredPlayers) {
                    broadcast(Protocol.canStart());
                }
            } catch (IOException e) {
                if (!serverSocket.isClosed()) e.printStackTrace();
            }
        }
    }

    // -----------------------------------------------------------------------
    // Game start
    // -----------------------------------------------------------------------

    public void startGame() {
        if (gameRunning) return;
        if (clients.size() < requiredPlayers) return;
        if (requiredPlayers == 4) {
            int t1 = 0, t2 = 0;
            for (ClientHandler c : clients) {
                int p = teamPrefs[c.getPlayerId()];
                if (p == 1) t1++; else if (p == 2) t2++;
            }
            if (t1 != 2 || t2 != 2) {
                ClientHandler host = clients.isEmpty() ? null : clients.get(0);
                if (host != null)
                    host.send(Protocol.chat(0, "** Teams must be evenly split (2 v 2) before starting the game."));
                return;
            }
            int[] mapping = resolvePidMapping();
            for (int slot = 1; slot <= 4; slot++) slotToPid.put(slot, mapping[slot]);
            gameRunning = true;
            broadcast(Protocol.start4P(mapping));
        } else {
            gameRunning = true;
            broadcast(Protocol.start(requiredPlayers));
        }
        ticker = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "game-server-tick");
            t.setDaemon(true);
            return t;
        });
        lastTickNanos = System.nanoTime();
        ticker.scheduleAtFixedRate(this::tick, 0, TICK_MS, TimeUnit.MILLISECONDS);
    }

    // -----------------------------------------------------------------------
    // Tick loop
    // -----------------------------------------------------------------------

    private void tick() {
        long now = System.nanoTime();
        double delta = (now - lastTickNanos) / 1_000_000_000.0;
        if (delta > 0.1) delta = 0.1;
        lastTickNanos = now;

        // Drain inbound commands
        List<ClientHandler.Command> batch = new ArrayList<>();
        inbound.drainTo(batch);
        for (ClientHandler.Command cmd : batch) {
            processCommand(cmd);
        }

        state.update(delta);
        broadcast(Protocol.state(buildStateJson()));

        if (state.isGameOver()) {
            if (!state.isSuddenDeath() && state.getScoreP1() == state.getScoreP2()) {
                // Regulation tie — tell clients to show the announcement, then keep ticking in sudden death
                broadcast(Protocol.gameOver(state.getScoreP1(), state.getScoreP2()));
                state.startSuddenDeath();
                return; // do NOT stop ticker
            }
            broadcast(Protocol.gameOver(state.getScoreP1(), state.getScoreP2()));
            ticker.shutdown();
            gameRunning = false;
            if (onGameOver != null) onGameOver.run();
        }
    }

    private void processCommand(ClientHandler.Command cmd) {
        String verb = Protocol.verb(cmd.line());
        String[] args = Protocol.args(cmd.line());
        int pid = slotToPid.getOrDefault(cmd.playerId(), cmd.playerId());

        switch (verb) {
            case Protocol.CMD_SPAWN -> {
                int lane = args.length > 0 ? parseInt(args[0], state.getSelectedRow(pid)) : state.getSelectedRow(pid);
                state.spawnUnit(pid, lane);
            }
            case Protocol.CMD_LANE_SELECT -> {
                if (args.length > 0) {
                    int row = parseInt(args[0], 0);
                    row = Math.max(0, Math.min(GameConfig.NUM_LANES - 1, row));
                    state.setSelectedRow(pid, row);
                    int teamId = pid <= 2 ? 1 : 2;
                    state.getLane().getBarn(teamId).setY(GameConfig.laneY(row) - 27.5);
                }
            }
            case Protocol.CMD_CHAT -> {
                // Relay chat to all clients: CHAT <pid> <original message>
                String msg = cmd.line().length() > Protocol.CMD_CHAT.length() + 1
                        ? cmd.line().substring(Protocol.CMD_CHAT.length() + 1)
                        : "";
                broadcast(Protocol.chat(pid, msg));
            }
            case Protocol.CMD_PING -> {
                ClientHandler handler = clientFor(pid);
                if (handler != null) handler.send("PONG");
            }
        }
    }

    // -----------------------------------------------------------------------
    // Snapshot JSON
    // -----------------------------------------------------------------------

    private String buildStateJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"t\":").append(String.format("%.2f", state.getTimeRemaining())).append(",");
        sb.append("\"s\":[").append(state.getScoreP1()).append(",").append(state.getScoreP2()).append("],");

        sb.append("\"cd\":[");
        for (int i = 1; i <= 4; i++) {
            var p = state.getPlayer(i);
            if (i > 1) sb.append(",");
            sb.append(p != null ? String.format("%.3f", p.getSpawnCooldown()) : "0");
        }
        sb.append("],");

        sb.append("\"nxt\":[");
        for (int i = 1; i <= 4; i++) {
            UnitType nxt = state.peekNext(i);
            if (i > 1) sb.append(",");
            sb.append(nxt != null ? nxt.ordinal() : -1);
        }
        sb.append("],");

        sb.append("\"rows\":[");
        for (int i = 1; i <= 4; i++) {
            if (i > 1) sb.append(",");
            sb.append(state.getSelectedRow(i));
        }
        sb.append("],");

        sb.append("\"units\":[");
        var units = state.getLane().getUnits();
        for (int i = 0; i < units.size(); i++) {
            Unit u = units.get(i);
            if (i > 0) sb.append(",");
            sb.append("{");
            sb.append("\"pid\":").append(u.getPlayerId()).append(",");
            sb.append("\"t\":").append(u.getType().ordinal()).append(",");
            sb.append("\"x\":").append(String.format("%.1f", u.getX())).append(",");
            sb.append("\"y\":").append(String.format("%.1f", u.getY())).append(",");
            sb.append("\"hp\":").append(String.format("%.1f", u.getHp())).append(",");
            sb.append("\"dir\":").append(u.getDirection()).append(",");
            sb.append("\"anim\":").append(String.format("%.3f", u.getAnimationTimer()));
            sb.append("}");
        }
        sb.append("],");

        sb.append("\"barns\":[");
        sb.append("{\"team\":1,\"hp\":").append(String.format("%.1f", state.getLane().getBarn(1).getHp())).append("},");
        sb.append("{\"team\":2,\"hp\":").append(String.format("%.1f", state.getLane().getBarn(2).getHp())).append("}");
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void onClientDisconnect(int pid) {
        if (gameRunning) return; // mid-game disconnects not handled here
        clients.removeIf(c -> c.getPlayerId() == pid);
        String name = nicknames.remove(pid);
        if (pid >= 1 && pid <= 4) teamPrefs[pid] = 0;  // clear team choice for this slot
        broadcast(Protocol.playerJoined(clients.size(), requiredPlayers));
        broadcast(Protocol.nickUpdate(pid, ""));        // tell clients to clear this slot
        broadcast(Protocol.teamRoster(teamPrefs));      // updated roster without departed player
        if (name != null) broadcast(Protocol.chat(0, "** " + name + " left the lobby"));
    }

    private void broadcast(String line) {
        for (ClientHandler c : clients) c.send(line);
    }

    private ClientHandler clientFor(int pid) {
        return clients.stream().filter(c -> c.getPlayerId() == pid).findFirst().orElse(null);
    }

    public void shutdown() {
        gameRunning = false;
        if (ticker != null) ticker.shutdownNow();
        if (acceptThread != null) acceptThread.interrupt();
        if (beaconThread != null) beaconThread.interrupt();
        for (ClientHandler c : clients) c.close();
        try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
    }

    private static String generateCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random rng = new Random();
        StringBuilder sb = new StringBuilder(GameConfig.LOBBY_CODE_LENGTH);
        for (int i = 0; i < GameConfig.LOBBY_CODE_LENGTH; i++) {
            sb.append(chars.charAt(rng.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Returns all site-local IPv4 addresses on active non-loopback interfaces,
     * comma-separated. Falls back to "127.0.0.1" if none found.
     */
    private static String detectLocalIps() {
        List<String> ips = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            if (ifaces != null) {
                for (NetworkInterface ni : Collections.list(ifaces)) {
                    if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                    for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                        if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                            ips.add(addr.getHostAddress());
                        }
                    }
                }
            }
        } catch (SocketException ignored) {}

        if (ips.isEmpty()) {
            // Fallback: UDP routing trick
            try (DatagramSocket s = new DatagramSocket()) {
                s.connect(InetAddress.getByName("8.8.8.8"), 53);
                String ip = s.getLocalAddress().getHostAddress();
                if (!ip.equals("0.0.0.0")) ips.add(ip);
            } catch (Exception ignored) {}
        }

        return ips.isEmpty() ? "127.0.0.1" : String.join(", ", ips);
    }

    private int[] resolvePidMapping() {
        List<Integer> team1 = new ArrayList<>(), team2 = new ArrayList<>(), unassigned = new ArrayList<>();
        for (ClientHandler c : clients) {
            int slot = c.getPlayerId();
            if      (teamPrefs[slot] == 1) team1.add(slot);
            else if (teamPrefs[slot] == 2) team2.add(slot);
            else                           unassigned.add(slot);
        }
        for (int slot : unassigned) {
            if (team1.size() < 2) team1.add(slot); else team2.add(slot);
        }
        int[] mapping = new int[5];
        int pid = 1;
        for (int slot : team1) { if (pid <= 2) mapping[slot] = pid++; }
        pid = 3;
        for (int slot : team2) { if (pid <= 4) mapping[slot] = pid++; }
        return mapping;
    }

    private static int parseInt(String s, int fallback) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }

    private static String sanitizeNick(String raw) {
        String s = raw.trim().replaceAll("\\s+", " ");
        if (s.isEmpty()) s = "Player";
        return s.length() > 16 ? s.substring(0, 16) : s;
    }
}
