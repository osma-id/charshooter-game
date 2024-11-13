package software_project;
/* Client.java
 * By Osman Idris
 * 05-31-2023 (MM-DD-YY)
 * Client side of the CharShooter game -- used only when LAN is activated.
 * JAVA SE-8 !!!
 * 
 */
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JOptionPane;

import server.Connection;

public class Client extends Thread {

	static DatagramSocket socket; 
	static InetAddress address;
	static int port;
	static HashMap<String, Connection> connections;
	private byte[] buf;
	static ByteArrayInputStream bais;
	static ObjectInputStream ois;
	boolean online = false;
	//TODO: convert to enum later
	final static int LOGIN = 1, MOVE = 2, SHOOT = 3, 
			DISCONNECT = 4, ENTITY_LIST = 5, EQUIPPED_CHANGE = 6, 
			STARTED_NEWWAVE = 7, ROUND_RESTART = 8; //packets

	public Client() {
		//constructor 1
		try {
			socket = new DatagramSocket();
			address = InetAddress.getByName(""); //add client computer name
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (java.net.UnknownHostException e) {
			e.printStackTrace();
		}
		buf = new byte[64000];
		connections = new HashMap<String, Connection>();
	}
	
	public Client(String specifiedHost, int specifiedPort) {
		//constructor 2
		port = specifiedPort;
		try {
			socket = new DatagramSocket();
			System.out.println(specifiedHost);
			System.out.println("specified port: " + port);
			address = InetAddress.getByName(specifiedHost);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (java.net.UnknownHostException e) {
			e.printStackTrace();
		}
		buf = new byte[64000];
		connections = new HashMap<String, Connection>();
	}

	@Override
	public void run() {
		online = true;
		String username = MainGame.localUsername;
		String hostName = "";
		int port = socket.getLocalPort();
		//Gets the hostname for this computer.
		try {hostName = InetAddress.getLocalHost().getHostName();} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		MainGame.player.id = username + hostName + port; //stores this in the player id
		sendPacket(LOGIN, null); //sends a login packet to the server.
		while (online) {
			receivePacket();
		}
		disconnect();
	}

	/**
	 * Creates a new byte array, getting rid of the old one.
	 */
	private void clearBuf() {
		buf = new byte[64000];
	}

	/**
	 * Recieves incoming packets.
	 */
	private void receivePacket() {
		clearBuf();
		DatagramPacket rPacket = new DatagramPacket(buf, buf.length); //receive packet
		try {socket.receive(rPacket);} catch (IOException e) {e.printStackTrace();}
		handlePacket(rPacket);
	}

	/**
	 * Categorizes and initiates tasks based on the type of packets. 
	 * @param rPacket The received packet from the server. 
	 */
	private void handlePacket(DatagramPacket rPacket) {
		String[] dataArray = new String(rPacket.getData(), rPacket.getOffset(), 
				rPacket.getLength()).split("\\s+"); //Gets data from the receiving packet and stores it.
		int packetType = 0;
		int counter = 0;

		//the first value of the packet will always be the packet type.
		//dataArray formats are shown in each case below.
		try {packetType = Integer.parseInt(dataArray[0]);} 
		catch (NumberFormatException e) {
			packetType = ENTITY_LIST;
		} 
		
		//declaring beforehand so that variables are not undefined after the switch statement.
		String username;
		String hostName;
		int port;
		int x,y,mx,my;
		switch (packetType) {
		case LOGIN: 
			//LOGGING IN PACKET: dataArray format: (packetType, username, hostName, port, uid)
			username = dataArray[1];
			hostName = dataArray[2];
			port = Integer.parseInt(dataArray[3]);
			String uid = dataArray[4];
			if (connectionExists(username, hostName, port)) break;
			connections.put(username + hostName + port, new Connection(username, hostName, port, uid));
			MainGame.existingEntities.add(new Player(username + hostName + port, uid));
			break;
		case SHOOT:
			//SHOOTING PACKET: dataArray format: (packetType, username, hostName, port, x, y, mx, my)
			username = dataArray[1];
			hostName = dataArray[2];
			port = Integer.parseInt(dataArray[3]);
			x = Integer.parseInt(dataArray[4]);
			y = Integer.parseInt(dataArray[5]);
			mx = Integer.parseInt(dataArray[6]);
			my = Integer.parseInt(dataArray[7]);
			shoot(username, hostName, port, x, y, mx, my);
			break;
		case DISCONNECT:
			//DISCONNECTING PACKET: dataArray format: (packetType, username, hostName, port)
			username = dataArray[1];
			hostName = dataArray[2];
			port = Integer.parseInt(dataArray[3]);
			this.removeConnection(username, hostName, port);
			break;
		case ENTITY_LIST:
			//ENTITY_LIST: format (existingEntities)
			try {
				bais = new ByteArrayInputStream(buf);
				ois = new ObjectInputStream(bais);
				CopyOnWriteArrayList<Entity> tempList = (CopyOnWriteArrayList<Entity>)ois.readObject();
				removeDeadEntities(tempList);
				for (Entity entity: tempList) {
					handleEntity(entity);
				}
				ois.close();
				bais.close();
			} catch (IOException | ClassNotFoundException  e1) {
				e1.printStackTrace();
			}
			break;
		case EQUIPPED_CHANGE: 
			//EQUIPPED CHANGE PACKET: format (packetType, equipped)
			int equipped = Integer.parseInt(dataArray[1]);
			switch (equipped) {
			case MainGame.PISTOL:
				MainGame.pistol.unlocked = true;
				break;
			case MainGame.SHOTGUN: 
				MainGame.shotgun.unlocked = true;
				break;
			case MainGame.RIFLE: 
				MainGame.rifle.unlocked = true;
				break;
			}
			Player.equipped = equipped;
			break;
		case STARTED_NEWWAVE: 
			//STARTED_NEWWAVE: format (packetType, waveNUm)
			Listeners.newWave = true;
			Listeners.waveNum = Integer.parseInt(dataArray[1]);
			break;
		case ROUND_RESTART:
			int choice = JOptionPane.showOptionDialog(null, "Restart round? ", 
					null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
			switch (choice) {
			case -1: case 1:
				sendPacket(ROUND_RESTART, "false");
				break;
			case 0:
				sendPacket(ROUND_RESTART, "true");
				break;
			}
			break;
		}
	}

	/**
	 * Sends packets to the server always including the packetType, username, hostName, and port.
	 * @param packetType An integer value that dictates the packet type being sent to the server.
	 * @param miscInfo An object of type String that sends extra information like restarting round confirmation. More uses will be added later.
	 */
	static void sendPacket(int packetType, String miscInfo) {
		byte[] buf = new byte[2048];

		String username = MainGame.localUsername;
		String hostName = "";
		int port = socket.getLocalPort();
		try {hostName = InetAddress.getLocalHost().getHostName();} 
		catch (UnknownHostException e) {e.printStackTrace();}
		int x = MainGame.player.mapX;
		int y = MainGame.player.mapY;
		String uid = MainGame.player.uid;
		boolean lSideFacing = MainGame.player.lSideFacing;

		String data = "";
		switch (packetType) {
		case LOGIN: 
			//LOGGING IN PACKET: dataArray format: (packetType, username, hostName, port, uid)
			data = packetType + " " + username + " " + hostName + " " + port + " " + uid;
			break;
		case MOVE:
			//MOVING PACKET: dataArray format: (packetType, username, hostName, port, x, y, lSideFacing)
			data = packetType + " " + username + " " + hostName + " " + port + " " + x + " " + y + " " + lSideFacing;
			//System.out.println(data);
			break;
		case SHOOT:
			//SHOOTING PACKET: dataArray format: (packetType, username, hostName, port, x, y, mx, my)
			int mx = MainGame.listeners.mx + MainGame.player.mapX - MainGame.player.screenX;
			int my = MainGame.listeners.my + MainGame.player.mapY - MainGame.player.screenY;
			data = packetType + " " + username + " " + hostName + " " + port + " " + x + " " + y + " " 
					+ mx + " " + my;
			break;
		case DISCONNECT:
			//DISCONNECTING PACKET: dataArray format: (packetType, username, hostName, port)
			data = packetType + " " + username + " " + hostName + " " + port;
			break;
		case EQUIPPED_CHANGE: 
			//EQUIPPED CHANGE PACKET: format: (packetType, username, hostName, port, equipped)
			data = packetType + " " + username + " " + hostName + " " + port + " " + Player.equipped;
			break;
		case ROUND_RESTART:
			//ROUND_RESTART PACKET: format: (packetType, username, hostName, port, true/false)
			data = packetType + " " + username + " " + hostName + " " + port + " " + miscInfo;
			break;
		}

		buf = data.getBytes();
		if (packetType == LOGIN) System.out.println("login request sent by " + username); 
		DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, address, Client.port);
		try {socket.send(sendPacket);} catch (IOException e) {e.printStackTrace();}
	}

	public void shoot(String username, String hostName, int port, int x, int y, int mx, int my) {

	}
	
	public void removeDeadEntities(CopyOnWriteArrayList<Entity> entList) {
		for (int i = 0; i < MainGame.existingEntities.size(); i++) {
			if (!existsInArray(MainGame.existingEntities.get(i), entList)) {
				MainGame.existingEntities.remove(i);
				i--;
			}
		}
	}
	
	public void handleEntity(Entity entity) {
		if (existsInArray(entity, MainGame.existingEntities)) {
			if (!entity.uid.equalsIgnoreCase(MainGame.player.uid)) {
				MainGame.existingEntities.get(getPosInArray(entity)).modifyEntity(
						entity.xx, entity.yy, entity.hp, entity.lSideFacing, entity.spritePhase);
			} else { //if local player
				Player p1 = (Player) MainGame.existingEntities.get(getPosInArray(entity));
				p1.modifyLocalPlayer(entity.hp);
				MainGame.existingEntities.set(getPosInArray(entity), p1);
			}
		} else {
			MainGame.existingEntities.add(entity);
		}
		
	}
	
	public boolean existsInArray(Entity entity, CopyOnWriteArrayList<Entity> entList) {
		for (Entity entity1: entList) {
			if (entity1.uid.equalsIgnoreCase(entity.uid)) return true;
		}
		return false;
	}
	
	public int getPosInArray(Entity entity) {
		int pos = -1;
		for (Entity entity1: MainGame.existingEntities) {
			pos++;
			if (entity1.uid.equalsIgnoreCase(entity.uid)) break;
		}
		return pos;
	}

	public boolean connectionExists(String username, String hostName, int port) {
		for (HashMap.Entry<String, Connection> connection: connections.entrySet()) {
			String key = connection.getKey();

			if (key.equalsIgnoreCase(username + hostName + port)) {
				return true;
			}
		}
		return false;
	}

	private void removeConnection(String username, String hostName, int port) {
		connections.remove(username + hostName + port);
		for (int i = 0; i < MainGame.existingEntities.size(); i++) {
			if (MainGame.existingEntities.get(i) instanceof Player) {
				if (MainGame.existingEntities.get(i).id.equalsIgnoreCase(username+hostName+port)) {
					MainGame.existingEntities.remove(i);
					break;
				}
			}
		}
	}

	private void disconnect() {
		sendPacket(04, null);
		online = false;
		socket.close();
	}

}
