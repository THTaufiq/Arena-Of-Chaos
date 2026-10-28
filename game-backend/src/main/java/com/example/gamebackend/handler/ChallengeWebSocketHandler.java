package com.example.gamebackend.handler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.gamebackend.model.ChallengePlayer;
import com.example.gamebackend.model.ChallengeRoom;
import com.example.gamebackend.service.ChallengeRoomManager;

@Component
public class ChallengeWebSocketHandler extends TextWebSocketHandler {

    private final ChallengeRoomManager roomManager;
    private final ObjectMapper objectMapper;

    // Map of roomCode -> List of sessions in that room
    private final Map<String, List<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    
    // Maps to track session associations for easy cleanup
    private final Map<String, String> sessionRoomCodes = new ConcurrentHashMap<>();
    private final Map<String, String> sessionPlayerNames = new ConcurrentHashMap<>();

    public ChallengeWebSocketHandler(ChallengeRoomManager roomManager, ObjectMapper objectMapper) {
        this.roomManager = roomManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Received websocket message: " + payload);

        try {
            JsonNode root = objectMapper.readTree(payload);
            if (!root.has("type")) {
                sendError(session, "Missing 'type' field in message.");
                return;
            }

            String type = root.get("type").asText();
            switch (type) {
                case "CREATE_ROOM":
                    handleCreateRoom(session, root);
                    break;
                case "JOIN_ROOM":
                    handleJoinRoom(session, root);
                    break;
                case "START_ROOM":
                    handleStartRoom(session, root);
                    break;
                case "PLAYER_UPDATE":
                    handlePlayerUpdate(session, root);
                    break;
                case "END_CHALLENGE":
                    handleEndChallenge(session, root);
                    break;
                default:
                    // Echo back received for other messages or custom handler
                    session.sendMessage(new TextMessage("MESSAGE_RECEIVED"));
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error processing websocket message: " + e.getMessage());
            sendError(session, "Invalid message format: " + e.getMessage());
        }
    }

    private void handleCreateRoom(WebSocketSession session, JsonNode root) throws IOException {
        if (!root.has("playerName") || !root.has("duration")) {
            sendError(session, "CREATE_ROOM requires 'playerName' and 'duration'.");
            return;
        }

        String playerName = root.get("playerName").asText();
        int duration = root.get("duration").asInt();

        ChallengeRoom room = roomManager.createRoom(playerName, duration);
        String roomCode = room.getRoomCode();

        // Register host session
        List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
        sessions.add(session);
        roomSessions.put(roomCode, sessions);

        sessionRoomCodes.put(session.getId(), roomCode);
        sessionPlayerNames.put(session.getId(), playerName);

        // Send ROOM_CREATED reply to host
        Map<String, Object> response = Map.of(
            "type", "ROOM_CREATED",
            "roomCode", roomCode,
            "duration", duration
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void handleJoinRoom(WebSocketSession session, JsonNode root) throws IOException {
        if (!root.has("roomCode") || !root.has("playerName")) {
            sendError(session, "JOIN_ROOM requires 'roomCode' and 'playerName'.");
            return;
        }

        String roomCode = root.get("roomCode").asText();
        String playerName = root.get("playerName").asText();

        ChallengeRoom room = roomManager.findRoom(roomCode);
        if (room == null) {
            sendError(session, "Room not found.");
            return;
        }

        if (room.getPlayers().size() >= 2) {
            sendError(session, "Room is full.");
            return;
        }

        if (room.getStatus() != ChallengeRoom.RoomStatus.WAITING) {
            sendError(session, "Room is already in progress or finished.");
            return;
        }

        // Add second player
        room = roomManager.joinRoom(roomCode, playerName);
        if (room == null) {
            sendError(session, "Failed to join room.");
            return;
        }

        // Register session for joining player
        List<WebSocketSession> sessions = roomSessions.get(roomCode);
        if (sessions == null) {
            sessions = new CopyOnWriteArrayList<>();
            roomSessions.put(roomCode, sessions);
        }
        sessions.add(session);

        sessionRoomCodes.put(session.getId(), roomCode);
        sessionPlayerNames.put(session.getId(), playerName);

        // Get player names
        List<String> playerNames = room.getPlayers().stream()
                .map(ChallengePlayer::getPlayerName)
                .collect(Collectors.toList());

        // Notify both players
        Map<String, Object> response = Map.of(
            "type", "PLAYER_JOINED",
            "roomCode", roomCode,
            "players", playerNames
        );
        String responseJson = objectMapper.writeValueAsString(response);

        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(responseJson));
            }
        }
    }

    private void handleStartRoom(WebSocketSession session, JsonNode root) throws IOException {
        if (!root.has("roomCode") || !root.has("playerName")) {
            sendError(session, "START_ROOM requires 'roomCode' and 'playerName'.");
            return;
        }

        String roomCode = root.get("roomCode").asText();
        String playerName = root.get("playerName").asText();

        ChallengeRoom room = roomManager.findRoom(roomCode);
        if (room == null) {
            sendError(session, "Room not found.");
            return;
        }

        synchronized (room) {
            if (!room.getHostName().equalsIgnoreCase(playerName)) {
                sendError(session, "Only the host can start the game.");
                return;
            }

            if (room.getPlayers().size() != 2) {
                sendError(session, "Room must have exactly 2 players to start.");
                return;
            }

            if (room.getStatus() != ChallengeRoom.RoomStatus.WAITING) {
                sendError(session, "Room is already running or finished.");
                return;
            }

            room.setStatus(ChallengeRoom.RoomStatus.RUNNING);

            List<String> playerNames = room.getPlayers().stream()
                    .map(ChallengePlayer::getPlayerName)
                    .collect(Collectors.toList());

            // Notify both players
            Map<String, Object> response = Map.of(
                "type", "START_CHALLENGE",
                "roomCode", roomCode,
                "duration", room.getDuration(),
                "players", playerNames
            );
            String responseJson = objectMapper.writeValueAsString(response);

            List<WebSocketSession> sessions = roomSessions.get(roomCode);
            if (sessions != null) {
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(responseJson));
                    }
                }
            }
        }
    }

    private void handlePlayerUpdate(WebSocketSession session, JsonNode root) throws IOException {
        if (!root.has("roomCode") || !root.has("playerName") || !root.has("score") || !root.has("life") || !root.has("enemyKilled")) {
            sendError(session, "PLAYER_UPDATE requires 'roomCode', 'playerName', 'score', 'life', and 'enemyKilled'.");
            return;
        }

        String roomCode = root.get("roomCode").asText();
        String playerName = root.get("playerName").asText();
        int score = root.get("score").asInt();
        int life = root.get("life").asInt();
        int enemyKilled = root.get("enemyKilled").asInt();

        ChallengeRoom room = roomManager.findRoom(roomCode);
        if (room == null) {
            sendError(session, "Room not found.");
            return;
        }

        synchronized (room) {
            if (room.getStatus() != ChallengeRoom.RoomStatus.RUNNING) {
                sendError(session, "Room is not in RUNNING state.");
                return;
            }

            // Find player and update in memory
            ChallengePlayer targetPlayer = null;
            for (ChallengePlayer p : room.getPlayers()) {
                if (p.getPlayerName().equalsIgnoreCase(playerName)) {
                    targetPlayer = p;
                    break;
                }
            }

            if (targetPlayer == null) {
                sendError(session, "Player not found in room.");
                return;
            }

            targetPlayer.setScore(score);
            targetPlayer.setLife(life);
            targetPlayer.setEnemyKilled(enemyKilled);

            // Sort players by score descending
            List<ChallengePlayer> sortedPlayers = room.getPlayers().stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()))
                    .collect(Collectors.toList());

            // Build ranking response
            List<Map<String, Object>> ranking = sortedPlayers.stream()
                    .map(p -> Map.<String, Object>of(
                        "playerName", p.getPlayerName(),
                        "score", p.getScore(),
                        "life", p.getLife(),
                        "enemyKilled", p.getEnemyKilled()
                    ))
                    .collect(Collectors.toList());

            Map<String, Object> response = Map.of(
                "type", "LIVE_RANKING",
                "roomCode", roomCode,
                "ranking", ranking
            );
            String responseJson = objectMapper.writeValueAsString(response);

            List<WebSocketSession> sessions = roomSessions.get(roomCode);
            if (sessions != null) {
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(responseJson));
                    }
                }
            }
        }
    }

    private void handleEndChallenge(WebSocketSession session, JsonNode root) throws IOException {
        if (!root.has("roomCode")) {
            sendError(session, "END_CHALLENGE requires 'roomCode'.");
            return;
        }

        String roomCode = root.get("roomCode").asText();

        ChallengeRoom room = roomManager.findRoom(roomCode);
        if (room == null) {
            sendError(session, "Room not found.");
            return;
        }

        synchronized (room) {
            if (room.getStatus() != ChallengeRoom.RoomStatus.RUNNING) {
                sendError(session, "Room is not in RUNNING state.");
                return;
            }

            room.setStatus(ChallengeRoom.RoomStatus.FINISHED);

            // Sort players by score descending
            List<ChallengePlayer> sortedPlayers = room.getPlayers().stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()))
                    .collect(Collectors.toList());

            // Determine winner
            String winner = "TIE";
            if (!sortedPlayers.isEmpty()) {
                if (sortedPlayers.size() == 1) {
                    winner = sortedPlayers.get(0).getPlayerName();
                } else {
                    ChallengePlayer p1 = sortedPlayers.get(0);
                    ChallengePlayer p2 = sortedPlayers.get(1);
                    if (p1.getScore() > p2.getScore()) {
                        winner = p1.getPlayerName();
                    } else if (p2.getScore() > p1.getScore()) {
                        winner = p2.getPlayerName();
                    }
                }
            }

            // Build ranking mapping for response (only name and score)
            List<Map<String, Object>> ranking = sortedPlayers.stream()
                    .map(p -> Map.<String, Object>of(
                        "playerName", p.getPlayerName(),
                        "score", p.getScore()
                    ))
                    .collect(Collectors.toList());

            Map<String, Object> response = Map.of(
                "type", "CHALLENGE_RESULT",
                "roomCode", roomCode,
                "winner", winner,
                "ranking", ranking
            );
            String responseJson = objectMapper.writeValueAsString(response);

            List<WebSocketSession> sessions = roomSessions.get(roomCode);
            if (sessions != null) {
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(responseJson));
                    }
                }
            }
        }
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        if (session.isOpen()) {
            Map<String, String> error = Map.of(
                "type", "ERROR",
                "message", message
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("WebSocket connection closed: " + session.getId());
        String roomCode = sessionRoomCodes.remove(session.getId());
        sessionPlayerNames.remove(session.getId());

        if (roomCode != null) {
            List<WebSocketSession> sessions = roomSessions.get(roomCode);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    roomSessions.remove(roomCode);
                    roomManager.removeRoom(roomCode);
                    System.out.println("Room " + roomCode + " removed due to all players disconnecting.");
                }
            }
        }
    }
}
