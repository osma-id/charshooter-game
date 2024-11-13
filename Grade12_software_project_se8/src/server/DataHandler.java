package server;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.HashMap;

import software_project.Bullet;
import software_project.Entity;
import software_project.MapManager;
import software_project.Pistol;
import software_project.Player;
import software_project.Rifle;
import software_project.Shotgun;
import software_project.Zombie;

/** Handles data into variables in the Data class.
 * Tasks include handling wave changes, player changes, running the timer, and handling entities.
 * Handling entities includes damage between bullet collision and zombies, and players and zombies.
 * Also handles repainting map visualizer (which shows approximate locations of entities using hit-boxes on the map)
 * @author Osman Idris
 *
 */
public class DataHandler {

	static int timerReplica;
	static double totalFrames;
	static long lastTime;
	static int fps; //is being used to measure tps (not the right way and will be changed later)

	DataHandler() {}

	public class TL implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			timerReplica++;

			handleEntities(); //also has damaging them if needed
			handlePlayerGuns();
			handleWave();
			handlePlayerChanges();
			measureFPS();
			//damageEntity(); // TO DAMAGE ENTITY
			//readExistingEn();
			if (MapVisualizer.window != null) {
				MapVisualizer.dPanel.repaint();
			}
		}

	}

	/** Handles changes to the player like periodic healing, and sprite phases.
	 * 
	 */
	public void handlePlayerChanges() {
		for (Entity entity: Data.existingEntities) {
			if (entity instanceof Player) {
				if (timerReplica % 30 == 0 && entity.hp < entity.spawnHp) entity.hp += 1;
				if (timerReplica % 25 == 0) Server.connections.get(entity.id).moving = false;
				Server.connections.get(entity.id).setSpritePhase();
			}
		}
		this.syncPlayerAndConnection();
	}

	/** Damages entities using for loops (for-each cannot be used to manipulate the ArrayList).
	 * Traverses the ArrayList for two tasks: 
	 * 1) Find if entities are intersecting and damaging them if that is true.
	 * 2) Killing entities if their health is below 0.
	 * 
	 */
	public void damageEntity() {
		for (int i = 0; i < Data.existingEntities.size(); i++) {
			for (int j = 0; j < Data.existingEntities.size(); j++) {
				if (i==j) continue;
				Entity tempEn1 = Data.existingEntities.get(i);
				Entity tempEn2 = Data.existingEntities.get(j);
				if (tempEn1.intersects(tempEn2) 
						&& !tempEn1.recievedDmg 
						&& !(tempEn1 instanceof Zombie && tempEn2 instanceof Zombie)
						&& !(tempEn1 instanceof Player 
								&& tempEn2 instanceof Bullet)) {
					if (tempEn1 instanceof Zombie && tempEn2 instanceof Bullet) {
						Zombie enZ1 = (Zombie) tempEn1;
						Bullet enB1 = (Bullet) tempEn2;
						enZ1.shotBy = enB1.shotBy;
						tempEn1 = enZ1;
						tempEn2 = enB1;
					}
					tempEn1.hp -= tempEn2.damage;
					tempEn1.recievedDmg = true;
					Data.existingEntities.set(i, tempEn1);
					Data.existingEntities.set(j, tempEn2);
				}
				if (killEntity(Data.existingEntities.get(i), i)) i--;
			}
		}
	}

	
	/** Checks if the health of the entity is less than 0 then proceeds to kill the entity.
	 * @param entity The object of type entity that needs to be "killed".
	 * @param i The index of the object in the existingEntities ArrayList.
	 * @return Whether the object is dead or not. 
	 */
	boolean killEntity(Entity entity, int i)  {
		boolean entityDead = false;
		if (entity instanceof Bullet) {
			if (entity.xx > MapManager.MAPW || entity.yy < 0 || entity.xx > MapManager.MAPH || entity.yy < 0) {
				Data.existingEntities.remove(i);
				entityDead = true;
			}
		}
		if (entity.hp <= 0) {
			Data.existingEntities.remove(i);
			entityDead = true;
			if (entity instanceof Zombie) {
				Zombie en2 = (Zombie)entity;
				if (en2.shotBy != null) Server.connections.get(en2.shotBy).addScore();
			}
			if (entity instanceof Player) {
				if (getAmntPlayersAlive() == 0) Server.sendMiscPacket(Server.ROUND_RESTART, null);
			}
		}
		return entityDead;
	}

	/** Handles tasks for entities such as setting sprite phase, setting focus for zombies, 
	 * setting damage delay, and sending data to clients of the final entity list at the end of it.
	 * 
	 */
	public void handleEntities() {
		// Used for zombies to target players.
		Point[] targetPoints = new Point[Server.connections.size()]; 
		int numIndex = 0;
		
		//For players
		for (HashMap.Entry<String, Connection> connection: Server.connections.entrySet()) {
			targetPoints[numIndex] = connection.getValue().getCoords();
			numIndex++;
		}
		
		//for all entities
		for (Entity entity: Data.existingEntities) {
			entity.setSpritePhase(); // automatically doesn't do for bullets
			entity.serverMove();
			entity.setDmgDelay();
			if (entity instanceof Zombie) {
				Zombie tempEn = (Zombie)entity;
				tempEn.setFocus(targetPoints);
				entity = (Entity)tempEn;
			}
			//if (entity instanceof Bullet) System.out.println(entity.xx + " " + entity.yy);
		}

		Server.sendMiscPacket(Server.ENTITY_LIST, null);
	}

	void measureFPS() {
		totalFrames++;
		if (System.nanoTime() > lastTime + 1000000000) {
			lastTime = System.nanoTime();
			fps = (int) totalFrames;
			totalFrames = 0;
			System.out.println("Serverside: " + fps);
		}
	}

	public int getAmntPlayersAlive() {
		int playersAlive = 0;
		for (Entity entity: Data.existingEntities) {
			if (entity instanceof Player) playersAlive++;
		}
		return playersAlive;
	}

	/** Synchronizes the player entity and connection object.
	 * 
	 */
	public void syncPlayerAndConnection() {
		for (Entity entity: Data.existingEntities) {
			if (entity instanceof Player) {
				entity.mapX = Server.connections.get(entity.id).getCoords().x;
				entity.mapY = Server.connections.get(entity.id).getCoords().y;
				entity.xx = Server.connections.get(entity.id).getCoords().x;
				entity.yy = Server.connections.get(entity.id).getCoords().y;
				entity.moving = Server.connections.get(entity.id).moving;
				entity.lSideFacing = Server.connections.get(entity.id).lSideFacing;
				entity.spritePhase = Server.connections.get(entity.id).spritePhase;
			}
		}
	}

	/** Handles the gun recoil, and bullet aim for each player.
	 * 
	 */
	public void handlePlayerGuns() {
		for (HashMap.Entry<String, Connection> connection: Server.connections.entrySet()) {
			Connection tempValue = connection.getValue();
			if (timerReplica % 2 == 0 && tempValue.recoilTime > 0) {
				connection.getValue().recoilTime--;
			}
			if (tempValue.hasShot) {
				switch (tempValue.getEquipped()) {
				case Data.PISTOL:
					if (tempValue.recoilTime <= 0) { 
						connection.getValue().recoilTime = Pistol.totalRecoilTime;
						Data.existingEntities.add(new Bullet(tempValue.getCoords().x, 
								tempValue.getCoords().y, tempValue.getMouseCoords().x, 
								tempValue.getMouseCoords().y, Data.PISTOL, tempValue.getUsername()
								+ tempValue.getHostName() + tempValue.getPort()));
					}
					break;
				case Data.SHOTGUN:
					if (connection.getValue().recoilTime <= 0) { 
						connection.getValue().recoilTime = Shotgun.totalRecoilTime;
						for (int i = 0; i < 5; i++) {
							Data.existingEntities.add(new Bullet(tempValue.getCoords().x, 
									tempValue.getCoords().y, tempValue.getMouseCoords().x, 
									tempValue.getMouseCoords().y, Data.SHOTGUN, tempValue.getUsername()
									+ tempValue.getHostName() + tempValue.getPort()));
						}
					}
					break;
				case Data.RIFLE:
					if (connection.getValue().recoilTime <= 0) { 
						connection.getValue().recoilTime = Rifle.totalRecoilTime;
						Data.existingEntities.add(new Bullet(tempValue.getCoords().x, 
								tempValue.getCoords().y, tempValue.getMouseCoords().x, 
								tempValue.getMouseCoords().y, Data.RIFLE, tempValue.getUsername()
								+ tempValue.getHostName() + tempValue.getPort()));
					}
					break;
				}
				connection.getValue().hasShot = false;
			}

		}
	}

	
	/** Uses the Wave class to start new waves after a delay.
	 * 
	 */
	public void handleWave() {
		if (!Wave.getWaveState()) {
			Wave.changeSettings(); // changes the settings according to difficulty setting
			Data.waveEndDelay++;
			if (Data.waveEndDelay == 150) {
				Wave.startNewWave();
				Data.waveEndDelay = 0;
			}
		}
		//Sends the packet to every connection.
		if (Data.newWave) {
			Server.sendMiscPacket(Server.STARTED_NEWWAVE, null);
			Data.newWave = false;
		}
	}

	/** Prints out the entity types in the existing entities array list.
	 * Used for debugging and testing purposes.
	 * 
	 */
	public static void readExistingEn() {
		for (Entity entity: Data.existingEntities) {
			if (entity instanceof Player) {
				System.out.print("Player, ");
			} else if (entity instanceof Zombie) System.out.print("Zombie, ");
			else System.out.print("Bullet, ");
		}
		System.out.println();
	}

	public static void restartRound() {};

}
