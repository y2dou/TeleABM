package teleABM;

import teleABM.Range;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
	 private static Charset UTF8 = Charset.forName("UTF-8");
	
	public static void createPNG(GridValueLayer grid, File file) {
		Dimensions d = grid.getDimensions();
		int width = (int) d.getWidth();
		int height = (int) d.getHeight();
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		
		
	//	BufferedStream buffer
		
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
				if ((int) grid.get(i, j) == 6) {
				//	int val = (int) (255d * (grid.get(i, j) - range.getLower()) / (range.getUpper() - range.getLower()));
					g.setColor(new Color(255,255,51));  //corn, yellow color);
					g.drawRect(i, height - j - 1, 1, 1);
				}
				if ((int) grid.get(i, j) == 2) {
					//	int val = (int) (255d * (grid.get(i, j) - range.getLower()) / (range.getUpper() - range.getLower()));
						g.setColor( new Color(0,255,0)); //soy,light green
						g.drawRect(i, height - j - 1, 1, 1);
					}
				if ((int) grid.get(i, j) == 3) {
					//	int val = (int) (255d * (grid.get(i, j) - range.getLower()) / (range.getUpper() - range.getLower()));
						g.setColor(new Color(255,255,255));  //rice
						g.drawRect(i, height - j - 1, 1, 1);
					}
				
				if ((int) grid.get(i, j) == 10) {
					//	int val = (int) (255d * (grid.get(i, j) - range.getLower()) / (range.getUpper() - range.getLower()));
						g.setColor(Color.red);  //other
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
	
	 public static void writeFile(GridValueLayer grid) throws IOException {
		 Dimensions d = grid.getDimensions();
			int width = (int) d.getWidth();
			int height = (int) d.getHeight();
			
		
		  
		     int[][] pixel = new int[width][height];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					 pixel[i][j] = (int) grid.get(i, j);
			//		 System.out.println(pixel[i][j]);
				}
			}
		//	new File((String) RunState.getInstance().getFromRegistry("path") + "/distanceToWater.png")
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			String output = null;
			output=toString(width,height,pixel);
			Path path = Paths.get("./output", RunState.getInstance().getRunInfo().toString()+timeStamp+"-tick5.txt");
	//		File baseDirectory = new File("./output/");
	//		File subDirectory = new File (baseDirectory, RunState.getInstance().getRunInfo().toString());
    //    	File file =	new File(subDirectory, "tick5.txt");
        //	file.getParent();
		//   System.out.println(output);
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(
		           new   FileOutputStream(path.toString()), "utf-8"))) 
			{
		      writer.write(output);
		     writer.close();
		   }
//https://stackoverflow.com/questions/2885173/how-do-i-create-a-file-and-write-to-it-in-java
			//answer two
	//		https://www.dreamincode.net/forums/topic/307635-produce-a-matrix-table-using-tostring/
			



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
	

	
	public static void write (String filename, int[]x) throws IOException{
		  BufferedWriter outputWriter = null;
		  outputWriter = new BufferedWriter(new FileWriter(filename));
		 
		  for (int i = 0; i < x.length; i++) {
		    // Maybe:
		    outputWriter.write(x[i]+"");
		    // Or:
	//	    outputWriter.write(Integer.toString(x[i]);
		    outputWriter.newLine();
		  }
		  outputWriter.flush();  
		  outputWriter.close();  
		}
	
	public static String toString(int width, int height, int[][] pixel) {
		String output = "ncols 501\n";
		output +="nrows 550\n";
		output += "xllcorner  491078.83689939\n";
		output +="yllcorner     5254210.3919105\n";
        output +="cellsize 250\n";
        output +="NODATA_value  0\n";


	//	for(int i=0; i<height;i++)
		for (int i=height-1; i>=0; i--)	
			//pay attention here, I don't know why, but you need to read the arraylist (height)
			//in the opposite direcition
		{	for (int j=0;j<width;j++)
				{
				output = output+ pixel[j][i]+ " ";			
				}
		 output +="\n"; 
		}
	//	System.out.println(output);
		return output;
	}
}
