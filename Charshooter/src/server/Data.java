package server;

import java.util.concurrent.CopyOnWriteArrayList;

import software_project.Entity;
import software_project.Gun;
import software_project.Pistol;
import software_project.Rifle;
import software_project.Shotgun;

/** Holds most of the variables and data used in performing operations.
 * @author Osman Idris
 *
 */
public class Data {
	
	/* Here a CopyOnWriteArrayList is being used so that no exceptions occur
	 * during the manipulation of values in the ArrayList which initially caused errors
	 * between synchronized threads on both server and client side. 
	 */
	static CopyOnWriteArrayList<Entity> existingEntities = new CopyOnWriteArrayList<Entity>();
	static int zombsLeft, waveNum, waveEndDelay = 0;
	static boolean newWave;
	static int difficulty;
	static Gun pistol, shotgun, rifle;
	final static int EASY = 1, MEDIUM = 2, HARD = 3;
	final static int PISTOL = 1, SHOTGUN = 2, RIFLE = 3;
	
	public Data() {
		pistol = new Pistol(); 
		shotgun = new Shotgun();
		rifle = new Rifle();
	}
	
	
	
}
