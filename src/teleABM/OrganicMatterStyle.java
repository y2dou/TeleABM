package teleABM;

import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;

public class OrganicMatterStyle implements ValueLayerStyleOGL {
	
	  protected ValueLayer layer;
	  Map<Integer,Color> colorMap; 
	
	/* (non-Javadoc)
	 * @see repast.simphony.visualizationOGL2D.ValueLayerStyleOGL#getColor(double[])
	 */
	  
		public OrganicMatterStyle(){
			colorMap = new HashMap<Integer,Color>();

			
			colorMap.put(6, new Color(240,0, 0));
			colorMap.put(5, new Color(240,0, 0));
			
			colorMap.put(4, new Color(255, 255, 0));
			colorMap.put(3, new Color(255, 255, 255 / 3));
			colorMap.put(2, new Color(255, 255, 255 / 2));
			colorMap.put(1, new Color(0, 255, (int) (255 / 1.2)));
			colorMap.put(0, Color.white);
			
	   
		
		}
	
	@Override
	public Color getColor(double... coordinates) {
		
	    double organicMatter = (double)layer.get(coordinates);
	    
	
	    
	//    return new Color(0, green, 0);
	    
		if (organicMatter > 30) {
			return colorMap.get(3);
		}
		else if (organicMatter >20) {
			return colorMap.get(2);
		}
		else if(organicMatter >10) {
			return colorMap.get(1);
		}
		return colorMap.get(0);
	}

	/* (non-Javadoc)
	 * @see repast.simphony.visualizationOGL2D.ValueLayerStyleOGL#getCellSize()
	 */
	@Override
	public float getCellSize() {
		// TODO Auto-generated method stub
		 Parameters para = RunEnvironment.getInstance().getParameters();
		return (float) ((Integer) para.getValue("cellSize"));
		//return 30.0f;
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

