package server;

import java.awt.Point;
import java.net.InetAddress;
import java.net.UnknownHostException;


/** Each new connection to the server is stored as an object of this class.
 * Players on the server are treated as connections and their values are stored in this class.
 * @author Osman Idris
 *
 */
public class Connection {
	private String username = "";
	private String hostName = "";
	private String uid = "";
	private InetAddress address;
	private int port;
	private int x,y;
	private int mx, my;
	private int equipped = Data.PISTOL;
	private int score;
	public boolean moving = false, lSideFacing = false;
	public boolean roundRestartChoice = false;
	public int recoilTime = 0; //for guns
	public boolean hasShot = false;
	public int spritePhase = 0;
	
	/** Constructor creating a new object of the Connection class.
	 * @param username First part of the string data taken from incoming login request packets.
	 * @param hostName Second part of the string data.
	 * @param port Third part of the string data, parsed into an integer
	 * @param uid Last part of the string data, will be implemented later on.
	 */
	public Connection(String username, String hostName, int port, String uid) {
		this.username = username;
		this.hostName = hostName;
		this.port = port;
		this.uid = uid;
		
		try {
			address = InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {e.printStackTrace();}
	}
	
	/** Second constructor.
	 * @param username First part of the string data taken from incoming login request packets.
	 * @param hostName Second part of the string data.
	 * @param port Third part of the string data, parsed into an integer
	 * @param x xcoordinate of where the user is
	 * @param y ycoordinate of where the user is
	 */
	Connection(String username, String hostName, int port, int x, int y) {
		this.username = username;
		this.hostName = hostName;
		this.port = port;
		this.x = x;
		this.y = y;
		
		try {
			address = InetAddress.getByName(hostName);
		} catch (UnknownHostException e) {e.printStackTrace();}
	}
	
	public String getUsername() {return username;}
	public String getHostName() {return hostName;}
	public InetAddress getAddress() {return address;}
	public int getPort() {return port;}
	public Point getCoords() {return new Point(x,y);}
	public Point getMouseCoords() {return new Point(mx,my);}
	public void setMouseCoords(int mx, int my) {
		this.mx = mx;
		this.my = my;
	}
	
	/** Sets the coordinates of the player on the server to the given location.
	 * Uses coordinates from incoming packets.
	 * @param x
	 * @param y
	 */
	public void setCoords(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getEquipped() {return equipped;}
	void setEquipped(int equipped) {this.equipped = equipped;}
	void addScore() {score += 2;}
	public String getUID() {return uid;}
	
	/**
	 * Sets the phase of the sprites for the player at the given time. 
	 * This could also be handled on client side, but its done here to make sure sprite phase
	 * for each player is the same on every player's screen.
	 */
	public void setSpritePhase() {
		if (moving) { //if player is moving cycle 3-6
			if (DataHandler.timerReplica % 17 == 0) spritePhase++;
			if (spritePhase == 6 || spritePhase <= 2) spritePhase = 3;
		} else { //if not moving, cycle 1-2
			if (DataHandler.timerReplica % 20 == 0) spritePhase++;
			if (spritePhase >= 3) spritePhase = 1;
		} 
	}
}
