package com.animalfarm.net;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Runs on the server side — one thread per connected player.
 * Reads incoming command lines and enqueues them for the GameServer tick loop.
 */
public class ClientHandler implements Runnable {

    public record Command(int playerId, String line) {}

    private final int playerId;
    private final Socket socket;
    private final BlockingQueue<Command> inbound;
    private final PrintWriter out;
    private volatile boolean running = true;

    public ClientHandler(int playerId, Socket socket, BlockingQueue<Command> inbound) throws IOException {
        this.playerId = playerId;
        this.socket   = socket;
        this.inbound  = inbound;
        this.out      = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    /** Send a line to this client. Thread-safe. */
    public void send(String line) {
        out.println(line);
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line;
            while (running && (line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    inbound.offer(new Command(playerId, line.trim()));
                }
            }
        } catch (IOException ignored) {
            // client disconnected
        } finally {
            running = false;
            close();
        }
    }

    public void close() {
        running = false;
        try { socket.close(); } catch (IOException ignored) {}
    }

    public boolean isConnected() {
        return running && !socket.isClosed();
    }

    public int getPlayerId() { return playerId; }
}
