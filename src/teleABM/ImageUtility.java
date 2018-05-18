package teleABM;

import teleABM.Range;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;


import repast.simphony.engine.environment.RunState;
import repast.simphony.space.Dimensions;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;
/*This class controls the output file
 * 
 * author: Yue Dou
 */
public class ImageUtility {
	private static int nodata = -9999;
	
	public static void createPNG(GridValueLayer grid, Range<Double> range, File file) {
		Dimensions d = grid.getDimensions();
		int width = (int) d.getWidth();
		int height = (int) d.getHeight();
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		
		g.setColor(Color.GRAY);
		g.fillRect(0, 0, width, height);
		
	/*	for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if ((int) grid.get(i, j) != nodata) {
					int val = (int) (255d * (grid.get(i, j) - range.getLower()) / (range.getUpper() - range.getLower()));
					g.setColor(new Color(val, val, val));
					g.drawRect(i, height - j - 1, 1, 1);
				}
			}
		} */
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if ((int) grid.get(i, j) == 0) {
				//	int val = (int) (255d * (grid.get(i, j) - range.getLower()) / (range.getUpper() - range.getLower()));
					g.setColor(Color.BLUE);
					g.drawRect(i, height - j - 1, 1, 1);
				}
			}
		}
		try {
	//		System.out.println("imgName "+img);
			ImageIO.write(img, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		g.dispose();
	}
	
	public static void createLandUsePNG(Iterable<SoybeanAgent> agents, GridValueLayer grid, int width, int height, File file) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		
		//background
		g.setColor(new Color(255,255,204));
		//light green gray ish;
		g.fillRect(0, 0, width, height);
		//water Cells
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if ((int) grid.get(i, j) == 0) {
				//	g.setColor(Color.BLUE);
					g.setColor(new Color(140,200,233));
					//light blue
					g.drawRect(i, height - j - 1, 1, 1);
				} 
			}
		}
		
     	Iterator<SoybeanAgent> iter = agents.iterator();
		while (iter.hasNext()) {
			SoybeanAgent h = iter.next();
	//		ContextUtils.getContext(h).getValueLayer("Distance to Water").get(coordinates)
			
			Iterator<LandCell> cellIter = h.getTenureCells().iterator();
					
			
			while (cellIter.hasNext()) {
				LandCell c = cellIter.next();
				g.setColor(c.getLandUse().getColor());
				
				//this paints the output file based on land use types
			//	g.setColor(Color.black);
				g.drawRect(c.getCell().getXlocation(), height - c.getCell().getYlocation()- 1, 1, 1);
			}
			
		}
	  
		try {
			ImageIO.write(img, "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		g.dispose();
	}
	
	public static void createLandCoverPNGs(OrganicSpace c, int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();

		
		
	}
	
	
}
