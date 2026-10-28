package com.example.gamebackend.model;

public class ChallengePlayer {
    private String playerName;
    private int score;
    private int life;
    private int enemyKilled;

    public ChallengePlayer() {
    }

    public ChallengePlayer(String playerName) {
        this.playerName = playerName;
        this.score = 0;
        this.life = 6; // Default starting life matching client default
        this.enemyKilled = 0;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    public int getEnemyKilled() {
        return enemyKilled;
    }

    public void setEnemyKilled(int enemyKilled) {
        this.enemyKilled = enemyKilled;
    }
}
