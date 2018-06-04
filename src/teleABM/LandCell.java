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

import cern.jet.random.AbstractDistribution;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.space.grid.RandomGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;



import  teleABM.PGMReader;
import teleABM.Range.*;
import teleABM.OrganicSpace;




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
	 private double lastyearSoc;
	 private boolean taken = false;
	 
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
	 
	
	private double tempZone = 0;
	private double precipitation = 0;
	 private double soil1=0;
	 private double soil2=0;
	 private double soil3=0;
	 
	 private double recommendedSoyPerHaFertilizerUse=10;
	 //according to a chinese paper, the average is 148 kg/ha,
	 //each cell is 30*30=900 sq m, that is 0.09hectare, 
	 //hence the recommended soy unit fertilizer use =
	 private double recommendedCornPerHaFertilizerUse=200.0;  //kg/ha
	 private double recommendedRicePerHaFertilizerUse=150.0;   //kg/ha
	 private double recommendedOtherPerHaFertilizerUse=100.0;  //kg/ha
	 
	 private double observedSoyPerHaFertilizerUse = 63.0;      //kg/ha
	 private double observedCornPerHaFertilizerUse = 224.0;    //kg/ha
	 private double observedRicePerHaFertilizerUse = 146.0;    //kg/ha
	 private double observedOtherPerHaFertilizerUse = 120.0;   //kg/ha
	 
	 private double waterRequirement;
	 
	

	
	public boolean isTaken() {
		return this.taken;
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
		 OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this.landHolder);
		//if it's only getContext(this), because land cells are not added to organicspace,
		 //has to be this.landholder
		
//		 GridValueLayer currentOrganic = (GridValueLayer) organicSpace.getValueLayer("CurrentOrganic");
		 organicSpace.setTempAt(20.0, this.getXlocation(), this.getYlocation());
		 setTempZone(organicSpace.getTempAt(this.getXlocation(), this.getYlocation()));
		 organicSpace.setPrecipitationAt(50.0, this.getXlocation(), this.getYlocation()); //unit: cm/year
		 setPrecipitation(organicSpace.getPrecipitationAt(this.getXlocation(), this.getYlocation()));
		 //tempretuare can be updated yearly later
		 
		 soc = organicSpace.getOrganicAt(this.getXlocation(), this.getYlocation());	
	//	 System.out.println("previous organic = "+ 	soc);
	
		 
		age();
	//	setFertilizerInput(this.getLandUse());
	//	setWaterRequirement(this.getLandUse());
	//	System.out.println(this.getSoc());
	
		//soc used in this year's crop growing is last year's soc
		
		//this is from the paper
		// The Influences of Different Nitrogen on the Soybean Production of 'Henong60'
		// Shen Xiaohui, et al. 2013
		// Journal of Agriculture 2013, 3(06):17-19
		// in Chinese
		if(this.getFertilizerInput()==0){
			soyYield = 2416.0;   //unit: kg/ha
		} else if (this.getFertilizerInput()<30.0){
			soyYield = 2643.0;   //unit: kg/ha
		} else if(this.getFertilizerInput()<60.0){
			soyYield = 2734.0;   //unit: kg/ha
		} else {
			soyYield = 2242.0;  //unit: kg/ha
		}
		soyYield = soyYield+RandomHelper.nextDoubleFromTo(-10.0,10.0);
		//soyYield = 2010.0/(this.getFertilizerInput()/recommendedSoyPerHaFertilizerUse ) 
		//		+ RandomHelper.nextDoubleFromTo(-100.0,100.0);
		soyYield = soyYield*(cellsize*cellsize/10000.0);  //per cell size yield
	//	soyYield=2000;
		
		//corn yield is from this paper:
		//Feiliao Xiaoying Hanshu zai Peifang Shifei zhong de yingyong
		//Jilin Feiliao xiaoying hanshu 
		//by Zhang Kuan, Wang XiuFang, Wu Wei, Hu Heyun, Wang Xiaocun
		//1990
		//in chinese
	/*	double fertilizerPerMu = (this.getFertilizerInput()/(cellsize*cellsize))*666.67;
	//	System.out.println("fertilizer per mu: "+fertilizerPerMu);
		if (this.getSoc()>20.0)// high fertility
			{
			
			
			cornYield = 803.3+12.84* fertilizerPerMu
			            -0.22*fertilizerPerMu*fertilizerPerMu
			              ;  
			//the unit for this is kg/mu
			cornYield =( cornYield/667.0)*(cellsize*cellsize); //per cell size yield
			//this corn yield is a bit too high.
			
		} else {
			//medium fertility field
	//		double fertilizerPerMu = (this.getFertilizerInput()/(cellsize*cellsize))*666.67;
			cornYield = 518.6+31.34* fertilizerPerMu
		            -0.66*fertilizerPerMu*fertilizerPerMu
		             ;  
		//the unit for this is kg/mu
			cornYield = ( cornYield/667.0)*(cellsize*cellsize); //per cell size yield
		}
		
		//the corn yield here is too high..
		cornYield = 0.7*cornYield;*/
		double fertilizerPerHa = this.getFertilizerInput()*10000.0/(cellsize*cellsize);
		cornYield = -0.18518 *fertilizerPerHa*fertilizerPerHa+53.133*fertilizerPerHa+4538.0;
		cornYield =cornYield/0.86; //this is to get wet weight (14% dry) dry weight= wet weight*(1-0.14)
		cornYield =( cornYield/10000.0)*(cellsize*cellsize);
	//	System.out.println("in land cell, corn yield = "+cornYield);
		//rice yield is from this paper:
		//Effects of Nitrogen Application Rate and Planting Density on Grain Yields, 
		//Yield Components and Nitrogen Use Efficiencies of Rice
		//DENG Zhong-hua et al, 2015
		//in chinese
		// y= 577.79 + 34.87X1 + 419.04X2 - 0.07X1^2 -0.34X1X2, 
		//   X1 N fertilizer, X2 seed density,
		//use seed density = 22.1 , get new equation for N fertilizer only
		// y = 5926.41 + 27.356X-0.07X^2
		
		riceYield = 5926.41 + 27.356*(this.getFertilizerInput()/(cellsize*cellsize))*10000.0
				    -0.07*(this.getFertilizerInput()/(cellsize*cellsize))*10000.0*(this.getFertilizerInput()/(cellsize*cellsize))*10000.0;
	    //unit is kg/ha
		
		if(riceAge > 0){
			//this is from 
			//Effects of Chemical Fertilizer and Organic Manure on Rice Yield and Soil Fertility
			//ZHANG Guorong, et al, 2009 at
			//Scientia Agricultura Sinica
			//in Chinese
			riceYield = riceYield -100*riceAge;
		}
		
		
		riceYield = (riceYield/10000.0)*(cellsize*cellsize);//per cell size yield
		
		
		otherYield = (double) 1000.0*this.getFertilizerInput()/recommendedOtherPerHaFertilizerUse;
		otherYield = otherYield * (cellsize*cellsize/10000.0);
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
		                                 //    System.out.println("soy yield="+soyYield);
		                                   //  System.out.println("get fertilizer input = "+this.getFertilizerInput());
		                                   //  System.out.println(this.getFertilizerInput()/recommendedSoyUnitFertilizerUse);
		                                    }
		if(this.getLandUse()==LandUse.OTHERCROPS) setCropYield(otherYield);
		
		carbonProcess();
		//for this to work, crop yield has to be set there
	//	 System.out.println("current organic = "+  getLastyearSoc());
		organicSpace.setOrganicAt(getLastyearSoc(), this.getXlocation(), this.getYlocation());
		
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
		//    System.out.println(" fertilizer input = "+fertilizerInput);
			return fertilizerInput;
		}

//      public void setFertilizerInput() {
	// changed to following on April 26, 2018
		
		public void setFertilizerInput(LandUse landuse) {

			if(this.landUse==LandUse.SOY){
			//	fertilizerInput=recommendedSoyUnitFertilizerUse*(1+soyAge);
				fertilizerInput = observedSoyPerHaFertilizerUse * ((cellsize*cellsize)/10000.0);

				 //problem before is that crop age can be zero to start with, 
				 //so when multiplied it's zero for fertilizer input
				
			}
			if(this.landUse==LandUse.CORN){
			
				fertilizerInput = observedCornPerHaFertilizerUse*((cellsize*cellsize)/10000.0);
			
		//		 System.out.println("CORN fertilizer input = "+fertilizerInput);
	
			}
			if(this.landUse==LandUse.RICE){
			
				fertilizerInput = observedRicePerHaFertilizerUse*((cellsize*cellsize)/10000.0);
		
			}
			if(this.landUse==LandUse.OTHERCROPS){
			//	fertilizerInput=recommendedOtherUnitFertilizerUse*((cellsize*cellsize)/10000);
				fertilizerInput = observedOtherPerHaFertilizerUse*((cellsize*cellsize)/10000.0);
		//		fertilizerInput=recommendedOtherUnitFertilizerUse;
			}
			
			
			//simplied fertilizer usage, longer year, more fertilizer. 
			//should overwrite to  
			    this.fertilizerInput = fertilizerInput+RandomHelper.nextDoubleFromTo(-2.5,2.5);
			
		
		}
		
		
		public void setTempZone(double temp){
			this.tempZone=temp;
		
		}
		public double getTempZone(){
			return tempZone;
		}
		
		public void setPrecipitation( double precipitation){
			this.precipitation = precipitation;
		}
		
		public double getPrecipitation(){
			return precipitation;
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
		
		public double getLastyearSoc(){
			return lastyearSoc;
		}

		public void carbonProcess() {
			//based on Yuxin's paper, to get the soc change 
			//with feedback of fertilizer use
			//soc is short for soil organic carbon, the unit is g/kg
			//sci is short for soil carbon input, the unit is Mg/ha
			
			double sci = 0;
			double newsoc = 0;
			double strawbiomass = 0;
			
			if(this.getLandUse()==LandUse.SOY){
				strawbiomass = 1.0818*this.getCropYield()+269.6;
				
				
				sci= // this is straw, biomass*0.445
						 0.445*strawbiomass  //straw
						 +0.262*((strawbiomass/1.26)*0.33) //roots
						// +0.436*biomass //grain is removed
						 + (100.0/10000.0)*(cellsize*cellsize) //rhizodeposition, yuxin's paper
				         + (10.0/10000.0)*(cellsize*cellsize)  //fertilizer carbon, yuxin's paper
				         + (20.0/10000.0)*(cellsize*cellsize)  //seed, yuxin's paper
				         ;
				//use cellsize*cellsize/10000.0 to make sci adjustable to cellsize
				//but follow change it back to per hectare 
			    sci = (sci/(cellsize*cellsize))*10000.0;				 
				newsoc = 0.13*this.getTempZone()-0.04*precipitation-0.27*soc
						+ 0.03*(getFertilizerInput()/(cellsize*cellsize))*10000.0 
						+ 0.68*(sci/1000.0);
				soc = soc + newsoc;
				
			}
		
			else if(this.getLandUse()==LandUse.CORN) {
			
	         	strawbiomass = 
	         		        	0.8509*((this.getCropYield()/(cellsize*cellsize))*10000.0 )
	         			        +2974.1
	         			       ;   //kg/ha
	         	sci =  //   0.453*strawbiomass                  //straw 
	         			//remove all straw
	         			+ 0.448*((strawbiomass/1.24)*0.28)    //roots
	         			+ 650.0                              //rhizodeposition, yuxin's paper
	         			+ 60.0                                //fertilizer carbon, yuxin's paper
	         			+ 10.0                               //seed, yuxin's paper
	         			;
	         	newsoc = 0.13*tempZone - 0.04*precipitation -0.27*soc 
						  + 0.03*(getFertilizerInput()/(cellsize*cellsize))*10000.0   //corn fertilizer is per ha
						  + 0.68*(sci/1000.0) ;
	         	soc = soc + newsoc;
			//	System.out.println("crop yield "+this.getCropYield()+" fertilizerInput: "+getFertilizerInput());
	        // 	System.out.println("strawbiomass "+strawbiomass+"  sci = "+sci+
	        // 			             " //newsoc = "+newsoc+" soc="+soc);
	         	
			}   else if(this.getLandUse()==LandUse.RICE){
				//ref: change characteristics of rice yield and soil organic matter 
				//and nitrogen contents under various long-term fertilization regims
				//Huang Jing et al, . Chinese Journal of Applied Ecology, 
				//2013, 24(7): 1889-1894
				//rice soc increased from 21.0 g/kg to 28.1 g/kg from 1980 to 2010
				//yearly increase is 0.25 g/kg
				//if it's NPK fertilizer (non organic fertilizer)
				newsoc = 0.25;
				soc = soc + newsoc; 
				
			}    else soc = soc*0.95;
				
		//	double newsoc =0;
			/*if (this.getLandUse()==LandUse.SOY){
				if (this.getLastLandUse()==LandUse.CORN) 
					newsoc = soc-0.0096*soc; //9.6% for ten years
				if (this.getLastLandUse()==LandUse.SOY)
					newsoc = soc-0.0377*soc; //37.7% for ten years
				if (this.getLastLandUse() ==LandUse.RICE) 
					newsoc = soc+RandomHelper.nextDoubleFromTo(0.79,1.37);
				}
			if (this.getLandUse()==LandUse.CORN){
				if(this.getLastLandUse()==LandUse.CORN)
					newsoc = soc+soc*0.0127;
				if(this.getLastLandUse()==LandUse.SOY)
					newsoc = soc-0.0096*soc;
				if(this.getLastLandUse()==LandUse.RICE)
					newsoc = soc+RandomHelper.nextDoubleFromTo(0.79,1.37);
			}
			if (this.getLandUse()==LandUse.RICE){
				
				newsoc = soc+RandomHelper.nextDoubleFromTo(0.79,1.37);
			}*/
			
			
	//		System.out.println("land use  "+this.getLandUse()+"  new soc: "+newsoc
	//				           +" soc="+soc);
			this.lastyearSoc=soc;
		}
		
		public double getWaterRequirement() {
			return waterRequirement;
		}

		public void setWaterRequirement(LandUse landuse) {
			double waterR = 0.0;
			double fertilizer=(this.getFertilizerInput()/(cellsize*cellsize))*10000.0;
			
			if(landuse == LandUse.RICE)
				{//unit is m^3/per ha
				//or should it be the real water usage, which is from 7500-22500;
			//	  waterR = 8913.4;  //this is calculated water requirement
				  waterR = RandomHelper.nextDoubleFromTo(7500, 22500);
				}
			
			   
			if(landuse == LandUse.SOY)
			{
				
		    	waterR=268.27+0.1737*fertilizer
			                       -0.0007*fertilizer*fertilizer;
		    	//unit is mm/ha
		    	waterR = waterR*10.0;  //unit is m^3/ha
		   
			}
			if (landuse == LandUse.CORN){
				if(this.getPrecipitation()*10.0>500)
					waterR = 583.34+RandomHelper.nextDoubleFromTo(-14.6, 14.6);
				else if(this.getPrecipitation()*10.0>400)
					waterR = 505.1+RandomHelper.nextDoubleFromTo(-5.0, 5.0);
				else waterR = 457.53+RandomHelper.nextDoubleFromTo(-4.4, 4.4);
			//above unit is mm
			   waterR = waterR*10.0; //now unit is m^3/ha
		
			}
			
			waterR = (waterR/10000.0)*(cellsize*cellsize);
	//		System.out.println("water "+waterR);
			this.waterRequirement=waterR;
		}


}