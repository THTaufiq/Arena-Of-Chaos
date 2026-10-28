package monster;

import java.awt.image.BufferedImage;
import java.util.Random;

import entity.Entity;
import main.GamePanel;
import object.OBJ_Coin_Bronze;
import object.OBJ_Heart;
import object.OBJ_ManaCrystal;

public class MON_Zombie extends Entity {

    GamePanel gp;
    
    private int contactDamageCooldown = 0;
    private final int CONTACT_DAMAGE_COOLDOWN_MAX = 30;
    private final int chaseRange;

    public MON_Zombie(GamePanel gp) {
        super(gp);

        this.gp = gp;

        type = type_monster;
        name = "Zombie";
        defaultSpeed = 1;
        speed = defaultSpeed;
        
        maxLife = 10;
        life = maxLife;
        attack = 4;
        defense = 1;
        exp = 6;
        score = 30;
        projectile = null;
        chaseRange = gp.tileSize * 12;

        solidArea.x = 4;
        solidArea.y = 18;
        solidArea.width = 42;
        solidArea.height = 30;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        getImage();
    }

    public void getImage() {
        int scaledSize = gp.tileSize - 2;
        
        BufferedImage frame1 = setup("/res/monster/zombiem1", scaledSize, scaledSize);
        BufferedImage frame2 = setup("/res/monster/zombiem2", scaledSize, scaledSize);

        up1 = frame1;
        up2 = frame2;
        down1 = frame1;
        down2 = frame2;
        right1 = frame1;
        right2 = frame2;
        left1 = mirrorImage(frame1);
        left2 = mirrorImage(frame2);
    }

    public void setAction() {
        int distX = Math.abs(worldX - gp.player.worldX);
        int distY = Math.abs(worldY - gp.player.worldY);
        int distance = Math.max(distX, distY);

        if (distance < chaseRange) {
            int goalCol = (gp.player.worldX + gp.player.solidArea.x) / gp.tileSize;
            int goalRow = (gp.player.worldY + gp.player.solidArea.y) / gp.tileSize;

            boolean pathFound = searchPath(goalCol, goalRow);
            if (pathFound == true) {
                onPath = true;
                actionLockCounter = 0;
                return;
            }
        }

        onPath = false;
        actionLockCounter++;

        if (actionLockCounter == 90) {
            Random random = new Random();
            int i = random.nextInt(100) + 1;

            if (i <= 25) {
                direction = "up";
            }
            if (i > 25 && i <= 50) {
                direction = "down";
            }
            if (i > 50 && i <= 75) {
                direction = "left";
            }
            if (i > 75 && i <= 100) {
                direction = "right";
            }

            actionLockCounter = 0;
        }
    }

    public void damageReaction() {
        actionLockCounter = 0;
        direction = gp.player.direction;
    }

    public void checkDrop() {
        int i = new Random().nextInt(100) + 1;

        if (i < 40) {
            dropItem(new OBJ_Coin_Bronze(gp));
        }
        if (i >= 40 && i < 70) {
            dropItem(new OBJ_Heart(gp));
        }
        if (i >= 70 && i < 100) {
            dropItem(new OBJ_ManaCrystal(gp));
        }
    }

    @Override
    public void update() {
        if (contactDamageCooldown > 0) {
            contactDamageCooldown--;
        }
        
        super.update();
    }

    @Override
    public void damagePlayer(int attack) {
        if (contactDamageCooldown <= 0 && gp.player.invincible == false) {
            super.damagePlayer(attack);
            
            contactDamageCooldown = CONTACT_DAMAGE_COOLDOWN_MAX;
        }
    }
}
