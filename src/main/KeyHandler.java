package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import ai.ScoreClient;

public class KeyHandler implements KeyListener {
    GamePanel gp;

    public boolean upPressed, downPressed, leftPressed, rightPressed, enterPressed, shotKeyPressed;

    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    public void nameInputState(int code) {
        if (code == KeyEvent.VK_BACK_SPACE) {
            if (gp.inputName.length() > 0) {
                gp.inputName = gp.inputName.substring(0, gp.inputName.length() - 1);
            }
        }

        if (code == KeyEvent.VK_ENTER) {
            String playerName = gp.inputName != null ? gp.inputName.trim() : "";
            if (playerName.isEmpty()) {
                playerName = "Player1";
            }
            gp.playerName = playerName;
            gp.inputName = "";
            gp.highScore = ScoreClient.getHighScore();
            gp.restart();
            gp.saveLoad.invalidateSave();
            gp.gameState = gp.playState;
            gp.playMusic(0);
            gp.ui.titleMessage = "";
        }

        if (code == KeyEvent.VK_ESCAPE) {
            gp.inputName = "";
            gp.gameState = gp.titleState;
        }
    }

    public void challengeComingSoonState(int code) {
        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_ESCAPE) {
            gp.gameState = gp.titleState;
            gp.ui.commandNum = 1;
        }
    }

    public void challengeLobbyState(int code) {
        if (code == KeyEvent.VK_W) {
            gp.ui.commandNum--;
            gp.playSE(9);
            if (gp.ui.commandNum < 0) {
                gp.ui.commandNum = 2;
            }
        }
        if (code == KeyEvent.VK_S) {
            gp.ui.commandNum++;
            gp.playSE(9);
            if (gp.ui.commandNum > 2) {
                gp.ui.commandNum = 0;
            }
        }
        if (code == KeyEvent.VK_ENTER) {
            gp.playSE(9);
            if (gp.ui.commandNum == 0) { // Host
                gp.challengeInputType = 0;
                gp.challengeInputBuffer = "";
                gp.challengeRequestSent = false;
                gp.gameState = gp.challengeInputState;
            } else if (gp.ui.commandNum == 1) { // Join
                gp.challengeInputType = 2;
                gp.challengeInputBuffer = "";
                gp.challengeRequestSent = false;
                gp.gameState = gp.challengeInputState;
            } else if (gp.ui.commandNum == 2) { // Back
                if (gp.challengeM != null) {
                    gp.challengeM.disconnect();
                    gp.challengeM = null;
                }
                gp.gameState = gp.titleState;
                gp.ui.commandNum = 1;
            }
        }
    }

    public void challengeInputState(int code) {
        if (code == KeyEvent.VK_BACK_SPACE) {
            if (gp.challengeInputBuffer.length() > 0) {
                gp.challengeInputBuffer = gp.challengeInputBuffer.substring(0, gp.challengeInputBuffer.length() - 1);
            }
        }
        if (code == KeyEvent.VK_ESCAPE) {
            gp.gameState = gp.challengeLobbyState;
            gp.challengeInputBuffer = "";
        }
        if (code == KeyEvent.VK_ENTER) {
            gp.playSE(9);
            String val = gp.challengeInputBuffer.trim();
            if (gp.challengeInputType == 0) { // Host Name
                if (val.isEmpty())
                    val = "HostPlayer";
                gp.hostPlayerName = val;
                gp.challengeInputType = 1;
                gp.challengeInputBuffer = "";
            } else if (gp.challengeInputType == 1) { // Host Duration
                int duration = 5;
                try {
                    if (!val.isEmpty()) {
                        duration = Integer.parseInt(val);
                    }
                } catch (NumberFormatException e) {
                    duration = 5;
                }
                if (duration <= 0)
                    duration = 5;
                gp.hostDurationMinutes = duration;

                // Connect and transition
                gp.challengeM = new ChallengeManager();
                gp.challengeM.connect();
                gp.gameState = gp.challengeWaitingState;
            } else if (gp.challengeInputType == 2) { // Join Name
                if (val.isEmpty())
                    val = "JoinPlayer";
                gp.joinPlayerName = val;
                gp.challengeInputType = 3;
                gp.challengeInputBuffer = "";
            } else if (gp.challengeInputType == 3) { // Join Room Code
                gp.joinRoomCode = val;

                // Connect and transition
                gp.challengeM = new ChallengeManager();
                gp.challengeM.connect();
                gp.gameState = gp.challengeWaitingState;
            }
        }
    }

    public void challengeWaitingState(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            gp.playSE(9);
            if (gp.challengeM != null) {
                gp.challengeM.disconnect();
                gp.challengeM = null;
            }
            gp.gameState = gp.challengeLobbyState;
            gp.challengeRequestSent = false;
        }
        if (code == KeyEvent.VK_ENTER) {
            if ((gp.challengeInputType == 0 || gp.challengeInputType == 1) &&
                    gp.challengeM != null && gp.challengeM.getOpponentName() != null) {
                gp.playSE(9);
                gp.challengeM.startRoom();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (gp.gameState == gp.nameInputState) {
            char c = e.getKeyChar();
            if ((c >= 32 && c <= 126) && gp.inputName.length() < 16) {
                gp.inputName += c;
            }
        }
        if (gp.gameState == gp.challengeInputState) {
            char c = e.getKeyChar();
            if (gp.challengeInputType == 1 || gp.challengeInputType == 3) {
                if (Character.isDigit(c) && gp.challengeInputBuffer.length() < 6) {
                    gp.challengeInputBuffer += c;
                }
            } else {
                if ((c >= 32 && c <= 126) && gp.challengeInputBuffer.length() < 16) {
                    gp.challengeInputBuffer += c;
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (gp.gameState == gp.titleState) {
            titleState(code);
        }

        else if (gp.gameState == gp.challengeComingSoonState) {
            challengeComingSoonState(code);
        }

        else if (gp.gameState == gp.challengeLobbyState) {
            challengeLobbyState(code);
        }

        else if (gp.gameState == gp.challengeInputState) {
            challengeInputState(code);
        }

        else if (gp.gameState == gp.challengeWaitingState) {
            challengeWaitingState(code);
        }

        else if (gp.gameState == gp.nameInputState) {
            nameInputState(code);
        }

        else if (gp.gameState == gp.playState) {
            playState(code);
        }

        else if (gp.gameState == gp.pauseState) {
            pauseState(code);
        }

        else if (gp.gameState == gp.dialogueState) {
            dialogueState(code);
        }

        else if (gp.gameState == gp.characterState) {
            characterState(code);
        }

        else if (gp.gameState == gp.optionState) {
            optionState(code);
        }

        else if (gp.gameState == gp.gameOverState) {
            gameOverState(code);
        }

        else if (gp.gameState == gp.tradeState) {
            tradeState(code);
        }

        else if (gp.gameState == gp.challengeResultState) {
            challengeResultState(code);
        }
    }

    public void titleState(int code) {
        if (code == KeyEvent.VK_W) {
            gp.ui.commandNum--;
            if (gp.ui.commandNum < 0) {
                gp.ui.commandNum = 3;
            }
            gp.ui.titleMessage = "";
        }

        if (code == KeyEvent.VK_S) {
            gp.ui.commandNum++;
            if (gp.ui.commandNum > 3) {
                gp.ui.commandNum = 0;
            }
            gp.ui.titleMessage = "";
        }

        if (code == KeyEvent.VK_ENTER) {
            if (gp.ui.commandNum == 0) {
                gp.gameMode = GameMode.SINGLE_PLAYER;
                gp.inputName = "";
                gp.gameState = gp.nameInputState;
                gp.ui.titleMessage = "";
            }
            if (gp.ui.commandNum == 1) {
                gp.gameMode = GameMode.CHALLENGE_MODE;
                gp.gameState = gp.challengeLobbyState;
                gp.ui.commandNum = 0;
                gp.ui.titleMessage = "";
            }
            if (gp.ui.commandNum == 2) {
                if (gp.saveLoad.loadGame()) {
                    gp.gameState = gp.playState;
                    gp.playMusic(0);
                    gp.ui.titleMessage = "";
                } else {
                    gp.ui.titleMessage = "No saved game available. Please start a new game.";
                }
            }
            if (gp.ui.commandNum == 3) {
                System.exit(0);
            }
        }

        if (gp.ui.titleScreenState == 0) {

        } else if (gp.ui.titleScreenState == 1) {
            if (code == KeyEvent.VK_W) {
                gp.ui.commandNum--;
                if (gp.ui.commandNum < 0) {
                    gp.ui.commandNum = 3;
                }
            }

            if (code == KeyEvent.VK_S) {
                gp.ui.commandNum++;
                if (gp.ui.commandNum > 3) {
                    gp.ui.commandNum = 0;
                }
            }

            if (code == KeyEvent.VK_ENTER) {
                if (gp.ui.commandNum == 0) {
                    gp.gameState = gp.playState;
                    gp.playMusic(0);
                }
                if (gp.ui.commandNum == 1) {
                    gp.gameState = gp.playState;
                    gp.playMusic(0);
                }
                if (gp.ui.commandNum == 2) {
                    gp.gameState = gp.playState;
                    gp.playMusic(0);
                }
                if (gp.ui.commandNum == 3) {
                    gp.ui.titleScreenState = 0;
                }
            }
        }
    }

    public void playState(int code) {
        if (code == KeyEvent.VK_W) {
            upPressed = true;
        }

        if (code == KeyEvent.VK_S) {
            downPressed = true;
        }

        if (code == KeyEvent.VK_A) {
            leftPressed = true;
        }

        if (code == KeyEvent.VK_D) {
            rightPressed = true;
        }

        if (code == KeyEvent.VK_P) {
            gp.gameState = gp.pauseState;
        }
        if (code == KeyEvent.VK_C) {
            gp.gameState = gp.characterState;
        }
        if (code == KeyEvent.VK_ENTER) {
            enterPressed = true;
        }
        if (code == KeyEvent.VK_F) {
            shotKeyPressed = true;
        }
        if (code == KeyEvent.VK_ESCAPE) {
            gp.gameState = gp.optionState;
        }

        if (code == KeyEvent.VK_R) {
            switch (gp.currentMap) {
                case 0:
                    gp.tileM.loadMap("/res/maps/worldV3.txt", 0);
                    break;
                case 1:
                    gp.tileM.loadMap("/res/maps/interior01.txt", 1);
                    break;
            }
        }
    }

    public void pauseState(int code) {
        if (code == KeyEvent.VK_P) {
            gp.gameState = gp.playState;
        }
    }

    public void dialogueState(int code) {
        if (code == KeyEvent.VK_ENTER) {
            gp.gameState = gp.playState;
        }
    }

    public void characterState(int code) {
        if (code == KeyEvent.VK_C) {
            gp.gameState = gp.playState;
        }

        if (code == KeyEvent.VK_ENTER) {
            gp.player.selectItem();
        }

        playerInventory(code);
    }

    public void optionState(int code) {
        if (code == KeyEvent.VK_ESCAPE) {
            gp.gameState = gp.playState;
        }

        if (code == KeyEvent.VK_ENTER) {
            enterPressed = true;
        }

        int maxCommandNum = 0;

        switch (gp.ui.subState) {
            case 0:
                maxCommandNum = 5;
                break;
            case 3:
                maxCommandNum = 1;
                break;
        }

        if (code == KeyEvent.VK_W) {
            gp.ui.commandNum--;
            gp.playSE(9);

            if (gp.ui.commandNum < 0) {
                gp.ui.commandNum = maxCommandNum;
            }
        }

        if (code == KeyEvent.VK_S) {
            gp.ui.commandNum++;
            gp.playSE(9);

            if (gp.ui.commandNum > maxCommandNum) {
                gp.ui.commandNum = 0;
            }
        }

        if (code == KeyEvent.VK_A) {
            if (gp.ui.subState == 0) {
                if (gp.ui.commandNum == 1 && gp.music.volumeScale > 0) {
                    gp.music.volumeScale--;
                    gp.music.checkVolume();
                    gp.playSE(9);
                }

                if (gp.ui.commandNum == 2 && gp.se.volumeScale > 0) {
                    gp.se.volumeScale--;
                    gp.playSE(9);
                }
            }
        }

        if (code == KeyEvent.VK_D) {
            if (gp.ui.subState == 0) {
                if (gp.ui.commandNum == 1 && gp.music.volumeScale < 5) {
                    gp.music.volumeScale++;
                    gp.music.checkVolume();
                    gp.playSE(9);
                }

                if (gp.ui.commandNum == 2 && gp.se.volumeScale < 5) {
                    gp.se.volumeScale++;
                    gp.playSE(9);
                }
            }
        }
    }

    public void gameOverState(int code) {
        if (code == KeyEvent.VK_ENTER) {
            gp.gameState = gp.titleState;
            gp.restart();
        }
    }

    public void challengeResultState(int code) {
        if (code == KeyEvent.VK_ENTER) {
            gp.playSE(9);
            if (gp.challengeM != null) {
                gp.challengeM.disconnect();
                gp.challengeM = null;
            }
            gp.gameState = gp.titleState;
            gp.ui.commandNum = 0;
        }
    }

    public void tradeState(int code) {
        if (code == KeyEvent.VK_ENTER) {
            enterPressed = true;
        }

        if (gp.ui.subState == 0) {
            if (code == KeyEvent.VK_W) {
                gp.ui.commandNum--;

                if (gp.ui.commandNum < 0) {
                    gp.ui.commandNum = 2;
                }

                gp.playSE(9);
            }

            if (code == KeyEvent.VK_S) {
                gp.ui.commandNum++;

                if (gp.ui.commandNum > 2) {
                    gp.ui.commandNum = 0;
                }

                gp.playSE(9);
            }
        }

        if (gp.ui.subState == 1) {
            npcInventory(code);

            if (code == KeyEvent.VK_ESCAPE) {
                gp.ui.subState = 0;
            }
        }

        if (gp.ui.subState == 2) {
            playerInventory(code);

            if (code == KeyEvent.VK_ESCAPE) {
                gp.ui.subState = 0;
            }
        }
    }

    public void playerInventory(int code) {
        if (code == KeyEvent.VK_W) {
            if (gp.ui.playerSlotRow != 0) {
                gp.ui.playerSlotRow--;
                gp.playSE(9);
            }
        }

        if (code == KeyEvent.VK_A) {
            if (gp.ui.playerSlotCol != 0) {
                gp.ui.playerSlotCol--;
                gp.playSE(9);
            }
        }

        if (code == KeyEvent.VK_S) {
            if (gp.ui.playerSlotRow != 3) {
                gp.ui.playerSlotRow++;
                gp.playSE(9);
            }
        }

        if (code == KeyEvent.VK_D) {
            if (gp.ui.playerSlotCol != 4) {
                gp.ui.playerSlotCol++;
                gp.playSE(9);
            }
        }
    }

    public void npcInventory(int code) {
        if (code == KeyEvent.VK_W) {
            if (gp.ui.npcSlotRow != 0) {
                gp.ui.npcSlotRow--;
                gp.playSE(9);
            }
        }

        if (code == KeyEvent.VK_A) {
            if (gp.ui.npcSlotCol != 0) {
                gp.ui.npcSlotCol--;
                gp.playSE(9);
            }
        }

        if (code == KeyEvent.VK_S) {
            if (gp.ui.npcSlotRow != 3) {
                gp.ui.npcSlotRow++;
                gp.playSE(9);
            }
        }

        if (code == KeyEvent.VK_D) {
            if (gp.ui.npcSlotCol != 4) {
                gp.ui.npcSlotCol++;
                gp.playSE(9);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_W) {
            upPressed = false;
        }

        if (code == KeyEvent.VK_S) {
            downPressed = false;
        }

        if (code == KeyEvent.VK_A) {
            leftPressed = false;
        }

        if (code == KeyEvent.VK_D) {
            rightPressed = false;
        }
        if (code == KeyEvent.VK_F) {
            shotKeyPressed = false;
        }
    }
}
