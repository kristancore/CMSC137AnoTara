package com.animalfarm.net;

/**
 * Wire protocol constants and helpers.
 *
 * Client → Server:
 *   SPAWN <lane>
 *   LANE_SELECT <row>
 *   CHAT <message text>
 *   PING
 *
 * Server → Client:
 *   LOBBY_OK <playerId> <lobbyCode>
 *   PLAYER_JOINED <connectedCount> <requiredCount>
 *   CAN_START                        (all players connected; host may press Start)
 *   CHAT <playerId> <message text>
 *   START <mode>                     (mode = 2 or 4)
 *   STATE <json>
 *   GAME_OVER <scoreTeamA> <scoreTeamB>
 *   ERROR <message>
 */
public final class Protocol {

    private Protocol() {}

    // Client → Server
    public static final String CMD_SPAWN       = "SPAWN";
    public static final String CMD_LANE_SELECT = "LANE_SELECT";
    public static final String CMD_CHAT        = "CHAT";
    public static final String CMD_PING        = "PING";

    // Server → Client
    public static final String MSG_LOBBY_OK      = "LOBBY_OK";
    public static final String MSG_PLAYER_JOINED = "PLAYER_JOINED";
    public static final String MSG_CAN_START     = "CAN_START";
    public static final String MSG_CHAT          = "CHAT";
    public static final String MSG_START         = "START";
    public static final String MSG_STATE         = "STATE";
    public static final String MSG_GAME_OVER     = "GAME_OVER";
    public static final String MSG_ERROR         = "ERROR";

    // -----------------------------------------------------------------------
    // Builders
    // -----------------------------------------------------------------------

    public static String spawn(int lane) {
        return CMD_SPAWN + " " + lane;
    }

    public static String laneSelect(int row) {
        return CMD_LANE_SELECT + " " + row;
    }

    public static String lobbyOk(int playerId, String code) {
        return MSG_LOBBY_OK + " " + playerId + " " + code;
    }

    public static String playerJoined(int connected, int required) {
        return MSG_PLAYER_JOINED + " " + connected + " " + required;
    }

    public static String start(int playerCount) {
        return MSG_START + " " + playerCount;
    }

    public static String state(String json) {
        return MSG_STATE + " " + json;
    }

    public static String gameOver(int scoreA, int scoreB) {
        return MSG_GAME_OVER + " " + scoreA + " " + scoreB;
    }

    public static String canStart() {
        return MSG_CAN_START;
    }

    public static String chat(int pid, String msg) {
        return MSG_CHAT + " " + pid + " " + msg;
    }

    public static String error(String msg) {
        return MSG_ERROR + " " + msg;
    }

    // -----------------------------------------------------------------------
    // Parsers (return null / -1 on parse failure)
    // -----------------------------------------------------------------------

    public static String verb(String line) {
        if (line == null || line.isBlank()) return "";
        int sp = line.indexOf(' ');
        return sp < 0 ? line.trim() : line.substring(0, sp).trim();
    }

    public static String[] args(String line) {
        if (line == null) return new String[0];
        int sp = line.indexOf(' ');
        if (sp < 0) return new String[0];
        return line.substring(sp + 1).trim().split(" ", -1);
    }

    /** Extract the JSON payload from a STATE line. */
    public static String stateJson(String line) {
        int sp = line.indexOf(' ');
        return sp < 0 ? "" : line.substring(sp + 1).trim();
    }
}
