package software_project;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JOptionPane;

import server.Server;

/** Handles all of the listeners and singleplayer/client side events.
 * @author Osman Idris
 *
 */
public class Listeners {

	static int zombsLeft;
	int waveEndDelay;
	static int waveNum = 0;
	int scoreBrdOffset; //To change posX of score board so that nums dont go off screen
	static int key;
	static int score = 0;
	static boolean isKeyPressed;
	//A replica of the timer being used. This was only made to quantify the timer (for graphics related purposes).
	static int timerReplica = 0; 
	final int PANW = MainGame.PANW;
	final int PANH = MainGame.PANH;
	static int mx, my; //mouse coords
	static String inputServerAddress = "";
	static int inputServerPort = -1;

	//for fps method
	static long lastTime = 0;
	static int fps = 0;
	static int totalFrames = 0;

	static int clicks = 0;
	static boolean newWave = false;

	public class TL implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent ae) {
			timerReplica++;
			if (MainGame.stateChanged()) {
				Button.resetAllButtons(MainGame.buttons);
			}
			//long time =System.currentTimeMillis();
			MainGame.buttons.get("Back").animate(timerReplica);
			switch (MainGame.gameState) {
			case MainGame.MENU:
				for (Map.Entry<String, Button> set :
					MainGame.buttons.entrySet()) {
					if (set.getValue().bttnType == Button.MENU) set.getValue().animate(timerReplica);;
				}
				clicks = 0; 
				break;
			case MainGame.GAME_SETTINGS:
				for (Map.Entry<String, Button> set :
					MainGame.buttons.entrySet()) {
					if (set.getValue().bttnType == Button.GAMETYPE) set.getValue().animate(timerReplica);;
				}
				clicks = 0;
				break;
			case MainGame.GAME: //game starts
				MainGame.dPanel.repaint();
				handleKeyPresses();
				handleGun();
				handlePlayerChanges();
				handleEntities();
				damageEntity();
				handleWaveChanges();
				clicks = 0;
				//System.out.println(System.currentTimeMillis()-time);
				break;
			case MainGame.ONLINE_GAME:
				handleKeyPresses();
				handleEntities();
				handlePlayerChanges();
				handleWaveChanges();
				measureFPS();
				clicks = 0;
				break;
			case MainGame.MODES:
				for (Map.Entry<String, Button> set :
					MainGame.buttons.entrySet()) {
					if (set.getValue().bttnType == Button.DIFFICULTY) set.getValue().animate(timerReplica);;
				}
				clicks = 0;
				break;
			case MainGame.HELP:
				clicks = 0;
				break;
			case MainGame.GAME_SETTINGS_HOSTORCLIENT:
				clicks = 0;
				for (Map.Entry<String, Button> set :
					MainGame.buttons.entrySet()) {
					if (set.getValue().bttnType == Button.CLIENTCHOICE) set.getValue().animate(timerReplica);;
				}
				break;
			}
		}

		
		/**
		 * 2 tasks: for entity collisions and to damage entities 
		 */
		public void damageEntity() {
			for (int i = 0; i < MainGame.existingEntities.size(); i++) {
				for (int j = 0; j < MainGame.existingEntities.size(); j++) {
					if (i==j) continue;
					Entity tempEn1 = MainGame.existingEntities.get(i);
					Entity tempEn2 = MainGame.existingEntities.get(j);
					if (tempEn1.intersects(tempEn2) 
							&& !tempEn1.recievedDmg 
							&& !tempEn1.id.equalsIgnoreCase(tempEn2.id)
							&& !(tempEn1 instanceof Player 
									&& tempEn2 instanceof Bullet)) {
						//tempEn1.hp -= tempEn2.damage;
						//tempEn1.recievedDmg = true;
						MainGame.existingEntities.set(i, tempEn1);
						MainGame.existingEntities.set(j, tempEn2);
					}
					if (killEntity(MainGame.existingEntities.get(i), i)) i--;
				}
			}
		}

		// kills entity depending on the type
		boolean killEntity(Entity entity, int i)  {
			boolean entityDead = false;
			//if (entity.id.equalsIgnoreCase("bullet")) {
			if (entity instanceof Bullet) {
				if (entity.screenX > MainGame.PANW || entity.screenX < 0 || entity.screenY > MainGame.PANH || entity.screenY < 0) {
					MainGame.existingEntities.remove(i);
					entityDead = true;
				}
			}
			if (entity.hp <= 0) {
				MainGame.existingEntities.remove(i);
				entityDead = true;
				if (entity instanceof Zombie) score += 2; //increases score when killing zombies
				if (entity instanceof Player) {
					JOptionPane.showMessageDialog(null, "Score: " + score, "END", JOptionPane.INFORMATION_MESSAGE);
					MainGame.gameState = MainGame.MENU; //TODO temporary
					restartRound();
				}
			}
			return entityDead;
		}
		
		void handleKeyPresses() {
			if (KListener.isKeyDown('S') || KListener.isKeyDown(40)) {
				key = 'S';
				MainGame.player.move();
			}
			if (KListener.isKeyDown('A') || KListener.isKeyDown(37)) {
				key = 'A';
				MainGame.player.move();
			}
			if (KListener.isKeyDown('W') || KListener.isKeyDown(38)) {
				key = 'W';
				MainGame.player.move();
			}
			if (KListener.isKeyDown('D') || KListener.isKeyDown(39)) {
				key = 'D';
				MainGame.player.move();
			}
			key = 0; //reset keys since key is used directly in listeners class
		}

		/**
		 * Handling guns (recoil timer)
		 */
		void handleGun() {
			for (Gun gun: MainGame.guns) {
				if (timerReplica % 2 == 0 && gun.recoilTime > 0) {
					gun.recoilTime--;
				}
			}
		}

		
		/**
		 * Handles changes to the player (health, movement)
		 * For singleplayer, heals the player.
		 * For multiplayer, its only responsible for movement. 
		 */
		void handlePlayerChanges() {
			switch (MainGame.gameState) {
			case MainGame.GAME:
				if (timerReplica % 30 == 0 && MainGame.player.hp < MainGame.player.spawnHp) MainGame.player.hp += 1;
				if (!isKeyPressed) MainGame.player.moving = false;
				break;
			case MainGame.ONLINE_GAME:
				if (!isKeyPressed) MainGame.player.moving = false;
				break;
			}
		}
		
		
		/**
		 * Just made for testing and debugging
		 */
		void measureFPS() {
			totalFrames++;
			if (System.nanoTime() > lastTime + 1000000000) {
				lastTime = System.nanoTime();
				fps = totalFrames;
				totalFrames = 0;
				System.out.println(fps);
			}
		}

		/**
		 * Handles changes to waves.
		 * For singleplayer, the method handles changing wave settings, and starting new waves (and graphics).
		 * For multiplayer, the method is only responsible for graphics related changes.
		 */
		void handleWaveChanges() {
			switch (MainGame.gameState) {
			case MainGame.GAME:
				if (!Wave.getWaveState()) {
					Wave.changeSettings(); // changes the settings according to difficulty setting
					waveEndDelay++;
					if (waveEndDelay == 150) {
						Wave.startNewWave();
						waveEndDelay = 0;
					}
				}
				//graphics related below...
				if (newWave) {
					if (timerReplica % 10 == 0) GameGraphics.brdHighlightToggle = ! GameGraphics.brdHighlightToggle;
					if (timerReplica % 200 == 0) {
						if (score != 0 && score == 1000) scoreBrdOffset += 20;
					}
					if (timerReplica % 250 == 0) {
						GameGraphics.brdHighlightToggle = false;
						newWave = false; //use for animation
					}
				}
				break;
			case MainGame.ONLINE_GAME:
				//graphics related things...
				if (newWave) {
					if (timerReplica % 10 == 0) GameGraphics.brdHighlightToggle = ! GameGraphics.brdHighlightToggle;
					if (timerReplica % 200 == 0) {
						if (score != 0 && score == 1000) scoreBrdOffset += 20;
					}
					if (timerReplica % 250 == 0) {
						GameGraphics.brdHighlightToggle = false;
						newWave = false; //use for animation
					}
				}
				break;
			}
		}

		/**
		 * Depending on the state of the game (singleplayer or multiplayer LAN),
		 *  the tasks to handle entities are different.
		 * For singleplayer, damage is handled differently (on local computer).
		 * For multiplayer, damage isn't handled here.
		 */
		void handleEntities() {
			//Switch statement to for each case (game vs LAN based online)
			switch(MainGame.gameState) {
			case MainGame.GAME:
				for (Entity entity: MainGame.existingEntities) {
					entity.setSpritePhase(); // automatically doesn't do for bullets
					if (entity instanceof Player) {
						if (MainGame.client != null && MainGame.client.connections.get(entity.id) != null) {
							entity.move();
						} else {
							entity.move();
						}
					} else {
						entity.move();
						entity.setDmgDelay();
					}
				}
				break;
			case MainGame.ONLINE_GAME:
				for (Entity entity: MainGame.existingEntities) { 
					if (entity instanceof Player) {
						if (MainGame.client != null && MainGame.client.connections.get(entity.id) != null) {
							entity.clientMove();
						} else {
							//more to add later
							entity.setSpritePhase();
							entity.move();
						}
					} else {
						entity.setSpritePhase();
						entity.clientMove();
					}
				}
				break;
			}
		}

		void restartRound() {
			MainGame.existingEntities.clear();
			waveNum = 0;
			zombsLeft = 0;
			score = 0;
			try {
				MainGame.existingEntities.add(MainGame.player = new Player(MainGame.localUsername
						+ InetAddress.getLocalHost().getHostName() + MainGame.client.socket.getLocalPort(), MainGame.player.uid));
			} catch (UnknownHostException e) {e.printStackTrace();}
		}
	}

	/**
	 * @author codefromthefog
	 * A subclass made to handle each wave (not as separate objects)
	 *
	 */
	static class Wave {
		static int initialSpawnNum = 5; //starting num of zombies
		static int incrementNum = 5; //incrementing zombies each wave

		Wave() {}

		//to check if the current wave has been completed, and check the num of zombies left
		static boolean getWaveState() {
			zombsLeft = 0;
			boolean waveState = false;
			//checking the entities array to see if any zombies are left
			for (Entity entity: MainGame.existingEntities) {
				if (entity instanceof Zombie) zombsLeft++; 
			}
			if (zombsLeft > 0) waveState = true; //wave not complete
			return waveState; //if wave is complete
		}

		/**
		 * Starting a new wave.
		 * Involves increasing the current wave number, adding more zombies to entities ArrayList and
		 *  checking if a gun can be unlocked by the player.
		 */
		static void startNewWave() {
			waveNum++;
			for (int i = 1; i < initialSpawnNum + incrementNum*(waveNum - 1); i++) MainGame.existingEntities.add(new Zombie());	
			newWave = true;
			//guns unlock relative to which wave you're at
			switch (waveNum) {
			case 1:
				MainGame.pistol.unlocked = true;
				Player.equipped = MainGame.PISTOL;
				break;
			case 5: 
				MainGame.shotgun.unlocked = true;
				Player.equipped = MainGame.SHOTGUN;
				break;
			case 15: 
				MainGame.rifle.unlocked = true;
				Player.equipped = MainGame.RIFLE;
				break;
			}
		}
		
		
		/**
		 * Changes the settings of the rounds depending on the
		 * difficulty the game is currently at. Difficulty is changed
		 *  in the difficulty menu.
		 */
		static void changeSettings() {
			switch(MainGame.difficulty) {
			case MainGame.EASY:
				initialSpawnNum = 5;
				incrementNum = 3;
				break;
			case MainGame.MEDIUM:
				initialSpawnNum = 8;
				incrementNum = 6;
				break;
			case MainGame.HARD:
				initialSpawnNum = 11;
				incrementNum = 9;
				break;
			}
		}
	}

	static class KListener implements KeyListener {
		private static boolean keysDown[] = new boolean[256];

		public static boolean isKeyDown(int key) {
			return keysDown[key];
		}

		@Override
		public void keyPressed(KeyEvent e) {			
			if (e.getKeyCode() < 256) keysDown[e.getKeyCode()] = true;
			for (int i = 0; i < keysDown.length - 1; i++) {
				if (isKeyDown(i)) isKeyPressed = true;
			}
			if (e.getKeyChar() == 'e' && Player.equipped != 3) {
				if (MainGame.guns.get(Player.equipped).unlocked) {
					Player.equipped += 1;
				}
			}
			if (e.getKeyChar() == 'q' && Player.equipped != 1) {
				if (MainGame.guns.get(Player.equipped - 1).unlocked) {
					Player.equipped -= 1;
				}			
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() < 256) keysDown[e.getKeyCode()] = false;
			int counter = 0;
			for (int i = 0; i < keysDown.length - 1; i++) {
				if (!isKeyDown(i)) counter++;
			}
			if (counter == 255) isKeyPressed = false;
		}

		@Override
		public void keyTyped(KeyEvent e) {}
	}

	class MListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			clicks = e.getClickCount();
			if (MainGame.gameState == MainGame.GAME) {
				if (Player.equipped == MainGame.PISTOL && MainGame.pistol.recoilTime == 0) {
					MainGame.existingEntities.add(new Bullet(e.getX(), e.getY(), MainGame.PISTOL));
					MainGame.pistol.recoilTime = Pistol.totalRecoilTime;
				}
				if (Player.equipped == MainGame.RIFLE && clicks > 0 && MainGame.rifle.recoilTime == 0)  {
					MainGame.existingEntities.add(new Bullet(e.getX(), e.getY(), MainGame.RIFLE));
					MainGame.rifle.recoilTime = Rifle.totalRecoilTime;
				}
				if (Player.equipped == MainGame.SHOTGUN && MainGame.shotgun.recoilTime == 0) {
					for (int i = 0; i < 5; i++) {
						MainGame.existingEntities.add(new Bullet(e.getX(), e.getY(), MainGame.SHOTGUN));
					}
					MainGame.shotgun.recoilTime = Shotgun.totalRecoilTime;
				}

			}
			if (MainGame.gameState == MainGame.ONLINE_GAME) {
				Client.sendPacket(Client.SHOOT, null); // was changed from MainGame.client -> Client, change back if it doesnt work

			}
		} //ADD OVERRIDE WHEN USING
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
	}

	class MMListener implements MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {}
		@Override
		public void mouseMoved(MouseEvent e) {
			mx = e.getX();
			my = e.getY();
		}
	}
}
