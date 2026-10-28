package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import entity.Entity;
import object.OBJ_Coin_Bronze;
import object.OBJ_Heart;
import object.OBJ_ManaCrystal;

public class UI {
    GamePanel gp;
    Graphics2D g2;
    Font maruMonica;
    BufferedImage heart_full, heart_half, heart_blank, crystal_full, crystal_blank, coin;
    public boolean messageOn = false;
    ArrayList<String> message = new ArrayList<>();
    ArrayList<Integer> messageCounter = new ArrayList<>();
    public boolean gameFinished = false;
    public String currentDialogue = "";
    public String titleMessage = "";
    public int commandNum = 0;
    public int titleScreenState = 0;
    public int playerSlotCol = 0;
    public int playerSlotRow = 0;
    public int npcSlotCol = 0;
    public int npcSlotRow = 0;
    int subState = 0;
    int counter = 0;
    public Entity npc;

    public UI(GamePanel gp) {
        this.gp = gp;

        try {
            InputStream is = getClass().getResourceAsStream("/res/font/x12y16pxMaruMonica.ttf");
            maruMonica = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Entity heart = new OBJ_Heart(gp);
        heart_full = heart.image;
        heart_half = heart.image2;
        heart_blank = heart.image3;

        Entity crystal = new OBJ_ManaCrystal(gp);
        crystal_full = crystal.image;
        crystal_blank = crystal.image2;

        Entity bronzeCoin = new OBJ_Coin_Bronze(gp);
        coin = bronzeCoin.down1;
    }

    public void addMessage(String text) {
        message.add(text);
        messageCounter.add(0);
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;

        g2.setFont(maruMonica);
        g2.setColor(Color.white);

        if (gp.gameState == gp.nameInputState) {
            drawNameInputScreen();
            return;
        }

        if (gp.gameState == gp.challengeComingSoonState) {
            drawChallengeComingSoonScreen();
            return;
        }

        if (gp.gameState == gp.challengeLobbyState) {
            drawChallengeLobbyScreen();
            return;
        }

        if (gp.gameState == gp.challengeInputState) {
            drawChallengeInputScreen();
            return;
        }

        if (gp.gameState == gp.challengeWaitingState) {
            drawChallengeWaitingScreen();
            return;
        }

        if (gp.gameState == gp.challengeDeadState) {
            drawChallengeDeadScreen();
            return;
        }

        if (gp.gameState == gp.challengeEndWaitingState) {
            drawChallengeEndWaitingScreen();
            return;
        }

        if (gp.gameState == gp.challengeResultState) {
            drawChallengeResultScreen();
            return;
        }

        if (gp.gameState == gp.titleState) {
            drawTitleScreen();
        }

        if (gp.gameState == gp.playState) {
            drawPlayerLife();
            drawMessage();
        }

        if (gp.gameState == gp.pauseState) {
            drawPlayerLife();
            drawPauseScreen();
        }

        if (gp.gameState == gp.dialogueState) {
            drawDialogueScreen();
        }

        if (gp.gameState == gp.characterState) {
            drawCharacterScreen();
            drawInventory(gp.player, true);
        }

        if (gp.gameState == gp.optionState) {
            drawOptionScreen();
        }

        if (gp.gameState == gp.gameOverState) {
            drawGameOverScreen();
        }

        if (gp.gameState == gp.transitionState) {
            drawTransition();
        }

        if (gp.gameState == gp.tradeState) {
            drawTradeScreen();
        }
    }

    public void drawNameInputScreen() {
        int width = gp.tileSize * 10;
        int height = gp.tileSize * 4;
        int x = gp.screenWidth / 2 - width / 2;
        int y = gp.screenHeight / 2 - height / 2;

        drawSubWindow(x, y, width, height);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 32F));
        String prompt = "Enter your name";
        int px = getXforCenteredText(prompt);
        int py = y + gp.tileSize;
        g2.setColor(Color.white);
        g2.drawString(prompt, px, py);

        String display = gp.inputName;
        boolean caret = (counter / 30) % 2 == 0;
        if (caret) {
            display += "|";
        }

        int dx = getXforCenteredText(display);
        int dy = py + gp.tileSize;
        g2.drawString(display, dx, dy);

        counter++;
    }

    public void drawPlayerLife() {
        int x = gp.tileSize / 2;
        int y = gp.tileSize / 2;
        int i = 0;

        while (i < gp.player.maxLife / 2) {
            g2.drawImage(heart_blank, x, y, null);
            i++;
            x += gp.tileSize;
        }

        x = gp.tileSize / 2;
        y = gp.tileSize / 2;
        i = 0;

        while (i < gp.player.life) {
            g2.drawImage(heart_half, x, y, null);
            i++;
            if (i < gp.player.life) {
                g2.drawImage(heart_full, x, y, null);
            }
            i++;
            x += gp.tileSize;
        }

        x = (gp.tileSize / 2) - 5;
        y = (int) (gp.tileSize * 1.5);
        i = 0;

        while (i < gp.player.maxMana) {
            g2.drawImage(crystal_blank, x, y, null);
            i++;
            x += 35;
        }

        x = (gp.tileSize / 2) - 5;
        y = (int) (gp.tileSize * 1.5);
        i = 0;

        while (i < gp.player.mana) {
            g2.drawImage(crystal_full, x, y, null);
            i++;
            x += 35;
        }

        if (gp.player.totalScore > gp.highScore) {
            gp.highScore = gp.player.totalScore;
        }

        String scoreText = "Score: " + gp.player.totalScore;
        String highScoreText = "High Score: " + gp.highScore;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30f));
        int textWidth = g2.getFontMetrics().stringWidth(scoreText);
        int textX = gp.screenWidth - textWidth - gp.tileSize / 4;
        int textY = gp.tileSize / 2 + 10;
        int highScoreWidth = g2.getFontMetrics().stringWidth(highScoreText);
        int highScoreX = gp.screenWidth - highScoreWidth - gp.tileSize / 4;
        int highScoreY = textY + 30;

        g2.setColor(Color.black);
        g2.drawString(scoreText, textX + 2, textY + 2);
        g2.setColor(Color.white);
        g2.drawString(scoreText, textX, textY);

        if (gp.gameMode == GameMode.SINGLE_PLAYER) {
            g2.setColor(Color.black);
            g2.drawString(highScoreText, highScoreX + 2, highScoreY + 2);
            g2.setColor(Color.white);
            g2.drawString(highScoreText, highScoreX, highScoreY);
        }

        if (gp.gameMode == GameMode.CHALLENGE_MODE) {
            String timeText = getFormattedChallengeTime();
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32f));
            int timeWidth = g2.getFontMetrics().stringWidth(timeText);
            int timeX = gp.screenWidth / 2 - timeWidth / 2;
            int timeY = gp.tileSize / 2 + 15;
            
            g2.setColor(Color.black);
            g2.drawString(timeText, timeX + 2, timeY + 2);
            g2.setColor(Color.white);
            g2.drawString(timeText, timeX, timeY);

            // Draw opponent info on the left side, under the mana crystal display
            if (gp.challengeM != null) {
                String oppName = gp.challengeM.getOpponentName();
                if (oppName == null) oppName = "Waiting...";
                String oppNameText = "Opponent: " + oppName;
                String oppScoreText = "Opponent Score: " + gp.challengeM.getOpponentScore();

                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 24f));
                int oppX = gp.tileSize / 2;
                int oppY = gp.tileSize * 3;

                g2.setColor(Color.black);
                g2.drawString(oppNameText, oppX + 2, oppY + 2);
                g2.drawString(oppScoreText, oppX + 2, oppY + 28);
                g2.setColor(Color.white);
                g2.drawString(oppNameText, oppX, oppY);
                g2.drawString(oppScoreText, oppX, oppY + 26);
            }
        }
    }

    public void drawMessage() {
        int messageX = gp.tileSize;
        int messageY = gp.tileSize * 4;

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 32f));

        for (int i = 0; i < message.size(); i++) {
            if (message.get(i) != null) {
                g2.setColor(Color.black);
                g2.drawString(message.get(i), messageX + 2, messageY + 2);

                g2.setColor(Color.white);
                g2.drawString(message.get(i), messageX, messageY);

                int counter = messageCounter.get(i) + 1;
                messageCounter.set(i, counter);

                messageY += 50;

                if (messageCounter.get(i) > 180) {
                    message.remove(i);
                    messageCounter.remove(i);
                }
            }
        }
    }

    public void drawTitleScreen() {
        if (titleScreenState == 0) {

            g2.setColor(new Color(0, 0, 0));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 96F));
            String text = "Arena of Chaos";
            int x = getXforCenteredText(text);
            int y = gp.tileSize * 3;

            g2.setColor(Color.gray);
            g2.drawString(text, x + 5, y + 5);

            g2.setColor(Color.white);
            g2.drawString(text, x, y);

            x = gp.screenWidth / 2 - (gp.tileSize * 2) / 2;
            y += gp.tileSize * 2;
            g2.drawImage(gp.player.down1, x, y, gp.tileSize * 2, gp.tileSize * 2, null);

            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48F));

            text = "SINGLE PLAYER";
            x = getXforCenteredText(text);
            y += gp.tileSize * 2.8;
            g2.drawString(text, x, y);
            if (commandNum == 0) {
                g2.drawString(">", x - gp.tileSize, y);
            }

            text = "CHALLENGE MODE";
            x = getXforCenteredText(text);
            y += 40;
            g2.drawString(text, x, y);
            if (commandNum == 1) {
                g2.drawString(">", x - gp.tileSize, y);
            }

            text = "LOAD GAME";
            x = getXforCenteredText(text);
            y += 40;
            g2.drawString(text, x, y);
            if (commandNum == 2) {
                g2.drawString(">", x - gp.tileSize, y);
            }

            text = "QUIT";
            x = getXforCenteredText(text);
            y += 40;
            g2.drawString(text, x, y);
            if (commandNum == 3) {
                g2.drawString(">", x - gp.tileSize, y);
            }

            if (!titleMessage.isEmpty()) {
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28F));
                g2.setColor(Color.yellow);
                int msgX = getXforCenteredText(titleMessage);
                int msgY = y + 45;
                g2.drawString(titleMessage, msgX, msgY);
                g2.setColor(Color.white);
            }
        }

        if (titleScreenState == 1) {
            g2.setColor(Color.white);
            g2.setFont(g2.getFont().deriveFont(42F));

            String text = "Select your class!";
            int x = getXforCenteredText(text);
            int y = gp.tileSize * 3;
            g2.drawString(text, x, y);

            text = "Fighter";
            x = getXforCenteredText(text);
            y += gp.tileSize * 3;
            g2.drawString(text, x, y);
            if (commandNum == 0) {
                g2.drawString(">", x - gp.tileSize, y);
            }

            text = "Thief";
            x = getXforCenteredText(text);
            y += gp.tileSize;
            g2.drawString(text, x, y);
            if (commandNum == 1) {
                g2.drawString(">", x - gp.tileSize, y);
            }

            text = "Sorcerer";
            x = getXforCenteredText(text);
            y += gp.tileSize;
            g2.drawString(text, x, y);
            if (commandNum == 2) {
                g2.drawString(">", x - gp.tileSize, y);
            }

            text = "Back";
            x = getXforCenteredText(text);
            y += gp.tileSize * 2;
            g2.drawString(text, x, y);
            if (commandNum == 3) {
                g2.drawString(">", x - gp.tileSize, y);
            }
        }
    }

    public void drawPauseScreen() {
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 80F));
        String text = "PAUSED";

        int x = getXforCenteredText(text);

        int y = gp.screenHeight / 2;

        g2.drawString(text, x, y);
    }

    public void drawDialogueScreen() {
        int x = gp.tileSize * 3;
        int y = gp.tileSize / 2;
        int width = gp.screenWidth - (gp.tileSize * 6);
        int height = gp.tileSize * 4;

        drawSubWindow(x, y, width, height);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 32F));
        x += gp.tileSize;
        y += gp.tileSize;

        for (String line : currentDialogue.split("\n")) {
            g2.drawString(line, x, y);
            y += 40;
        }
    }

    public void drawCharacterScreen() {
        final int frameX = gp.tileSize * 2;
        final int frameY = gp.tileSize;
        final int frameWidth = gp.tileSize * 5;
        final int frameHeight = gp.tileSize * 10;

        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        g2.setColor(Color.white);
        g2.setFont(g2.getFont().deriveFont(32f));

        int textX = frameX + 20;
        int textY = frameY + gp.tileSize;
        final int lineHeight = 35;

        g2.drawString("Level", textX, textY);
        textY += lineHeight;
        g2.drawString("Life", textX, textY);
        textY += lineHeight;
        g2.drawString("Mana", textX, textY);
        textY += lineHeight;
        g2.drawString("Strength", textX, textY);
        textY += lineHeight;
        g2.drawString("Dexterity", textX, textY);
        textY += lineHeight;
        g2.drawString("Attack", textX, textY);
        textY += lineHeight;
        g2.drawString("Defense", textX, textY);
        textY += lineHeight;
        g2.drawString("Exp", textX, textY);
        textY += lineHeight;
        g2.drawString("Next Level", textX, textY);
        textY += lineHeight;
        g2.drawString("Coin", textX, textY);
        textY += lineHeight + 10;
        g2.drawString("Weapon", textX, textY);
        textY += lineHeight + 15;
        g2.drawString("Shield", textX, textY);
        textY += lineHeight;

        int tailX = (frameX + frameWidth) - 30;
        textY = frameY + gp.tileSize;
        String value;

        value = String.valueOf(gp.player.level);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.life + "/" + gp.player.maxLife);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.mana + "/" + gp.player.maxMana);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.strength);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.dexterity);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.attack);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.defense);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.exp);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.nextLevelExp);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        value = String.valueOf(gp.player.coin);
        textX = getXforAlignToRightText(value, tailX);
        g2.drawString(value, textX, textY);
        textY += lineHeight;

        g2.drawImage(gp.player.currentWeapon.down1, tailX - gp.tileSize, textY - 24, null);
        textY += gp.tileSize;
        g2.drawImage(gp.player.currentShield.down1, tailX - gp.tileSize, textY - 24, null);

    }

    public void drawInventory(Entity entity, boolean cursor) {
        int frameX = 0;
        int frameY = 0;
        int frameWidth = 0;
        int frameHeight = 0;
        int slotCol = 0;
        int slotRow = 0;

        if (entity == gp.player) {
            frameX = gp.tileSize * 12;
            frameY = gp.tileSize;
            frameWidth = gp.tileSize * 6;
            frameHeight = gp.tileSize * 5;
            slotCol = playerSlotCol;
            slotRow = playerSlotRow;
        } else {
            frameX = gp.tileSize * 2;
            frameY = gp.tileSize;
            frameWidth = gp.tileSize * 6;
            frameHeight = gp.tileSize * 5;
            slotCol = npcSlotCol;
            slotRow = npcSlotRow;
        }

        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        final int slotXstart = frameX + 20;
        final int slotYstart = frameY + 20;
        int slotX = slotXstart;
        int slotY = slotYstart;
        int slotSize = gp.tileSize + 3;

        for (int i = 0; i < entity.inventory.size(); i++) {
            if (entity.inventory.get(i) == entity.currentWeapon
                    || entity.inventory.get(i) == entity.currentShield) {
                g2.setColor(new Color(240, 190, 90));
                g2.fillRoundRect(slotX, slotY, gp.tileSize, gp.tileSize, 10, 10);
            }

            g2.drawImage(entity.inventory.get(i).down1, slotX, slotY, null);
            slotX += slotSize;

            if (i == 4 || i == 9 || i == 14) {
                slotX = slotXstart;
                slotY += slotSize;
            }
        }

        if (cursor == true) {
            int cursorX = slotXstart + (slotSize * slotCol);
            int cursorY = slotYstart + (slotSize * slotRow);
            int cursorWidth = gp.tileSize;
            int cursorHeight = gp.tileSize;

            g2.setColor(Color.white);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 10, 10);

            int dFrameX = frameX;
            int dFrameY = frameY + frameHeight;
            int dFrameWidth = frameWidth;
            int dFrameHeight = gp.tileSize * 3;

            int textX = dFrameX + 20;
            int textY = dFrameY + gp.tileSize;
            g2.setFont(g2.getFont().deriveFont(28f));

            int itemIndex = getItemIndexOnSlot(slotCol, slotRow);

            if (itemIndex < entity.inventory.size()) {
                drawSubWindow(dFrameX, dFrameY, dFrameWidth, dFrameHeight);

                for (String line : entity.inventory.get(itemIndex).description.split("\n")) {
                    g2.drawString(line, textX, textY);
                    textY += 32;
                }
            }
        }
    }

    public void drawGameOverScreen() {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        int x;
        int y;
        String text;
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 110f));

        text = "Game Over";

        g2.setColor(Color.black);
        x = getXforCenteredText(text);
        y = gp.tileSize * 4;
        g2.drawString(text, x, y);

        g2.setColor(Color.white);
        g2.drawString(text, x - 4, y - 4);

        g2.setFont(g2.getFont().deriveFont(50f));
        text = "Quit";
        x = getXforCenteredText(text);
        y += gp.tileSize * 4;
        g2.drawString(text, x, y);

        g2.drawString(">", x - 40, y);
    }

    public void drawOptionScreen() {
        g2.setColor(Color.white);
        g2.setFont(g2.getFont().deriveFont(32f));

        int frameX = gp.tileSize * 6;
        int frameY = gp.tileSize;
        int frameWidth = gp.tileSize * 8;
        int frameHeight = gp.tileSize * 10;
        drawSubWindow(frameX, frameY, frameWidth, frameHeight);

        switch (subState) {
            case 0:
                options_top(frameX, frameY);
                break;
            case 1:
                options_fullScreenNotification(frameX, frameY);
                break;
            case 2:
                options_control(frameX, frameY);
                break;
            case 3:
                options_endGameConfirmation(frameX, frameY);
                break;
        }

        gp.keyH.enterPressed = false;
    }

    public void options_top(int frameX, int frameY) {
        int textX;
        int textY;

        String text = "Options";
        textX = getXforCenteredText(text);
        textY = frameY + gp.tileSize;
        g2.drawString(text, textX, textY);

        textX = frameX + gp.tileSize;
        textY += gp.tileSize * 2;
        g2.drawString("Full Screen", textX, textY);
        if (commandNum == 0) {
            g2.drawString(">", textX - 25, textY);

            if (gp.keyH.enterPressed == true) {
                if (gp.fullScreenOn == false) {
                    gp.fullScreenOn = true;
                } else if (gp.fullScreenOn = true) {
                    gp.fullScreenOn = false;
                }
                subState = 1;
            }
        }

        textY += gp.tileSize;
        g2.drawString("Music", textX, textY);
        if (commandNum == 1) {
            g2.drawString(">", textX - 25, textY);
        }

        textY += gp.tileSize;
        g2.drawString("SE", textX, textY);
        if (commandNum == 2) {
            g2.drawString(">", textX - 25, textY);
        }

        textY += gp.tileSize;
        g2.drawString("Control", textX, textY);
        if (commandNum == 3) {
            g2.drawString(">", textX - 25, textY);

            if (gp.keyH.enterPressed == true) {
                subState = 2;
                commandNum = 0;
            }
        }

        textY += gp.tileSize;
        g2.drawString("End Game", textX, textY);
        if (commandNum == 4) {
            g2.drawString(">", textX - 25, textY);

            if (gp.keyH.enterPressed == true) {
                subState = 3;
                commandNum = 0;
            }
        }

        textY += gp.tileSize * 2;
        g2.drawString("Back", textX, textY);
        if (commandNum == 5) {
            g2.drawString(">", textX - 25, textY);

            if (gp.keyH.enterPressed == true) {
                gp.gameState = gp.playState;
                commandNum = 0;
            }
        }

        textX = frameX + (int) (gp.tileSize * 4.5);
        textY = frameY + gp.tileSize * 2 + 24;
        g2.setStroke(new BasicStroke(3));
        g2.drawRect(textX, textY, 24, 24);

        if (gp.fullScreenOn == true) {
            g2.fillRect(textX, textY, 24, 24);
        }

        textY += gp.tileSize;
        g2.drawRect(textX, textY, 120, 24);
        int volumeWidth = 24 * gp.music.volumeScale;
        g2.fillRect(textX, textY, volumeWidth, 24);

        textY += gp.tileSize;
        g2.drawRect(textX, textY, 120, 24);
        volumeWidth = 24 * gp.se.volumeScale;
        g2.fillRect(textX, textY, volumeWidth, 24);

        gp.config.saveConfig();
    }

    public void options_fullScreenNotification(int frameX, int frameY) {
        int textX = frameX + gp.tileSize;
        int textY = frameY + gp.tileSize * 3;

        currentDialogue = "The change will take \neffect after restarting \nthe game.";

        for (String line : currentDialogue.split("\n")) {
            g2.drawString(line, textX, textY);
            textY += 40;
        }

        textY = frameY + gp.tileSize * 9;
        g2.drawString("Back", textX, textY);

        if (commandNum == 0) {
            g2.drawString(">", textX - 25, textY);

            if (gp.keyH.enterPressed == true) {
                subState = 0;
            }
        }
    }

    public void options_control(int frameX, int frameY) {
        int textX;
        int textY;

        String text = "Control";
        textX = getXforCenteredText(text);
        textY = frameY + gp.tileSize;
        g2.drawString(text, textX, textY);

        textX = frameX + gp.tileSize;
        textY += gp.tileSize;

        g2.drawString("Move", textX, textY);
        textY += gp.tileSize;
        g2.drawString("Confirm/Attack", textX, textY);
        textY += gp.tileSize;
        g2.drawString("Shoot/Cast", textX, textY);
        textY += gp.tileSize;
        g2.drawString("Character Screen", textX, textY);
        textY += gp.tileSize;
        g2.drawString("Pause", textX, textY);
        textY += gp.tileSize;
        g2.drawString("Options", textX, textY);
        textY += gp.tileSize;

        textX = frameX + gp.tileSize * 6;
        textY = frameY + gp.tileSize * 2;

        g2.drawString("WASD", textX, textY);
        textY += gp.tileSize;
        g2.drawString("ENTER", textX, textY);
        textY += gp.tileSize;
        g2.drawString("F", textX, textY);
        textY += gp.tileSize;
        g2.drawString("C", textX, textY);
        textY += gp.tileSize;
        g2.drawString("P", textX, textY);
        textY += gp.tileSize;
        g2.drawString("ESC", textX, textY);
        textY += gp.tileSize;

        textX = frameX + gp.tileSize;
        textY = frameY + gp.tileSize * 9;
        g2.drawString("Back", textX, textY);

        if (commandNum == 0) {
            g2.drawString(">", textX - 25, textY);

            if (gp.keyH.enterPressed == true) {
                subState = 0;
                commandNum = 3;
            }
        }
    }

    public void options_endGameConfirmation(int frameX, int frameY) {
        int textX = frameX + gp.tileSize;
        int textY = frameY + gp.tileSize * 3;

        currentDialogue = "Quit the game and \nreturn to the title screen?";

        for (String line : currentDialogue.split("\n")) {
            g2.drawString(line, textX, textY);
            textY += 40;
        }

        String text = "Yes";
        textX = getXforCenteredText(text);
        textY += gp.tileSize * 3;
        g2.drawString(text, textX, textY);

        if (commandNum == 0) {
            g2.drawString(">", textX - 25, textY);

            if (gp.keyH.enterPressed) {
                subState = 0;
                gp.saveLoad.saveGame();
                gp.ui.titleMessage = "Game saved.";
                gp.gameState = gp.titleState;
            }
        }

        text = "No";
        textX = getXforCenteredText(text);
        textY += gp.tileSize;
        g2.drawString(text, textX, textY);

        if (commandNum == 1) {
            g2.drawString(">", textX - 25, textY);

            if (gp.keyH.enterPressed) {
                subState = 0;
                commandNum = 4;
            }
        }
    }

    public void drawTransition() {
        counter++;
        int alpha = counter * 5;
        if (alpha > 255) {
            alpha = 255;
        }
        g2.setColor(new Color(0, 0, 0, alpha));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        if (counter >= 50) {
            counter = 0;

            gp.gameState = gp.playState;
            gp.currentMap = gp.eHandler.tempMap;

            gp.player.worldX = gp.tileSize * gp.eHandler.tempCol;
            gp.player.worldY = gp.tileSize * gp.eHandler.tempRow;

            gp.eHandler.previousEventX = gp.player.worldX;
            gp.eHandler.previousEventY = gp.player.worldY;
        }
    }

    public void drawTradeScreen() {
        switch (subState) {
            case 0:
                trade_select();
                break;
            case 1:
                trade_buy();
                break;
            case 2:
                trade_sell();
                break;
        }

        gp.keyH.enterPressed = false;
    }

    public void trade_select() {
        drawDialogueScreen();

        int x = gp.tileSize * 15;
        int y = gp.tileSize * 4;
        int width = gp.tileSize * 3;
        int height = (int) (gp.tileSize * 3.5);

        drawSubWindow(x, y, width, height);

        x += gp.tileSize;
        y += gp.tileSize;
        g2.drawString("Buy", x, y);

        if (commandNum == 0) {
            g2.drawString(">", x - 24, y);

            if (gp.keyH.enterPressed == true) {
                subState = 1;
            }
        }

        y += gp.tileSize;

        g2.drawString("Sell", x, y);

        if (commandNum == 1) {
            g2.drawString(">", x - 24, y);

            if (gp.keyH.enterPressed == true) {
                subState = 2;
            }
        }

        y += gp.tileSize;

        g2.drawString("Leave", x, y);

        if (commandNum == 2) {
            g2.drawString(">", x - 24, y);

            if (gp.keyH.enterPressed == true) {
                commandNum = 0;
                gp.gameState = gp.dialogueState;
                currentDialogue = "Come again, hehe!";
            }
        }
    }

    public void trade_buy() {
        drawInventory(gp.player, false);

        drawInventory(npc, true);

        int x = gp.tileSize * 2;
        int y = gp.tileSize * 9;
        int width = gp.tileSize * 6;
        int height = gp.tileSize * 2;

        drawSubWindow(x, y, width, height);
        g2.drawString("[ESC] Back", x + 24, y + 60);

        x = gp.tileSize * 12;
        y = gp.tileSize * 9;
        width = gp.tileSize * 6;
        height = gp.tileSize * 2;

        drawSubWindow(x, y, width, height);
        g2.drawString("Your Coin: " + gp.player.coin, x + 24, y + 60);

        int itemIndex = getItemIndexOnSlot(npcSlotCol, npcSlotRow);
        if (itemIndex < npc.inventory.size()) {
            x = (int) (gp.tileSize * 5.5);
            y = (int) (gp.tileSize * 5.5);
            width = (int) (gp.tileSize * 2.5);
            height = gp.tileSize;

            drawSubWindow(x, y, width, height);
            g2.drawImage(coin, x + 10, y + 8, 32, 32, null);

            int price = npc.inventory.get(itemIndex).price;
            String text = "" + price;
            x = getXforAlignToRightText(text, gp.tileSize * 8 - 20);
            g2.drawString(text, x, y + 34);

            if (gp.keyH.enterPressed == true) {
                if (npc.inventory.get(itemIndex).price > gp.player.coin) {
                    subState = 0;
                    gp.gameState = gp.dialogueState;
                    currentDialogue = "You need more coin to buy that!";
                    drawDialogueScreen();
                } else if (gp.player.inventory.size() == gp.player.maxInventorySize) {
                    subState = 0;
                    gp.gameState = gp.dialogueState;
                    currentDialogue = "Your inventory is full!";
                } else {
                    gp.player.coin -= npc.inventory.get(itemIndex).price;
                    gp.player.inventory.add(npc.inventory.get(itemIndex));
                }
            }
        }
    }

    public void trade_sell() {
        drawInventory(gp.player, true);

        int x;
        int y;
        int width;
        int height;

        x = gp.tileSize * 2;
        y = gp.tileSize * 9;
        width = gp.tileSize * 6;
        height = gp.tileSize * 2;

        drawSubWindow(x, y, width, height);
        g2.drawString("[ESC] Back", x + 24, y + 60);

        x = gp.tileSize * 12;
        y = gp.tileSize * 9;
        width = gp.tileSize * 6;
        height = gp.tileSize * 2;

        drawSubWindow(x, y, width, height);
        g2.drawString("Your Coin: " + gp.player.coin, x + 24, y + 60);

        int itemIndex = getItemIndexOnSlot(playerSlotCol, playerSlotRow);
        if (itemIndex < gp.player.inventory.size()) {
            x = (int) (gp.tileSize * 15.5);
            y = (int) (gp.tileSize * 5.5);
            width = (int) (gp.tileSize * 2.5);
            height = gp.tileSize;

            drawSubWindow(x, y, width, height);
            g2.drawImage(coin, x + 10, y + 8, 32, 32, null);

            int price = gp.player.inventory.get(itemIndex).price / 2;
            String text = "" + price;
            x = getXforAlignToRightText(text, gp.tileSize * 18 - 20);
            g2.drawString(text, x, y + 34);

            if (gp.keyH.enterPressed == true) {
                if (gp.player.inventory.get(itemIndex) == gp.player.currentWeapon
                        || gp.player.inventory.get(itemIndex) == gp.player.currentShield) {
                    commandNum = 0;
                    subState = 0;
                    gp.gameState = gp.dialogueState;
                    currentDialogue = "You cannot sell an equipped item!";
                } else {
                    gp.player.inventory.remove(itemIndex);
                    gp.player.coin += price;
                }
            }
        }
    }

    public int getItemIndexOnSlot(int slotCol, int slotRow) {
        int itemIndex = slotCol + (slotRow * 5);

        return itemIndex;
    }

    public void drawSubWindow(int x, int y, int width, int height) {
        Color c = new Color(0, 0, 0, 210);
        g2.setColor(c);
        g2.fillRoundRect(x, y, width, height, 35, 35);

        c = new Color(255, 255, 255);
        g2.setColor(c);
        g2.setStroke(new BasicStroke(5));
        g2.drawRoundRect(x + 5, y + 5, width - 10, height - 10, 25, 25);
    }

    public int getXforCenteredText(String text) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = gp.screenWidth / 2 - length / 2;

        return x;
    }

    public int getXforAlignToRightText(String text, int tailX) {
        int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        int x = tailX - length;

        return x;
    }

    public void drawChallengeComingSoonScreen() {
        // Background
        g2.setColor(Color.black);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Header Title
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 64F));
        String text = "CHALLENGE MODE";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 4;

        g2.setColor(Color.gray);
        g2.drawString(text, x + 3, y + 3);
        g2.setColor(Color.white);
        g2.drawString(text, x, y);

        // Status Subtext
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 36F));
        text = "Coming Soon!";
        x = getXforCenteredText(text);
        y += gp.tileSize * 2;
        g2.setColor(Color.yellow);
        g2.drawString(text, x, y);

        // Instruction Text
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 24F));
        text = "Press ENTER or ESC to return to Title Screen";
        x = getXforCenteredText(text);
        y += gp.tileSize * 3;
        g2.setColor(Color.white);
        g2.drawString(text, x, y);
    }

    public void drawChallengeLobbyScreen() {
        // Background
        g2.setColor(Color.black);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Header Title
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 64F));
        String text = "CHALLENGE LOBBY";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 3;

        g2.setColor(Color.gray);
        g2.drawString(text, x + 3, y + 3);
        g2.setColor(Color.white);
        g2.drawString(text, x, y);

        // Options menu
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 36F));
        
        text = "HOST CHALLENGE";
        x = getXforCenteredText(text);
        y += gp.tileSize * 2.5;
        g2.drawString(text, x, y);
        if (commandNum == 0) {
            g2.drawString(">", x - gp.tileSize, y);
        }

        text = "JOIN CHALLENGE";
        x = getXforCenteredText(text);
        y += 60;
        g2.drawString(text, x, y);
        if (commandNum == 1) {
            g2.drawString(">", x - gp.tileSize, y);
        }

        text = "BACK";
        x = getXforCenteredText(text);
        y += 60;
        g2.drawString(text, x, y);
        if (commandNum == 2) {
            g2.drawString(">", x - gp.tileSize, y);
        }
    }

    public void drawChallengeInputScreen() {
        // Background
        g2.setColor(Color.black);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Sub-window frame
        int width = gp.tileSize * 12;
        int height = gp.tileSize * 5;
        int x = gp.screenWidth / 2 - width / 2;
        int y = gp.screenHeight / 2 - height / 2;
        drawSubWindow(x, y, width, height);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 36F));
        g2.setColor(Color.white);

        String prompt = "";
        if (gp.challengeInputType == 0) {
            prompt = "Enter Host Player Name:";
        } else if (gp.challengeInputType == 1) {
            prompt = "Enter Duration (minutes):";
        } else if (gp.challengeInputType == 2) {
            prompt = "Enter Your Player Name:";
        } else if (gp.challengeInputType == 3) {
            prompt = "Enter Room Code (6 digits):";
        }

        int px = getXforCenteredText(prompt);
        int py = (int) (y + gp.tileSize * 1.5);
        g2.drawString(prompt, px, py);

        // Render input buffer
        String display = gp.challengeInputBuffer;
        boolean caret = (counter / 30) % 2 == 0;
        if (caret) {
            display += "|";
        }

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 32F));
        g2.setColor(Color.yellow);
        int dx = getXforCenteredText(display);
        int dy = (int) (py + gp.tileSize * 1.5);
        g2.drawString(display, dx, dy);

        // Subtext controls helper
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F));
        g2.setColor(Color.white);
        String helper = "[ENTER] Confirm   [ESC] Cancel";
        int hx = getXforCenteredText(helper);
        int hy = y + height - gp.tileSize / 2;
        g2.drawString(helper, hx, hy);

        counter++;
    }

    public void drawChallengeWaitingScreen() {
        // Background
        g2.setColor(Color.black);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Frame
        int width = gp.tileSize * 14;
        int height = gp.tileSize * 8;
        int x = gp.screenWidth / 2 - width / 2;
        int y = gp.screenHeight / 2 - height / 2;
        drawSubWindow(x, y, width, height);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 40F));
        g2.setColor(Color.white);

        String title = "CHALLENGE ROOM";
        int tx = getXforCenteredText(title);
        int ty = (int) (y + gp.tileSize * 1.2);
        g2.drawString(title, tx, ty);

        // Connection Status
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 20F));
        String status = "Connecting to backend...";
        if (gp.challengeM != null) {
            if (gp.challengeM.getErrorMessage() != null) {
                status = "Error: " + gp.challengeM.getErrorMessage();
                g2.setColor(Color.red);
            } else if (gp.challengeM.isConnected()) {
                status = "Connected to Server";
                g2.setColor(Color.green);
            } else {
                status = "Failed to connect. Offline?";
                g2.setColor(Color.red);
            }
        }
        int sx = getXforCenteredText(status);
        int sy = ty + 35;
        g2.drawString(status, sx, sy);
        g2.setColor(Color.white);

        // Room Info
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 30F));
        String codeStr = "Room Code: ";
        if (gp.challengeM != null && gp.challengeM.getRoomCode() != null) {
            codeStr += gp.challengeM.getRoomCode();
        } else {
            codeStr += "Generating...";
        }
        int cx = getXforCenteredText(codeStr);
        int cy = (int) (sy + gp.tileSize * 1.2);
        g2.drawString(codeStr, cx, cy);

        // Players List
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28F));
        int px = x + gp.tileSize * 2;
        int py = cy + gp.tileSize;

        g2.drawString("Players Joined:", px, py);
        py += 35;

        // List of players
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 26F));
        if (gp.challengeM != null) {
            String p1 = gp.challengeM.getPlayerName();
            String p2 = gp.challengeM.getOpponentName();
            
            if (gp.challengeInputType == 0 || gp.challengeInputType == 1) { // Host
                g2.drawString("1. " + (p1 != null ? p1 : "") + " (Host)", px + 20, py);
                py += 35;
                g2.drawString("2. " + (p2 != null ? p2 : "Waiting..."), px + 20, py);
            } else { // Joiner
                g2.drawString("1. " + (p2 != null ? p2 : "Waiting... ") + " (Host)", px + 20, py);
                py += 35;
                g2.drawString("2. " + (p1 != null ? p1 : "") + " (You)", px + 20, py);
            }
        }

        // Press ESC to go back subtext
        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 18F));
        g2.setColor(Color.white);
        String escHelper = "[ESC] Leave Room and Return to Lobby";
        int ex = getXforCenteredText(escHelper);
        int ey = y + height - gp.tileSize / 2;
        g2.drawString(escHelper, ex, ey);
    }

    private String getFormattedChallengeTime() {
        if (gp.challengeM == null) return "00:00";
        int totalSeconds = gp.challengeM.getTimeLeft();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("Time Left: %02d:%02d", minutes, seconds);
    }

    public void drawChallengeDeadScreen() {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 64F));
        g2.setColor(Color.red);
        String text = "YOU DIED";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 4;
        g2.drawString(text, x, y);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28F));
        g2.setColor(Color.white);
        text = "Waiting for challenge timer to finish...";
        x = getXforCenteredText(text);
        y += gp.tileSize * 2;
        g2.drawString(text, x, y);

        text = "Your Score: " + gp.player.totalScore;
        x = getXforCenteredText(text);
        y += gp.tileSize * 1.5;
        g2.setColor(Color.yellow);
        g2.drawString(text, x, y);

        // Draw timer
        String timeText = getFormattedChallengeTime();
        x = getXforCenteredText(timeText);
        y += gp.tileSize * 1.2;
        g2.setColor(Color.white);
        g2.drawString(timeText, x, y);
    }

    public void drawChallengeEndWaitingScreen() {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48F));
        g2.setColor(Color.yellow);
        String text = "TIME FINISHED";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 4;
        g2.drawString(text, x, y);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28F));
        g2.setColor(Color.white);
        text = "Waiting for challenge results from server...";
        x = getXforCenteredText(text);
        y += gp.tileSize * 2;
        g2.drawString(text, x, y);
    }

    public void drawChallengeResultScreen() {
        g2.setColor(Color.black);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 64F));
        g2.setColor(Color.yellow);
        String text = "CHALLENGE FINISHED";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 3;
        g2.drawString(text, x, y);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 36F));
        g2.setColor(Color.white);
        String winner = gp.challengeM != null ? gp.challengeM.getWinnerName() : "TIE";
        String winnerText = "Winner: " + winner;
        x = getXforCenteredText(winnerText);
        y += gp.tileSize * 1.5;
        g2.drawString(winnerText, x, y);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 28F));
        y += gp.tileSize;
        
        if (gp.challengeM != null && gp.challengeM.getFinalRanking() != null) {
            java.util.List<ChallengeManager.PlayerRank> ranking = gp.challengeM.getFinalRanking();
            for (int i = 0; i < ranking.size(); i++) {
                ChallengeManager.PlayerRank rank = ranking.get(i);
                String rankText = (i + 1) + ". " + rank.playerName + " - Score: " + rank.score;
                x = getXforCenteredText(rankText);
                g2.drawString(rankText, x, y);
                y += 40;
            }
        }

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 24F));
        g2.setColor(Color.gray);
        String helperText = "Press ENTER to return to Title Screen";
        x = getXforCenteredText(helperText);
        y = gp.screenHeight - gp.tileSize * 2;
        g2.drawString(helperText, x, y);
    }
}
