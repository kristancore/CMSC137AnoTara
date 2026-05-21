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
        // #region debug
        debugLog("discover called", "code=" + code + " expected=" + expected + " timeoutMs=" + timeoutMs, "H3,H4");
        // #endregion
        Thread.ofVirtual().start(() -> {
            // #region debug
            debugLog("discover thread started", "binding to port " + GameConfig.DISCOVERY_PORT, "H4");
            // #endregion
            try (DatagramSocket sock = new DatagramSocket(GameConfig.DISCOVERY_PORT)) {
                // #region debug
                debugLog("discover socket bound", "localPort=" + sock.getLocalPort(), "H4");
                // #endregion
                byte[] buf = new byte[64];
                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                long deadline = System.currentTimeMillis() + timeoutMs;
                int packetsReceived = 0;
                while (System.currentTimeMillis() < deadline) {
                    int remaining = (int) (deadline - System.currentTimeMillis());
                    if (remaining <= 0) break;
                    sock.setSoTimeout(remaining);
                    sock.receive(pkt);
                    packetsReceived++;
                    String payload = new String(pkt.getData(), 0, pkt.getLength(), StandardCharsets.UTF_8).trim();
                    // #region debug
                    debugLog("packet received #" + packetsReceived, "from=" + pkt.getAddress().getHostAddress() + " payload='" + payload + "' expected='" + expected + "' match=" + expected.equals(payload), "H2,H3");
                    // #endregion
                    if (expected.equals(payload)) {
                        // #region debug
                        debugLog("FOUND host", "ip=" + pkt.getAddress().getHostAddress(), "H3");
                        // #endregion
                        onFound.accept(pkt.getAddress().getHostAddress());
                        return;
                    }
                }
                // #region debug
                debugLog("discover timed out", "packetsReceived=" + packetsReceived + " expected='" + expected + "'", "H2,H3");
                // #endregion
            } catch (SocketTimeoutException e) {
                // #region debug
                debugLog("discover SocketTimeout (no packets)", "expected='" + expected + "'", "H1,H2");
                // #endregion
            } catch (Exception e) {
                // #region debug
                debugLog("discover EXCEPTION", e.getClass().getName() + ": " + e.getMessage(), "H4");
                // #endregion
            }
            onNotFound.run();
        });
    }

    // #region debug
    private static void debugLog(String msg, String data, String hyp) {
        try {
            java.net.URL url = new java.net.URL("http://localhost:8787/log");
            java.net.HttpURLConnection c = (java.net.HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST"); c.setDoOutput(true); c.setConnectTimeout(300); c.setReadTimeout(300);
            String body = "{\"sessionId\":\"udp-discovery-fail-228308\",\"msg\":\"" + msg.replace("\"","'") + "\",\"data\":\"" + data.replace("\"","'") + "\",\"hypothesisId\":\"" + hyp + "\"}";
            c.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            c.getInputStream().close();
        } catch (Exception ignored) {}
    }
    // #endregion
}
