package com.example.gamebackend.model;

import java.util.ArrayList;
import java.util.List;

public class ChallengeRoom {
    public enum RoomStatus {
        WAITING,
        RUNNING,
        FINISHED
    }

    private String roomCode;
    private String hostName;
    private int duration;
    private RoomStatus status;
    private List<ChallengePlayer> players = new ArrayList<>();

    public ChallengeRoom() {
    }

    public ChallengeRoom(String roomCode, String hostName, int duration) {
        this.roomCode = roomCode;
        this.hostName = hostName;
        this.duration = duration;
        this.status = RoomStatus.WAITING;
        // The host joins automatically when the room is created
        this.players.add(new ChallengePlayer(hostName));
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public List<ChallengePlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<ChallengePlayer> players) {
        this.players = players;
    }
}
