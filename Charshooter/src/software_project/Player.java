package software_project;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class Player extends Entity implements Serializable {

	static int equipped = 1;
	
	Player() {
		super(MainGame.PANW/2 - 20, MainGame.PANH/2 - 30, 40, 60); //original 20x30 size
		hp = spawnHp = 100;
		vx = vy = 3.5;
		moving = false;
		spritePhase = 1;
		xx = MapManager.MAPW/2 - width/2;
		yy = MapManager.MAPH/2 - height/2;
		
		x = screenX = MainGame.PANW/2 - width/2;
		y = screenY = MainGame.PANH/2 - height/2;

		mapX = (int) xx;
		mapY = (int) yy;
	}
	
	public Player(String id, String uid) {
		super(MainGame.PANW/2 - 20, MainGame.PANH/2 - 30, 40, 60); //original 20x30 size
		this.id = id;
		hp = spawnHp = 100;
		vx = vy = 3.5;
		moving = false;
		spritePhase = 1;
		xx = MapManager.MAPW/2 - width/2;
		yy = MapManager.MAPH/2 - height/2;
		
		this.uid = uid;
		
		x = screenX = MainGame.PANW/2 - width/2;
		y = screenY = MainGame.PANH/2 - height/2;

		mapX = (int) xx;
		mapY = (int) yy;
		
	}
	
	public void moveTo(int x, int y) {
		mapX = x;
		mapY = y;
	}
	
	//METHOD BY: Mr Harwood
	@Override
	public void move () {
		switch (Listeners.key) {
		case 'W': yy -= vy; moving = true; break;
		case 'S': yy += vy; moving = true; break;
		case 'A': xx -= vx; moving = true; lSideFacing = true; break;
		case 'D': xx += vx; moving = true; lSideFacing = false; break;
		}
		
		//update final positions
		
		if (xx < MainGame.PANW/2 - width/2 || xx > MainGame.PANW*3 - MainGame.PANW/2 - width/2) xx = mapX;
		if (yy < MainGame.PANH/2 - height/2 || yy > MainGame.PANH*3 - MainGame.PANH/2 - height/2) yy = mapY;
	
		mapX = (int)xx;
		mapY = (int)yy;
		if (MainGame.client != null && MainGame.client.online && moving) Client.sendPacket(Client.MOVE, null);
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
		if (moving) { //if player is moving cycle 3-6
			if (Listeners.timerReplica % 10 == 0) spritePhase++;
			if (spritePhase == 6 || spritePhase <= 2) spritePhase = 3;
		} else { //if not moving, cycle 1-2
			if (Listeners.timerReplica % 12 == 0) spritePhase++;
			if (spritePhase >= 3) spritePhase = 1;
		} 
	}
	
	void changeEquipment(int equipment) {
		switch (equipment) {
		case MainGame.PISTOL:
			equipped = equipment;
			break;
		case MainGame.SHOTGUN:
			equipped = equipment;
			break;
		case MainGame.RIFLE:
			equipped = equipment;
			break;
		}
	}

	@Override
	public void clientMove() {
		this.screenX = (int)xx - MainGame.player.mapX + MainGame.player.screenX;
		this.screenY = (int)yy - MainGame.player.mapY + MainGame.player.screenY;
	}

	@Override
	public void serverMove() {
		mapX = (int)xx;
		mapY = (int)yy;
		x = mapX;
		y = mapY;
	}

	@Override
	public String generateUUID() {UUID randomUUID = UUID.randomUUID();
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
	
	public void modifyLocalPlayer(int hp) {
		this.hp = hp;
	}

}
