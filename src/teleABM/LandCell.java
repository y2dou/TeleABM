package teleABM;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialException;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;
import repast.simphony.valueLayer.ValueLayer;
import teleABM.SoybeanAgent;


public class LandCell {
	Parameters p = RunEnvironment.getInstance().getParameters();
	double organicGrowRate = (double) p.getValue("organicGrowRate");
	int cellsize = (Integer) p.getValue("cellSize");
	 private SoybeanAgent landHolder;
	 private  int xlocation;
	 
	 private LandUse landUse;
     private LandUse lastLandUse;
     
	 private int ylocation;
	 private double elevation;
	 private double soc;
	 private double initialSoc;
	 private boolean taken;
	 
//	 private double soyHealth;
//	 private double cornHealth;
//	 private double riceHealth;
//	 private double otherHealth;
	 
	 private double soyYield;
	 private double cornYield;
	 private double riceYield;
	 private double otherYield;
	 private double cropYield; //this is to report back to soybean agent of yield easier
	 
	 private int soyAge;
	 private int cornAge;
	 private int riceAge;
	 private int otherAge;
	 private int forestAge;
	 
	 private int deforestedyear;
	 private double fertilizerInput;
	 private double fuelInput;
	 
	
	private int tempZone = 0;
	private double precipitation = 0;
	 private double soil1=0;
	 private double soil2=0;
	 private double soil3=0;
	 
	 private double recommendedSoyUnitFertilizerUse=0;
	 //according to a chinese paper, the average is 148 kg/ha,
	 //each cell is 30*30=900 sq m, that is 0.09hectare, 
	 //hence the recommended soy unit fertilizer use =
	 private double recommendedCornUnitFertilizerUse=200.0;
	 private double recommendedRiceUnitFertilizerUse=150.0;
	 private double recommendedOtherUnitFertilizerUse=100.0;
	 
	 private double observedSoyUnitFertilizerUse = 63.0;
	 private double observedCornUnitFertilizerUse = 224.0;
	 private double observedRiceUnitFertilizerUse = 146.0;
	 private double observedOtherUnitFertilizerUse = 120.0;
	

	public boolean isTaken() {
		return taken;
	}
	
	public LandCell(OrganicSpace organicSpace, int x, int y){
		//super(x,y);
	}

	public LandCell(OrganicSpace organicSpace, Grid grid,  int x, int y, double elevation, 
			double organic) {
		this.xlocation=x;
		this.ylocation=y;
		
		this.elevation=elevation;
		this.soc = organic;

}

	
//@ScheduledMethod(start = 0, interval = 1)
//because landcell is not an agent, scheuled method doesn't work here
//yield happens here
	public void transition() {
	
//	System.out.println("soy Yield: "+soyYield);
		age();
		setFertilizerInput(this.getLandUse());
	//	System.out.println(this.getSoc());
		setSoc(this.getSoc());
	//	setCropYield();
		/*if (soc>=0.5) {
			//if organic is on the upper half, 
			soyHealth = RandomHelper.nextDoubleFromTo(0.5, soc);
			cornHealth = RandomHelper.nextDoubleFromTo(0.5, soc);
			riceHealth = RandomHelper.nextDoubleFromTo(0.5, soc);
			otherHealth = RandomHelper.nextDoubleFromTo(0.5, soc);
		}
		else {
			soyHealth = RandomHelper.nextDoubleFromTo(soc,0.5);
			cornHealth = RandomHelper.nextDoubleFromTo(soc,0.5);
			riceHealth = RandomHelper.nextDoubleFromTo(soc,0.5);
			otherHealth = RandomHelper.nextDoubleFromTo(soc,0.5);
		}*/
		
	//	System.out.println("riceHEALTH "+riceHealth);
		
/*		soyYield = soyHealth * RandomHelper.getDistribution("soyYield").nextDouble()
				             *this.getFertilizerInput();
		cornYield = cornHealth * RandomHelper.getDistribution("cornYield").nextDouble()
				          *this.getFertilizerInput();
		riceYield = riceHealth * RandomHelper.getDistribution("riceYield").nextDouble()
				          *this.getFertilizerInput();
		otherYield = otherHealth * RandomHelper.getDistribution("otherYield").nextDouble()
				*this.getFertilizerInput();*/
		soyYield = 2000.0*this.getFertilizerInput()/recommendedSoyUnitFertilizerUse
				+RandomHelper.nextDoubleFromTo(-10.0,20.0);
	//	soyYield=2000;
		cornYield =( (double) -0.019*this.getFertilizerInput()*this.getFertilizerInput()
				    + 10.85*this.getFertilizerInput()
				    + 1840.6) * soc;
		
		if (this.getFertilizerInput()>recommendedRiceUnitFertilizerUse)
			riceYield = (double) (8112.4*(cellsize*cellsize/10000))*soc;
		 else riceYield = (double) (8112.4*(cellsize*cellsize/10000)
				                   -RandomHelper.nextDoubleFromTo(10, 50))*soc;
	//	riceYield = (double)  8000.0*this.getFertilizerInput()/recommendedRiceUnitFertilizerUse;
		
		otherYield = (double) 1000.0*this.getFertilizerInput()/recommendedOtherUnitFertilizerUse;
		
		//May 5, simplified yield function;
		
	//	System.out.println("riceYield "+riceYield);
	//	riceYield = 20;
	/*	if (lastLandUse==LandUse.RICE) 
			System.out.println("tick "+RunState.getInstance().
				getScheduleRegistry()
				.getModelSchedule().getTickCount()+" riceYield "+riceYield);*/
		
		if(this.getLandUse()==LandUse.CORN) {setCropYield(cornYield);
		                                   //  System.out.println("corn yield = "+cornYield);
		}
		if(this.getLandUse()==LandUse.RICE) setCropYield(riceYield);
		if(this.getLandUse()==LandUse.SOY) { setCropYield(soyYield);
		                                    // System.out.println("soy yield="+this.getCropYield());
		                                   //  System.out.println("get fertilizer input = "+this.getFertilizerInput());
		                                   //  System.out.println(this.getFertilizerInput()/recommendedSoyUnitFertilizerUse);
		                                    }
		if(this.getLandUse()==LandUse.OTHERCROPS) setCropYield(otherYield);
		
		
	}
	
	private void age() {
		// TODO Auto-generated method stub
		if (lastLandUse==LandUse.SOY) soyAge++;
		 else soyAge=0;
		
	    if (lastLandUse==LandUse.CORN) cornAge++;  
	    else cornAge=0;
	    
		if (lastLandUse==LandUse.RICE) riceAge++;
		  else  riceAge=0;
		
		if (lastLandUse==LandUse.FOREST) forestAge++;
	      else forestAge=0;
		
		if(lastLandUse==LandUse.OTHERCROPS) otherAge++;
		  else otherAge=0;
		
		
	}

	public void setLandHolder(boolean taken, SoybeanAgent landHolder) {
		//System.out.println("land holder "+landHolder.getID());
		//System.out.println(this.xlocation+" "+this.ylocation);
		this.taken = true;
		this.landHolder = landHolder;
	}
	
	public SoybeanAgent getLandHolder() {
		return landHolder;
	}
	
	public void setCropYield(double yield) {
	/*	if (landUse == LandUse.SOY) cropYield=soyYield;
		if (landUse == LandUse.CORN) cropYield=cornYield;
		if (landUse == LandUse.RICE) cropYield = riceYield;
		if (landUse == LandUse.OTHERCROPS) cropYield=otherYield;	*/	
//		System.out.println("yield= "+yield);
		this.cropYield=yield;
	}
	
	public double getCropYield(){
		return cropYield;
	}
	protected LandCell canTakePossession(GridValueLayer tenureField,
			 int x, int y) {
		try {		
				if (this.getLandHolder() == null) {
					return this;
				}
			
		} catch (SpatialException e) {
		}
		return null;
	}

	protected LandCell canTakePossession(GridValueLayer tenureField,
			Point p) {

		return canTakePossession(tenureField,  p.x, p.y);
	}
	
	public LandCell getCell() {
		return this;
	}
	
	public void setLandUse(LandUse landUse) {
		this.landUse = landUse;
	}
	
	public void setLastLandUse(LandUse landUse) {
	//	 OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
		this.lastLandUse = landUse;
		
	/*	if(landUse==LandUse.RICE)
		organicSpace.setLandUse(3, this.getXlocation(), this.getYlocation());
		if(landUse==LandUse.FOREST)
			organicSpace.setLandUse(5, this.getXlocation(), this.getYlocation());*/
	}
	
	public LandUse getLandUse () {
		return this.landUse;
	}
	
	public LandUse getLastLandUse() {
		return lastLandUse;
	}

	

	public void readLandUse(OrganicSpace organicSpace, int x, int y){
   	 //problem here is it puts all kinds of land use to the agent
    	if (organicSpace.getLandUseAt(x, y)==2){
    		this.landUse=LandUse.SOY;
    	} else if (organicSpace.getLandUseAt(x, y)==3) {
    		this.landUse=LandUse.RICE;
    	} else if (organicSpace.getLandUseAt(x, y)==6) {
    		this.landUse=LandUse.CORN;
    	} else if (organicSpace.getLandUseAt(x, y)==4) {
    		this.landUse=LandUse.OTHERCROPS;
    	} else if (organicSpace.getLandUseAt(x, y)==5){
    		this.landUse=LandUse.FOREST; 
    	} else if (organicSpace.getLandUseAt(x, y)==7){		    		
    		this.landUse=LandUse.BUILDING; 
    	} else {
    		this.landUse=LandUse.WATER; 
    	}
	}
	
	 public int getXlocation() {
			return xlocation;
		}

		public void setXlocation(int xlocation) {
			this.xlocation = xlocation;
		}

		public int getYlocation() {
			return ylocation;
		}

		public void setYlocation(int ylocation) {
			this.ylocation = ylocation;
		}
		
		public double getFertilizerInput() {
			return fertilizerInput;
		}

//      public void setFertilizerInput() {
	// changed to following on April 26, 2018
		
		public void setFertilizerInput(LandUse landuse) {

			if(this.landUse==LandUse.SOY){
			//	fertilizerInput=recommendedSoyUnitFertilizerUse*(1+soyAge);
			//	fertilizerInput = recommendedSoyUnitFertilizerUse * ((cellsize*cellsize)/10000);
				fertilizerInput = observedSoyUnitFertilizerUse * ((cellsize*cellsize)/10000.0);
		//		System.out.println("observed fertilizer input = "+observedSoyUnitFertilizerUse);
		//		System.out.println("cellsize = "+cellsize);
		//		System.out.println("soy fertilizer input = "+fertilizerInput);
		//		fertilizerInput=recommendedSoyUnitFertilizerUse;
				 //problem before is that crop age can be zero to start with, 
				 //so when multiplied it's zero for fertilizer input
				
			}
			if(this.landUse==LandUse.CORN){
			//	fertilizerInput=recommendedCornUnitFertilizerUse*((cellsize*cellsize)/10000);
				fertilizerInput = observedCornUnitFertilizerUse*((cellsize*cellsize)/10000.0);
		//		 System.out.println("CORN fertilizer input = "+fertilizerInput);
		//		fertilizerInput=recommendedCornUnitFertilizerUse;
			}
			if(this.landUse==LandUse.RICE){
			//	fertilizerInput=recommendedRiceUnitFertilizerUse*((cellsize*cellsize)/10000);
				fertilizerInput = observedRiceUnitFertilizerUse*((cellsize*cellsize)/10000.0);
		//		 System.out.println("OTHER fertilizer input = "+fertilizerInput);
		//		fertilizerInput=recommendedRiceUnitFertilizerUse;
			}
			if(this.landUse==LandUse.OTHERCROPS){
			//	fertilizerInput=recommendedOtherUnitFertilizerUse*((cellsize*cellsize)/10000);
				fertilizerInput = observedOtherUnitFertilizerUse*((cellsize*cellsize)/10000.0);
		//		fertilizerInput=recommendedOtherUnitFertilizerUse;
			}
			
			
			//simplied fertilizer usage, longer year, more fertilizer. 
			//should overwrite to  
			    this.fertilizerInput = fertilizerInput+RandomHelper.nextIntFromTo(-5,5);
			   
			//	this.fertilizerInput = RandomHelper.nextDoubleFromTo(0.8, 1.2);
		}
		
		
		public void setTempZone(){
			this.tempZone=1;
			this.precipitation = 500;
		}
		public int getTempZone(){
			return tempZone;
		}
		public void setSoil1(){
			this.soil1=0.2;
		}
		public void setSoil2(){
			this.soil2=0.2;
		}
		public void setSoil3(){
			this.soil3=1-soil1-soil2;
		}
		public double getSoil1(){
			return soil1;
		}
		public double getSoil2(){
			return soil2;
		}
		public double getSoil3(){
			return soil3;
		}
		
	    public double getFuelInput() {
				return fuelInput;
			}

		public void setFuelInput(double fuelInput) {
				this.fuelInput = fuelInput;
			}
		
		public double getSoc() {
			return soc;
		}

		public void setSoc(double soc) {
			double sci=0;
			
			if(this.getLandUse()==LandUse.SOY){
				sci= // this is straw, (this.getCropYield()*1.0818+269.6)*0.445
						+ 0.262*this.getCropYield()
						+ 0.262*this.getCropYield()*0.4;
				 
				soc = soc-(0.13*tempZone-0.04*precipitation-0.27*soc
						+0.03*getFertilizerInput()+0.68*sci);
			}
		
			else soc=0.95*soc;
			
			System.out.println("soc="+soc);
			this.soc=soc;
		}

}