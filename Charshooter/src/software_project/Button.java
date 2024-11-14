package software_project;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import server.Server;

/** A class made for buttons in the game. 
 * @author Osman Idris
 *
 */
public class Button extends JButton {
	private Color color;
	private Color colorOver;
	private Color brdrColor;
	private Color clickClr;
	private Color currentClr;

	private String text;
	private boolean over = false;
	private int brdrRadius = 22;
	private int animDist = 0;

	int bttnType = 0;
	//TODO: change to enums
	final static int INVALID = 0, MENU = 1, DIFFICULTY = 2, GAMETYPE = 3, CLIENTCHOICE = 4, BACK = 5, EXIT = 6;

	int animStep = 0; //used for animation
	final static double range = 30;
	final static int PANW = 1200;
	final static int PANH = 840;
	public boolean clicked = false;
	public boolean clickAnim = false;
	public boolean hovering = false; //when hovering over the button
	public boolean hoverAnim = false;
	public boolean animating = false;
	boolean reversing = false; //when the button returns to its default color
		
	SwingWorker<Void, Void> taskDelayWorker;

	public Color getClr() {return color;}
	public Color getBorderColor() {return brdrColor;}
	public Color getClickClr() {return clickClr;}
	public int getBrdrRad() {return brdrRadius;}

	public void setClr(Color color) {
		this.color = color;
		this.currentClr = color;
	}
	public void setBorderColor(Color brdrColor) {this.brdrColor = brdrColor;}
	public void setClickClr(Color clickClr) {this.clickClr = clickClr ;}
	public void setBrdrRad(int brdrRadius) {this.brdrRadius = brdrRadius;}
	public void setText2(String text) {
		this.text = text;
		this.setText(text);
	}

	public Button() {
		super();
		color = Color.GREEN;
		brdrColor = color.darker().darker().darker();
		colorOver = color.darker().darker();
		currentClr = color;
		clickClr = color.darker().darker().darker().darker();
		setText2("");
		setBackground(color);
		setFont(GameGraphics.vt323.deriveFont(55f));
		setContentAreaFilled(false);
		setBorderPainted(false);
		setFocusable(false);
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent me) {

				hovering = true;
				hoverAnim = true;
				animStep = 0;
				currentClr = color;
				reversing = false;
			}

			@Override
			public void mouseExited(MouseEvent me) {
				if (!clickAnim) {
					hovering = false;
				}
				hovering = false;

			}

			@Override
			public void mousePressed(MouseEvent me) {
				animating = true;
				clicked = true;
				clickAnim = true;
				animStep = 0;
				reversing = false;
				if (hovering) currentClr = colorOver;
				else currentClr = color;
			}

			@Override
			public void mouseReleased(MouseEvent me) {}
		});
	}

	public ActionListener getActionL() {return this.actionListener;}
	public int getAnimDist() {return animDist;}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2.setColor(getBorderColor());
		g2.fillRoundRect(0, 0, getWidth(), getHeight(), brdrRadius, brdrRadius);
		g2.setColor(getBackground());
		//  Border set 2 Pix
		g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, brdrRadius, brdrRadius);
		super.paintComponent(g);
	}

	/**
	 * Made for button animation. Do not remove.
	 * Interpolates to find a new value based on two of the old values.
	 * @param ratio
	 * @param value1
	 * @param value2
	 * @return
	 */
	int interpolate(double ratio, int value1, int value2) {
		return (int)Math.abs((ratio * value1) + ((1 - ratio) * value2));
	}

	public void animate(int elapsedTime) {
		if (clickAnim) playClickAnim();

		if (hoverAnim && !clickAnim && animStep >= 0) playHoverAnim();

		if (reversing && elapsedTime % 500 == 0) reversing = false;
	}

	private void playClickAnim() {
		if (!reversing) {
			animating = true;
			int red = interpolate(animStep/range, clickClr.getRed(), currentClr.getRed());
			int green = interpolate(animStep/range, clickClr.getGreen(), currentClr.getGreen());
			int blue = interpolate(animStep/range, clickClr.getBlue(), currentClr.getBlue());
			currentClr = new Color(red, green, blue);
			if (currentClr.equals(clickClr)) {
				reversing = true;
				animStep = (int) range;
			}
			setBackground(currentClr);
			animStep++;
		}
		if (reversing) {
			if (hovering) {
				int red = interpolate(animStep/range, currentClr.getRed(), colorOver.getRed());
				int green = interpolate(animStep/range, currentClr.getGreen(), colorOver.getGreen());
				int blue = interpolate(animStep/range, currentClr.getBlue(), colorOver.getBlue());
				if (currentClr.equals(colorOver)) {
					reversing = false;
					animStep = 0;
					clickAnim = false;
					animating = false;
				}
				currentClr = new Color(red, green, blue);
				setBackground(currentClr);
				animStep--;

			} else {
				int red = interpolate(animStep/range, currentClr.getRed(), color.getRed());
				int green = interpolate(animStep/range, currentClr.getGreen(), color.getGreen());
				int blue = interpolate(animStep/range, currentClr.getBlue(), color.getBlue());
				if (currentClr.equals(color)) {
					reversing = false;
					animStep = 0;
					clickAnim = false;
					animating = false;
				}
				currentClr = new Color(red, green, blue);
				setBackground(currentClr);
				animStep--;

			}
		}
	}

	/**
	 * Animates the button to get darker when hovering over it.
	 * Uses interpolation with individual RGB values (default and hover) 
	 * along with the ratio of steps over the range to give a smooth transition into the hover state of the button.
	 * (which is when the button appears slightly darker when you hover your mouse over it.)
	 */
	private void playHoverAnim() {
		if (!reversing) {
			int red = interpolate(animStep/range, colorOver.getRed(), currentClr.getRed());
			int green = interpolate(animStep/range, colorOver.getGreen(), currentClr.getGreen());
			int blue = interpolate(animStep/range, colorOver.getBlue(), currentClr.getBlue());
			if (!hovering) {
				reversing = true;
				animStep = (int) range - 1;
			}
			if (currentClr.equals(colorOver) || animStep == range) {
				animStep = (int) range;
				currentClr = colorOver;
			} else {
				//System.out.println(red + " " + green + " " + blue);
				currentClr = new Color(red, green, blue);
				animStep++;
			}
			setBackground(currentClr);
		}
		if (reversing) {
			int red = interpolate(animStep/range, currentClr.getRed(), color.getRed());
			int green = interpolate(animStep/range, currentClr.getGreen(), color.getGreen());
			int blue = interpolate(animStep/range, currentClr.getBlue(), color.getBlue());
			if (currentClr.equals(color)) {
				reversing = false;
				animStep = 0;
				hoverAnim = false;
			}
			currentClr = new Color(red, green, blue);
			setBackground(currentClr);
			animStep--;
		}
	}
	
	class AL implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Button eventSource = (Button)e.getSource();
			taskDelayWorker = new SwingWorker<Void, Void>() {

				@Override
				protected Void doInBackground() throws Exception {
					Thread.sleep(300);
					//event source is a button, the source of the event from Action Listener
					handleTask(eventSource.text);
					return null;
				}
				
			};
			taskDelayWorker.execute();
		}
		
	}
	
	/**
	 * Resets the graphics for all the buttons.
	 * @param buttons HashMap of all of the buttons that exist in the game. Must not be null.
	 */
	static void resetAllButtons(HashMap<String, Button> buttons) {
		for (Map.Entry<String, Button> set :buttons.entrySet()) {
			set.getValue().color = Color.GREEN;
			set.getValue().animStep = 0;
			set.getValue().clickAnim = false;
			set.getValue().animating = false;
			set.getValue().hovering = false;
			set.getValue().reversing = false;
			set.getValue().brdrColor = set.getValue().color.darker().darker().darker();
			set.getValue().colorOver = set.getValue().color.darker().darker();
			set.getValue().currentClr = set.getValue().color;
			set.getValue().clickClr = set.getValue().color.darker().darker().darker().darker();
			set.getValue().setBackground(set.getValue().color);
		}
	}
	
	/**
	 * Handles the task for each button based on its ID or name.
	 * @param bttnID The name of the button that was clicked
	 */
	static void handleTask(String bttnID) {
		switch (bttnID) {
		case "Play":
			MainGame.gameState = MainGame.GAME_SETTINGS;
			MainGame.removeButtons(MENU);
			MainGame.addButtons(GAMETYPE);
			MainGame.addButtons(BACK);
			break;
		case "Difficulty":
			MainGame.gameState = MainGame.MODES;
			MainGame.removeButtons(MENU);
			MainGame.addButtons(DIFFICULTY);
			MainGame.addButtons(BACK);
			break;
		case "Help":
			MainGame.gameState = MainGame.HELP;
			MainGame.removeButtons(MENU);
			MainGame.addButtons(BACK);
			break;
		case "Exit":
			System.exit(1);
			break;
		case "Easy":
			MainGame.difficulty = MainGame.EASY;
			break;
		case "Medium":
			MainGame.difficulty = MainGame.MEDIUM;
			break;
		case "Hard":
			MainGame.difficulty = MainGame.HARD;
			break;
		case "Singleplayer":
			MainGame.removeButtons(GAMETYPE);
			MainGame.removeButtons(BACK);
			MainGame.gameType = MainGame.SINGLEPLAYER;
			MainGame.gameState = MainGame.GAME;
			break;
		case "Multiplayer":
			MainGame.removeButtons(GAMETYPE);
			MainGame.addButtons(CLIENTCHOICE);
			MainGame.gameType = MainGame.MULTIPLAYER;
			MainGame.gameState = MainGame.GAME_SETTINGS_HOSTORCLIENT;
			break;
		case "Create":
			MainGame.removeButtons(CLIENTCHOICE);
			MainGame.removeButtons(BACK);
			MainGame.server = new Server();
			MainGame.server.start();
			MainGame.client = new Client(MainGame.server.getServerAddress(), MainGame.server.getServerPort());
			MainGame.client.start();
			MainGame.gameState = MainGame.ONLINE_GAME;
			break;
		case "Join":
			String address = JOptionPane.showInputDialog("Server Address: ");
			int port = -1;
			while (true) {
				try {port = Integer.parseInt(JOptionPane.showInputDialog("Server Port: "));} 
				catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Format Exception", "Please use Integer format", JOptionPane.INFORMATION_MESSAGE);
					continue;
				}
				break;
			}
			MainGame.removeButtons(CLIENTCHOICE);
			MainGame.removeButtons(BACK);
			MainGame.client = new Client(address, port); //EDIT THIS
			MainGame.client.start();
			MainGame.gameState = MainGame.ONLINE_GAME;
			break;
		case "Back":
			switch (MainGame.gameState) {
			case MainGame.MODES:
				MainGame.gameState = MainGame.MENU;
				MainGame.removeButtons(BACK);
				MainGame.removeButtons(DIFFICULTY);
				MainGame.addButtons(MENU);
				break;
			case MainGame.HELP:
				MainGame.gameState = MainGame.MENU;
				MainGame.removeButtons(BACK);
				MainGame.addButtons(MENU);
				break;
			case MainGame.GAME_SETTINGS: 
				MainGame.gameState = MainGame.MENU;
				MainGame.removeButtons(GAMETYPE);
				MainGame.removeButtons(BACK);
				MainGame.addButtons(MENU);
			break;
			case MainGame.GAME_SETTINGS_HOSTORCLIENT: 
				MainGame.gameState = MainGame.GAME_SETTINGS;
				MainGame.removeButtons(CLIENTCHOICE);
				MainGame.addButtons(GAMETYPE);
			break;
			}
			break;
		}
	}
	
	/**
	 * Creates a new button with the given parameters and stores it in the provided HashMap.
	 * @param text The text the buttons should display
	 * @param x x-coordinate of the top left corner of where the button needs to be on the screen
	 * @param y y-coordinate of the top left corner of where the button needs to be on the screen
	 * @param type The type of button that is used to display it on the specified menu (help menu, difficulty menu or regular menu)
	 * @param buttons The HashMap used to store the buttons.
	 */
	private static void createButton(String text, int x, int y, final int type, HashMap<String, Button> buttons) {
		Button bttn = new Button();
		bttn.setText2(text);
		bttn.setBounds(x, y, bttn.getPreferredSize().width, bttn.getPreferredSize().height);
		bttn.bttnType = type;
		bttn.addActionListener(bttn.new AL());
		buttons.put(bttn.text, bttn);
	}

	/**
	 * Creates the specified buttons in the provided buttons hashmap
	 * @param buttons
	 */
	static void setupButtons(HashMap<String, Button> buttons) {
		createButton("Play", (int)(PANW/8 * 3.62), PANH/6 * 2, MENU, buttons);
		createButton("Difficulty", (int)(PANW/8 * 3.19), (int)(PANH/6 * 2.75), MENU, buttons);
		createButton("Help", (int)(PANW/8 * 3.62), (int)(PANH/6 * 3.5), MENU, buttons);
		createButton("Exit", (int)(PANW/8 * 3.62), (int)(PANH/6 * 4.25), MENU, buttons);

		createButton("Back", (int)(PANW/8 * 3.62), (int)(PANH/6 * 4.5), BACK, buttons);
		
		createButton("Easy", PANW/8 + 120, (int)(PANH/6 * 3.8), DIFFICULTY, buttons);
		createButton("Medium", (int)(PANW/8 * 3.46), (int)(PANH/6 * 3.8), DIFFICULTY, buttons);
		createButton("Hard", (int)(PANW/8 * 5.5), (int)(PANH/6 * 3.8), DIFFICULTY, buttons);

		createButton("Singleplayer", (int)(PANW/8 * 1.1), (int)(PANH/6 * 3.8), GAMETYPE, buttons);
		createButton("Multiplayer", (int)(PANW/8 * 5.1), (int)(PANH/6 * 3.8), GAMETYPE, buttons);
		
		createButton("Create", (int)(PANW/8 + 120), (int)(PANH/6 * 3.8), CLIENTCHOICE, buttons);
		createButton("Join", (int)(PANW/8 * 5 + 50), (int)(PANH/6 * 3.8), CLIENTCHOICE, buttons);

	}

}
