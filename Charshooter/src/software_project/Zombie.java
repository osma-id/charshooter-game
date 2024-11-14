package software_project;

import java.awt.Point;
import java.io.Serializable;
import java.util.UUID;

/** Zombie objects.
 * @author Osman Idris
 *
 */
public class Zombie extends Entity implements Serializable {

	public String shotBy;

	public Zombie() {
		super(0, 0, 40, 60);
		id = "";
		damage = 10; hp = spawnHp = 20;
		spritePhase = 1;
		switch (MainGame.difficulty) {
		case MainGame.EASY:
			vx = vy = 4;
			hp = spawnHp = 10;
			damage = 10;
			break;
		case MainGame.MEDIUM:
			vx = vy = 5.75;
			hp = spawnHp = 15;
			hp = 15;
			break;
		case MainGame.HARD:
			vx = vy = 7.25;
			hp = spawnHp = 20;
			damage = 20;
			break;
		}
		
		setSpawnCoords();

		screenX = (int) xx;
		screenY = (int) yy;
	}
	
	@Override
	public void move() {
		//distanceX or distanceY
		double dx = (MainGame.player.mapX + MainGame.player.width) - (xx + this.width);
		double dy = (MainGame.player.mapY + MainGame.player.height) - (yy + this.height);
		double angle = Math.atan2(dy,dx);

		xx += 0.5*vx*Math.cos(angle);
		yy += 0.5*vy*Math.sin(angle);
		
		if (angle > 1.5 || angle < -1.5) lSideFacing = true; //for sprite
		else lSideFacing = false;
		
		screenX = (int)xx - MainGame.player.mapX + MainGame.player.screenX;
		screenY = (int)yy - MainGame.player.mapY + MainGame.player.screenY;

		x = screenX; //needed for intersections, change later
		y = screenY;
	}
	
	@Override
	public void clientMove() {
		screenX = (int)xx - MainGame.player.mapX + MainGame.player.screenX;
		screenY = (int)yy - MainGame.player.mapY + MainGame.player.screenY;
		
		x = screenX;
		y = screenY;
	}
	
	@Override
	public void serverMove() {
		//distanceX or distanceY
		double dx = (targetMapX + MainGame.player.width) - (xx + this.width);
		double dy = (targetMapY + MainGame.player.height) - (yy + this.height);
		double angle = Math.atan2(dy,dx);

		xx += 0.5*vx*Math.cos(angle);
		yy += 0.5*vy*Math.sin(angle);
		
		if (angle > 1.5 || angle < -1.5) lSideFacing = true; //for sprite
		else lSideFacing = false;
		
		mapX = (int) xx;
		mapY = (int) yy;
		x = (int) xx;
		y = (int) yy;
	}
	
	public void setFocus(Point[] targets) {
		
		double[] hyps = new double[targets.length];
		for (int i = 0; i < targets.length; i++) {
			double dx1 = (targets[i].x - this.mapX);
			double dy1 = (targets[i].y - this.mapY);
			double hyp1 = Math.sqrt( (dx1 * dx1) + (dy1 * dy1) );
			hyps[i] = hyp1;
		}
		int leastDisIndex = -1;
		for (int i = 0; i < hyps.length; i++) {
			leastDisIndex = i;
			for (int j = 0; j < hyps.length; j++) {
				if (hyps[j] < hyps[i]) {
					leastDisIndex = j;
				} 
			}
		}

		targetMapX = targets[leastDisIndex].x;
		targetMapY = targets[leastDisIndex].y;
	}
	
	private void setSpawnCoords() {
		switch ((int) (Math.random()*4 + 1)) {
		case 1: //left side of the screen
			xx = 0;
			yy = ((int) (Math.random()*MapManager.MAPH)); 
			break;
		case 2: //right side of the screen
			xx = MapManager.MAPW;
			yy = ((int) (Math.random()*MapManager.MAPH));
			break;
		case 3: //top of the screen
			xx = ((int) (Math.random()*MapManager.MAPW));
			yy = 0; 
			break;
		case 4: //bottom of screen
			xx = ((int) (Math.random()*MapManager.MAPW));
			yy = MapManager.MAPH;
			break;
		}
	}
	
	@Override
	public void setDmgDelay() {
		if (recievedDmg) dmgDelay++;
		if (dmgDelay >= 30) {
			dmgDelay = 0;
			recievedDmg = false;
		}
	}

	@Override
	public void setSpritePhase() {
		if (Listeners.timerReplica % 11 == 0) spritePhase++;
		if (spritePhase == 7) spritePhase = 1;
	}

	@Override
	public String generateUUID() {
		UUID randomUUID = UUID.randomUUID();
		return randomUUID.toString().replaceAll("-", "").substring(0, 11);
	}
	
	@Override
	public void modifyEntity(double xx, double yy, int hp, boolean lSideFacing, int spritePhase) {
		this.xx = xx;
		this.yy = yy;
		this.hp = hp;
		this.lSideFacing = lSideFacing;
		this.spritePhase = spritePhase;
	}
	
}
