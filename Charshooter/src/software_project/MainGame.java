package software_project;
/* MainGame.java
 * By Osman Idris
 * 05-31-2023 (MM-DD-YY)
 * Top-down zombie shooter with different guns and modes. 
 * JAVA SE-8 !!!
 * 
 * Game version: 0.1.6
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import server.Server;
import software_project.Listeners.KListener;

public class MainGame {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MainGame();
			}
		});
	}

	final static int PANW = 1200; //screen width
	final static int PANH = 840; // height

	
	JFrame window;
	static GameGraphics gameGraphics = new GameGraphics();
	public static GameGraphics.DrawingPanel dPanel = gameGraphics.new DrawingPanel();
	static CopyOnWriteArrayList<Entity> existingEntities = new CopyOnWriteArrayList<Entity>();
	static CopyOnWriteArrayList<Entity> existingEntities2 = new CopyOnWriteArrayList<Entity>(); //for the purposes of testing

	static HashMap<String, Button> buttons;

	static Entity player; 
	MapManager map = new MapManager();
	static Timer timer;
	//TODO: change to enum rather than final int
	final static int MENU = 1, GAME = 2, ONLINE_GAME = 3, MODES = 4, HELP = 5, GAME_SETTINGS = 6, GAME_SETTINGS_HOSTORCLIENT = 7; // MENU OPTIONS, DO NOT CHANGE!!
	final static int EASY = 1, MEDIUM = 2, HARD = 3; // GAME MODES, DO NOT CHANGE!!
	final static int NOT_SELECTED = 0, SINGLEPLAYER = 1, MULTIPLAYER = 2; // GAME TYPES
	final static int PISTOL = 1, SHOTGUN = 2, RIFLE = 3; // GUNS, DO NOT CHANGE
	static String localUsername = "";
	static Gun pistol, shotgun, rifle; //declaring variables
	static ArrayList<Gun> guns = new ArrayList<Gun>();
	static int previousState = MENU; //previous state of the game
	static int gameState = MENU; // current state of the game
	static int difficulty = EASY;
	static int gameType = NOT_SELECTED;
	static boolean roundRestart;
	static ImageManager images = new ImageManager(); 
	static Listeners listeners = new Listeners();

	static Server server;
	static Client client;

	MainGame() {
		window = new JFrame("CharShooter Game");
		window.setResizable(false); 
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dPanel.addKeyListener(new KListener());
		dPanel.addMouseMotionListener(listeners.new MMListener());
		dPanel.addMouseListener(listeners.new MListener());
		dPanel.setLayout(null);
		dPanel.setFocusable(true);
		window.add(dPanel);
		
		//Will get the username which is important for LAN (multiplayer)
		localUsername = JOptionPane.showInputDialog("Name: "); 
		if (localUsername == null) System.exit(1); //terminate if you press cancel on the inputdialog
		MainGame.existingEntities.add(MainGame.player = new Player()); //adding the player as the first entity
		
		buttons = new HashMap<String, Button>();
		Button.setupButtons(buttons); //setting up buttons using a static method

		//all buttons (with preset values) added to the panel
		dPanel.add(buttons.get("Play")); 
		dPanel.add(buttons.get("Difficulty"));
		dPanel.add(buttons.get("Help"));
		dPanel.add(buttons.get("Exit"));
		
		//initializing the guns from earlier
		pistol = new Pistol(); 
		shotgun = new Shotgun(); 
		rifle = new Rifle();
		guns.add(pistol); guns.add(shotgun); guns.add(rifle); //adding them to the guns arraylist
		
		window.validate();
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);

		//Using a timer to run the game (rather than a while loop)
		timer = new Timer(1, listeners.new TL());
		timer.start();
		Thread thread1 = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (dPanel != null) {
						dPanel.repaint();
						//paint IMMEDIATELY!!
					}
				}
			}});
		thread1.start();
	}
	
	//adding the buttons of the specified type to the panel (screen)
	static void addButtons(int type) {
		//for-each loop for hashmaps
		for (Map.Entry<String, Button> set:buttons.entrySet()) {
			if (set.getValue().bttnType == type) dPanel.add(set.getValue());
		}
	}
	
	//removing the buttons from the screen
	static void removeButtons(int type) {
		for (Map.Entry<String, Button> set :
			buttons.entrySet()) {
			if (set.getValue().bttnType == type) dPanel.remove(set.getValue());
		}
	}
	
	//checks for change, and updates previousState variable; used to make changes according to events
	static boolean stateChanged() {
		if (previousState == gameState) return false;
		previousState = gameState;
		return true;
	}

}
