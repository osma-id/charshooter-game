package software_project;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.HashMap;

/** Entity (of type Rectangle to ensure hit-box collisions can be detected).
 * @author Osman Idris
 *
 */
public abstract class Entity extends Rectangle implements Serializable {
	

	static HashMap<String, Entity> entitiesToAdd;
	double vx, vy;
	public int hp = 0; //spawnHp for first hp
	public int spawnHp = 0;
	public int damage = 0;
	int dmgDelay = 0;
	int screenX, screenY; //coordinates on the screen (for drawing) TODO MIGHT HAVE TO CHANGE
	public int mapX, mapY; //coordinates on the whole map 
	public int targetMapX, targetMapY;
	public int spritePhase = 1;
	public double xx, yy;
	public String id = ""; //add id of person who shoots the bullet if entity is a bullet.
	public String uid = generateUUID();
	public boolean recievedDmg = false;
	public boolean lSideFacing = false;
	public boolean moving; //for sprite

	Entity(int x, int y, int width, int height) {
		super(x,y,width,height);
	}
	
	abstract public void move();
	abstract public void clientMove();
	abstract public void serverMove();
	abstract public void setDmgDelay();
	abstract public void setSpritePhase(); //ONLY for sprites to change phases
	abstract public String generateUUID(); //for npc's
	abstract public void modifyEntity(double xx, double yy, int hp, boolean lSideFacing, int spritePhase);
	
}
