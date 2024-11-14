package software_project;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class ImageManager {
	
	//Player
	public static BufferedImage spriteIdle1 = ImageManager.loadImage("/spriteidle.png");
	public static BufferedImage spriteIdle2 = ImageManager.loadImage("/spriteidle2.png");
	public static BufferedImage spriteWalk1 = ImageManager.loadImage("/spritewalk1.png");
	public static BufferedImage spriteWalk2 = ImageManager.loadImage("/spritewalk2.png");
	public static BufferedImage spriteWalk3 = ImageManager.loadImage("/spritewalk3.png");
	static BufferedImage[] plyrSprites = {spriteIdle1, spriteIdle2, spriteWalk1, spriteWalk2, spriteWalk3};

	//Zombie
	public static BufferedImage zombSprite1 = ImageManager.loadImage("/zombwalk1.png");
	public static BufferedImage zombSprite2 = ImageManager.loadImage("/zombwalk2.png");
	public static BufferedImage zombSprite4 = ImageManager.loadImage("/zombwalk4.png");
	public static BufferedImage zombSprite5 = ImageManager.loadImage("/zombwalk5.png");
	public static BufferedImage zombSprite6 = ImageManager.loadImage("/zombwalk6.png");
	static BufferedImage[] zombSprites = {zombSprite1, zombSprite2, zombSprite1, zombSprite4, zombSprite5, zombSprite6};
	static BufferedImage grassImg = loadImage("/grass.png");
	static BufferedImage treeImg = loadImage("/tree2.png");

	ImageManager() {}

	static void playSprite(Entity entity, Graphics g, int spritePhase) {
		if (entity instanceof Zombie) {
			if (entity.lSideFacing) {
				g.drawImage(zombSprites[spritePhase - 1], entity.screenX + entity.width, entity.screenY, -entity.width, entity.height, null);
			} else {
				g.drawImage(zombSprites[spritePhase - 1], entity.screenX, entity.screenY, entity.width, entity.height, null);			
			}
		}			
		if (entity instanceof Player) {
			
			//check if the entity is localplayer or not
				if (entity.lSideFacing) {
					g.drawImage(plyrSprites[spritePhase - 1], entity.screenX + entity.width, entity.screenY, -entity.width, entity.height, null);
					if (spritePhase == 1 || spritePhase == 2) {
						g.drawImage(plyrSprites[spritePhase - 1], entity.screenX, entity.screenY, entity.width, entity.height, null);
					}
				}
				if (!entity.lSideFacing) {
					g.drawImage(plyrSprites[spritePhase - 1], entity.screenX, entity.screenY, entity.width, entity.height, null);
				}
		}
	}
	
	static BufferedImage loadImage(String filename) {
		InputStream inputStr = ImageManager.class.getResourceAsStream(filename);
		BufferedImage img = null;			
		try {
			img = ImageIO.read(inputStr);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.toString());
			JOptionPane.showMessageDialog(null, "An image failed to load: " + filename , "ERROR", JOptionPane.ERROR_MESSAGE);
		}
		//DEBUG
		//if (img == null) System.out.println("null");
		//else System.out.printf("w=%d, h=%d%n",img.getWidth(),img.getHeight());

		return img;
	}
	
}

