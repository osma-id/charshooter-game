package server;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JPanel;

import software_project.Entity;
import software_project.Player;
import software_project.Zombie;

/** Visualizes the full map of the server and locations of every entity.
 * @author Osman Idris
 *
 */
public class MapVisualizer {
	final static int PANW = 1200;
	final static int PANH = 840;
	static JFrame window;
	static DrawingPanel dPanel;
	
	public MapVisualizer() {
		window = new JFrame("CharShooter Map Visualizer (Server)");
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		dPanel = new DrawingPanel();
		dPanel.setFocusable(true);
		window.add(dPanel);
		window.validate();
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}
	
	class DrawingPanel extends JPanel {
		DrawingPanel() {
			window.setSize(new Dimension(PANW, PANH));
			window.setBackground(Color.GRAY);
		}
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for (Entity entity: Data.existingEntities) {
				if (entity instanceof Zombie) g.setColor(Color.GREEN);
				else if (entity instanceof Player) g.setColor(Color.BLUE);
				else g.setColor(Color.RED);
				g.drawRect(entity.mapX/3, entity.mapY/3, entity.width/3, entity.height/3);
			}
		}
	}
	
}
