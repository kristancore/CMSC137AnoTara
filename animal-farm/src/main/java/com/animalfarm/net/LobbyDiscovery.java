package com.animalfarm.net;

import com.animalfarm.model.GameConfig;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Listens on the LAN broadcast channel for a host beacon matching a given lobby code.
 * Runs on a virtual thread; callbacks fire on that same thread — use Platform.runLater if needed.
 */
public final class LobbyDiscovery {

    private LobbyDiscovery() {}

    /**
     * @param code       6-char lobby code the joiner typed
     * @param timeoutMs  how long to wait before giving up
     * @param onFound    called with the host's IP string on success
     * @param onNotFound called if no matching beacon is received within timeoutMs
     */
    public static void discover(String code, int timeoutMs,
                                Consumer<String> onFound, Runnable onNotFound) {
        String expected = "ANIMALFARM " + code.trim().toUpperCase();
        Thread.ofVirtual().start(() -> {
            try (DatagramSocket sock = new DatagramSocket(GameConfig.DISCOVERY_PORT)) {
                byte[] buf = new byte[64];
                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                long deadline = System.currentTimeMillis() + timeoutMs;
                while (System.currentTimeMillis() < deadline) {
                    int remaining = (int) (deadline - System.currentTimeMillis());
                    if (remaining <= 0) break;
                    sock.setSoTimeout(remaining);
                    sock.receive(pkt);
                    String payload = new String(pkt.getData(), 0, pkt.getLength(), StandardCharsets.UTF_8).trim();
                    if (expected.equals(payload)) {
                        onFound.accept(pkt.getAddress().getHostAddress());
                        return;
                    }
                }
            } catch (SocketTimeoutException ignored) {
            } catch (Exception ignored) {}
            onNotFound.run();
        });
    }
}
