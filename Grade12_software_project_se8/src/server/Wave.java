package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;

import software_project.Entity;
import software_project.Player;
import software_project.Zombie;

/** Contains methods for waves and settings for each wave (that can be manipulated).
 * @author Osman Idris
 * 
 */
class Wave {
		static int initialSpawnNum = 5;
		static int incrementNum = 5;

		Wave() {}

		/**
		 * Gets the state of the wave (whether the wave is complete or incomplete)
		 * @return waveState Boolean value of true if the wave is complete and false if not.
		 */
		static boolean getWaveState() {
			Data.zombsLeft = 0;
			boolean waveState = false;
			for (Entity entity: Data.existingEntities) {
				if (entity instanceof Zombie) Data.zombsLeft++;
			}
			if (Data.zombsLeft > 0) waveState = true; //wave not complete
			return waveState; //if wave is complete
		}

		/**
		 * Starts a new wave and handles tasks that need to be done on the start of the new wave.
		 */
		static void startNewWave() {
			System.out.println("new wave");
			Data.waveNum++;
			for (int i = 1; i < initialSpawnNum + incrementNum*(Data.waveNum - 1); i++) Data.existingEntities.add(new Zombie());	
			Data.newWave = true;
			switch (Data.waveNum) {
			case 1:
				Data.pistol.unlocked = true; //same for all players
				Server.sendMiscPacket(Server.EQUIPPED_CHANGE, "" + Data.PISTOL);
				break;
			case 5: 
				Data.shotgun.unlocked = true;
				Server.sendMiscPacket(Server.EQUIPPED_CHANGE, "" + Data.SHOTGUN);
				break;
			case 15: 
				Data.rifle.unlocked = true;
				Server.sendMiscPacket(Server.EQUIPPED_CHANGE, "" + Data.RIFLE);
				break;
			}
		}

		/**
		 * Changes settings of the waves based on the difficulty settings.
		 * The settings being manipulated have to do with initial number of zombies spawning and
		 *  the increment of zombies each wave.
		 */
		static void changeSettings() {
			switch(Data.difficulty) {
			case Data.EASY:
				initialSpawnNum = 5;
				incrementNum = 3;
				break;
			case Data.MEDIUM:
				initialSpawnNum = 8;
				incrementNum = 6;
				break;
			case Data.HARD:
				initialSpawnNum = 11;
				incrementNum = 9;
				break;
			}
		}
	}