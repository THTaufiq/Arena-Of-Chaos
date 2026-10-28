package main;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Main {

    public static JFrame window;

    public static void main(String[] args) {
        window = new JFrame();
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("ARENA OF CHAOS");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);


        window.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (gamePanel.gameMode == GameMode.SINGLE_PLAYER && gamePanel.player != null && gamePanel.player.life > 0) {
                    if (gamePanel.gameState != gamePanel.titleState && 
                        gamePanel.gameState != gamePanel.nameInputState && 
                        gamePanel.gameState != gamePanel.gameOverState) {
                        gamePanel.saveLoad.saveGame();
                    }
                }
            }
        });

        gamePanel.config.loadConfig();
        if (gamePanel.fullScreenOn == true) {
            window.setUndecorated(true);
        }

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.setupGame();
        gamePanel.startGameThread();
    }
}
