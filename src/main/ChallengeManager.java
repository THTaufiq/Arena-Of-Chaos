package main;

import java.util.ArrayList;
import java.util.List;

public class ChallengeManager {

    public static class PlayerRank {
        public String playerName;
        public int score;
        public int life;
        public int enemyKilled;
    }

    private final GameWebSocketClient client;

    // In-memory properties
    private String playerName;
    private String roomCode;
    private int duration;
    private int timeLeft;
    private boolean challengeStarted = false;
    private String opponentName;
    private int opponentScore;
    private int opponentLife;
    private int opponentKills;
    private String winnerName;
    private List<PlayerRank> finalRanking = new ArrayList<>();
    private String errorMessage = null;
    private boolean challengeEndRequested = false;
    private long lastOpponentUpdateTimestamp = 0;

    public ChallengeManager() {
        this.client = new GameWebSocketClient();
    }

    public void connect() {
        client.connect(this::handleServerMessage);
    }

    public void createRoom(String playerName, int duration) {
        this.playerName = playerName;
        String message = String.format("{\"type\":\"CREATE_ROOM\",\"playerName\":\"%s\",\"duration\":%d}",
                sanitize(playerName), duration);
        client.sendMessage(message);
    }

    public void joinRoom(String playerName, String roomCode) {
        this.playerName = playerName;
        this.roomCode = roomCode;
        String message = String.format("{\"type\":\"JOIN_ROOM\",\"roomCode\":\"%s\",\"playerName\":\"%s\"}",
                sanitize(roomCode), sanitize(playerName));
        client.sendMessage(message);
    }

    public void startRoom() {
        if (roomCode != null && playerName != null) {
            String message = String.format("{\"type\":\"START_ROOM\",\"roomCode\":\"%s\",\"playerName\":\"%s\"}",
                    sanitize(roomCode), sanitize(playerName));
            client.sendMessage(message);
        } else {
            System.err.println("Cannot start room: roomCode or playerName is missing.");
        }
    }

    public void sendPlayerUpdate(int score, int life, int enemyKilled) {
        if (roomCode != null && playerName != null) {
            String message = String.format("{\"type\":\"PLAYER_UPDATE\",\"roomCode\":\"%s\",\"playerName\":\"%s\",\"score\":%d,\"life\":%d,\"enemyKilled\":%d}",
                    sanitize(roomCode), sanitize(playerName), score, life, enemyKilled);
            client.sendMessage(message);
        }
    }

    public void endChallenge() {
        if (roomCode != null && !challengeEndRequested) {
            challengeEndRequested = true;
            String message = String.format("{\"type\":\"END_CHALLENGE\",\"roomCode\":\"%s\"}",
                    sanitize(roomCode));
            client.sendMessage(message);
        }
    }

    public void handleServerMessage(String json) {
        try {
            String type = getStringField(json, "type");
            if (type == null) return;

            switch (type) {
                case "ROOM_CREATED":
                    handleRoomCreated(json);
                    break;
                case "PLAYER_JOINED":
                    handlePlayerJoined(json);
                    break;
                case "START_CHALLENGE":
                    handleStartChallenge(json);
                    break;
                case "LIVE_RANKING":
                    handleLiveRanking(json);
                    break;
                case "CHALLENGE_RESULT":
                    handleChallengeResult(json);
                    break;
                 case "ERROR":
                     String errMsg = getStringField(json, "message");
                     System.err.println("Server Error: " + errMsg);
                     this.errorMessage = errMsg;
                     break;
                default:
                    System.out.println("Unknown message type: " + type);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Failed to parse server message: " + e.getMessage());
        }
    }

    private void handleRoomCreated(String json) {
        this.roomCode = getStringField(json, "roomCode");
        this.duration = getIntField(json, "duration");
        this.timeLeft = this.duration;
        this.challengeStarted = false;
        System.out.println("Room Created! Code: " + roomCode + ", Duration: " + duration + "s");
    }

    private void handlePlayerJoined(String json) {
        this.roomCode = getStringField(json, "roomCode");
        List<String> players = getPlayersField(json);
        System.out.println("Players in room: " + players);
        updateOpponentDetails(players);
    }

    private void handleStartChallenge(String json) {
        this.roomCode = getStringField(json, "roomCode");
        this.duration = getIntField(json, "duration");
        this.timeLeft = this.duration;
        this.challengeStarted = true;
        this.opponentLife = 6; // Initialize to default max life
        List<String> players = getPlayersField(json);
        System.out.println("Challenge Started");
        System.out.println("Challenge Started! Players: " + players + ", Duration: " + duration + "s");
        updateOpponentDetails(players);
    }

    private void handleLiveRanking(String json) {
        long now = System.currentTimeMillis();
        if (now - lastOpponentUpdateTimestamp >= 2000) {
            lastOpponentUpdateTimestamp = now;
            this.roomCode = getStringField(json, "roomCode");
            List<PlayerRank> ranks = parseRanking(json);
            updateOpponentFromRanks(ranks);
        }
    }

    private void handleChallengeResult(String json) {
        this.roomCode = getStringField(json, "roomCode");
        this.winnerName = getStringField(json, "winner");
        this.finalRanking = parseRanking(json);
        this.challengeStarted = false;
        this.timeLeft = 0; // Prevent timer from getting stuck at 1-2s if opponent ended the match first
        System.out.println("Challenge Result Received");
        System.out.println("Challenge Finished! Winner: " + winnerName);
        for (PlayerRank rank : finalRanking) {
            System.out.println(rank.playerName + " - Score: " + rank.score);
        }
    }

    private void updateOpponentDetails(List<String> players) {
        if (playerName != null) {
            for (String name : players) {
                if (!name.equalsIgnoreCase(playerName)) {
                    this.opponentName = name;
                    break;
                }
            }
        }
    }

    private void updateOpponentFromRanks(List<PlayerRank> ranks) {
        if (playerName != null) {
            for (PlayerRank rank : ranks) {
                if (!rank.playerName.equalsIgnoreCase(playerName)) {
                    this.opponentName = rank.playerName;
                    this.opponentScore = rank.score;
                    this.opponentLife = rank.life;
                    this.opponentKills = rank.enemyKilled;
                    System.out.println("Opponent (" + opponentName + ") Update - Score: " + opponentScore + ", Life: " + opponentLife + ", Kills: " + opponentKills);
                    break;
                }
            }
        }
    }

    // --- JSON Parsing Helpers ---

    private String getStringField(String json, String fieldName) {
        String search = "\"" + fieldName + "\"";
        int index = json.indexOf(search);
        if (index == -1) return null;
        int colon = json.indexOf(":", index);
        if (colon == -1) return null;
        int startQuote = json.indexOf("\"", colon);
        if (startQuote == -1) return null;
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return null;
        return json.substring(startQuote + 1, endQuote);
    }

    private int getIntField(String json, String fieldName) {
        String search = "\"" + fieldName + "\"";
        int index = json.indexOf(search);
        if (index == -1) return 0;
        int colon = json.indexOf(":", index);
        if (colon == -1) return 0;
        int start = colon + 1;
        while (start < json.length() && !Character.isDigit(json.charAt(start)) && json.charAt(start) != '-') {
            start++;
        }
        if (start >= json.length()) return 0;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private List<String> getPlayersField(String json) {
        List<String> list = new ArrayList<>();
        int playersIndex = json.indexOf("\"players\"");
        if (playersIndex == -1) return list;
        int startBracket = json.indexOf("[", playersIndex);
        int endBracket = json.indexOf("]", playersIndex);
        if (startBracket == -1 || endBracket == -1) return list;
        String playersContent = json.substring(startBracket + 1, endBracket);
        String[] parts = playersContent.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("\"") && part.endsWith("\"")) {
                list.add(part.substring(1, part.length() - 1));
            }
        }
        return list;
    }

    private List<PlayerRank> parseRanking(String json) {
        List<PlayerRank> ranks = new ArrayList<>();
        int rankingIndex = json.indexOf("\"ranking\"");
        if (rankingIndex == -1) return ranks;
        int startBracket = json.indexOf("[", rankingIndex);
        int endBracket = json.indexOf("]", rankingIndex);
        if (startBracket == -1 || endBracket == -1) return ranks;
        String rankingContent = json.substring(startBracket + 1, endBracket);
        
        int searchStart = 0;
        while (true) {
            int openBrace = rankingContent.indexOf("{", searchStart);
            if (openBrace == -1) break;
            int closeBrace = rankingContent.indexOf("}", openBrace);
            if (closeBrace == -1) break;
            
            String objStr = rankingContent.substring(openBrace, closeBrace + 1);
            PlayerRank r = new PlayerRank();
            r.playerName = getStringField(objStr, "playerName");
            r.score = getIntField(objStr, "score");
            r.life = getIntField(objStr, "life");
            r.enemyKilled = getIntField(objStr, "enemyKilled");
            ranks.add(r);
            
            searchStart = closeBrace + 1;
        }
        return ranks;
    }

    private String sanitize(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // --- Getters & Setters ---
    
    public String getPlayerName() { return playerName; }
    public String getRoomCode() { return roomCode; }
    public int getDuration() { return duration; }
    public int getTimeLeft() { return timeLeft; }
    public void setTimeLeft(int timeLeft) { this.timeLeft = timeLeft; }
    public boolean isChallengeStarted() { return challengeStarted; }
    public String getOpponentName() { return opponentName; }
    public int getOpponentScore() { return opponentScore; }
    public int getOpponentLife() { return opponentLife; }
    public int getOpponentKills() { return opponentKills; }
    public String getWinnerName() { return winnerName; }
    public List<PlayerRank> getFinalRanking() { return finalRanking; }
    
    public boolean isConnected() {
        return client.isConnected();
    }
    
    public void disconnect() {
        client.disconnect();
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void clearErrorMessage() {
        this.errorMessage = null;
    }
}
