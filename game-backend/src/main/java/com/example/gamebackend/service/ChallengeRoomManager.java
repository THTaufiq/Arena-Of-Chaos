package com.example.gamebackend.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.gamebackend.model.ChallengePlayer;
import com.example.gamebackend.model.ChallengeRoom;

@Service
public class ChallengeRoomManager {
    private final Map<String, ChallengeRoom> rooms = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public ChallengeRoom createRoom(String hostName, int duration) {
        String roomCode;
        // Generate a unique 6-digit numeric room code
        do {
            roomCode = String.format("%06d", random.nextInt(1000000));
        } while (rooms.containsKey(roomCode));

        ChallengeRoom room = new ChallengeRoom(roomCode, hostName, duration);
        rooms.put(roomCode, room);
        return room;
    }

    public ChallengeRoom joinRoom(String roomCode, String playerName) {
        ChallengeRoom room = rooms.get(roomCode);
        if (room == null) {
            return null; // Room not found
        }
        
        synchronized (room) {
            if (room.getStatus() != ChallengeRoom.RoomStatus.WAITING) {
                return null; // Can't join a game that is already running or finished
            }
            if (room.getPlayers().size() >= 2) {
                return null; // Room is full (Max players = 2)
            }
            
            // Check if player with same name already exists in room
            boolean exists = room.getPlayers().stream()
                    .anyMatch(p -> p.getPlayerName().equalsIgnoreCase(playerName));
            if (!exists) {
                room.getPlayers().add(new ChallengePlayer(playerName));
            }
            return room;
        }
    }

    public ChallengeRoom findRoom(String roomCode) {
        return rooms.get(roomCode);
    }

    public ChallengeRoom removeRoom(String roomCode) {
        return rooms.remove(roomCode);
    }
}
