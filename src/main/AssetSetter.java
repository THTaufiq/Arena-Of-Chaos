package main;

import entity.NPC_Merchant;
import monster.MON_GreenSlime;
import monster.MON_Zombie;
import object.OBJ_Axe;
import object.OBJ_Coin_Bronze;
import object.OBJ_Door;
import object.OBJ_Heart;
import object.OBJ_Key;
import object.OBJ_ManaCrystal;
import object.OBJ_Potion_Red;
import object.OBJ_Shield_Blue;
import tile_interactive.IT_DryTree;

public class AssetSetter {

    GamePanel gp;

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    public void setObject() {
        int mapNum = 0;
        int i = 0;

        gp.obj[mapNum][i] = new OBJ_Axe(gp);
        gp.obj[mapNum][i].worldX = gp.tileSize * 33;
        gp.obj[mapNum][i].worldY = gp.tileSize * 21;
        i++;

        gp.obj[mapNum][i] = new OBJ_Shield_Blue(gp);
        gp.obj[mapNum][i].worldX = gp.tileSize * 37;
        gp.obj[mapNum][i].worldY = gp.tileSize * 21;
        i++;

        gp.obj[mapNum][i] = new OBJ_Potion_Red(gp);
        gp.obj[mapNum][i].worldX = gp.tileSize * 31;
        gp.obj[mapNum][i].worldY = gp.tileSize * 12;
        i++;
    }

    public void setNPC() {
        int mapNum = 0;
        int i = 0;

        mapNum = 1;
        i = 0;
        gp.npc[mapNum][i] = new NPC_Merchant(gp);
        gp.npc[mapNum][i].worldX = gp.tileSize * 12;
        gp.npc[mapNum][i].worldY = gp.tileSize * 7;
        i++;

    }

    public void setMonster() {
        int mapNum = 0;

        int i = 0;
        gp.monster[mapNum][i] = new MON_GreenSlime(gp);
        gp.monster[mapNum][i].worldX = gp.tileSize * 23;
        gp.monster[mapNum][i].worldY = gp.tileSize * 36;
        i++;

        gp.monster[mapNum][i] = new MON_GreenSlime(gp);
        gp.monster[mapNum][i].worldX = gp.tileSize * 23;
        gp.monster[mapNum][i].worldY = gp.tileSize * 37;
        i++;

        gp.monster[mapNum][i] = new MON_GreenSlime(gp);
        gp.monster[mapNum][i].worldX = gp.tileSize * 24;
        gp.monster[mapNum][i].worldY = gp.tileSize * 37;
        i++;

        gp.monster[mapNum][i] = new MON_GreenSlime(gp);
        gp.monster[mapNum][i].worldX = gp.tileSize * 34;
        gp.monster[mapNum][i].worldY = gp.tileSize * 42;
        i++;

        gp.monster[mapNum][i] = new MON_GreenSlime(gp);
        gp.monster[mapNum][i].worldX = gp.tileSize * 38;
        gp.monster[mapNum][i].worldY = gp.tileSize * 42;
        i++;
    }

    public void setInteractiveTile() {
        int mapNum = 0;

        int i = 0;

        gp.iTile[mapNum][i] = new IT_DryTree(gp, 27, 12);
        i++;
        gp.iTile[mapNum][i] = new IT_DryTree(gp, 28, 12);
        i++;
        gp.iTile[mapNum][i] = new IT_DryTree(gp, 29, 12);
        i++;
        gp.iTile[mapNum][i] = new IT_DryTree(gp, 30, 12);
        i++;
        gp.iTile[mapNum][i] = new IT_DryTree(gp, 32, 12);
        i++;
        gp.iTile[mapNum][i] = new IT_DryTree(gp, 33, 12);
        i++;
        gp.iTile[mapNum][i] = new IT_DryTree(gp, 18, 40);
        i++;
        gp.iTile[mapNum][i] = new IT_DryTree(gp, 17, 40);
        i++;
        gp.iTile[mapNum][i] = new IT_DryTree(gp, 16, 40);
        i++;
        gp.iTile[mapNum][i] = new IT_DryTree(gp, 15, 40);
        i++;
        gp.iTile[mapNum][i] = new IT_DryTree(gp, 14, 40);
        i++;
        gp.iTile[mapNum][i] = new IT_DryTree(gp, 13, 40);
        i++;
        gp.iTile[mapNum][i] = new IT_DryTree(gp, 12, 40);
        i++;
    }

    public void setZombieWave() {
        int mapNum = 0;
        int spawnAttempts = 0;
        int maxSpawns = 5;
        int maxAttempts = 100;
        
        for (int zombieCount = 0; zombieCount < maxSpawns && spawnAttempts < maxAttempts; zombieCount++) {
            int emptySlot = -1;
            for (int i = 0; i < gp.monster[mapNum].length; i++) {
                if (gp.monster[mapNum][i] == null) {
                    emptySlot = i;
                    break;
                }
            }
            
            if (emptySlot == -1) {
                break;
            }
            
            int spawnX = -1;
            int spawnY = -1;
            boolean validSpawn = false;
            
            for (int attempt = 0; attempt < 20 && !validSpawn; attempt++) {
                spawnAttempts++;
                
                int randomTileX = (int) (Math.random() * gp.maxWorldCol);
                int randomTileY = (int) (Math.random() * gp.maxWorldRow);
                
                spawnX = randomTileX * gp.tileSize;
                spawnY = randomTileY * gp.tileSize;
                
                int tileNum = gp.tileM.mapTileNum[mapNum][randomTileX][randomTileY];
                
                if (gp.tileM.tile[tileNum].collision == false) {
                    int distX = Math.abs(spawnX - gp.player.worldX) / gp.tileSize;
                    int distY = Math.abs(spawnY - gp.player.worldY) / gp.tileSize;
                    
                    if (distX >= 5 && distY >= 5) {
                        boolean occupied = false;
                        
                        for (int i = 0; i < gp.monster[mapNum].length; i++) {
                            if (gp.monster[mapNum][i] != null) {
                                if (gp.monster[mapNum][i].worldX == spawnX && 
                                    gp.monster[mapNum][i].worldY == spawnY) {
                                    occupied = true;
                                    break;
                                }
                            }
                        }
                        
                        if (!occupied) {
                            validSpawn = true;
                        }
                    }
                }
            }
            
            if (validSpawn) {
                gp.monster[mapNum][emptySlot] = new MON_Zombie(gp);
                gp.monster[mapNum][emptySlot].worldX = spawnX;
                gp.monster[mapNum][emptySlot].worldY = spawnY;
            }
        }
    }

    public boolean areAllGreenSlimesDefeated() {
        int mapNum = gp.currentMap;
        
        for (int i = 0; i < gp.monster[mapNum].length; i++) {
            if (gp.monster[mapNum][i] != null) {
                if (gp.monster[mapNum][i].name.equals("Green Slime") && gp.monster[mapNum][i].alive == true) {
                    return false;
                }
            }
        }
        
        return true;
    }

    public void spawnContinuousEnemy() {
        int mapNum = 0;
        
        // Find empty slot
        int emptySlot = -1;
        for (int i = 0; i < gp.monster[mapNum].length; i++) {
            if (gp.monster[mapNum][i] == null) {
                emptySlot = i;
                break;
            }
        }
        
        // If monster array is full, skip spawning
        if (emptySlot == -1) {
            return;
        }
        
        int spawnX = -1;
        int spawnY = -1;
        boolean validSpawn = false;
        
        for (int attempt = 0; attempt < 100 && !validSpawn; attempt++) {
            int randomTileX = (int) (Math.random() * gp.maxWorldCol);
            int randomTileY = (int) (Math.random() * gp.maxWorldRow);
            
            // Exclude the house entry tile
            if (randomTileX == 10 && randomTileY == 39) {
                continue;
            }
            
            int tileNum = gp.tileM.mapTileNum[mapNum][randomTileX][randomTileY];
            
            if (gp.tileM.tile[tileNum].collision == false) {
                int px = gp.player.worldX;
                int py = gp.player.worldY;
                
                spawnX = randomTileX * gp.tileSize;
                spawnY = randomTileY * gp.tileSize;
                
                int distX = Math.abs(spawnX - px) / gp.tileSize;
                int distY = Math.abs(spawnY - py) / gp.tileSize;
                
                if (distX >= 5 && distY >= 5) {
                    boolean occupied = false;
                    for (int i = 0; i < gp.monster[mapNum].length; i++) {
                        if (gp.monster[mapNum][i] != null) {
                            if (gp.monster[mapNum][i].worldX == spawnX && 
                                gp.monster[mapNum][i].worldY == spawnY) {
                                occupied = true;
                                break;
                            }
                        }
                    }
                    if (!occupied) {
                        validSpawn = true;
                    }
                }
            }
        }
        
        if (validSpawn) {
            double choice = Math.random();
            if (choice < 0.3) {
                gp.monster[mapNum][emptySlot] = new MON_GreenSlime(gp);
            } else {
                gp.monster[mapNum][emptySlot] = new MON_Zombie(gp);
            }
            gp.monster[mapNum][emptySlot].worldX = spawnX;
            gp.monster[mapNum][emptySlot].worldY = spawnY;
        }
    }
}
