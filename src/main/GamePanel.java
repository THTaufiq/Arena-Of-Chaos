package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JPanel;

import ai.PathFinder;
import entity.Entity;
import entity.Player;
import tile.TileManager;
import tile_interactive.InteractiveTile;

public class GamePanel extends JPanel implements Runnable {
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 20;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;
    public final int maxMap = 10;
    public int currentMap = 0;

    int screenWidth2 = screenWidth;
    int screenHeight2 = screenHeight;
    BufferedImage tempScreen;
    Graphics2D g2;
    public boolean fullScreenOn = false;

    int FPS = 60;

    public TileManager tileM = new TileManager(this);
    public KeyHandler keyH = new KeyHandler(this);
    Sound music = new Sound();
    Sound se = new Sound();
    public CollisionChecker cChecker = new CollisionChecker(this);
    public AssetSetter aSetter = new AssetSetter(this);
    public UI ui = new UI(this);
    public EventHandler eHandler = new EventHandler(this);
    Config config = new Config(this);
    public PathFinder pFinder = new PathFinder(this);
    public SaveLoad saveLoad = new SaveLoad(this);
    Thread gameThread;

    public Player player = new Player(this, keyH);
    public String playerName = "Player1";
    public String inputName = "";
    public int highScore = 0;
    public Entity obj[][] = new Entity[maxMap][20];
    public Entity npc[][] = new Entity[maxMap][10];
    public Entity monster[][] = new Entity[maxMap][20];
    public InteractiveTile iTile[][] = new InteractiveTile[maxMap][50];
    public Entity projectile[][] = new Entity[maxMap][20];
    public ArrayList<Entity> particleList = new ArrayList<>();
    ArrayList<Entity> entityList = new ArrayList<>();

    public boolean zombieWaveSpawned = false;

    public GameMode gameMode = GameMode.SINGLE_PLAYER;
    public int gameState;
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int dialogueState = 3;
    public final int characterState = 4;
    public final int optionState = 5;
    public final int gameOverState = 6;
    public final int transitionState = 7;
    public final int tradeState = 8;
    public final int nameInputState = 9;
    public final int challengeComingSoonState = 10;
    public final int challengeLobbyState = 11;
    public final int challengeInputState = 12;
    public final int challengeWaitingState = 13;
    public final int challengeDeadState = 14;
    public final int challengeEndWaitingState = 15;
    public final int challengeResultState = 16;

    public ChallengeManager challengeM = null;
    public String challengeInputBuffer = "";
    public int challengeInputType = 0; // 0: Host Name, 1: Host Duration, 2: Join Name, 3: Join Room Code
    public String hostPlayerName = "";
    public String joinPlayerName = "";
    public int hostDurationMinutes = 5;
    public String joinRoomCode = "";
    public boolean challengeRequestSent = false;
    public int challengeTimerCounter = 0;
    public boolean challengeEndSent = false;
    public int enemySpawnCounter = 0;
    public final int enemySpawnInterval = 240; // 4 seconds at 60 FPS

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));

        this.setBackground(Color.black);

        this.setDoubleBuffered(true);

        this.addKeyListener(keyH);

        this.setFocusable(true);
    }

    public void setupGame() {
        aSetter.setObject();
        aSetter.setNPC();
        aSetter.setMonster();
        aSetter.setInteractiveTile();
        gameState = titleState;

        tempScreen = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        g2 = (Graphics2D) tempScreen.getGraphics();

        if (fullScreenOn == true) {
            setFullScreen();
        }
    }

    public void retry() {
        player.setDefaultPositions();
        player.restoreLifeAndMana();
        aSetter.setNPC();
        aSetter.setMonster();

        zombieWaveSpawned = false;
        enemySpawnCounter = 0;
    }

    public void restart() {
        player.setDefaultValues();
        player.setDefaultPositions();
        player.restoreLifeAndMana();
        player.setItems();
        aSetter.setObject();
        aSetter.setNPC();
        aSetter.setMonster();
        aSetter.setInteractiveTile();

        zombieWaveSpawned = false;
        challengeTimerCounter = 0;
        challengeEndSent = false;
        enemySpawnCounter = 0;
    }

    public void setFullScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        gd.setFullScreenWindow(Main.window);

        screenWidth2 = Main.window.getWidth();
        screenHeight2 = Main.window.getHeight();
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {

        double drawInterval = 1_000_000_000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while (gameThread != null) {

            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if (delta >= 1) {
                update();

                drawToTempScreen();
                drawToScreen();

                delta--;
                drawCount++;
            }

            if (timer >= 1_000_000_000) {
                drawCount = 0;
                timer = 0;
            }
        }
    }

    public void update() {
        if (gameState == playState) {
            if (zombieWaveSpawned == false && aSetter.areAllGreenSlimesDefeated()) {
                aSetter.setZombieWave();
                zombieWaveSpawned = true;
                ui.addMessage("Zombie wave incoming!");
            }

            if (zombieWaveSpawned == true && currentMap == 0) {
                boolean stopSpawning = false;
                if (gameMode == GameMode.CHALLENGE_MODE) {
                    if (challengeM == null || !challengeM.isChallengeStarted() || challengeM.getTimeLeft() <= 0) {
                        stopSpawning = true;
                    }
                }
                if (player.life <= 0) {
                    stopSpawning = true;
                }
                if (!stopSpawning) {
                    enemySpawnCounter++;
                    if (enemySpawnCounter >= enemySpawnInterval) {
                        enemySpawnCounter = 0;
                        aSetter.spawnContinuousEnemy();
                    }
                }
            }

            player.update();

            for (int i = 0; i < npc[1].length; i++) {
                if (npc[currentMap][i] != null) {
                    npc[currentMap][i].update();
                }
            }

            for (int i = 0; i < monster[1].length; i++) {
                if (monster[currentMap][i] != null) {
                    if (monster[currentMap][i].alive == true && monster[currentMap][i].dying == false) {
                        monster[currentMap][i].update();
                    }
                    if (monster[currentMap][i].dying == true) {
                        monster[currentMap][i].updateDying();
                    }
                    if (monster[currentMap][i].alive == false) {
                        monster[currentMap][i].checkDrop();
                        monster[currentMap][i] = null;
                    }
                }
            }

            for (int i = 0; i < projectile[1].length; i++) {
                if (projectile[currentMap][i] != null) {
                    if (projectile[currentMap][i].alive == true) {
                        projectile[currentMap][i].update();
                    }
                    if (projectile[currentMap][i].alive == false) {
                        projectile[currentMap][i] = null;
                    }
                }
            }

            for (int i = 0; i < particleList.size(); i++) {
                if (particleList.get(i) != null) {
                    if (particleList.get(i).alive == true) {
                        particleList.get(i).update();
                    }
                    if (particleList.get(i).alive == false) {
                        particleList.remove(i);
                    }
                }
            }

            for (int i = 0; i < iTile[1].length; i++) {
                if (iTile[currentMap][i] != null) {
                    iTile[currentMap][i].update();
                }
            }
        }

        if (gameState == pauseState) {
        }

        if (gameState == challengeWaitingState) {
            if (challengeM != null && challengeM.isConnected() && !challengeRequestSent) {
                challengeRequestSent = true;
                challengeM.clearErrorMessage();
                if (challengeInputType == 1) { // Host
                    challengeM.createRoom(hostPlayerName, hostDurationMinutes * 60);
                } else if (challengeInputType == 3) { // Join
                    challengeM.joinRoom(joinPlayerName, joinRoomCode);
                }
            }
            if (challengeM != null && challengeM.isChallengeStarted()) {
                gameMode = GameMode.CHALLENGE_MODE;
                playerName = challengeM.getPlayerName();
                restart();
                gameState = playState;
                playMusic(0);
                System.out.println("Challenge gameplay started");
            }
        }

        // Ticks down the challenge timer if challenge mode is active
        if (gameMode == GameMode.CHALLENGE_MODE && challengeM != null && challengeM.isChallengeStarted()) {
            challengeTimerCounter++;
            if (challengeTimerCounter >= 60) {
                challengeTimerCounter = 0;
                int currentTimeLeft = challengeM.getTimeLeft();
                if (currentTimeLeft > 0) {
                    challengeM.setTimeLeft(currentTimeLeft - 1);
                }
                // Send current score, life, and enemy kills to the backend
                challengeM.sendPlayerUpdate(player.totalScore, player.life, player.enemyKilled);
            }

            // Check if BOTH players are dead (life <= 0) before timer ends
            boolean bothDead = player.life <= 0 && challengeM.getOpponentLife() <= 0;

            if ((challengeM.getTimeLeft() <= 0 || bothDead) && !challengeEndSent) {
                challengeEndSent = true;
                challengeM.endChallenge();
                if (bothDead) {
                    System.out.println("Both players are dead. Ending challenge early.");
                } else {
                    System.out.println("Challenge time finished. Ending challenge.");
                }
                gameState = challengeEndWaitingState;
            }
        }

        // Transition to result state once the server returns winner results
        if (gameMode == GameMode.CHALLENGE_MODE && challengeM != null && challengeM.getWinnerName() != null
                && gameState != challengeResultState) {
            gameState = challengeResultState;
            stopMusic();
            playSE(4); // Play fanfare sound effect
        }
    }

    public void drawToTempScreen() {
        if (gameState == titleState || gameState == challengeComingSoonState ||
                gameState == challengeLobbyState || gameState == challengeInputState ||
                gameState == challengeWaitingState || gameState == challengeDeadState ||
                gameState == challengeEndWaitingState || gameState == challengeResultState) {
            ui.draw(g2);
        } else {
            tileM.draw(g2);

            for (int i = 0; i < iTile[1].length; i++) {
                if (iTile[currentMap][i] != null) {
                    iTile[currentMap][i].draw(g2);
                }
            }

            entityList.add(player);

            for (int i = 0; i < npc[1].length; i++) {
                if (npc[currentMap][i] != null) {
                    entityList.add(npc[currentMap][i]);
                }
            }

            for (int i = 0; i < obj[1].length; i++) {
                if (obj[currentMap][i] != null) {
                    entityList.add(obj[currentMap][i]);
                }
            }

            for (int i = 0; i < monster[1].length; i++) {
                if (monster[currentMap][i] != null) {
                    entityList.add(monster[currentMap][i]);
                }
            }

            for (int i = 0; i < projectile[1].length; i++) {
                if (projectile[currentMap][i] != null) {
                    entityList.add(projectile[currentMap][i]);
                }
            }

            for (int i = 0; i < particleList.size(); i++) {
                if (particleList.get(i) != null) {
                    entityList.add(particleList.get(i));
                }
            }

            Collections.sort(entityList, new Comparator<Entity>() {

                @Override
                public int compare(Entity e1, Entity e2) {
                    int result = Integer.compare(e1.worldY, e2.worldY);
                    return result;
                }

            });

            for (int i = 0; i < entityList.size(); i++) {
                entityList.get(i).draw(g2);
            }

            entityList.clear();

            ui.draw(g2);
        }
    }

    public void drawToScreen() {
        Graphics g = getGraphics();
        g.drawImage(tempScreen, 0, 0, screenWidth2, screenHeight2, null);
        g.dispose();
    }

    public void playMusic(int i) {
        music.setFile(i);
        music.play();
        music.loop();
    }

    public void stopMusic() {
        music.stop();
    }

    public void playSE(int i) {
        se.setFile(i);
        se.play();
    }
}
