package software_project;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/** Responsible for the creation of the map.
 * @author Osman Idris
 *
 */
public class MapManager {
	public final static int MAPW = MainGame.PANW * 3; //TOTAL MAP WIDTH
	public final static int MAPH = MainGame.PANH * 3; // TOTAL MAP HEIGHT

	final static int GRASS = 2; //might be used later
	final static int TREES = 3;
	final static int MUD = 4;

	final static int mapRows = MAPW/Tile.tileW; //could be used too
	final static int mapCols = MAPH/Tile.tileH;

	private static Tile[][] mapView = new Tile[mapRows][mapCols]; // if different colors are being added

	MapManager() {
		addTiles();
		setMapGraphics();
		//printMapView(); // use for debugging, prints map view 2d Array
	}

	private void addTiles() {
		for (int i = 0; i < mapView.length; i++) {
			for (int j = 0; j < mapView[0].length; j++) {
				mapView[i][j] = new Tile(Tile.tileW * i, Tile.tileH * j);
			}
		}
	}

	//draws map by using tile width and number of tiles in TOTAL MAP SIZE
	static void drawMap(Graphics g) {
		for (int i = 0; i < mapView.length; i++) {
			for (int j = 0; j < mapView[0].length; j++) {
				Tile tempTile = mapView[i][j];

				g.setColor(tempTile.color);

				//SCREEN X is where it is DRAWN on the screen (can be in negatives too)
				//TILE X is the tile's x coordinate in the whole map
				int tileScreenX = tempTile.tileX - MainGame.player.mapX + MainGame.player.screenX;
				int tileScreenY = tempTile.tileY - MainGame.player.mapY + MainGame.player.screenY;

				g.fillRect(tileScreenX, tileScreenY, Tile.tileW, Tile.tileH);
				//if (ImageManager.grassImg.equals(tempTile.img)) {
				g.drawImage(tempTile.img, tileScreenX, tileScreenY, Tile.tileW, Tile.tileH, null);
				//}
				//TODO ADD TREES
			}
		}
	}

	void setMapGraphics() {
		//set type
		for (int i = 0; i < mapView.length; i++) {
			for (int j = 0; j < mapView[0].length; j++) {
				int treeTileCount = 0; // there can't be 3 tree type tiles in a row
				int random = (int)(Math.random() * 100);
				
				/* tree spawning explained: If in the range of the rows and columns of tiles where the player
				 * can travel, if the type of those tiles is not final int GRASS, and if the area around them 
				 * is not type TREES (tree type tiles have portions of an image, which can overlap if the
				 *  spawning is not done in a 2x2 square of tiles).
				 */
				if (random > 98) {
					// 'i' is rows, 'j' is columns and are being used as domain and range
					if ( (i >= 29 && i <= 150) && 
							(j >= 13 && j <= 70) ) {

						if (mapView[i][j].type != GRASS) {
							// if statement below: check to make sure tiles aren't overwritten
							if (areTilesClear(i, j)) {
								mapView[i][j].type = TREES; 
								mapView[i+1][j].type = TREES;
								mapView[i][j+1].type = TREES;
								mapView[i+1][j+1].type = TREES;
							}
						}

					}
				} 
				// setting type to grass
				if (mapView[i][j].type != TREES) {
					if (i >= 29 && i <= 150) { //Domain of grass
						if (j >= 13 && j <= 70) mapView[i][j].type = GRASS; //Range of grass
					}
				} 
			}
		}
		// add color attribute
		for (int i = 0; i < mapView.length; i++) {
			for (int j = 0; j < mapView[0].length; j++) {
				if (mapView[i][j].type == GRASS) {
					mapView[i][j].color = new Color(129, 158, 49);
					if (Math.random()*100 < 25) mapView[i][j].img = ImageManager.grassImg;
				}
				//drawing trees
				if (mapView[i][j].type == TREES &&
						mapView[i+1][j].type == TREES &&
						mapView[i][j+1].type == TREES &&
						mapView[i+1][j+1].type == TREES) {
					mapView[i][j].color = new Color(129, 158, 49);
					mapView[i+1][j].color = new Color(129, 158, 49);
					mapView[i][j+1].color = new Color(129, 158, 49);
					mapView[i+1][j+1].color = new Color(129, 158, 49);

					mapView[i][j].img = ImageManager.treeImg.getSubimage(0, 0, 10, 15);
					mapView[i+1][j].img = ImageManager.treeImg.getSubimage(10, 0, 10, 15);
					mapView[i][j+1].img = ImageManager.treeImg.getSubimage(0, 15, 10, 15);
					mapView[i+1][j+1].img = ImageManager.treeImg.getSubimage(10, 15, 10, 15);
				}
			}
		}

	}
	
	//checks if the area around the given tiles already has trees
	boolean areTilesClear(int i, int j) {
		if (mapView[i-1][j].type != TREES && // left top
				mapView[i][j - 1].type != TREES && //top left
				mapView[i - 1][j + 1].type != TREES && // left bottom
				mapView[i][j + 2].type != TREES && // bottom left
				mapView[i + 1][j + 2].type != TREES && // bottom right
				mapView[i + 2][j + 1].type != TREES && // right bottom 
				mapView[i + 2][j].type != TREES && // right top
				mapView[i + 1][j - 1].type != TREES) { // top right
			return true;
		}
		return false;
	}


	void printMapView() { //debugging method 1
		for (int i = 0; i < mapView.length; i++) {
			for (int j = 0; j < mapView[0].length; j++) {
				System.out.print(mapView[i][j].type + " ");
			}
			System.out.println("");
		}
	}

	static class Tile {
		private final static int tileW = 20, tileH = 30;
		int tileX, tileY;
		Color color = new Color(79, 94, 8);
		BufferedImage img;
		int type; // TODO: use to add obstacles

		Tile(int x, int y) {
			this.tileX = x;
			this.tileY = y;
		}

		//Another handy constructor
		Tile(int x, int y, BufferedImage img) {
			this.tileX = x;
			this.tileY = y;
			this.img = img;
		}

		int getTileWidth() {return tileW;}
		int getTileHeight() {return tileH;}
	}

}
