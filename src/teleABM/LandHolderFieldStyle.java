/**
 * 
 */
package teleABM;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;


/**
 * @author geododo
 *
 */
public class LandHolderFieldStyle implements ValueLayerStyleOGL{
	

	 protected ValueLayer layer;
	  Map<Integer,Color> colorMap; 
	/* (non-Javadoc)
	 * @see repast.simphony.visualizationOGL2D.ValueLayerStyleOGL#getColor(double[])
	 */
	  
		public LandHolderFieldStyle(){
	
			 colorMap = new HashMap<Integer,Color>();
			 
			  colorMap.put(0, Color.gray); 
				colorMap.put(1, Color.red); 
				colorMap.put(2, Color.YELLOW); 
				colorMap.put(3, Color.GREEN); 
				colorMap.put(4, Color.CYAN); 
				colorMap.put(5, Color.magenta);
				
}

	@Override
	public Color getColor(double... coordinates) {
		// TODO Auto-generated method stub
		
		
	    int id = (int) (layer.get(coordinates)+1) %10;
	    int idx = ((int) layer.get(coordinates)/10)%10;
	    
	    int rand = (id+idx)%10;
	 //   int random = ThreadLocalRandom.current().nextInt(0, 9);
	    
	           if (rand == 0){
	        	   return colorMap.get(0);
	           }
	            else if (rand == 1||rand==9) {
					return colorMap.get(1);
				}
				else if(rand == 2||rand==8){
					return colorMap.get(2);
				} 
				else if(rand == 3||rand==7){
					return colorMap.get(3);
				}
				else  if (rand ==4||rand==6){
					return colorMap.get(4);
				}
				else  {
					return colorMap.get(5);
				} 
				 
					
	}

	@Override
	public float getCellSize() {
		// TODO Auto-generated method stub
		
		 Parameters para = RunEnvironment.getInstance().getParameters();
			return ((Integer) para.getValue("cellSize"));
		//return 30.0f;
//		return 250.0f;
	}

	@Override
	public void init(ValueLayer layer) {
		// TODO Auto-generated method stub
		this.layer=layer;
		
	}
}