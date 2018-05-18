package teleABM;

import  teleABM.PGMReader;
import teleABM.Range.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cern.jet.random.AbstractDistribution;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.valueLayer.GridValueLayer;

public class OrganicSpace extends DefaultContext<Object> {

	// the Sugar space has a grid that tracks the maximum
	// amount of sugar and the current value at each x,y coordinate.

	// default sugar grow rate and the default maximum sugar
	Parameters p = RunEnvironment.getInstance().getParameters();

	double organicGrowRate = (double) p.getValue("organicGrowRate");

	private int xdim = (Integer)p.getValue("worldWidth");
	private int ydim = (Integer)p.getValue("worldHeight");
//	private boolean receivingSystem = p.getBoolean("receiving system representation");
//	private boolean sendingSystem = p.getBoolean("sending system representation");
	int numAgents = (Integer)p.getValue("initialNumAgents");
	protected double originx;
	protected double originy;
	protected double cellsize;
	private int nodata;
	protected Set<LandCell> landCells;
	protected LandCell landcell;
	protected List<LandCell> allLandCells = new LinkedList<LandCell>();
	public List<Point> allAgriculturalPoints = new LinkedList<Point>();
	
	List<ReceivingSoybeanAgent> receivingSoybeanAgents =
			new LinkedList<ReceivingSoybeanAgent>();
  	List<SendingSoybeanAgent> sendingSoybeanAgents =
			new LinkedList<SendingSoybeanAgent>();

	public OrganicSpace(String organicFile) {
		super("OrganicSpace");

		Grid<Object> grid = GridFactoryFinder.createGridFactory(null)
		.createGrid("Grid", this, new GridBuilderParameters<Object>(
				new WrapAroundBorders(), 
				new RandomGridAdder<Object>(), false, xdim, ydim));

		
	/*	Grid<SpatialAgent> grid = GridFactoryFinder.createGridFactory(null).createGrid("Grid",
				//		Grid<Object> landscapeGrid = GridFactoryFinder.createGridFactory(null).createGrid("LandscapeGrid",			
						this, new GridBuilderParameters<SpatialAgent>(
										new WrapAroundBorders(),
										new RandomGridAdder<SpatialAgent>(), true, xdim, ydim));*/
		//		this.addProjection(grid);
				
		GridValueLayer currentOrganic = new GridValueLayer("CurrentOrganic",true,
				new WrapAroundBorders(), xdim, ydim);
		GridValueLayer maxOrganic= new GridValueLayer("MaxOrganic", true, 
				new WrapAroundBorders(), xdim, ydim);
		this.addValueLayer(currentOrganic);
		this.addValueLayer(maxOrganic);
    
		
		BufferedInputStream stream = null;
	
	//	createValueLayerFromRandom(this, currentOrganic);
		//for now, the organic range is from 0.1 to 1. it takes 10 years to go back to max.
		createValueLayerFromRandom(this, maxOrganic);
//		System.out.println("test "+currentOrganic.get(100,100));
		
		
	

		//read ascii file from here
		stream = null;
		Range<Double> soilOrganicCarbon = new Range<Double>(0d, 0d);
		try {
			if(TeleABMBuilder.receivingSystem)
				stream = new BufferedInputStream(new FileInputStream("misc/organicmatter.asc"));
		//	stream = new BufferedInputStream(new FileInputStream("misc/heilong_2005.asc"));
			else	if(TeleABMBuilder.sendingSystem)
			{	stream = new BufferedInputStream(new FileInputStream("misc/2005.txt"));
		//	System.out.println("sending system land use read "+landUseField.get(500,500));
			}
			currentOrganic = loadFieldFromStream(this, stream, "CurrentOrganic", soilOrganicCarbon);
	//		System.out.println(this.getValueLayer("Land Use Field"));
		} catch (IOException e) {
			e.printStackTrace();
		//	elevation = createValueLayerFromRandom(this, elevation);
			createValueLayerFromRandom(this, currentOrganic);
		} finally {
			try {
				if (stream != null) 
					stream.close();
			} catch (IOException e) {}
		}
		
		this.addValueLayer(currentOrganic);
		
	//	elevation = createValueLayerFromRandom(this);
		GridValueLayer elevation = new GridValueLayer("Elevation",true,
				new WrapAroundBorders(), xdim, ydim);
		createValueLayerFromRandom(this, elevation);
		this.addValueLayer(elevation);
		//finish reading ascii file here
		
		for (int i = 0 ;i < xdim; i++){
			for (int j = 0; j<ydim; j++){
				if (elevation.get(i,j) >= 0){
				new LandCell(this, grid, i, j, elevation.get(i,j), currentOrganic.get(i,j));
		//		this.add(LandCell);
			
			} else {
		//		new WaterCell(context, landscapeGrid, i, j, elevation);
			}
			}
		}
		
		GridValueLayer landHolderField = new GridValueLayer("Land Holder Field", true,
				new WrapAroundBorders(), xdim, ydim);
	//	System.out.println(context.width);
	//	System.out.println("height"+context.height);
		this.addValueLayer(landHolderField);
//		takePossession(tenureField, landscapeGrid, RandomHelper.getDistribution("hectares").nextDouble(), environmentalContext.getCellsize(), coordinates);
	//	 System.out.println(	this.getElevationAt(10, 10));
	//	System.out.println("elevation field created in OrganicSpace");
		
		GridValueLayer landUseField = new GridValueLayer("Land Use Field", true,
				new WrapAroundBorders(), xdim, ydim);		
		
		stream = null;
		Range<Double> landUseTypes = new Range<Double>(0d, 0d);
		try {
			if(TeleABMBuilder.receivingSystem)
				stream = new BufferedInputStream(new FileInputStream("misc/baoshan_06_crops.asc"));
		//	stream = new BufferedInputStream(new FileInputStream("misc/heilong_2005.asc"));
			else	if(TeleABMBuilder.sendingSystem)
			{	stream = new BufferedInputStream(new FileInputStream("misc/2005.txt"));
		//	System.out.println("sending system land use read "+landUseField.get(500,500));
			}
			landUseField = loadFieldFromStream(this, stream, "Land Use Field", landUseTypes);
	//		System.out.println(this.getValueLayer("Land Use Field"));
		} catch (IOException e) {
			e.printStackTrace();
		//	elevation = createValueLayerFromRandom(this, elevation);
			createValueLayerFromRandom(this, landUseField);
		} finally {
			try {
				if (stream != null) 
					stream.close();
			} catch (IOException e) {}
		}
		
		this.addValueLayer(landUseField);
		
		
		//add trader agent vision file
		GridValueLayer traderAgentField = new GridValueLayer("Trader Agent Field", true,
				new WrapAroundBorders(), xdim, ydim);
	//	System.out.println(context.width);
	//	System.out.println("height"+context.height);
		this.addValueLayer(traderAgentField);
		
		
	}



	// The actual implementation of growback rule G, pg 182 (Appendix B).
	@ScheduledMethod(start=0,interval=1)
	public void updateOrganic() {
		int organicAtSpot;
		int maxOrganicAtSpot;

		GridValueLayer currentOrganic = (GridValueLayer)getValueLayer("CurrentOrganic");
		GridValueLayer maxOrganic = (GridValueLayer)getValueLayer("MaxOrganic");

		for (int i = 0; i < xdim; i++) {
			for (int j = 0; j < ydim; j++) {
				organicAtSpot = (int)currentOrganic.get(i,j);
				maxOrganicAtSpot = (int)maxOrganic.get(i,j);

				if (organicGrowRate == -1) 
					currentOrganic.set(maxOrganicAtSpot,i,j);

				else 
					if (organicAtSpot != maxOrganicAtSpot) 
						if (organicAtSpot + organicGrowRate <= maxOrganicAtSpot) 
							currentOrganic.set(organicAtSpot + organicGrowRate, i, j);

						else 
							currentOrganic.set(maxOrganicAtSpot, i, j);         
			}
		}
	}

	// takes all the sugar at this coordinate, leaving no sugar.
	public int takeOrganicAt(int x, int y) {
		GridValueLayer currentOrganic = (GridValueLayer)getValueLayer("CurrentOrganic");
		int i = (int)currentOrganic.get(x,y);		
		currentOrganic.set(0, x,y);
		
		return i;
	}

	// gets the amount of sugar at this x,y coordinate
	public int getOrganicAt(int x, int y) {		
		GridValueLayer currentOrganic = (GridValueLayer)getValueLayer("CurrentOrganic");
		return (int) currentOrganic.get(x,y);
	}
	public double getElevationAt(int x, int y){
		
	
		GridValueLayer elevation = (GridValueLayer) this.getValueLayer("Elevation");
	
		return (double) elevation.get(x,y);

	}
	
	public void setLandHolder (double agentID, int x, int y) {
		GridValueLayer landholderField = (GridValueLayer) getValueLayer("Land Holder Field");
		landholderField.set(agentID, x,y);
	}
	public Integer getLandHolder(int x, int y){
		GridValueLayer landholderField = (GridValueLayer) getValueLayer("Land Holder Field");
		return (int) landholderField.get(x,y);
	}
	
	public void setTraderAgent(double agentID, int x, int y){
		GridValueLayer traderAgentField = (GridValueLayer) getValueLayer("Trader Agent Field");
		traderAgentField.set(agentID, x,y);
	}
	
	public Integer getTraderAgent(int x, int y){
		GridValueLayer traderAgentField = (GridValueLayer) getValueLayer("Trader Agent Field");
		return (int) traderAgentField.get(x,y);
	}
	
	
	
	public void setLandUse (int landUse, int x, int y){
		GridValueLayer landUseField = (GridValueLayer) getValueLayer("Land Use Field");
		landUseField.set(landUse, x,y);
	}
	
	public int getLandUseAt(int x, int y){
		GridValueLayer landUseField = (GridValueLayer) getValueLayer("Land Use Field");
		return  (int) landUseField.get(x,y);
	}
	
	private GridValueLayer loadFieldFromStream(OrganicSpace context, InputStream stream, String fieldName) throws IOException {
		return loadFieldFromStream(context, stream, fieldName, null);
	}
	
	private GridValueLayer loadFieldFromStream(OrganicSpace context, InputStream stream, String fieldName, Range<Double> range) throws IOException {
		int type;
		BufferedReader r = new BufferedReader(new InputStreamReader(stream));
		StreamTokenizer st = new StreamTokenizer(r);

		st.parseNumbers();
		st.wordChars('_', '_');
		st.eolIsSignificant(false);
		st.lowerCaseMode(true);
		// cols
		type = st.nextToken();
		type = st.nextToken();
		xdim = (int) st.nval;
		// rows
		type = st.nextToken();
		type = st.nextToken();
		ydim = (int) st.nval;
		// xllcorner
		type = st.nextToken();
		type = st.nextToken();
		originx = st.nval; 
		// yllcorner
		type = st.nextToken();
		type = st.nextToken();
		originy = st.nval;
		// cellSize
		type = st.nextToken();
		type = st.nextToken();
		cellsize = st.nval;
		
		GridValueLayer field = createField(context, fieldName);
		
		// termx and termy
		// double termx = Math.floor(originx) + cellSize * width;
		// double termy = Math.floor(originy) + cellSize * height;
		// missing
		type = st.nextToken();
		if (type == StreamTokenizer.TT_NUMBER) {
			st.pushBack();
			nodata = -9999;
		} else {
			type = st.nextToken();
			nodata = (int) st.nval;
		}
		st.ordinaryChars('E', 'E');

		double d1;
		
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (int i = ydim - 1; i >= 0; i--) {
			for (int j = 0; j < xdim; j++) {
				st.nextToken();
				d1 = st.nval;
				
				// handle exponents
				type = st.nextToken();
				if (type != StreamTokenizer.TT_NUMBER
						&& type != StreamTokenizer.TT_EOF) {
					if ((st.sval.charAt(0) == 'e' || st.sval.charAt(0) == 'E') && st.sval.length() > 1) {
						d1 = d1 * Math.pow(10.0, Double.valueOf(st.sval.substring(1)));
					} else {
						type = st.nextToken();
						d1 = d1 * Math.pow(10.0, st.nval);
					}
				} else {
					st.pushBack();
				}

				if ((int) d1 != nodata) {
					field.set(d1, j, i);
					
					min = Math.min(min, d1);
					max = Math.max(max, d1);
				} else
					field.set(Double.NaN, j, i);
			}
		}
		
		if (range != null) {
			range.setLower(min);
			range.setUpper(max);
		}
		
		return field;
	}




private GridValueLayer createField(OrganicSpace context, String fieldName) {
	return createField(context, fieldName, true);
}

private GridValueLayer createField(OrganicSpace context, String fieldName, boolean dense) {
	GridValueLayer field = new GridValueLayer(fieldName, dense,
//			new repast.simphony.space.grid.StrictBorders(), xdim, ydim);
			new WrapAroundBorders(), xdim, ydim);
	context.addValueLayer(field);
	return field;
}

//private GridValueLayer createValueLayerFromRandom(OrganicSpace context, GridValueLayer valueLayer) {
//	GridValueLayer elevationField = createField(context, "Elevation");
	private void createValueLayerFromRandom(OrganicSpace context, GridValueLayer valueLayer){
    RandomHelper.registerDistribution("elevationRange", RandomHelper.createUniform(0.1,1));
	AbstractDistribution elevationDist = RandomHelper.getDistribution("elevationRange");
	for (int i = 0; i < xdim; i++) {
		for (int j = 0; j < ydim; j++) {			
			valueLayer.set(elevationDist.nextDouble(), i, j);
			
		//	if (valueLayer.get(i,j)>0) System.out.println(valueLayer.get(i,j));
			//this if statement worked
		}
	}
//	return valueLayer;
}
	public  void populateLandCells(){
	//	Grid<Object> grid = (Grid) this.getProjection("Grid");
		int count=0;
	/*	for (int i=xdim/numAgents;i<600;i++) {
			  	for (int j=500;j<600;j++){
			  		 LandCell c = 
			  				 new LandCell(this,grid,
					    		  i, j,
					    		  this.getElevationAt(i,j),
					    		  this.getOrganicAt(i,j));
			  		 
			  		 c.readLandUse(this, i, j);
			  		 if (c.getLandUse()==LandUse.SOY){
			  			allLandCells.add(c);
			            }
			            if (c.getLandUse()==LandUse.CORN){
			            	allLandCells.add(c);
			            }
			            if (c.getLandUse()==LandUse.RICE){
			            	allLandCells.add(c);
			            }
			            if (c.getLandUse()==LandUse.OTHERCROPS){
			            	allLandCells.add(c);
			            }
			  		 
			  	//	allLandCells.add(c);
			  	
			//  		System.out.println("created one cell "+count++);
			  	}
		}*/
	//	this.getValueLayer("Land Use Field");
	
		for(int i=0; i<xdim; i++){
			for (int j = 0; j<ydim;j++){
				
				while (TeleABMBuilder.receivingSystem) {
				if (this.getLandUseAt(i, j)>=2&&this.getLandUseAt(i, j)<=4)
				{
					Point p=new Point(i,j);
					allAgriculturalPoints.add(p);
				}
				if (this.getLandUseAt(i, j)==6){
					Point p=new Point(i,j);
					allAgriculturalPoints.add(p);
				}
				}
				
				while (TeleABMBuilder.sendingSystem){
					
					if (this.getLandUseAt(i, j)>=1&&this.getLandUseAt(i, j)<=3)
					{
						Point p=new Point(i,j);
						allAgriculturalPoints.add(p);
						count++;
						System.out.println("really?! "+count);
					}
					if (this.getLandUseAt(i, j)==9){
						Point p = new Point(i,j);
						allAgriculturalPoints.add(p);
						count++;
					}
				}
			}
		}
		System.out.println("availble sizes "+allAgriculturalPoints.size());
		System.out.println("really?! "+count);
//		this.allAgriculturalPoints = allAgriculturalPoints;
		
	}
	
	public List<LandCell> getAllLandCells(){
	 return this.allLandCells;	
	}
	
	public List<Point> getAgriculturalPoints(){
		return allAgriculturalPoints;
	}
	
	public List<Point> canPossess(){
		 
      int xLook, yLook;
  //    List<Point> canTake = allAgriculturalPoints;
     
   //   System.out.println("count possess cells "+allAgriculturalPoints.size());
      
     while (allAgriculturalPoints.iterator().hasNext()){
    	  Point p = allAgriculturalPoints.iterator().next();
    //	  System.out.println(this.getLandHolder(p.x, p.y));
    	  if (this.getLandHolder(p.x,p.y)>0){
    			  System.out.println("conflict");
    		  allAgriculturalPoints.iterator().remove();
            }
      
     
     }
     System.out.println("remove has taken "+allAgriculturalPoints.size());

  return allAgriculturalPoints;
      
 //     return allAgriculturalPoints;

}
	
	/*public SoybeanAgent getAllSoybeanAgent(){
		if(TeleABMBuilder.receivingSystem)
		return (SoybeanAgent) this.
				receivingSoybeanAgents.get(0);
		else 
			return (SoybeanAgent) sendingSoybeanAgents.get(0);
	}*/
	
	
	
}