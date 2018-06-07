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
import repast.simphony.dataLoader.ContextCreator;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;

public class OrganicSpace extends DefaultContext<Object> {

	// the Sugar space has a grid that tracks the maximum
	// amount of sugar and the current value at each x,y coordinate.

	// default sugar grow rate and the default maximum sugar
	Parameters p = RunEnvironment.getInstance().getParameters();

	double organicGrowRate = (double) p.getValue("organicGrowRate");

	private int xdim ;
	private int ydim ;
//	private boolean receivingSystem = p.getBoolean("receiving system representation");
//	private boolean sendingSystem = p.getBoolean("sending system representation");
	int numAgents = (Integer)p.getValue("initialReceivingNumAgents");
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
	//	super("organicSpace"); 
		Grid<Object> grid;
	//projection
		
		GridValueLayer currentOrganic;
		GridValueLayer maxOrganic;
		GridValueLayer elevation;
		GridValueLayer landHolderField;
		GridValueLayer tempField;
		GridValueLayer precipitationField;
		GridValueLayer landUseField;
		GridValueLayer traderAgentField;
	//list of value layers being added to the system/display;	
		
	//	if(TeleABMBuilder.receivingSystem) {
		if(organicFile =="misc/organicmatter.asc")	{
	//	    OrganicSpace<Object> organicSpace = new OrganicSpace<Object>; 
	
		    xdim = (Integer)p.getValue("receivingWorldWidth");
			 ydim = (Integer)p.getValue("receivingWorldHeight");
			 grid = GridFactoryFinder.createGridFactory(null)
						.createGrid("Grid", this, new GridBuilderParameters<Object>(
								new WrapAroundBorders(), 
								new RandomGridAdder<Object>(), false, xdim, ydim));
		 this.setTypeID("organicSpaceReceiving");
		 this.setId("organicSpaceReceiving");
		 System.out.println("test if receiving context being created="+this.getTypeID());
	//	 System.out.println(ContextUtils.getContext(this));
	//	 System.out.println(this.getTypeID());
//		 OrganicSpace organicSpaceReceiving = new OrganicSpace().setId("organicSpaceReceiving");;
		 
		 
				 
		} 
		else if(organicFile=="misc/2005sinop.asc") 
		{
			  xdim = (Integer)p.getValue("sendingWorldWidth");
				 ydim = (Integer)p.getValue("sendingWorldHeight");
				 
				grid = GridFactoryFinder.createGridFactory(null)
							.createGrid("gridSending", this, new GridBuilderParameters<Object>(
									new WrapAroundBorders(), 
									new RandomGridAdder<Object>(), false, xdim, ydim));
			    this.setTypeID("organicSpaceSending");
				this.setId("organicSpaceSending");
				 System.out.println("test if sending context being created="+this.getTypeID());
				 
				 
				
				
		} else {
			xdim=1000;
			ydim=1000;
			grid = GridFactoryFinder.createGridFactory(null)
					.createGrid("Grid", this, new GridBuilderParameters<Object>(
							new WrapAroundBorders(), 
							new RandomGridAdder<Object>(), false, xdim, ydim));
			
	    
		}
		
		
		
	/*	Grid<Object> grid = GridFactoryFinder.createGridFactory(null)
		.createGrid("Grid", this, new GridBuilderParameters<Object>(
				new WrapAroundBorders(), 
				new RandomGridAdder<Object>(), false, xdim, ydim));*/

		
//add current organic layer
	
		if(this.getTypeID()=="organicSpaceReceiving")
			{
			currentOrganic = new GridValueLayer("CurrentOrganicReceiving",true,
					new WrapAroundBorders(), xdim, ydim);
			System.out.println("receiving: current organic");
			}
		else if(this.getTypeID()=="organicSpaceSending")
			{
			currentOrganic = new GridValueLayer("CurrentOrganicSending",true,
					new WrapAroundBorders(), xdim, ydim);
	         System.out.println("sending dimension="+currentOrganic.getDimensions()); }
		else {currentOrganic = new GridValueLayer("CurrentOrganicSending",true,
				new WrapAroundBorders(), xdim, ydim);}
		
		BufferedInputStream stream = null;
	
	//	createValueLayerFromRandom(this, currentOrganic);
	//for now, the organic range is from 0.1 to 1. it takes 10 years to go back to max.
		

		
		//read ascii file from here
		stream = null;
		Range<Double> soilOrganicCarbon = new Range<Double>(0d, 0d);
		try {
	//		if(TeleABMBuilder.receivingSystem)
			if(this.getId()=="organicSpaceReceiving")	
			{
				stream = new BufferedInputStream(new FileInputStream("misc/organicmatter.asc"));
				currentOrganic = loadFieldFromStream(this, stream, "CurrentOrganicReceiving", soilOrganicCarbon);
			}
		//	stream = new BufferedInputStream(new FileInputStream("misc/heilong_2005.asc"));
			else	//if(TeleABMBuilder.sendingSystem)
				if(this.getId()=="organicSpaceSending")	
			{	stream = new BufferedInputStream(new FileInputStream("misc/2005.txt"));
		    	currentOrganic = loadFieldFromStream(this, stream, "CurrentOrganicSending", soilOrganicCarbon);
		//	System.out.println("sending system land use read "+landUseField.get(500,500));
			}
		//	currentOrganic = loadFieldFromStream(this, stream, "CurrentOrganicReceiving", soilOrganicCarbon);
	//		System.out.println("receiving: current organic");
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
		
//add max organic layer		
				if(this.getTypeID()=="organicSpaceReceiving")
				maxOrganic= new GridValueLayer("MaxOrganicReceiving", true, 
						new WrapAroundBorders(), xdim, ydim);
				else 
				maxOrganic= new GridValueLayer("MaxOrganicSending", true, 
							new WrapAroundBorders(), xdim, ydim);
			
				
				this.addValueLayer(maxOrganic);
				
				
				createValueLayerFromRandom(this, maxOrganic);
			//	System.out.println("max organic dimension"+this.getValueLayer("MaxOrganicReceiving").getDimensions());
				
	
//add elevation to the system		
		if(this.getTypeID()=="organicSpaceReceiving")
		{
		    elevation = new GridValueLayer("ElevationReceiving",true,
				new WrapAroundBorders(), xdim, ydim);
		 
		    createValueLayerFromRandom(this, elevation); 
		    this.addValueLayer(elevation);
			System.out.println("receiving: elevation");
		    }
		else {
			 elevation = new GridValueLayer("ElevationSending",true,
						new WrapAroundBorders(), xdim, ydim);
			    this.addValueLayer(elevation);
				    createValueLayerFromRandom(this, elevation); 
				    System.out.println("sending: elevation");
		}
				
	//	this.addValueLayer(elevation);
		//finish reading ascii file here

		

//iterate land cells
		for (int i = 0 ;i < xdim; i++){
			for (int j = 0; j<ydim; j++){
				if (elevation.get(i,j) >= 0){
			//	new LandCell(this, grid, i, j, elevation.get(i,j), currentOrganic.get(i,j));
				
		//		this.add(LandCell);
			
			} else {
		//		new WaterCell(context, landscapeGrid, i, j, elevation);
			}
			}
		}
		
//create Land holder value layer to the system		
		if(this.getTypeID()=="organicSpaceReceiving")
		{ landHolderField = new GridValueLayer("Land Holder Field Receiving", true,
				new WrapAroundBorders(), xdim, ydim);
		  System.out.println("receiving: land holder field");
		}
		else if(this.getTypeID()=="organicSpaceSending")
		{	landHolderField = new GridValueLayer("Land Holder Field Sending", true,
					new WrapAroundBorders(), xdim, ydim); }
		else landHolderField = new GridValueLayer("Land Holder Field Sending", true,
				new WrapAroundBorders(), xdim, ydim);
	//	System.out.println(landHolderField..xdim);
	//	System.out.println("height"+this.ydim);
		this.addValueLayer(landHolderField);
//		takePossession(tenureField, landscapeGrid, RandomHelper.getDistribution("hectares").nextDouble(), environmentalContext.getCellsize(), coordinates);
	//	 System.out.println(	this.getElevationAt(10, 10));
	//	System.out.println("elevation field created in OrganicSpace");
	
		
//add temperature zone
		if(this.getTypeID()=="organicSpaceReceiving")
		 tempField = new GridValueLayer("temp zone Receiving", true, 
				new WrapAroundBorders(),xdim, ydim);
		else
		 tempField = new GridValueLayer("temp zone Sending", true, 
					new WrapAroundBorders(),xdim, ydim);
		this.addValueLayer(tempField);

//add precipitation field
		if(this.getTypeID()=="organicSpaceReceiving")
		precipitationField = new GridValueLayer("precipitation zone Receiving", true, 
				new WrapAroundBorders(),xdim, ydim);
		else
		precipitationField = new GridValueLayer("precipitation zone Sending", true, 
					new WrapAroundBorders(),xdim, ydim);
		this.addValueLayer(precipitationField);

		
//read land use
		stream = null;
		Range<Double> landUseTypes = new Range<Double>(0d, 0d);
		try {
			if(this.getTypeID()=="organicSpaceReceiving") 
			{
				landUseField = new GridValueLayer("Land Use Field Receiving", true,
	     				new WrapAroundBorders(), xdim, ydim);
				this.addValueLayer(landUseField);
	        	stream = new BufferedInputStream(new FileInputStream("misc/baoshan_06_crops.asc"));
	        	landUseField = loadFieldFromStream(this, stream, "Land Use Field Receiving", landUseTypes);
	        	System.out.println("receiving: land use field");
			}
			else {
				landUseField = new GridValueLayer("Land Use Field Sending", true,
						new WrapAroundBorders(), xdim, ydim);
				this.addValueLayer(landUseField);
		    	stream = new BufferedInputStream(new FileInputStream("misc/2005.txt"));
		    	landUseField = loadFieldFromStream(this, stream, "Land Use Field Sending", landUseTypes);
			}
		    }  catch (IOException e) {
					e.printStackTrace();
				//	elevation = createValueLayerFromRandom(this, elevation);
					landUseField = new GridValueLayer("Land Use Field Receiving", true,
		     				new WrapAroundBorders(), xdim, ydim);	
					createValueLayerFromRandom(this, landUseField);
				} finally {
					try {
						if (stream != null) 
							stream.close();
					} catch (IOException e) {}
				}
		
		
		
		
		//try {
		//	if(TeleABMBuilder.receivingSystem)
				
		//	stream = new BufferedInputStream(new FileInputStream("misc/baoshan.asc"));
		//	stream = new BufferedInputStream(new FileInputStream("misc/heilong_2005.asc"));
		//	else	if(TeleABMBuilder.sendingSystem)
		//	{	
		//	System.out.println("sending system land use read "+landUseField.get(500,500));
		//	}
			
		//	System.out.println(this.getValueLayer("Land Use Field Receiving"));
		//} catch (IOException e) {
		//	e.printStackTrace();
		//	elevation = createValueLayerFromRandom(this, elevation);
		//	createValueLayerFromRandom(this, landUseField);
		//} finally {
		//	try {
		//		if (stream != null) 
		//			stream.close();
		//	} catch (IOException e) {}
		//}
		
		this.addValueLayer(landUseField);
		
		
		//add trader agent vision file
		if(this.getTypeID()=="organicSpaceReceiving") 
	     traderAgentField = new GridValueLayer("Trader Agent Field Receiving", true,
				new WrapAroundBorders(), xdim, ydim);
		else
		traderAgentField = new GridValueLayer("Trader Agent Field Sending", true,
						new WrapAroundBorders(), xdim, ydim);
	//	System.out.println(context.width);
	//	System.out.println("height"+context.height);
		this.addValueLayer(traderAgentField);
		
	//	return this;
	}



	// The actual implementation of growback rule G, pg 182 (Appendix B).
//	@ScheduledMethod(start=0,interval=1)
	public void updateOrganic() {
		int organicAtSpot;
		int maxOrganicAtSpot;

		GridValueLayer currentOrganic ;
		GridValueLayer maxOrganic ;

		
		if(this.getTypeID()=="organicSpaceReceiving"){
			currentOrganic = (GridValueLayer)getValueLayer("CurrentOrganicReceiving");
			maxOrganic = (GridValueLayer)getValueLayer("MaxOrganicReceiving");
		} else
		{
			currentOrganic = (GridValueLayer)getValueLayer("CurrentOrganicSending");
			maxOrganic = (GridValueLayer)getValueLayer("MaxOrganicSending");
		}
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
		
		GridValueLayer currentOrganic;
		
		if(this.getTypeID()=="organicSpaceReceiving")
				currentOrganic = (GridValueLayer)getValueLayer("CurrentOrganicReceiving");
		else 
			currentOrganic = (GridValueLayer)getValueLayer("CurrentOrganicSending");
		
		
		int i = (int)currentOrganic.get(x,y);		
		currentOrganic.set(0, x,y);
		
		return i;
	}

	// gets the amount of sugar at this x,y coordinate
	public int getOrganicAt(int x, int y) {		
		GridValueLayer currentOrganic;
		if(this.getTypeID()=="organicSpaceReceiving")
		currentOrganic = (GridValueLayer)getValueLayer("CurrentOrganicReceiving");
		else
			currentOrganic = (GridValueLayer)getValueLayer("CurrentOrganicSending");
		return (int) currentOrganic.get(x,y);
	}
	
	public void setOrganicAt(double soc, int x, int y){
		GridValueLayer currentOrganic;
		if(this.getTypeID()=="organicSpaceReceiving")
		currentOrganic = (GridValueLayer)getValueLayer("CurrentOrganicReceiving");
		else
			currentOrganic = (GridValueLayer)getValueLayer("CurrentOrganicSending");
		
		currentOrganic.set(soc, x,y);
	}
	
	public void setTempAt(double temp, int x, int y){
		GridValueLayer tempField; 
		if(this.getTypeID()=="organicSpaceReceiving")
			tempField = (GridValueLayer)getValueLayer("temp zone Receiving");
		else 
			tempField =(GridValueLayer)getValueLayer("temp zone Sending");
		tempField.set(temp, x,y);
	}
	
	public double getTempAt(int x, int y){
		GridValueLayer tempField; 
		if(this.getTypeID()=="organicSpaceReceiving")
			tempField = (GridValueLayer)getValueLayer("temp zone Receiving");
		else 
			tempField =(GridValueLayer)getValueLayer("temp zone Sending");
		return tempField.get(x,y);
	}
	
	public void setPrecipitationAt(double precipitation, int x, int y){
		GridValueLayer precipitationField;
		
		if(this.getTypeID()=="organicSpaceReceiving")
		
			precipitationField= (GridValueLayer)getValueLayer("precipitation zone Receiving");
		else 
			precipitationField= (GridValueLayer)getValueLayer("precipitation zone Sending");
		
		precipitationField.set(precipitation, x,y);
	}
	
	public double getPrecipitationAt(int x, int y){
    	GridValueLayer precipitationField;
		
		if(this.getTypeID()=="organicSpaceReceiving")
		
			precipitationField= (GridValueLayer)getValueLayer("precipitation zone Receiving");
		else 
			precipitationField= (GridValueLayer)getValueLayer("precipitation zone Sending");
		
		
		return precipitationField.get(x,y);
	}
	
	public double getElevationAt(int x, int y){
		
		GridValueLayer elevation ;
		if(this.getTypeID()=="organicSpaceReceiving")
			elevation= (GridValueLayer) this.getValueLayer("ElevationReceiving");
		else 
			elevation= (GridValueLayer) this.getValueLayer("ElevationSending");
		return (double) elevation.get(x,y);

	}
	
	public void setLandHolder (double agentID, int x, int y) {
		GridValueLayer landholderField;
		if(this.getTypeID()=="organicSpaceReceiving")
		 landholderField = (GridValueLayer) getValueLayer("Land Holder Field Receiving");
		else 
			 landholderField = (GridValueLayer) getValueLayer("Land Holder Field Sending");
		landholderField.set(agentID, x,y);
	}
	public Integer getLandHolder(int x, int y){
		GridValueLayer landholderField;
		if(this.getTypeID()=="organicSpaceReceiving")
		 landholderField = (GridValueLayer) getValueLayer("Land Holder Field Receiving");
		else 
			 landholderField = (GridValueLayer) getValueLayer("Land Holder Field Sending");
		return (int) landholderField.get(x,y);
	}
	
	public void setTraderAgent(double agentID, int x, int y){
		GridValueLayer traderAgentField;
		
		if(this.getTypeID()=="organicSpaceReceiving")
		
		traderAgentField= (GridValueLayer) getValueLayer("Trader Agent Field Receiving");
		else
			traderAgentField= (GridValueLayer) getValueLayer("Trader Agent Field Sending");
		
		traderAgentField.set(agentID, x,y);
	}
	
	
	
	public Integer getTraderAgent(int x, int y){
          GridValueLayer traderAgentField;
		
		if(this.getTypeID()=="organicSpaceReceiving")
		
		traderAgentField= (GridValueLayer) getValueLayer("Trader Agent Field Receiving");
		else
			traderAgentField= (GridValueLayer) getValueLayer("Trader Agent Field Sending");
		return (int) traderAgentField.get(x,y);
	}
	
	
	
	public void setLandUse (int landUse, int x, int y){
		GridValueLayer landUseField ;
		if(this.getTypeID()=="organicSpaceReceiving")
			landUseField = (GridValueLayer) getValueLayer("Land Use Field Receiving");
		else
			landUseField = (GridValueLayer) getValueLayer("Land Use Field Sending");
		landUseField.set(landUse, x,y);
	}
	
	public int getLandUseAt(int x, int y){
		GridValueLayer landUseField ;
		if(this.getTypeID()=="organicSpaceReceiving")
			landUseField = (GridValueLayer) getValueLayer("Land Use Field Receiving");
		else
			landUseField = (GridValueLayer) getValueLayer("Land Use Field Sending");
		return  (int) landUseField.get(x,y);
	}
	
	private GridValueLayer loadFieldFromStream(OrganicSpace organicSpace, InputStream stream, String fieldName) throws IOException {
		return loadFieldFromStream(organicSpace, stream, fieldName, null);
	}
	
	private GridValueLayer loadFieldFromStream(OrganicSpace organicSpace, InputStream stream, String fieldName, Range<Double> range) throws IOException {
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
		
		GridValueLayer field = createField(organicSpace, fieldName);
		
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




private GridValueLayer createField(OrganicSpace organicSpace, String fieldName) {
	return createField(organicSpace, fieldName, true);
}

private GridValueLayer createField(OrganicSpace organicSpace, String fieldName, boolean dense) {
	GridValueLayer field = new GridValueLayer(fieldName, dense,
//			new repast.simphony.space.grid.StrictBorders(), xdim, ydim);
			new WrapAroundBorders(), xdim, ydim);
	organicSpace.addValueLayer(field);
	return field;
}

//private GridValueLayer createValueLayerFromRandom(OrganicSpace context, GridValueLayer valueLayer) {
//	GridValueLayer elevationField = createField(context, "Elevation");
	private void createValueLayerFromRandom(OrganicSpace organicSpace, GridValueLayer valueLayer){
		
	if(organicSpace.getTypeID()=="organicSpaceReceiving"){
		   xdim = (Integer)p.getValue("receivingWorldWidth");
			 ydim = (Integer)p.getValue("receivingWorldHeight");
	}
	else {
		  xdim = (Integer)p.getValue("sendingWorldWidth");
			 ydim = (Integer)p.getValue("sendingWorldHeight");
	}
    RandomHelper.registerDistribution("elevationRange", RandomHelper.createUniform(0.1,1));
	AbstractDistribution elevationDist = RandomHelper.getDistribution("elevationRange");
	for (int i = 0; i < xdim; i++) {
		for (int j = 0; j < ydim; j++) {			
			valueLayer.set(elevationDist.nextDouble(), i, j);
		//	valueLayer.set(10.0,i,j);
		//	if (valueLayer.get(i,j)>0) System.out.println(valueLayer.get(i,j));
			//this if statement worked
		}
	}
//	return valueLayer;
}

	public  void populateLandCells(){
	//	Grid<Object> grid = (Grid) this.getProjection("Grid");
		int count=0;
	
		for(int i=0; i<xdim; i++){
			for (int j = 0; j<ydim;j++){
				
				while (TeleABMBuilder.receivingSystem) {
				if (this.getLandUseAt(i, j)>=2&&this.getLandUseAt(i, j)<=3)
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