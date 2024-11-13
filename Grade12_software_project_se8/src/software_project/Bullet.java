package software_project;

import java.io.Serializable;
import java.util.UUID;

public class Bullet extends Entity implements Serializable {

	int mx, my;
	int type;
	public String shotBy = "";
	double spread;

	Bullet(int mx, int my, int bulletType) {
		super((int)MainGame.player.xx, (int)MainGame.player.yy, 7, 7);
		id = "bullet";
		hp = damage = 10;
		mapX = mapY = 0;
		vx = vy = 5;
		xx = MainGame.player.xx + (MainGame.player.width/2);
		yy = MainGame.player.yy;
		changeType(bulletType);

		this.screenX = (int) xx;
		this.screenY = (int) yy;
		this.x = screenX;
		this.y = screenY;
		this.mx = mx + MainGame.player.mapX - MainGame.player.screenX; //TODO FIX
		this.my = my + MainGame.player.mapY - MainGame.player.screenY;
	}

	public Bullet(int px, int py, int mx, int my, int bulletType, String shotBy) {
		super(px, py, 7, 7);
		id = "";
		hp = damage = 10;
		mapX = mapY = 0;
		vx = vy = 5;
		xx = px + (MainGame.player.width/2);
		yy = py + (MainGame.player.height/2);
		this.shotBy = shotBy;
		changeType(bulletType);

		this.screenX = (int) xx;
		this.screenY = (int) yy;
		this.x = screenX;
		this.y = screenY;
		this.mx = mx; //TODO FIX
		this.my = my;
	}

	@Override
	public void move() {
		double dx = mx - (xx + width);
		double dy = my - (yy + height);
		//System.out.println("Cursor: " + mx + " " + my);
		double angle = Math.atan2(dy, dx);

		xx += 1.5*vx*Math.cos(angle + spread);
		yy += 1.5*vy*Math.sin(angle + spread);
		mx += 1.5*vx*Math.cos(angle + spread); //made to make sure bullet goes infinitely
		my += 1.5*vy*Math.sin(angle + spread);

		screenX = (int) xx - MainGame.player.mapX + MainGame.player.screenX;
		screenY = (int) yy - MainGame.player.mapY + MainGame.player.screenY;
		//System.out.println("Screen coords: " + screenX + " " + screenY);
		x = screenX;
		y = screenY;
	}

	@Override
	public void setDmgDelay() {
		if (recievedDmg) dmgDelay++;
		if (dmgDelay >= 30) {
			dmgDelay = 0;
			recievedDmg = false;
		}
	} //TODO: work on it or remove it

	@Override
	public void setSpritePhase() {}

	public void changeType(int currentEquip) {
		switch (currentEquip) {
		case MainGame.PISTOL:
			vx = vy = 5;
			spread = calculateSpread(-MainGame.pistol.maxSpread, MainGame.pistol.maxSpread);
			damage = 10;
			break;
		case MainGame.SHOTGUN:
			vx = vy = 3;
			spread = calculateSpread(-MainGame.shotgun.maxSpread, MainGame.shotgun.maxSpread);
			damage = 15;
			break;
		case MainGame.RIFLE:
			vx = vy = 8;
			spread = calculateSpread(-MainGame.rifle.maxSpread, MainGame.rifle.maxSpread);
			damage = 20;
			hp = 20;
			break;
		}
	}

	double calculateSpread(double min, double max) {
		double totalRange = (max - min);
		return (Math.random() * totalRange) + min;
	}

	@Override
	public void clientMove() {
		screenX = (int)xx - MainGame.player.mapX + MainGame.player.screenX;
		screenY = (int)yy - MainGame.player.mapY + MainGame.player.screenY;
	}

	@Override
	public void serverMove() {
		//distanceX or distanceY
		double dx = (mx + MainGame.player.width) - (xx + this.width);
		double dy = (my + MainGame.player.height) - (yy + this.height);
		double angle = Math.atan2(dy,dx);

		xx += 1.5*vx*Math.cos(angle + spread);
		yy += 1.5*vy*Math.sin(angle + spread);
		mx += 1.5*vx*Math.cos(angle + spread); //made to make sure bullet goes infinitely
		my += 1.5*vy*Math.sin(angle + spread);

		mapX = (int) xx;
		mapY = (int) yy;
		x = (int)xx;
		y = (int)yy;
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
		//don't need lsidefacing or spritephase for bullet right now
	}

}
