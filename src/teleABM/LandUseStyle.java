package teleABM;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;

public class LandUseStyle implements ValueLayerStyleOGL{
	
	  protected ValueLayer layer;
	  Map<Integer,Color> colorMap; 
	  
	  public LandUseStyle(){
		  colorMap = new HashMap<Integer,Color>();
		  
		  if (TeleABMBuilder.receivingSystem){
		  colorMap.put(0, Color.gray); //no land use
			colorMap.put(1, new Color(0,0,255)); //water
			colorMap.put(2, new Color(0,255,0)); //soy,light green
			colorMap.put(3, new Color(255,255,255)); //rice,white
			colorMap.put(4, new Color(100,100,100)); //othercrops
			colorMap.put(5, new Color(10,200,0));  //forest
			colorMap.put(6, new Color(255,255,51));  //corn, yellow color
		//	colorMap.put(6, new Color(0,0,0));
			colorMap.put(7, new Color(153,0,0));   //building, dark red
			
			colorMap.put(40, new Color(0,255,0)); //soybean for heilongjiang
			colorMap.put(41, new Color(255,255,255)); //rice,white
			colorMap.put(42, new Color(255,255,51));  //corn, yellow color
		  }
		  if (TeleABMBuilder.sendingSystem){
			  colorMap.put(0, Color.gray); //no land use
			  colorMap.put(1, new Color(0,255,0)); //soy single season
				colorMap.put(2, new Color(10,200,0)); //soy,double cropping
				colorMap.put(3, new Color(255,255,255)); //cotton, yellow
				colorMap.put(9, Color.YELLOW); //soy-cutton
		  }
	  }
	  
		public Color getColor(double... coordinates) {
			
		    int landUse = (int)layer.get(coordinates);
		    
			if (landUse == 0) {
				return colorMap.get(0);
			}
			else if (landUse == 1) {
				return colorMap.get(1);
			}
			else if(landUse == 2){
				return colorMap.get(2);
			} 
			else if(landUse == 3){
				return colorMap.get(3);
			}
			else if(landUse == 4) {
				return colorMap.get(4);
			}
			else if(landUse == 5) {
				return colorMap.get(5);
			} 
			else if(landUse == 6) {
				return colorMap.get(6);
			} else if(landUse == 7) {
				return colorMap.get(7);
			} else if(landUse ==9){
				return colorMap.get(9);
			} else if (landUse ==40){
				return colorMap.get(40);
			} else if(landUse ==41){
				return colorMap.get(41);
			} else {
				return colorMap.get(42);
			}
			
		}
		
		@Override
		public float getCellSize() {
			// TODO Auto-generated method stub
			
			 Parameters para = RunEnvironment.getInstance().getParameters();
				return ((Integer) para.getValue("cellSize"));
		//	return 30.0f;
			
		//	return 250.0f;
		}

		/* (non-Javadoc)
		 * @see repast.simphony.visualizationOGL2D.ValueLayerStyleOGL#init(repast.simphony.valueLayer.ValueLayer)
		 */
		@Override
		public void init(ValueLayer layer) {
			// TODO Auto-generated method stub
	           this.layer=layer;
		}

}
