package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;

import javax.swing.Timer;

import software_project.Entity;
import software_project.Player;

/** Handles server side processes such as sending packets, establishing connections, and handling incoming packets.
 * @author Osman Idris
 *
 */
public class Server extends Thread {

	boolean running = false;
	private static DatagramSocket socket;
	static HashMap<String, Connection> connections;
	final static int LOGIN = 1, MOVE = 2, SHOOT = 3, 
			DISCONNECT = 4, ENTITY_LIST = 5, EQUIPPED_CHANGE = 6, 
			STARTED_NEWWAVE = 7, ROUND_RESTART = 8; 
	static DataHandler dHandler = new DataHandler();
	static Data data = new Data();
	static MapVisualizer mapVis;
	Timer roundTimer;

	/** Constructor that creates a new DatagramSocket and initializes the connections HashMap.
	 * 
	 */
	public Server() {
		try {
			socket = new DatagramSocket();

		} catch (SocketException e) {e.printStackTrace();}
		connections = new HashMap<String, Connection>();
	}

	
	@Override
	public void run() {
		System.out.println("Server now online");
		running = true;
		roundTimer = new Timer(0, dHandler.new TL());
		mapVis = new MapVisualizer();

		while (running) {
			//handles receiving packets.
			handlePacket(receivePacket());
			if (connections.size() > 1 && !roundTimer.isRunning()) {
				roundTimer.start();
			}
		}
		
		System.out.println("Server Offline");
		socket.close();
	}

	/** Receives incoming user packets.
	 * @return Returns the received packet.
	 */
	private DatagramPacket receivePacket() {
		byte[] recvBuf = new byte[2048];
		DatagramPacket rPacket = new DatagramPacket(recvBuf, recvBuf.length);
		try {socket.receive(rPacket);} catch (IOException e) {e.printStackTrace();}
		return rPacket;
	}

	/** Handles packets by first unpacking the packet and interpreting by storing parts of it
	 * into variables based on the format. 
	 * Types of packets include login, movement, shooting, disconnecting, changing equipment (guns),
	 *  and wanting to restart the round.
	 * @param rPacket The received packet.
	 */
	private void handlePacket(DatagramPacket rPacket) {
		String[] dataArray = new String(rPacket.getData(), rPacket.getOffset(), 
				rPacket.getLength()).split("\\s+");
		int packetType = Integer.parseInt(dataArray[0]);
		String username = dataArray[1];
		String hostName = dataArray[2];
		int port = Integer.parseInt(dataArray[3]);
		int x,y;

		switch (packetType) {
		case LOGIN: 
			//LOGGING IN PACKET: dataArray format: (packetType, username, hostName, port, uid)
			String uid = dataArray[4];
			if (!connectionExists(username, hostName, port)) {
				connections.put(username + hostName + port, new Connection(username, hostName, port, uid));
				Data.existingEntities.add(new Player(username + hostName + port, uid));
			}
			this.sendPacket(packetType, username, hostName, port, uid);
			sendMiscPacket(STARTED_NEWWAVE, null);
			break;
		case MOVE:
			//MOVING PACKET: dataArray format: (packetType, username, hostName, port, x, y, lSideFacing)
			x = Integer.parseInt(dataArray[4]);
			y = Integer.parseInt(dataArray[5]);
			connections.get(username+hostName+port).setCoords(x, y);
			connections.get(username+hostName+port).moving = true;
			connections.get(username+hostName+port).lSideFacing = Boolean.parseBoolean(dataArray[6]);
			break;
		case SHOOT:
			//SHOOTING PACKET: dataArray format: (packetType, username, hostName, port, x, y, mx, my)
			int mx = Integer.parseInt(dataArray[6]);
			int my = Integer.parseInt(dataArray[7]);
			connections.get(username + hostName + port).hasShot = true;
			connections.get(username + hostName + port).setMouseCoords(mx, my);
			break;
		case DISCONNECT:
			//DISCONNECTING PACKET: dataArray format: (packetType, username, hostName, port, uid)
			username = dataArray[1];
			hostName = dataArray[2];
			port = Integer.parseInt(dataArray[3]);
			String uid1 = dataArray[4];
			this.removeConnection(username, hostName, port);
			this.sendPacket(packetType, username, hostName, port, uid1);
			break;
		case EQUIPPED_CHANGE:
			username = dataArray[1];
			hostName = dataArray[2];
			port = Integer.parseInt(dataArray[3]);
			connections.get(username+hostName+port);
			break;
		case ROUND_RESTART:
			username = dataArray[1];
			hostName = dataArray[2];
			port = Integer.parseInt(dataArray[3]);
			if (dataArray[4].equalsIgnoreCase("false")) {
				Server.connections.get(username+hostName+port).roundRestartChoice = false;
			}
			if (dataArray[4].equalsIgnoreCase("true")) {
				Server.connections.get(username+hostName+port).roundRestartChoice = true;
			}
			if (getRoundRestartChoices() == connections.size()) DataHandler.restartRound();
			else this.shutdown();
			break;
		}
	}

	/** Sending the packet to the specified connection.
	 * @param packetType The type of packet denoted by an integer. 
	 * @param username Username of the player that needs the packet.
	 * @param hostName Computer name that the packet needs to be sent to.
	 * @param port Port number of the client computer.
	 * @param uid Unique identifier.
	 */
	private void sendPacket(int packetType, String username, String hostName, int port, String uid) {

		String data = "";

		for (HashMap.Entry<String, Connection> connection: connections.entrySet()) {
			String key = connection.getKey();

			if (key.equalsIgnoreCase(username + hostName + port)) {
				for (HashMap.Entry<String, Connection> connection1: connections.entrySet()) {
					String key1 = connection1.getKey();
					Connection value1 = connection1.getValue();
					if (!key1.equalsIgnoreCase(key)) {
						data = packetType + " " + value1.getUsername() + " " + value1.getHostName() + 
								" " + value1.getPort() + " " + value1.getUID();
						DatagramPacket sendPacket1 = new DatagramPacket(data.getBytes(), 
								data.getBytes().length, connection.getValue().getAddress(),
								connection.getValue().getPort());
						try {socket.send(sendPacket1);} catch (IOException e) {e.printStackTrace();}
					}
				}
			} else {
				//send connection to
				data = packetType + " " + username + " " + hostName + " " + port + " " + uid;
				DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), 
						data.getBytes().length, connection.getValue().getAddress(),
						connection.getValue().getPort());
				try {socket.send(sendPacket);} catch (IOException e) {e.printStackTrace();}

			}
		}
	}

	/** A different type of packet that may hold more miscellaneous information. 
	 * Will implement more later.
	 * @param packetType
	 * @param miscInfo
	 */
	static void sendMiscPacket(int packetType, String miscInfo) {
		if (miscInfo != null) miscInfo = miscInfo.trim();
		switch (packetType) {
		case EQUIPPED_CHANGE:
			//data format: packetType
			sendToAllPlayers(packetType + " " + miscInfo);
			break;
		case ENTITY_LIST:
			//data format: arraylist
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream(64000);
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.flush();
				oos.writeObject(Data.existingEntities);
				byte[] sendBytes = baos.toByteArray();
				for (HashMap.Entry<String, Connection> connection: connections.entrySet()) {
					DatagramPacket packet = new DatagramPacket(sendBytes, sendBytes.length,
							connection.getValue().getAddress(), connection.getValue().getPort());
					socket.send(packet);
				}
			} catch (IOException e) {e.printStackTrace();}
			break;
		case STARTED_NEWWAVE:
			//data format: packetType, waveNum
			sendToAllPlayers(packetType + " " + Data.waveNum);
			break;
		case ROUND_RESTART:
			//data format: packetType
			sendToAllPlayers(packetType + "");
			break;
		}
	}

	/** Sends the specified packet with data to all the players in the list of connections.
	 * @param data The data of type String that needs to be sent.
	 */
	public static void sendToAllPlayers(String data) {
		for (HashMap.Entry<String, Connection> connection: connections.entrySet()) {
			byte[] sendBytes = data.getBytes();
			DatagramPacket packet = new DatagramPacket(sendBytes, sendBytes.length,
					connection.getValue().getAddress(), connection.getValue().getPort());
			try {socket.send(packet);} catch (IOException e) {e.printStackTrace();}
		}
	}

	/** For the restarting of the round (after confirmation with all players). 
	 * More to add.
	 * @return The amount of true choices (int).
	 */
	public int getRoundRestartChoices() {
		int numChoices = 0;
		for (HashMap.Entry<String, Connection> connection: connections.entrySet()) {
			if (connection.getValue().roundRestartChoice) numChoices++;
		}
		return numChoices;
	}

	
	/** Checking if the specified connection exists.
	 * @param username player's username of type String
	 * @param hostName player's computer name of type String
	 * @param port computer's port number of type int.
	 * @return
	 */
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
	}

	public int getServerPort() {
		System.out.println("Port: " + socket.getLocalPort());
		return socket.getLocalPort();
	}
	public String getServerAddress() {
		String servAddress = "";
		try {
			servAddress = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			servAddress = "Unknown";
		}
		System.out.println(servAddress);
		return servAddress;
	}
	private void shutdown() {running = false;}
}
