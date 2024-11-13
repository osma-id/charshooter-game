package software_project;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JPanel;

/** Has to do with running all the graphics in the game.
 * @author Osman Idris
 *
 */
public class GameGraphics {

	final static int PANW = MainGame.PANW;
	final static int PANH = MainGame.PANH;
	final static Color GUITHEME1 = new Color(255, 255, 255); //white
	final static Color GUITHEME2 = new Color(0,0,0,200); //black
	final static Color GUITHEME3 = Color.GREEN.darker(); //might change all colors to custom values
	static boolean brdHighlightToggle; // for wave board blinking green
	int spritePhase = 1; // for menu zombies
	public static Font vt323;

	GameGraphics() {
		brdHighlightToggle = false;
		// Buttons contstructor parameters: (int x, int y, int width, String id)
	}

	class DrawingPanel extends JPanel {

		DrawingPanel() {
			this.setPreferredSize(new Dimension(PANW, PANH));
			this.setBackground(Color.BLACK); 

			try {
				vt323 = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/VT323-Regular.ttf")).deriveFont(72f);
				GraphicsEnvironment ge = 
						GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(vt323);
			} catch (FontFormatException e) {	
			} catch (IOException e) {}
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			g2.setStroke(new BasicStroke(3));

			g.setFont(vt323);
			g.setColor(GUITHEME1);

			switch (MainGame.gameState) {
			case MainGame.MENU:
				// MENU
				g.setColor(GUITHEME1);
				drawTitle(g);
				drawMenuGUI(g, g2);
				break;
			case MainGame.GAME_SETTINGS:
				g.setColor(GUITHEME1);
				drawTitle(g);
				break;
			case MainGame.GAME:
				//GAME
				MapManager.drawMap(g);
				for (Entity entity: MainGame.existingEntities) { //drawing entities
					if (entity instanceof Bullet) {
						g.setColor(Color.RED.darker());
						g.drawOval(entity.screenX, entity.screenY, entity.width, entity.height);
					} else {
						ImageManager.playSprite(entity, g, entity.spritePhase);
						drawHealthBar(entity, g, g2);
					}
				}
				drawWaveBoard(g);
				drawScoreBoard(g);
				drawEnemiesBoard(g);
				drawCurrentEquip(g);
				if (brdHighlightToggle) waveBoardHighlight(g);
				break;
			case MainGame.ONLINE_GAME:
				//GAME
				MapManager.drawMap(g);
				for (Entity entity: MainGame.existingEntities) { //drawing entities
					if (entity instanceof Bullet) {
						g.setColor(Color.RED.darker());
						g.drawOval(entity.screenX, entity.screenY, entity.width, entity.height);
					} else {
						try {
							ImageManager.playSprite(entity, g, entity.spritePhase);}
						catch (ArrayIndexOutOfBoundsException e) {}
						drawHealthBar(entity, g, g2);
					}
				}
				drawWaveBoard(g);
				drawScoreBoard(g);
				drawEnemiesBoard(g);
				drawCurrentEquip(g);
				if (brdHighlightToggle) waveBoardHighlight(g);
				break;
			case MainGame.MODES:
				drawModesTitle(g);
				difficultyGUI(g);
				break;
			case MainGame.HELP:
				drawHelpGUI(g);
				break;
			}

		}

		// FOR MAIN GAME
		void drawEnemiesBoard(Graphics g) {
			g.setColor(GUITHEME2);
			g.fillRoundRect(PANW/6, -20, PANW/6, 70, 20, 20);
			g.setColor(GUITHEME1);
			g.drawRoundRect(PANW/6, -20, PANW/6, 70, 20, 20);
			// text
			g.setFont(vt323.deriveFont(30f));
			g.drawString("Enemies: " + Listeners.zombsLeft, PANW/6 + 10, 35);
		}

		void drawWaveBoard(Graphics g) {
			g.setColor(GUITHEME2);
			g.fillRoundRect(PANW/3, -20, PANW/3, 95, 20, 20);
			g.setColor(GUITHEME1);
			g.drawRoundRect(PANW/3, -20, PANW/3, 95, 20, 20);
			// text
			g.setFont(vt323.deriveFont(70f));
			g.drawString("Wave: " + Listeners.waveNum, PANW/2 - 100, 60);
		}

		void waveBoardHighlight(Graphics g) {
			g.setColor(GUITHEME3);
			g.setFont(vt323.deriveFont(70f));
			g.drawString("Wave: " + Listeners.waveNum, PANW/2 - 100, 60);
		}

		void drawScoreBoard(Graphics g) {
			g.setColor(GUITHEME2);
			g.fillRoundRect(PANW/6 * 4, -20, PANW/6, 70, 20, 20);
			g.setColor(GUITHEME1);
			g.drawRoundRect(PANW/6 * 4, -20, PANW/6, 70, 20, 20);
			// text
			g.setFont(vt323.deriveFont(35f));
			g.drawString("Score: " + Listeners.score, PANW/6 * 4 + 50 - MainGame.listeners.scoreBrdOffset, 35);
		}

		void drawHealthBar(Entity entity, Graphics g, Graphics2D g2) {
			g.setColor(Color.RED);
			g.fillRect(entity.screenX - 5, entity.screenY - 10, 50, 5);
			g.setColor(Color.GREEN);
			g.fillRect(entity.screenX - 5, entity.screenY - 10, (int)(entity.hp * (50.0 / entity.spawnHp)), 5);
			g.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke(1));
			g.drawRect(entity.screenX - 5, entity.screenY - 10, 50, 5);
			g2.setStroke(new BasicStroke(3));
			if (entity instanceof Player) {
				g.setColor(Color.BLACK);
				g.setFont(vt323.deriveFont(15f));
				g.drawString(entity.id.substring(0,4), entity.screenX - 20, entity.screenY - 10);	//alter for username
			}
			g2.setStroke(new BasicStroke(1));
		}

		void drawCurrentEquip(Graphics g) {
			float oldFontSize = g.getFont().getSize2D();
			g.setFont(vt323.deriveFont(60f));
			g.setColor(GUITHEME2);
			g.fillRoundRect(10, PANH - 80, PANW/4 + 10, 70, 20, 20);
			g.setColor(GUITHEME1);
			g.drawRoundRect(10, PANH - 80, PANW/4 + 10, 70, 20, 20);
			g.setColor(Color.RED.darker());
			g.drawString("Gun: " + MainGame.guns.get(Player.equipped - 1).id, 20, PANH - 20);
			g.setFont(vt323.deriveFont(oldFontSize));
		}

		// MENU methods
		void drawTitle(Graphics g) {
			g.setFont(vt323);
			g.setFont(g.getFont().deriveFont(Font.BOLD, 120F));
			g.setColor(GUITHEME3);
			g.drawString("CharShooter", PANW/3 - 70, PANH/13 * 2 + 10);
			g.setColor(GUITHEME1);
			g.drawString("CharShooter", PANW/3 - 74, PANH/13 * 2);
			g.setFont(g.getFont().deriveFont(Font.PLAIN, 72F)); 
		}

		void drawMenuGUI(Graphics g, Graphics2D g2) {
			g.setColor(GUITHEME3);
			g2.setStroke(new BasicStroke(10));
			g.drawRoundRect(PANW/3 - 5, PANH/8 * 2 + 35, PANW/3 + 10,(int) (PANH/4 * 2.3) - 10, 20, 20);
			g2.setStroke(new BasicStroke(3));
			g.setColor(GUITHEME1);
			g.drawRoundRect(PANW/3 - 10, PANH/8 * 2 + 30, PANW/3 + 20,(int) (PANH/4 * 2.3), 20, 20);
			g.drawRoundRect(PANW/3, PANH/8 * 2 + 40, PANW/3,(int) (PANH/4 * 2.3) - 20, 20, 20);
			g2.setStroke(new BasicStroke(2));
			g.setFont(g.getFont().deriveFont(Font.PLAIN, 40F)); 
			g.drawString("Name: " + MainGame.localUsername, 15, 40);
		}

		// HELP MENU METHODS
		void drawHelpGUI(Graphics g) {
			g.drawRoundRect(PANW/6, PANH/6, PANW/6 * 4, (int)(PANH / 6 * 3.6), 20, 20);
			g.drawRoundRect(PANW/6 + 10, PANH/6 + 10, PANW/6 * 4 - 20, (int)(PANH / 6 * 3.6) - 20, 20, 20);
			g.setColor(GUITHEME1);
			drawRules(g);
		}

		void drawRules(Graphics g) {
			float oldFontSize = g.getFont().getSize2D();
			g.setFont(g.getFont().deriveFont(50f));
			g.drawString("- Use keys         to move", PANW/6 + 20, PANH/6 + 60);
			g.drawString("- Survive         waves", PANW/6 + 20, PANH/6 + 110);
			g.drawString("- Fight", PANW/6 + 20, PANH/6 + 160);
			g.drawString("- Get the         score", PANW/6 + 20, PANH/6 + 210);
			g.drawString("- Click to", PANW/6 + 20, PANH/6 + 260);
			g.setColor(GUITHEME3);
			g.drawString("           W,A,S,D", PANW/6 + 20, PANH/6 + 60);
			g.drawString("          ENDLESS", PANW/6 + 20, PANH/6 + 110);
			g.drawString("         ZOMBIES", PANW/6 + 20, PANH/6 + 160);
			g.drawString("          HIGHEST ", PANW/6 + 20, PANH/6 + 210);
			g.drawString("           SHOOT", PANW/6 + 20, PANH/6 + 260);
			g.setFont(g.getFont().deriveFont(oldFontSize));
			g.drawString("ENJOY ", (int)(PANW/6 * 2.6), PANH/6 * 4 + 50);
			g.setColor(GUITHEME1);
		}

		//DIFFICULTY 
		void drawModesTitle(Graphics g) {
			g.setFont(vt323);
			g.setFont(g.getFont().deriveFont(Font.BOLD, 100F));
			g.setColor(GUITHEME3);
			g.drawString("Difficulty Level", PANW/4 - 40, PANH/13 * 2 - 10);
			g.setColor(GUITHEME1);
			g.drawString("Difficulty Level", PANW/4 - 44, PANH/13 * 2 - 20);
			g.setFont(g.getFont().deriveFont(Font.PLAIN, 72F)); 
		}

		void difficultyGUI(Graphics g) {
			g.drawRoundRect(PANW/6, PANH/6, PANW/6 * 4, (int)(PANH / 6 * 3.6), 20, 20);
			g.drawRoundRect(PANW/6 + 10, PANH/6 + 10, PANW/6 * 4 - 20, (int)(PANH / 6 * 3.6) - 20, 20, 20);
			g.setColor(GUITHEME1);
		}

	}

	void waveBoardHighlight(Graphics g) {
		g.setColor(GUITHEME3);
		g.setFont(vt323.deriveFont(70f));
		g.drawString("Wave: " + Listeners.waveNum, PANW/2 - 100, 60);
	}

}
