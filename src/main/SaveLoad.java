package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class SaveLoad {
    GamePanel gp;
    private final String saveFileName = "save_game.dat";

    public SaveLoad(GamePanel gp) {
        this.gp = gp;
    }

    public void saveGame() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(saveFileName))) {
            pw.println(gp.currentMap);
            pw.println(gp.player.worldX);
            pw.println(gp.player.worldY);
            pw.println(gp.player.direction);
            pw.println(gp.player.life);
            pw.println(gp.player.maxLife);
            pw.println(gp.player.mana);
            pw.println(gp.player.maxMana);
            pw.println(gp.player.totalScore);
            pw.println(gp.player.coin);
            pw.println("true");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void invalidateSave() {
        File saveFile = new File(saveFileName);
        if (!saveFile.exists()) {
            return;
        }

        try {
            int map = 0;
            int worldX = 0;
            int worldY = 0;
            String direction = "down";
            int life = 0;
            int maxLife = 0;
            int mana = 0;
            int maxMana = 0;
            int totalScore = 0;
            int coin = 0;

            try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
                String line;
                if ((line = br.readLine()) != null) map = Integer.parseInt(line);
                if ((line = br.readLine()) != null) worldX = Integer.parseInt(line);
                if ((line = br.readLine()) != null) worldY = Integer.parseInt(line);
                if ((line = br.readLine()) != null) direction = line;
                if ((line = br.readLine()) != null) life = Integer.parseInt(line);
                if ((line = br.readLine()) != null) maxLife = Integer.parseInt(line);
                if ((line = br.readLine()) != null) mana = Integer.parseInt(line);
                if ((line = br.readLine()) != null) maxMana = Integer.parseInt(line);
                if ((line = br.readLine()) != null) totalScore = Integer.parseInt(line);
                if ((line = br.readLine()) != null) coin = Integer.parseInt(line);
            }

            try (PrintWriter pw = new PrintWriter(new FileWriter(saveFile))) {
                pw.println(map);
                pw.println(worldX);
                pw.println(worldY);
                pw.println(direction);
                pw.println(life);
                pw.println(maxLife);
                pw.println(mana);
                pw.println(maxMana);
                pw.println(totalScore);
                pw.println(coin);
                pw.println("false");
            }
        } catch (Exception e) {
            saveFile.delete();
        }
    }

    public boolean loadGame() {
        File saveFile = new File(saveFileName);
        if (!saveFile.exists()) {
            return false;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(saveFile))) {
            int map = Integer.parseInt(br.readLine());
            int worldX = Integer.parseInt(br.readLine());
            int worldY = Integer.parseInt(br.readLine());
            String direction = br.readLine();
            int life = Integer.parseInt(br.readLine());
            int maxLife = Integer.parseInt(br.readLine());
            int mana = Integer.parseInt(br.readLine());
            int maxMana = Integer.parseInt(br.readLine());
            int totalScore = Integer.parseInt(br.readLine());
            int coin = Integer.parseInt(br.readLine());

            String availableLine = br.readLine();
            if (availableLine != null) {
                boolean available = Boolean.parseBoolean(availableLine.trim());
                if (!available) {
                    return false;
                }
            }

            gp.currentMap = map;
            gp.player.worldX = worldX;
            gp.player.worldY = worldY;
            gp.player.direction = direction;
            gp.player.life = life;
            gp.player.maxLife = maxLife;
            gp.player.mana = mana;
            gp.player.maxMana = maxMana;
            gp.player.totalScore = totalScore;
            gp.player.coin = coin;
            gp.player.attacking = false;
            gp.player.invincible = false;
            gp.player.shotAvailableCounter = 30;
            gp.player.attack = gp.player.getAttack();
            gp.player.defense = gp.player.getDefense();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
