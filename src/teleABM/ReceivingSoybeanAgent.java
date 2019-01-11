/**
 * 
 */
package teleABM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javolution.util.FastTable;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

/**
 * @author geododo
 *
 */
public class ReceivingSoybeanAgent extends SoybeanAgent{
	
	
	private int familyPopulation;
	//average is 3.65
	private double hhdHeadMale;
	private int age;
	//household head age, use average 45 (minus 10 to meet the 2005 time) for now
	private double dependentRatio;
	//average is 0.21
	private double genderRatio;
	

	//private boolean hhdHeadHealth;
	private double hhdHeadunHealth;
	//411 is healthy, 28 is unhealty
	private int occupation;
	//full time farmer, 2=part time farmer, 3=non time farmer
	
	private double unhealthProportion;
	
	boolean joinedCooperatives=false;
	protected double lastYearSoyPrice;
	protected double lastYearCornPrice;
	protected double lastYearRicePrice;
	protected double lastYearOtherPrice;
	protected double knowInternationalTrade;
	protected double whetherknow_soybean_ixYes;
	protected double whether_pericoupledperi;
	protected double whetherknow_transgeneYes;
	protected double whether_know_import_gmoYes;
	 
	protected double costConvertToRicePaddy=2000.0/(cellsizeReceiving*cellsizeReceiving);
	
	protected double totalFertilizerInput=0;
	
	protected double totalFuelInput=0;
	
	protected double totalWaterInput = 0;

	protected  FastTable<LandCell> planningSoyCells = new FastTable<LandCell>();
	protected  FastTable<LandCell> planningCornCells = new FastTable<LandCell>();
	protected FastTable<LandCell> planningRiceCells = new FastTable<LandCell>();
	protected  FastTable<LandCell> planningOtherCells = new FastTable<LandCell>();
	//these are to record all planning cells

	private double soySubsidy ;
	private double cornSubsidy ;
	private double riceSubsidy ;
	
	double cornProportion = 0;
	double riceProportion = 0;
	double soyProportion = 0;
	double lastYearCornProportion = 0.0; 
	double lastYearRiceProportion = 0.0;
	double lastYearSoyProportion = 0.0;
	
	double meanTemp=2.6;
	double meanSoila=22.02;
	double meanSoilb=57.14;
	double meanSoilc = 20.83;
//	double meanAge=48.48;
	double meanEducationYear=7.75;
//	double meanDependentRatio=0.198;
//	double meanGenderRatio=0.16;
//	double meanAllSchoolYear = 6.29;
	double meanAllSchoolYear ;
//	double meanNoOffFarmIncome = 0.45;
	int noOffFarmIncome ;
	int noBigMachine;
//	double meanSoyPriceDelta=-0.04;
//	double meanCornPriceDelta=-0.0006;
//	double meanRicePriceDelta=0.388;
	 protected Map<LandUse, ArrayList<Double>> prices = new HashMap<LandUse, ArrayList<Double>>();
	
	public ReceivingSoybeanAgent() {
		super();
	}
	
	public ReceivingSoybeanAgent(int id) {
		super(id);
		initializeReceiving();

	}
	
	public void initializeReceiving(){
		//special initialize for receiving systems;
        //dependent ratio, gender ratio are not normal distribution, 
        //so they have to be put in teleABMBuilder
		
		
	   double r = RandomHelper.nextDoubleFromTo(0.0, 1.0);
	    
// to initialize household head		
		if (r<=0.073) {
			//99/(99+1256)
			hhdHeadMale=0.0;		  
		} else hhdHeadMale=1.0;
//to initialize family population		
		//normal distribution, 1.15 is SD, 3.46 is mean value
		 Random ran = new Random();	
	 //   familyPopulation = (int) Math.round(r*1.15+3.46);
	    familyPopulation = (int) ( ran.nextGaussian()*1.15+3.46);
		//average is 3.46, almost look like normal distribution
	    
	    //to initialize hhdHead Age, normal distribution 
	 //   r = new Random(); //no need to create a random object every time
	   ran = new Random();
	  //  age = (int) Math.round(r*10.8+38.5);   
		age = (int) (ran.nextGaussian()*10.8+38.5);
	   //household head age, use average 38.5 (minus 10 to meet the 2005 time) for now
		//almost normal distribution
	     
	 //   r = new Random();
	 //   genderRadio = r.nextGaussian()*0.18+0.160; 
	    //mean is 0.1597 sd=0.18
		   ran = new Random();
	    meanAllSchoolYear = ran.nextGaussian()*1.805494+9.721;
	    // sd(regtest$average.all.schoolyear,na.rm = TRUE)
	    //in reg_proportion.R
	    
	    if(r<0.664) noOffFarmIncome = 0;
	    else {if(r<0.900) noOffFarmIncome = 1;  
	    else {if(r<0.98) noOffFarmIncome = 2;
	    else noOffFarmIncome = RandomHelper.nextIntFromTo(3, 5);
	    	} 
	    }
	    
	    
	/*    table(regtest$number.off.farm.income)
      in reg_proportion.R
	    0   1   2   3   4   5 
	  546 194  68   6   6   2 (total = 822)
	  
	  */
	    
	    if(r<0.165) noBigMachine = 0;
	    else {
	    	if(r<0.627) noBigMachine = RandomHelper.nextIntFromTo(1, 3);  
	    
	        else {if(r<0.776) noBigMachine = 4;
	              else { if(r<0.869)
	    	             noBigMachine = 5;
	    
	                      else {
	                         	if(r<0.956) 
	                         		noBigMachine = RandomHelper.nextIntFromTo(6, 7);
	    	                    else
	    	                    	noBigMachine = RandomHelper.nextIntFromTo(8, 12);
	    	                     } 
	                     }
	                  }
	    }
	    /*
	     table(regtest$numberofbigmachine)

  0   1   2   3   4   5   6   7   8   9  10  12 
136 126 130 124 122  76  46  26  26   6   2   2  (total =822)
	     */
	    
	//    r = new Random();
		if (r<0.716) hhdHeadunHealth = 0.0;
		else hhdHeadunHealth = 1.0;
		//970 is healthy, 384 is unhealthy
		
	//	 r = new Random();
	
			if (r<0.04) occupation = 3;
			else { if (r<0.18) occupation =2;
		           else occupation = 1;}
		//1116 full time farmer, 56 non farmer, 184 part-time farmer =1356 total	
			
	//	 r = new Random();
		//unhealthProportion = r.nextGaussian()*0.188+0.11;
		if(r<0.75) unhealthProportion = 0;
		else // unhealthProportion = 0.1106;
		        unhealthProportion = RandomHelper.nextDoubleFromTo(0.05615, 1);
		
		if (r < 0.540) 	knowInternationalTrade=1.0;
		else knowInternationalTrade=0.0;
		
		if (r<0.281)  whetherknow_soybean_ixYes=1.0;
		else whetherknow_soybean_ixYes=0.0;
		
		if (r<0.022) whether_pericoupledperi = 1.0;
		else whether_pericoupledperi=0.0;
		
		if (r<0.709) whetherknow_transgeneYes = 1.0;
		else whetherknow_transgeneYes=0.0;
		
		if (r<0.312) whether_know_import_gmoYes = 1.0;
		else whether_know_import_gmoYes=0.0;
		
		
		
		
		///environment cost
		//this.setFertilizerUnitCost(0.8);
		//n fertilizer price is 80 yuan/bag, which is 0.8yuan/kg
		//however, there are K, P fertilizer and other chemicals farmers need to buy,
		//according to data, corn per ha fertilizer cost is [1050-1875]
        //divided by fertilizer amount, the unit here should be 8 yuan/kg;
		this.setFertilizerUnitCost(8.0);
		
		//this.setFuelUnitCost(6.21);
		// 6.21 yuan/litre
		//same applies to fuel cost
		//
		this.setFuelUnitCost(6.2);
		
		
		this.setCornPerHaFuelInput(102.6);
		this.setSoyPerHaFuelInput(80.43);
		this.setRicePerHaFuelInput(163.6);
		this.setOtherPerHaFuelInput(30.0);				
		
	}
	
	@Override
    public void updateProduction(OrganicSpace organicSpace) {
    	 //here is to excute LUCC change,
    	//land use decision is only decision
    	   this.soyCells.clear();
		   this.cornCells.clear();
		   this.riceCells.clear();
		   this.otherCells.clear();
		   this.agriculturalCells.clear();
		   
		   this.riceProduction = 0;
		   this.cornProduction = 0;
		   this.soyProduction = 0;
		   this.otherProduction = 0;
		   //every year, set all production = 0;
		   
		   //clear last year's crop cells. 

	//    updateLabor();
	  // to start maybe not count labour;
	   
	//make decision first
	    //following are actual land use change;
	//    System.out.println("receiving tenure "+this.tenureCells.size());
	    
	   for(int i =0;i< this.tenureCells.size();i++) {
		    LandCell c =   this.getTenureCells().get(i);
		 //   updateLandUse(c);
		 //   tenureCells.get(i).
		//    landUseDecision();
	//	    System.out.println(c.getLandUse());
	//	    System.out.println(organicSpace.getLandUseAt(c.getXlocation(), c.getYlocation()));
		    c.transition();
		
		 //   if (c.getLastLandUse()==LandUse.RICE) {
		    if (c.getLandUse()==LandUse.RICE) { 	
		    	this.riceCells.add(c);
		    	this.agriculturalCells.add(c);
		    	riceProduction+=c.getRiceYield();
		//    	System.out.println(c.getCropYield());
		    	
		  //  	System.out.println("rice++");
		    } else if (c.getLandUse()==LandUse.CORN) {
		    	this.cornCells.add(c);
		    	cornProduction+=c.getCornYield();
		    	this.agriculturalCells.add(c);
		    	
		    } else if (c.getLandUse()==LandUse.SOY) {
		    	this.soyCells.add(c);
		    	soyProduction+=c.getSoyYield();
		    	this.agriculturalCells.add(c);
		    	
		//   	  System.out.println("soy yiled= "+c.getCropYield());
		    	
		    } else if(c.getLandUse()==LandUse.OTHERCROPS){
		   	    this.otherCells.add(c);
		   //	    System.out.println("other cells");
		    	otherProduction+=c.getOtherYield();
		    	
		    }
	   }
	   
	 //here it updates this year's production, so this.riceCells etc are newly counted
	//   System.out.println(riceProduction);
	   if (riceProduction>0) this.grownRice=true;
	   else this.grownRice=false;
	   if (cornProduction>0) this.grownCorn=true;
	   else this.grownCorn=false;
	   if (soyProduction>0) { this.grownSoy=true; this.grownSoyYears=grownSoyYears+1;
	//   System.out.println("has soy production "+grownSoyYears);
	
	   }
	   
	   if (otherProduction>0) this.grownOther=true;
	   
	   this.setGrownSoyYears(grownSoyYears);
		this.setSoyProduction(soyProduction);
		this.setCornProduction(cornProduction);
		this.setRiceProduction(riceProduction);
		this.setOtherProduction(otherProduction);
		
		lastYearCornProportion = (double) cornCells.size()/agriculturalCells.size();
		lastYearSoyProportion = (double) soyCells.size()/agriculturalCells.size();
		lastYearRiceProportion = (double) riceCells.size()/agriculturalCells.size();
		
    }
    
	@Override
    public void updateCost(OrganicSpace organicSpace){
    	//perHaFertilizerInput is the average fertilizer usage by the farmer. 
    			double totalCost = 0;
    			double totalfertilizeruse=0;
    			
    			if(soyCells.size()>0){
    			for (int i=0; i<soyCells.size();i++){
    				//setFertilizerInput() has already been put at land use decision ();
    				totalCost+=soyCells.get(i).getFertilizerInput()*fertilizerUnitCost;   				
    				totalfertilizeruse+= soyCells.get(i).getFertilizerInput();
    			//	System.out.println("cell no. "+i+" use fertilizer "+soyCells.get(i).getFertilizerInput());
    			}
    			soyPerHaFertilizerInput=totalfertilizeruse/soyCells.size();    					
    			soyPerHaFertilizerInput = soyPerHaFertilizerInput/(cellsizeReceiving*cellsizeReceiving)*10000.0;    			
    			setSoyPerHaFertilizerInput(soyPerHaFertilizerInput);
    			totalfertilizeruse = 0;
    			}
    			
    			if(cornCells.size()>0){
    				for (int i=0; i<cornCells.size();i++){
    					totalCost+=cornCells.get(i).getFertilizerInput()*fertilizerUnitCost;
    					totalfertilizeruse+=cornCells.get(i).getFertilizerInput();
    					}
    				cornPerHaFertilizerInput=totalfertilizeruse/cornCells.size();
    				cornPerHaFertilizerInput = cornPerHaFertilizerInput/(cellsizeReceiving*cellsizeReceiving)*10000.0;
    				setCornPerHaFertilizerInput(cornPerHaFertilizerInput);
    				totalfertilizeruse=0;
    				}
    			
    			if(riceCells.size()>0){
    				for (int i=0; i<riceCells.size();i++){
    					totalCost+=riceCells.get(i).getFertilizerInput()*fertilizerUnitCost;
    					totalfertilizeruse+=riceCells.get(i).getFertilizerInput();
    				}
    				ricePerHaFertilizerInput=totalfertilizeruse/riceCells.size();
    				ricePerHaFertilizerInput = ricePerHaFertilizerInput/(cellsizeReceiving*cellsizeReceiving)*10000.0;
    				setRicePerHaFertilizerInput(ricePerHaFertilizerInput);
    				totalfertilizeruse = 0;
    				}   			
    			if(otherCells.size()>0){
    				for (int i=0; i<otherCells.size();i++){
    					totalCost+=otherCells.get(i).getFertilizerInput()*fertilizerUnitCost;
    				    totalfertilizeruse+=otherCells.get(i).getFertilizerInput();
    				}
    				otherPerHaFertilizerInput=totalfertilizeruse/otherCells.size();
    				otherPerHaFertilizerInput = otherPerHaFertilizerInput/(cellsizeReceiving*cellsizeReceiving)*10000.0;
    				setOtherPerHaFertilizerInput(otherPerHaFertilizerInput);
    				}
    			
    }
	
	
	@Override
	public void landUseDecision(OrganicSpace organicSpace) {
		//this function is to make current year land use decisions
		
//		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		//tick has to be called everytime it is used. 	
		
		totalFuelInput = 0.0;
		totalFertilizerInput = 0.0;
		totalWaterInput = 0.0;
		
		planningSoyCells.clear();
		planningCornCells.clear();
		planningRiceCells.clear();
		planningOtherCells.clear();
		
//		int landUseNumber;
	//	 OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
//		 System.out.println("receiving land use decision: "+organicSpace.getTypeID());
		 soyPerHaFuelInput = this.getSoyPerHaFuelInput();
		 cornPerHaFuelInput = this.getCornPerHaFuelInput();
		 ricePerHaFuelInput = this.getRicePerHaFuelInput();
		 otherPerHaFuelInput = this.getOtherPerHaFuelInput();
		 fuelUnitCost = this.getFuelUnitCost();
		 
//		LandUse highestLandUse=LandUse.SOY;
/*		System.out.println("current soy price: "+cornPrices.get(2));
		System.out.println("last year soy price: "+cornPrices.get(1));
		System.out.println("two years ago soy price: "+cornPrices.get(0));	*/
	    
	//    int i=priceMemoryLimit -1; 

	//    System.out.println(soyPrices.get(0));
	    //this is to make sure the first three years run well.

	      
	    Collections.sort(this.tenureCells, new SortByRcount());
	//    Collections.sort(this.tenureCells, new SortbyRoll()); 
	  SimUtilities.shuffle(this.tenureCells, RandomHelper.getUniform());
	//  this.tenureCells.sort();
	//	  SimUtilities.shuffle(this.planningSoyCells, RandomHelper.getUniform());
		//   this.planningSoyCells.sort();
		   


		 int maxprob = this.maxLogisticProbility();
		 //the calculation of max prob is in another function
		 
			List<Integer> listToChange = new ArrayList<Integer>();
			List<Integer> listNotChange = new ArrayList<Integer>();
	
	//	this.grownSoyYears=0;
		if(maxprob==0 && this.grownSoyYears==0 )   //this is the type that never grow soybeans
		{ 
			// never grow soybean
			//if it is rice, then keep it rice, 
			//if it was corn, check if rice is way more profitable. convert to rice
			if((lastYearCornPerHaProfit + costConvertToRicePaddy) < lastYearRicePerHaProfit)
			{
				//if rice is super profitable; change 10% corn to rice;
		//		for(LandCell c: cornCells) {
				for(int j = 0; j<cornCells.size();j++) {
					System.out.println("why there is null pointer "+cornCells.size()+
							" "+
							cornCells.get(j).getXlocation() +" "
							+ cornCells.get(j).getLandUse());
					if(RandomHelper.nextDoubleFromTo(0.0, 1) < 0.5 ){
					
						if(cornCells.get(j).getNextToRice()||cornCells.get(j).getRCount()>0)
							
							//there was a null pointer here when number of agents increased to 2,000, 
							//but I don't know why
						  {
							System.out.println(cornCells.get(j).getNextToRice()+" how many "+cornCells.get(j).getRCount());
							planningRiceCells.add(cornCells.get(j));
							cornCells.remove(cornCells.get(j));
							capital-=costConvertToRicePaddy;
						  }
						else planningCornCells.add(cornCells.get(j));
						
					}
					else {
						planningCornCells.add(cornCells.get(j));
					}
				}
				
				for(LandCell c: riceCells) {
					planningRiceCells.add(c);
				}
			} 
			else	
			{
				for(LandCell c: riceCells) {
					planningRiceCells.add(c);
				}
				
				for(LandCell c: cornCells) {
					planningCornCells.add(c);
				}
			}
		//end of if(	max==0 && this.grownSoyYears==0 )

		}
		
		if(maxprob==0 && this.grownSoyYears>0 ) {
			//reduce soybean growing number
			//need to overwrite based on the regression;
			listToChange.clear();
			listNotChange.clear();
			
			 SimUtilities.shuffle(this.soyCells, RandomHelper.getUniform());
			 
			//go through soy cell list, change to either corn or rice
			for (int j =0; j<this.soyCells.size();j++) 
			 {						
					if(RandomHelper.nextDoubleFromTo(0, 1)>0.5 && 
							j < soyProportion*this.tenureCells.size() )
					{
				    	listToChange.add(j);						
					} else {
					    listNotChange.add(j);	
					}
			 
			 }	
			
			for(Integer i:listToChange)
			{
				if((lastYearCornPerHaProfit + costConvertToRicePaddy)<lastYearRicePerHaProfit 
						)
				{ if( soyCells.get(i).getNextToRice() ||soyCells.get(i).getRCount()>0)
					{
					 planningRiceCells.add(soyCells.get(i));
				     capital-=costConvertToRicePaddy;
				     }
				  else planningCornCells.add(soyCells.get(i));
				
				}
				else
					planningCornCells.add(soyCells.get(i));	
			}
			
			for(Integer i:listNotChange)
			{
				planningSoyCells.add(soyCells.get(i));
			}
			//add last year's corn and rice to planning cell list					
			for(LandCell c: cornCells)
			{
				planningCornCells.add(c);
			}
			for (LandCell c:riceCells)
			{
				planningRiceCells.add(c);
			}
																			
			}
			
		
		if(maxprob==1) {
			listToChange.clear();
			listNotChange.clear();
			//abandon soy
			for(LandCell c:soyCells)
			{
				//convert all soy cells to corn and rice
			  
				if((lastYearCornPerHaProfit+costConvertToRicePaddy) < lastYearRicePerHaProfit)
				{
					if(c.getNextToRice() ||c.getRCount()>0) 
					{
						planningRiceCells.add(c);
						capital-=costConvertToRicePaddy;
					}else 
						planningCornCells.add(c);
					
				}
				else 
					planningCornCells.add(c);	
			}
			//old corn and rice cells stay in planning
			for(LandCell c:cornCells){
				planningCornCells.add(c);
			}
			
			for(LandCell c:riceCells){
				planningRiceCells.add(c);
			}
			

		}
		
		if(maxprob==2){
			//continue grow soy, but may change proportion
	//		System.out.println("let's see here "+organicSpace.getTypeID());
	//		System.out.println("soy size="+this.soyCells.size()+" corn size="+this.cornCells.size());
			listToChange.clear();
			listNotChange.clear();
			
			double random = RandomHelper.nextDoubleFromTo(0.0, 0.5);
			for (int j=0; j<this.soyCells.size()*random;j++) 
			 {	
				listToChange.add(j);						
			 
			 }	
			for (int j=(int)Math.round(soyCells.size()*(1-random));j<soyCells.size();j++)
			{
				listNotChange.add(j);
			}
			
			for(Integer i:listToChange){
				if(2.0*lastYearCornPerHaProfit<lastYearRicePerHaProfit)
				{
					planningRiceCells.add(soyCells.get(i));
				    capital-=costConvertToRicePaddy;
				}
				else
					planningCornCells.add(soyCells.get(i));		
			}
			
			for(Integer i:listNotChange){
				planningSoyCells.add(soyCells.get(i));
			}
			
			for(LandCell c:cornCells){
				planningCornCells.add(c);
			}
			
			for(LandCell c:riceCells){
				planningRiceCells.add(c);
			}
	//		System.out.println("planning="+planningSoyCells.size()+" //corn="+planningCornCells.size());
		}
		
		if(maxprob==3){
			//new grower
			listToChange.clear();
			listNotChange.clear();
			
			double random = RandomHelper.nextDoubleFromTo(0.0, 0.5);
			for (int j=0; j<this.cornCells.size()*random;j++) 
			 {	
				listToChange.add(j);						
			 
			 }				
			for (int j=(int)Math.round(cornCells.size()*(1-random));j<cornCells.size();j++)
			{
				listNotChange.add(j);
			}
			
			for(Integer i:listToChange){
				planningSoyCells.add(cornCells.get(i));
			}
			
			for(Integer i:listNotChange){
				planningCornCells.add(cornCells.get(i));
			}
		    for(LandCell c:soyCells){
		    	planningSoyCells.add(c);
		    }
			
			for(LandCell c:riceCells){
				planningRiceCells.add(c);
			}
			
		
		}
		
		
		//update land use and environment-nexus:
		//fertilizer, fuel, and water use
		
	//	this.updateLandUse(organicSpace);
	
	}
	
	public void landUseDecisionBeta(OrganicSpace organicSpace){
		totalFuelInput = 0.0;
		totalFertilizerInput = 0.0;
		totalWaterInput = 0.0;
		
		planningSoyCells.clear();
		planningCornCells.clear();
		planningRiceCells.clear();
		planningOtherCells.clear();
		

		
		List<Integer> listToChange = new ArrayList<Integer>();
		List<Integer> listNotChange = new ArrayList<Integer>();
		
		double[] profit = new double[4];
	
		profit[0] = lastYearSoyPerHaProfit;
		profit[1] = lastYearCornPerHaProfit;
		profit[2] = lastYearRicePerHaProfit;
		profit[3] = lastYearOtherPerHaProfit;
		; 
		double highestProfit = profit[0];
		int count=0;
		for (int i = 0; i<4; i++){
		//	System.out.println(i+" = "+profit[i]);
			if (highestProfit<profit[i])
				{ highestProfit = profit[i];
		          count = i;
				}
	        }
	//    System.out.println("highest count="+count);
	//    count=0;
		if(count==0)  //highest profit is soybean 
		{
			for (int i=0; i <cornCells.size()*0.5;i++){
				listToChange.add(i);
			}
			for (int i= (int) Math.round(cornCells.size()*0.5);i<cornCells.size();i++){
				listNotChange.add(i);
			}
			
			  for(LandCell c:riceCells){
			    	planningRiceCells.add(c);
			    }
			  for( Integer i: listNotChange){
					planningCornCells.add(cornCells.get(i));
				}
			  for(Integer i:listToChange)
				   planningSoyCells.add(cornCells.get(i));
			  
			  for(LandCell c:soyCells)
				  planningSoyCells.add(c);
		}
		
		if (count == 1) {  //highest profit is corn 
			
			for (int i=0; i <soyCells.size()*0.8;i++){
				if(soyCells.get(i).getSoyAge()>=1)
				listToChange.add(i);
				else
					listNotChange.add(i);
			}
			for (int i= (int) Math.round(soyCells.size()*0.8);i<soyCells.size();i++){
				listNotChange.add(i);
			}
			
			
			for(LandCell c:riceCells) {
				planningRiceCells.add(c);
			}
			for (Integer i: listNotChange) {
				planningSoyCells.add(soyCells.get(i));
			}
			for (Integer i: listToChange) {
				planningCornCells.add(soyCells.get(i));				
			}
			for(LandCell c: cornCells) {
				planningCornCells.add(c);
			}
			
		}
		
		if (count ==2) {
			if (lastYearSoyPerHaProfit>lastYearCornPerHaProfit) {   //if soy makes more money than corn
				for (int i=0; i <cornCells.size()*0.5;i++){
					listToChange.add(i);          //change to rice
					
				}
				for (int i= (int) Math.round(cornCells.size()*0.5);i<cornCells.size();i++){
					listNotChange.add(i);          //may change to soybean?
				}
				for (Integer i: listToChange) {
					planningRiceCells.add(cornCells.get(i));
					capital-=costConvertToRicePaddy;
				}
				for (Integer i: listNotChange) {
					planningSoyCells.add(cornCells.get(i));
				}
				for (LandCell c:soyCells) {
					planningSoyCells.add(c);
				}
			
			
			} else {                                                  //if corn makes more money than soy
				for (int i=0; i <soyCells.size()*0.5;i++){   
					
					listToChange.add(i);    //change to rice
				}
				for (int i= (int) Math.round(soyCells.size()*0.5);i<soyCells.size();i++){
					listNotChange.add(i);           //may change to corn?
				}
				for (Integer i: listToChange) {
					planningRiceCells.add(soyCells.get(i));		
					capital-=costConvertToRicePaddy;
				}
				for (Integer i: listNotChange) {
					planningCornCells.add(soyCells.get(i));
				}
				
				for (LandCell c:cornCells) {
					planningCornCells.add(c);
				}
			}
			
			for(LandCell c:riceCells) {
				planningRiceCells.add(c);
			}
			
			
			
		}
		
		this.updateLandUse(organicSpace);
		
	
		
	
	}
	
	
	@Override
	public void updateProfit(){
	
		//	tick has to be called everytime it is used;
		double cPrice=0;
		 
		
//		lastYearSoyPerHaProfit=0;
//		lastYearCornPerHaProfit=0;
//		lastYearRicePerHaProfit=0;
//		lastYearOtherPerHaProfit=0;
		//current year price	
		profit=0;
		
//		System.out.println("update profit at soybean abstract: "
//		                +tick+": "+ this.getCommodityPrice(LandUse.SOY));
		
		//cPrice=this.getCommodityPrice(LandUse.SOY);
//		cPrice=  this.traderAgents.get().getCommodityPrice(LandUse.SOY);
//		System.out.println("WHAT S WRONG "+soySoldToTraderAgent);
		if(soySoldToTraderAgent != null)
		 {	
			cPrice = soySoldToTraderAgent.getCommodityPrice(LandUse.SOY)*2;	   
		 }
		 else
			{cPrice =0.5;
			 System.out.println("this worked? "+cPrice);
			}
		
		
		
	//	cPrice = 
	//	System.out.println("to check if price is signed  "+cPrice);
		
		soyPrices.add(cPrice);
		if (soyPrices.size()>priceMemoryLimit) {
			soyPrices.remove(0); //remove least recent price
		   
		}
		
	//	cPrice=this.getCommodityPrice(LandUse.CORN);
		cPrice = cornSoldToTraderAgent.getCommodityPrice(LandUse.CORN);
		cornPrices.add(cPrice);
		if (cornPrices.size()>priceMemoryLimit) {
			cornPrices.remove(0); //remove least recent price
		}
		
		cPrice = riceSoldToTraderAgent.getCommodityPrice(LandUse.RICE);
		ricePrices.add(cPrice);
		if (ricePrices.size()>priceMemoryLimit) {
			ricePrices.remove(0); //remove least recent price
		}
		
//		if (grownSoy && soyCells.size()>0) {
		if(soyProduction > 0 && soyCells.size()>0)	{
//			double soyYield=0;
//			for (int i=0; i < soyCells.size();i++) {
//				yield = soyCells.get(i).getCropYield();
//				soyYield+=yield;
			//	cPrice=this.soySoldToTraderAgent.getCommodityPrice(LandUse.SOY);
				
			cPrice = soySoldToTraderAgent.getCommodityPrice(LandUse.SOY)*2;

				
		//		System.out.println("update profit: "+tick+ " "+
			//	         cPrice+" yield "+yield);
				profit+=soyProduction*cPrice;		
				
				this.soySoldToTraderAgent.addSoyAmount(soyProduction);
				this.soySoldToTraderAgent.purchaseCommodity(soyProduction*cPrice);
				
	//			System.out.println("sold soy for "+soyProduction*cPrice+" yuan");
			
			soyPerHaYield = (double) soyProduction/soyCells.size();
	//		System.out.println(soyPerHaYield);
			soyPerHaYield = soyPerHaYield /(cellsizeReceiving*cellsizeReceiving)*10000.0;
			//has to convert this to from cell yield to per ha yield
			
			lastYearSoyPerHaProfit = soyPerHaYield*cPrice-getSoyPerHaFuelInput()*fuelUnitCost
					                 -soyPerHaFertilizerInput*fertilizerUnitCost 
					                 - 400.0 //seed cost
					                 + soySubsidy;
	//		System.out.println(soyPerHaYield+" soy price "+cPrice+
	//				" fuel  "+getSoyPerHaFuelInput()*fuelUnitCost);
		//	System.out.println("soy per ha yield="+soyPerHaYield+" per ha fuel input="+soyPerHaFuelInput+
		//			"soyPerHaFertilizerInput = "+soyPerHaFertilizerInput);
		//	System.out.println("capital = "+capital+" soy profit = "+lastYearSoyPerHaProfit);
			
		}
		
//		System.out.println("soy yield: "
	 //           +this.getCommodityPrice(LandUse.CORN));
		if (cornProduction>0) {
				
	//		cPrice = cornSoldToTraderAgent.getCommodityPrice(LandUse.CORN)*2;
			
			if(cornSoldToTraderAgent != null)
			 {	cPrice = cornSoldToTraderAgent.getCommodityPrice(LandUse.CORN)*2;
			   
			 }
			 else
				{cPrice =0.5;
				 System.out.println("this worked? C "+cPrice);
				}
			
				profit+=cornProduction*cPrice;
			
				this.cornSoldToTraderAgent.addCornAmount(cornProduction);
				this.cornSoldToTraderAgent.purchaseCommodity(cornProduction*cPrice);
				
			cornPerHaYield=(double) (cornProduction/cornCells.size());
			cornPerHaYield = (cornPerHaYield/(cellsizeReceiving*cellsizeReceiving))*10000.0;
			lastYearCornPerHaProfit = cornPerHaYield*cPrice-cornPerHaFuelInput*fuelUnitCost
	                -cornPerHaFertilizerInput*fertilizerUnitCost 
	                -785.0 //seed cost
	                + cornSubsidy
	                ;
		//	System.out.println("corn size="+cornCells.size()*900/10000.0+" corn per ha yield="+cornPerHaYield
		//			+" corn per ha fuel inpu=" +cornPerHaFuelInput+
		//			" corn fertilizer input "+cornPerHaFertilizerInput);
		//	System.out.println("capital = "+capital+" corn profit = "+ lastYearCornPerHaProfit);
		}
		if (riceProduction>0) {
			
			if(riceSoldToTraderAgent != null)
			 {	
				cPrice = riceSoldToTraderAgent.getCommodityPrice(LandUse.RICE)*2;			   
			 }
			 else
				{cPrice =0.5;
				 System.out.println("this worked? R "+cPrice);
				}
			
				profit+=riceProduction*cPrice;
				
				this.riceSoldToTraderAgent.addRiceAmount(riceProduction);
				this.riceSoldToTraderAgent.purchaseCommodity(riceProduction*cPrice);
				
			ricePerHaYield=(double) riceProduction/riceCells.size();
			ricePerHaYield=(ricePerHaYield/(cellsizeReceiving*cellsizeReceiving))*10000.0;
			lastYearRicePerHaProfit = ricePerHaYield*cPrice-ricePerHaFuelInput*fuelUnitCost
	                -ricePerHaFertilizerInput*fertilizerUnitCost 
	                -600.0  //seed cost
	                + riceSubsidy;
		//	System.out.println("capital = "+capital+"rice profit = "+ lastYearRicePerHaProfit);
		}
		if (grownOther) {
			
			if(otherSoldToTraderAgent != null)
			 {	
				cPrice = otherSoldToTraderAgent.getCommodityPrice(LandUse.OTHERCROPS)*2;			   
			 }
			 else
				{cPrice =1;
				 System.out.println("this worked? O "+cPrice);
				}
			
				otherPrices.add(cPrice);
				if (otherPrices.size()>priceMemoryLimit) {
					otherPrices.remove(0); //remove least recent price
				
				profit+=otherProduction*cPrice;
				
				this.otherSoldToTraderAgent.addOtherAmount(otherProduction);
				this.otherSoldToTraderAgent.purchaseCommodity(otherProduction*cPrice);
				}
			otherPerHaYield=otherProduction/otherCells.size();
			lastYearOtherPerHaProfit = otherPerHaYield*cPrice-otherPerHaFuelInput*fuelUnitCost
	                -otherPerHaFertilizerInput*fertilizerUnitCost;
		//	System.out.println("capital = "+capital+"other profit = "+lastYearOtherPerHaProfit);
		}
		
		capital+=profit;
	}
	
	
	
	public void decidingTradingPartner(){
		double highestPrice=0;
		double soyPrice=0;
		int soySoldToTraderAgentID=0;
		double cornPrice=0;
		int cornSoldToTraderAgentID=0;
		double ricePrice=0;
		int riceSoldToTraderAgentID=0;
		double otherPrice=0;
		int otherSoldToTraderAgentID=0;
		
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	//	System.out.println(tick+ " trader size: "+this.traderAgents.size());
	//	soyPrice=getCommodityPrice(LandUse.SOY);
	//	if(tick>0)
	//	soyPrice=lastYearSoyPrice;
	//	System.out.println("tick: "+tick+" "+soyPrice);
		
	//	if (grownSoy) {
			//check who has the highest price
		if(this.traderAgents.size()>0) 
		
		{
			for (int i=0; i<this.traderAgents.size();i++) {
				soyPrice=traderAgents.get(i).getCommodityPrice(LandUse.SOY);
		//		System.out.println("soy price "+soyPrice);
		//		System.out.println("highest price "+highestPrice);
				
				if (soyPrice>highestPrice) {
					highestPrice=soyPrice;
					soySoldToTraderAgentID = i;
				}				
			}			
			soySoldToTraderAgent = this.traderAgents.get(soySoldToTraderAgentID);
			lastYearSoyPrice = highestPrice;
		
	//		System.out.println("soy agent "+this.getID()+" trade with "+
	//	                     soySoldToTraderAgent.getID() +" at tick "+tick);
			
	//		System.out.println("big or small trader"+soySoldToTraderAgent.capital);
	//	}
		
	//	if (grownCorn) {
			highestPrice=0;
			for (int i=0;i<this.traderAgents.size();i++){
				cornPrice=traderAgents.get(i).getCommodityPrice(LandUse.CORN);
		//		cornPrice=getCommodityPrice(LandUse.CORN);
				if (cornPrice>highestPrice) {
					highestPrice=cornPrice;
					cornSoldToTraderAgentID = i;
				}				
			}			
			cornSoldToTraderAgent = this.traderAgents.get(cornSoldToTraderAgentID);
			lastYearCornPrice = highestPrice;
	//	}
		
	//	if (grownRice) {
			highestPrice=0;
			for (int i=0;i<this.traderAgents.size();i++){
				ricePrice=traderAgents.get(i).getCommodityPrice(LandUse.RICE);
	//			ricePrice=getCommodityPrice(LandUse.RICE);
	//			System.out.println("commodity type: "+traderAgents.get(i).getCommodityType());
	//			System.out.println("commodity price: "+traderAgents.get(i).getCommodityPrice(LandUse.RICE));
				if (ricePrice>highestPrice) {
					highestPrice=ricePrice;
					riceSoldToTraderAgentID = i;
				}				
			}			
			lastYearRicePrice = highestPrice;
			riceSoldToTraderAgent = this.traderAgents.get(riceSoldToTraderAgentID);
		//	System.out.println("sold to rice agent: "+riceSoldToTraderAgentID+" "+highestPrice);
		//}
	//	if (grownOther) {
			highestPrice=0;
			for (int i=0;i<this.traderAgents.size();i++){
				otherPrice=traderAgents.get(i).getCommodityPrice(LandUse.OTHERCROPS);
	//			otherPrice=getCommodityPrice(LandUse.OTHERCROPS);
				if (otherPrice>highestPrice) {
					highestPrice=ricePrice;
					otherSoldToTraderAgentID = i;
				}				
			}			
			lastYearOtherPrice = highestPrice;
			otherSoldToTraderAgent = this.traderAgents.get(otherSoldToTraderAgentID);
			
		}
	//	}
		
		else {
	          System.out.println("NO TRADE AGENTS");
	    	}
		
			
	}

	public double getDependentRatio() {
		return dependentRatio;
	}

	public void setDependentRatio(double dependentRatio) {
		this.dependentRatio = dependentRatio;
	}
	
	public double getGenderRatio() {
		return genderRatio;
	}

	public void setGenderRatio(double genderRatio) {
		this.genderRatio = genderRatio;
	}
	
	public double getTotalFertilizerInput() {
		return totalFertilizerInput;
	}

	public void setTotalFertilizerInput(double totalFertilizerInput) {
		this.totalFertilizerInput = totalFertilizerInput;
	}
	
	public double getTotalFuelInput() {
		return totalFuelInput;
	}

	public void setTotalFuelInput(double totalFuelInput) {
		this.totalFuelInput = totalFuelInput;
	}
	
	public double getTotalWaterInput() {
		return totalWaterInput;
	}

	public void setTotalWaterInput(double totalWaterInput) {
		this.totalWaterInput = totalWaterInput;
	}

	@Override
	public void updateLandUse(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
		
	//	 Collections.sort(this.tenureCells, new SortbyRoll());
		
		// boolean nextToRice = false;
		  
	//	agriculturalCells.clear();
	//	soyCells.clear();
	//	cornCells.clear();
	//	riceCells.clear();
	//	otherCells.clear();
		
		 for(LandCell c: this.tenureCells) {
		     
	        	for(GridCell<LandCell> cell: c.nghCell) 
	        	{
			    
	        		int landuseNumber = organicSpace.getLandUseAt(cell.getPoint().getX(), cell.getPoint().getY());
		        
			         if(landuseNumber == 3||landuseNumber ==1)//it's next to water or rice
			        	 
			           { 
				            c.setNextToRice();
				        
			            	break;
			        	}
			         
			         if(landuseNumber == 41) { //sun jing
			        	 c.setNextToRice();
			        	 break;
			         }
			        		
		
		          }
		 }
		 
			
		for(int j=0;j<planningSoyCells.size();j++){
			//		System.out.println("still grow soys "+organicSpace.getTypeID());
					LandCell c = planningSoyCells.get(j);
					c.setLastLandUse(c.getLandUse());
					c.setLandUse(LandUse.SOY);
			//		soyCells.add(c);
					organicSpace.setLandUse(2, c.getXlocation(), c.getYlocation());
			//		organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
					c.setFertilizerInput(LandUse.SOY);
					c.setFuelInput((soyPerHaFuelInput/10000.0)*(cellsizeReceiving*cellsizeReceiving));
					c.setWaterRequirement(LandUse.SOY);
				
					capital-= c.getFertilizerInput()*fertilizerUnitCost +
							  c.getFuelInput()*fuelUnitCost;
					 totalFertilizerInput+= c.getFertilizerInput();
					  totalFuelInput+= c.getFuelInput();
					  totalWaterInput+= c.getWaterRequirement();
				}
				
				for(int j=0;j<planningCornCells.size();j++){
//					System.out.println("change to corn");
					LandCell c = planningCornCells.get(j);
					c.setLastLandUse(c.getLandUse());
					c.setLandUse(LandUse.CORN);
			//		cornCells.add(c);
					organicSpace.setLandUse(6, c.getXlocation(), c.getYlocation());
					c.setFertilizerInput(LandUse.CORN);
					c.setFuelInput((cornPerHaFuelInput/10000.0)*(cellsizeReceiving*cellsizeReceiving));
					c.setWaterRequirement(LandUse.CORN);
					
					capital-= c.getFertilizerInput()*fertilizerUnitCost +
						      c.getFuelInput()*fuelUnitCost;
					 totalFertilizerInput+= c.getFertilizerInput();
					  totalFuelInput+= c.getFuelInput();
					  totalWaterInput+= c.getWaterRequirement();
					
				}
				
				for(int j=0;j<planningRiceCells.size();j++){
					LandCell c = planningRiceCells.get(j);
					c.setLastLandUse(c.getLandUse());
					c.setLandUse(LandUse.RICE);
			//		riceCells.add(c);
					organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
				//	organicSpace.setLandUse(6, c.getXlocation(), c.getYlocation());
					c.setFertilizerInput(LandUse.RICE);
					c.setFuelInput((ricePerHaFuelInput/10000.0)*(cellsizeReceiving*cellsizeReceiving));
					c.setWaterRequirement(LandUse.RICE);
					
					capital-= c.getFertilizerInput()*fertilizerUnitCost +
							  c.getFuelInput()*fuelUnitCost;
					 totalFertilizerInput+= c.getFertilizerInput();
					  totalFuelInput+= c.getFuelInput();
					  totalWaterInput+= c.getWaterRequirement();
					
				}
				
				
			for(int j=0;j<planningOtherCells.size();j++){
				LandCell c = planningOtherCells.get(j);
				c.setLastLandUse(c.getLandUse());
				c.setLandUse(LandUse.OTHERCROPS);
			//	this.otherCells.add(c);
				organicSpace.setLandUse(4, c.getXlocation(), c.getYlocation());
	//		System.out.println("there is other cell");
			}
			
			setTotalFertilizerInput(totalFertilizerInput);
			setTotalFuelInput(totalFuelInput);
			setTotalWaterInput(totalWaterInput);
			
		
			
	
	}

	public double getSoySubsidy() {
		return soySubsidy;
	}

	public void setSoySubsidy(double soySubsidy) {
		this.soySubsidy = soySubsidy;
	}

	public double getCornSubsidy() {
		return cornSubsidy;
	}

	public void setCornSubsidy(double cornSubsidy) {
		this.cornSubsidy = cornSubsidy;
	}

	public double getRiceSubsidy() {
		return riceSubsidy;
	}

	public void setRiceSubsidy(double riceSubsidy) {
		this.riceSubsidy = riceSubsidy;
	}

	@Override
	public void landUseDecisionLogisticRegression(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
		
	}
	
	public double cropProportion(){
		
	//	double meanAllSchoolYear = 6.29;
	//	double meanNoOffFarmIncome = 0.45;
	//	double meanNoBigMachine = 1.02;
		
	
		
		planningSoyCells.clear();
		planningCornCells.clear();
		planningRiceCells.clear();
		planningOtherCells.clear();
	
		double dryarea = 0;  //because in regression, it's in hectare, 
		//here we have to convert the unit from number of cells to hectares.
		double irrigatedarea=0;
		
		if(this.tenureCells.size()*this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/
				10000.0 > 10.0
				&&
				this.tenureCells.size()*this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/
				10000.0 <100) 
	    	{ 
			   dryarea = 	(this.tenureCells.size()-this.getRiceCellSize())*
			                (this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/10000.0)
			                /10.0;
		       irrigatedarea = 	this.getRiceCellSize()*
                (this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/10000.0)
                /10.0;
		    }
		else if(this.tenureCells.size()*this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/
				10000.0 >= 100.0
				&&
				this.tenureCells.size()*this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/
				10000.0 <1000.0) 
			{
		     	dryarea = 	(this.tenureCells.size()-this.getRiceCellSize())*	
                            (this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/10000.0)
                            /100.0;
		     	irrigatedarea = 	this.getRiceCellSize()*
		                (this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/10000.0)
		                /100.0;
			}
		else	if(this.tenureCells.size()*this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/
				10000.0 >= 1000.0
				&&
				this.tenureCells.size()*this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/
				10000.0 <10000.0) 
			{
			  dryarea = 	(this.tenureCells.size()-this.getRiceCellSize())*
                             (this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/10000.0)
                            /1000.0;
			  irrigatedarea = 	this.getRiceCellSize()*
		                (this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/10000.0)
		                /1000.0;
			}
		else {
			dryarea = (this.tenureCells.size()-this.getRiceCellSize())*
                (this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/10000.0);
			irrigatedarea = 	this.getRiceCellSize()*
	                (this.tenureCells.get(0).getCellSize()*this.tenureCells.get(0).getCellSize()/10000.0);
		
		}
		
		//at this moment, updateProduction recounts all three; 
		int maxProb = maxLogisticProbility();
	    //[1] abandon soybean
		//[2] continue grow
		//[3] new grower		
		//[0] never grow soybean
	
			
       if(grownRice) {
    	/*   summary(riceregtest$proportion.Rice)
    	    Min.  1st Qu.   Median     Mean  3rd Qu.     Max. 
    	0.004376 0.200000 0.500000 0.555600 1.000000 1.000000 */
  //if(dryarea==0 && maxProb ==0)
    if(dryarea==0 && maxProb==0)
    	   //before adding "||maxProb ==0", the full rice farmers proportion is too small
    		//however, after adding it, it grows too easily;
    		//the proper pattern shows up at early simulation stage (tick==1)
    		{ 
    		  riceProportion = 1.0;
    		  soyProportion = 0.0;
    		  cornProportion = 0.0;
    		}
    	else
    	{ riceProportion = 
    			   0.966394 +  //constant
   				-0.002925*age +  //age
   				0.00318 *(irrigatedarea+dryarea)*(agriculturalCells.size()/tenureCells.size()) +   			
   		      -0.050012*familyPopulation + //No.family member
   		         0.070912*dependentRatio + //dependent ratio (kids and elders/number of all family
   		                                   //members)
   		         -0.121038*genderRatio +
   		         -0.00745*meanAllSchoolYear +
   		         0.052838*noOffFarmIncome +//mean average all school year
   		        -0.008513*noBigMachine + 
   		      -0.496737 *  unhealthProportion +
   		      -0.219897* whetherknow_soybean_ixYes+
   		      0.009434 *irrigatedarea*(agriculturalCells.size()/tenureCells.size())+
   		      0.00248 * dryarea *(agriculturalCells.size()/tenureCells.size())+
   		      0.007539 *riceSoldToTraderAgent.getCommodityPrice(LandUse.RICE) 		      
   		        ;
    	   
    //	   summary(riceregtest$proportion.Soybean)
    //	   Min. 1st Qu.  Median    Mean 3rd Qu.    Max. 
    //	 0.0000  0.0000  0.0000  0.1288  0.1667  0.9000 
    		   soyProportion = RandomHelper.nextDoubleFromTo(0,0.1667) ; 
    	   
               if(riceProportion+soyProportion<1)
    		   cornProportion = 1-riceProportion-soyProportion;
               else 
            	   cornProportion = 0.0;
           }
       }
       else {
				
		 /* soyProportion = 
				  0.2435298 +  //constant
   				(-0.0006918)*age +  //age
   				0.0008699 *(irrigatedarea+dryarea)*(agriculturalCells.size()/tenureCells.size())+   			
   				-0.0114311*familyPopulation + //husband age
   		        0.0260328*dependentRatio + //husband education
   		        -0.0170938*genderRatio +
   		         0.0179353*meanAllSchoolYear +
   		        0.0051761*meanNoOffFarmIncome +//mean average all school year
   		       0.0069395*meanNoBigMachine + 
   		   -0.1763695 *  unhealthProportion +
   		      0.0939421* whetherknow_soybean_ixYes+
   		      -0.0044082*irrigatedarea *(agriculturalCells.size()/tenureCells.size())+
   		   -0.0040474* dryarea*(agriculturalCells.size()/tenureCells.size()) +
   		     -0.0617767 *soySoldToTraderAgent.getCommodityPrice(LandUse.SOY) 
   		      +
   		      0.1297759*cornSoldToTraderAgent.getCommodityPrice(LandUse.CORN)
   		      +
   		      -0.24*getPriceDelta(LandUse.SOY)
   		      +0.26*getPriceDelta(LandUse.CORN)
   		   ;*/
    	   soyProportion = 
 				  0.0323736 +  //constant
    				0.0002698*age +  //age
    				0.0012274 *(irrigatedarea+dryarea)*(agriculturalCells.size()/tenureCells.size())+   			
    				-0.0193608*familyPopulation + //husband age
    		        0.0711046*dependentRatio + //husband education
    		        -0.0293848*genderRatio +
    		         0.0197117*meanAllSchoolYear +
    		        0.00046*noOffFarmIncome +//mean average all school year
    		       -0.002741*noBigMachine + 
    		   -0.1505203 *  unhealthProportion +
    		      0.1125172* whetherknow_soybean_ixYes+
    		      0.0005269 *irrigatedarea *(agriculturalCells.size()/tenureCells.size())+
    		      -0.0045468* dryarea*(agriculturalCells.size()/tenureCells.size()) +
    		     0.0491402 *soySoldToTraderAgent.getCommodityPrice(LandUse.SOY) 
    		      //+
    		  //    0.174*cornSoldToTraderAgent.getCommodityPrice(LandUse.CORN)
    		   ;
		  
		  riceProportion = 0.002033+0.079428*riceSoldToTraderAgent.getCommodityPrice(LandUse.RICE);		
	//	cornProportion = 0.352334 + 0.137903*cornSoldToTraderAgent.getCommodityPrice(LandUse.CORN);
        cornProportion = 1-soyProportion-riceProportion; 
     //   riceProportion = 0.0;
        
       }
       
    int		tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount(); 
   
    if(tick==1) {
    		if(grownRice) {
    	   // 	riceProportion = lastYearRiceProportion*1.55;    	
    		//	riceProportion = riceProportion *2;
    		//	soyProportion = soyProportion*1.1;
    		//	cornProportion = 1 - riceProportion - soyProportion; 
    			riceProportion =1.0;
    			soyProportion = 0.0;
    			cornProportion = 0.0;
    			
    		}
    		else {    		
    			cornProportion = cornProportion*0.4;
    			soyProportion = soyProportion *1.3;
    			riceProportion = riceProportion *2; 
    	//		soyProportion = 0.9;
    	//		cornProportion=0.1;
    			}
    		
    	//	riceProportion = 1.0;
    	//			cornProportion = 0;
    	//	soyProportion = 0.0;
    //		System.out.println("tick=1: "+riceProportion+" "+soyProportion + " corn "+ cornProportion);
    	}
    
  //  if(tick==3) {
  //  	soyProportion = 0.5* soyProportion;
   // 	cornProportion = 1.15 * cornProportion;
   // }
  //     System.out.println(tick);
   //    System.out.println("soy "+ soyProportion +" //  "+ lastYearSoyProportion);
    //   System.out.println("corn "+ cornProportion +" // "+ lastYearCornProportion);
     //  System.out.println("rice "+ riceProportion +" // "+ lastYearRiceProportion);
       
     /*  if(Math.abs((riceProportion - lastYearRiceProportion)/lastYearRiceProportion) > 0.3)		
			riceProportion = lastYearRiceProportion *RandomHelper.nextDoubleFromTo(0.7, 1.3);      
      
       if(Math.abs((cornProportion - lastYearCornProportion)/lastYearCornProportion) > 0.3)
			cornProportion = cornProportion *RandomHelper.nextDoubleFromTo(0.5, 0.8);*/
       
   //    if(getPriceDelta(LandUse.SOY) < 0 || getPriceDelta(LandUse.CORN)>0.05)
   // 	   soyProportion = soyProportion * RandomHelper.nextDoubleFromTo(0.3,0.8);
   
    if(tick>=4 && getPriceDelta(LandUse.SOY)<0) {
    	
    	double rand = RandomHelper.nextDoubleFromTo(0.7, 0.9);
		if(soyProportion*rand> lastYearSoyProportion && lastYearSoyProportion >0)
		{ 
		//	System.out.println(tick);
		//	System.out.println("soy price goes down "+soyProportion+ " "+lastYearSoyProportion);
			soyProportion = lastYearSoyProportion;
	    	
		}
		else
			soyProportion = soyProportion *rand;
		
		cornProportion = cornProportion * RandomHelper.nextDoubleFromTo(1.2, 1.5);
		riceProportion = 1-soyProportion - cornProportion;
	}	//this is to adjust the downgoing soybean price;
    
    if(tick>=6 || getPriceDelta(LandUse.CORN)>0.1){
    	//this is to control that corn proportion doesn't drop too much 
    	//because of soy price increase
    	
    	double rand = RandomHelper.nextDoubleFromTo(1.0, 1+getPriceDelta(LandUse.CORN));
		if(cornProportion*rand> lastYearCornProportion && lastYearCornProportion >0)
		{ 
		//	System.out.println(tick);
		//	System.out.println("soy price goes down "+soyProportion+ " "+lastYearSoyProportion);
			cornProportion = cornProportion *rand;	    	
		}
		else
			cornProportion = lastYearCornProportion;
		
		soyProportion = soyProportion * RandomHelper.nextDoubleFromTo(0.7,0.95);
		riceProportion = 1-soyProportion - cornProportion;
    }
    	
    if(tick<3 ) {
    	double rand = RandomHelper.nextDoubleFromTo(0.7, 0.9);
		if(cornProportion *  rand> lastYearCornProportion &&lastYearCornProportion >0)
		{ 
		//	System.out.println(tick);
		//	System.out.println("soy price goes down "+soyProportion+ " "+lastYearSoyProportion);
			cornProportion = lastYearCornProportion;
	    	
		}
		else
			cornProportion = cornProportion *rand;
		
		soyProportion = soyProportion * RandomHelper.nextDoubleFromTo(1.05, 1.1);
	//	riceProportion = 1-soyProportion - cornProportion;	
    
    }	
    
  //  if(tick>=5 && getPriceDelta(LandUse.CORN)>0)
  //  	cornProportion = lastYearCornProportion; 
    
    
		if(riceProportion<0||riceProportion>1) 
	//		riceProportion = irrigatedarea/(irrigatedarea+dryarea); 
	    	riceProportion = 0.002033+0.079428*riceSoldToTraderAgent.getCommodityPrice(LandUse.RICE);	
		//riceProportion can be negative?!!
		if(soyProportion<0||soyProportion>1) 
		//	cornProportion = RandomHelper.nextDoubleFromTo(0, dryarea/(irrigatedarea+dryarea)); 
			soyProportion = RandomHelper.nextDoubleFromTo(0, 1-riceProportion); 
		//cornProportion can be negative?!!		
		if (soyProportion+riceProportion <= 1)
	    	cornProportion = 1.0 - soyProportion - riceProportion;
		else 
		//	cornProportion = RandomHelper.nextDoubleFromTo(0, dryarea/(irrigatedarea+dryarea)); 
		cornProportion = 0.352334 + 0.137903*cornSoldToTraderAgent.getCommodityPrice(LandUse.CORN);
		
	
		
/*		if(Math.abs((cornProportion-lastYearCornProportion)/lastYearCornProportion)>0.15){
		//	System.out.println(lastYearCornProportion+" too much changes: "+
		//			cornProportion);
			if(lastYearCornProportion>0.0 && cornProportion>lastYearCornProportion)
			cornProportion = lastYearCornProportion*1.15;
			else 
				cornProportion = lastYearCornProportion*0.85;
			//riceProportion = lastYearRiceProportion*0.95;
			//soyProportion = lastYearSoyProportion*0.85;
			soyProportion = 1- cornProportion;
		//	System.out.println(cornProportion +" last year "+ lastYearCornProportion);
		} */
/*		tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	//	if(tick <= 2)	
		if((riceProportion-lastYearRiceProportion)/lastYearRiceProportion>0.1){
			System.out.println(lastYearRiceProportion+" rice too much changes: "+
					riceProportion);
			riceProportion = (lastYearRiceProportion+0.02)*1.15;
			cornProportion = lastYearCornProportion*0.65;
			soyProportion = lastYearSoyProportion*0.55;
		//	System.out.println(cornProportion +" last year "+ lastYearCornProportion);
		}*/

	//	if((soyProportion - lastYearSoyProportion)/lastYearSoyProportion >= 0.1){
	//		 cornProportion = cornProportion + soyProportion *0.15;
	//		soyProportion = soyProportion * 0.85;		   
			//riceProportion = riceProportion;
	//	}


		
		setCornProportion(cornProportion);
		setSoyProportion (soyProportion);
		
//		System.out.println("corn proportion "+cornProportion);
//		System.out.println("soy proportion "+soyProportion);
//		System.out.println("rice proportion "+riceProportion+" :\\");
		return riceProportion;
	}
	
	
	
	public int maxLogisticProbility() {
		
		double[] exp= new double[4];
		double[] probability = new double[4];
		//[1] abandon soybean
		//[2] continue grow
		//[3] new grower		
		//[0] never grow soybean
		//to use mean to start with
	
		
		double[] coef_1= new double[20];
		double[] coef_2= new double[20];
		double[] coef_3= new double[20];
		
		
		
	    coef_1[0]=8.55; coef_1[1]=0.976;coef_1[2]=-0.193;coef_1[3]=-0.125;
	    coef_1[4]=-0.137; coef_1[5]=0.009;coef_1[6]=-0.033;coef_1[7]=-0.053;
	    coef_1[8]=-0.206; coef_1[9]=0.099;coef_1[10]=0.192;coef_1[11]=0.390;
	    coef_1[12]=-0.012; coef_1[13]=0.757;coef_1[14]=-0.264;coef_1[15]=-0.115;
	    coef_1[16]=0.564; coef_1[17]=-1.324;coef_1[18]=-0.041;coef_1[19]=1.999;
	    
	    
	    
	    coef_2[0]=50.76; coef_2[1]=1.994;coef_2[2]=-0.708;coef_2[3]=-0.518;
	    coef_2[4]=-0.658; coef_2[5]=0.009;coef_2[6]=-0.007;coef_2[7]=-0.451;
	    coef_2[8]=-0.138; coef_2[9]=-0.022;coef_2[10]=-0.019;coef_2[11]=0.225;
	    coef_2[12]=0.289; coef_2[13]=0.455;coef_2[14]=-0.606;coef_2[15]=0.441;
	    coef_2[16]=0.079; coef_2[17]=-1.835;coef_2[18]=-1.273;coef_2[19]=3.554;
	    
	    
	    coef_3[0]=-27.58; coef_3[1]=1.36;coef_3[2]=-0.011;coef_3[3]=0.275;
	    coef_3[4]=0.139; coef_3[5]=-0.047;coef_3[6]=-0.127;coef_3[7]=2.277;
	    coef_3[8]=4.323; coef_3[9]=0.129;coef_3[10]=-0.089;coef_3[11]=0.211;
	    coef_3[12]=0.520; coef_3[13]=0.402;coef_3[14]=0.943;coef_3[15]=1.224;
	    coef_3[16]=-1.237; coef_3[17]=1.826;coef_3[18]=-1.428;coef_3[19]=4.37;
	    
	//    int i=priceMemoryLimit -1; 
	    double soyPriceDelta;
	    double cornPriceDelta;
	    double ricePriceDelta;
	    
	    soyPriceDelta = getPriceDelta(LandUse.SOY);
	    cornPriceDelta = getPriceDelta(LandUse.CORN);
	    ricePriceDelta = getPriceDelta(LandUse.RICE);
		   
		exp[1] = // 1.05*age +
				         // 8.80*familyPopulation +
				         coef_1[0] +  //constant
				         coef_1[1]*meanTemp +  //temp
				         coef_1[2]*meanSoila + //soil.002
				         coef_1[3]*meanSoilb + //soil.002.02
				         coef_1[4]*meanSoilc + //soil.2
				         coef_1[5]*age + //husband age
				         coef_1[6]*meanEducationYear + //husband education
				         coef_1[7]*dependentRatio +
				         coef_1[8]*genderRatio +
				         coef_1[9]*meanAllSchoolYear +//mean average all school year
				         coef_1[10]*noOffFarmIncome + 
				         coef_1[11] *  noBigMachine +
				  //        (-0.32)*unhealthProportion+
				         coef_1[12]*knowInternationalTrade+ 
				         coef_1[13]*whetherknow_soybean_ixYes+
				         coef_1[14]*whether_pericoupledperi+
				         coef_1[15]*whetherknow_transgeneYes+
				         coef_1[16]*whether_know_import_gmoYes+
				      //    0.05*knowInternationalTrade +
				         coef_1[17]*
				     //     (soyPrices.get(2).doubleValue()-
				      //     soyPrices.get(1).doubleValue()) +
				         soyPriceDelta+
				          coef_1[18]*
				     //     (cornPrices.get(2).doubleValue()-
				     //      cornPrices.get(1).doubleValue())+
				          cornPriceDelta+
				          coef_1[19] *
				     //     (ricePrices.get(2).doubleValue()-
					//	  ricePrices.get(1).doubleValue())
				          ricePriceDelta
				          ;
				          
		exp[2] = // 1.05*age +
		         // 8.80*familyPopulation +
		         coef_2[0] +  //constant
		         coef_2[1]*meanTemp +  //temp
		         coef_2[2]*meanSoila + //soil.002
		         coef_2[3]*meanSoilb + //soil.002.02
		         coef_2[4]*meanSoilc + //soil.2
		         coef_2[5]*age + //husband age
		         coef_2[6]*meanEducationYear + //husband education
		         coef_2[7]*dependentRatio +
		         coef_2[8]*genderRatio +
		         coef_2[9]*meanAllSchoolYear +//mean average all school year
		         coef_2[10]*noOffFarmIncome + 
		         coef_2[11] *  noBigMachine +
		  //        (-0.32)*unhealthProportion+
		         coef_2[12]*knowInternationalTrade+ 
		         coef_2[13]*whetherknow_soybean_ixYes+
		         coef_2[14]*whether_pericoupledperi+
		         coef_2[15]*whetherknow_transgeneYes+
		         coef_2[16]*whether_know_import_gmoYes+
		      //    0.05*knowInternationalTrade +
		         coef_2[17]*
		        soyPriceDelta+
		          coef_2[18]*
		        cornPriceDelta+
		          coef_2[19] *
		          ricePriceDelta
		          ;
	
		exp[3] = // 1.05*age +
		         // 8.80*familyPopulation +
		         coef_3[0] +  //constant
		         coef_3[1]*meanTemp +  //temp
		         coef_3[2]*meanSoila + //soil.002
		         coef_3[3]*meanSoilb + //soil.002.02
		         coef_3[4]*meanSoilc + //soil.2
		         coef_3[5]*age + //husband age
		         coef_3[6]*meanEducationYear + //husband education
		         coef_3[7]*dependentRatio +
		         coef_3[8]*genderRatio +
		         coef_3[9]*meanAllSchoolYear +//mean average all school year
		         coef_3[10]*noOffFarmIncome + 
		         coef_3[11] *  noBigMachine +
		  //        (-0.32)*unhealthProportion+
		         coef_3[12]*knowInternationalTrade+ 
		         coef_3[13]*whetherknow_soybean_ixYes+
		         coef_3[14]*whether_pericoupledperi+
		         coef_3[15]*whetherknow_transgeneYes+
		         coef_3[16]*whether_know_import_gmoYes+
		      //    0.05*knowInternationalTrade +
		         coef_2[17]*
			        soyPriceDelta+
			          coef_2[18]*
			        cornPriceDelta+
			          coef_2[19] *
			          ricePriceDelta
		          ;
/*		System.out.println("exp 1 = "+exp[1]);
		System.out.println("exp 2 = "+exp[2]);
		System.out.println("exp 3 = "+exp[3]);
		System.out.println("   ");*/
		double expSum=Math.exp(exp[1])+Math.exp(exp[2])+Math.exp(exp[3])+1;
		probability[0]=1/expSum;
		probability[1]=Math.exp(exp[1])/expSum;
		probability[2]=Math.exp(exp[2])/expSum;
		probability[3]=Math.exp(exp[3])/expSum;
		
	//	probability[0]=1-probability[1]-probability[2]-probability[3];
	//	System.out.println("prob 1 = "+probability[1]);
	//	System.out.println("prob 2 = "+probability[2]);
	//	System.out.println("prob 3 = "+probability[3]);
	//	System.out.println("prob 0 = "+probability[0]);
	//	System.out.println(" end ");
		
		//[1] abandon soybean
		//[2] continue grow
		//[3] new grower		
		//[0] never grow soybean
		int max=2;
		double maxProb=probability[0];
		
		
//to calculate the max probability
		   if(probability[1]>maxProb){
			   maxProb=probability[1];
			   max=1;
		   } else if(probability[2]>maxProb)
		         {
			       maxProb=probability[2];
			       max=2;
		          } else if(probability[3]>maxProb)
		             {  maxProb=probability[3];
		                max=3;} else{
		                	maxProb=probability[0];
		                	max=0;
		                }
		   return max;
	}
	
	
	public void landUseDecisionCelluar(OrganicSpace organicSpace) {
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		planningSoyCells.clear();
		planningCornCells.clear();
		planningRiceCells.clear();
		planningOtherCells.clear();
	
		
		//at this moment, updateProduction recounts all three; 
		int maxProb = maxLogisticProbility();
	    //[1] abandon soybean
		//[2] continue grow
		//[3] new grower		
		//[0] never grow soybean
	
		
		//celluar automatic algorithm
         double[] profit = new double[3];
		 int highest = 1;
		profit[0] = lastYearSoyPerHaProfit;
		profit[1] = lastYearCornPerHaProfit;
		profit[2] = lastYearRicePerHaProfit;
	//	for(int i =0;i<=2;i++)
	//		System.out.println(i+" "+profit[i]);
	//	profit[3] = lastYearOtherPerHaProfit; 
		
		if(profit[0]>=profit[1]) {
		   
			if (profit[0]>=profit[2]) 
				highest = 0;
			else highest =2;
				 
			} else {//0<1
				if(profit[1]>=profit[2])
					highest=1;
				else highest =2;
			}
	
		
	//	if(highest==0)
		 double riceProportion = cropProportion();
//		 if(grownRice && tick<=3) { riceProportion = 0.0; soyProportion=1.0;}
	//	System.out.println(tick+ " max here =" + highest);
//		if(highest==1) System.out.println("corn is highest");
	
	//	  Collections.sort(this.tenureCells, new SortByRcount());
		SimUtilities.shuffle(this.tenureCells, RandomHelper.getUniform());
		SimUtilities.shuffle(this.agriculturalCells, RandomHelper.getUniform());
		
		
	//	 System.out.println("rice proportion " + riceProportion+"// last year "+lastYearRiceProportion);
				 
		 List<Integer> listToChange = new ArrayList<Integer>();
			List<Integer> listNotChange = new ArrayList<Integer>();

		 int count = 0;
	//	 highest = 0;
	//	 if(grownRice) riceProportion = 0.9;
	//	 else riceProportion=0.1;
			
		 
	//	 soyProportion = 1-riceProportion*0.8;
	//	 cornProportion = 1-riceProportion-soyProportion;
	//	 riceProportion =0.3;
		 Random rand = new Random();
		 double expansion = rand.nextGaussian()*0.01+0.1;
	//	 double expansion= RandomHelper.nextDoubleFromTo(-0.02, 0.03);
		 int numberOfCells;
		 //this is to simulate expansion;
		 if( agriculturalCells.size()*(1+expansion) < tenureCells.size())
			 numberOfCells =  (int) (agriculturalCells.size()*(1+expansion)) ;
		 else 
			 numberOfCells = tenureCells.size();
		 
		
		//	 numberOfCells = (int) agriculturalCells.size()+this.getTenureCellSize()
	//	 System.out.println("number of expansion" + numberOfCells + "?=" +tenureCells.size());
		int riceCellCount = (int) (riceProportion * numberOfCells);
		int cornCellCount = (int) (cornProportion * numberOfCells);
		int soyCellCount = (int) (soyProportion * numberOfCells);
	
	//	System.out.println("R: "+ riceCellCount+"/ C: "+cornCellCount+"/: S "+ soyCellCount);
	   
		
		listToChange.clear();
		 listNotChange.clear();
		
		 
if(tick==1) {
	
	 planningRiceCells.addAll(riceCells);
		
}
//else {

		 if (highest == 2 )    //rice highest		
		 { 
           tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		//	 riceProportion = riceProportion+RandomHelper.nextDoubleFromTo(0.01, 0.05);
			// cornProportion = 1-riceProportion-soyProportion;
			 Collections.sort(this.tenureCells, new SortByRcount());
			 Collections.sort(this.agriculturalCells, new SortByRcount());
			 
	        double riceCutOff=0.0;
			 if(tick==1)
				 riceCutOff=0.03;
			 else riceCutOff=0.08;
		//	 planningRiceCells.addAll(riceCells);
			 
			 for ( int n=0; n < numberOfCells; n++)	    
		     {   
		    	 LandCell c = this.tenureCells.get(n);
		    	 //first check if existing rice cell satisfy
		  
		    	  if( planningRiceCells.size() <= riceCellCount)
		    			 //riceProportion )
		    	//	 if((double) ((planningRiceCells.size())/numberOfCells) <= riceProportion ) 
		    		 {
			    		//   if(c.getLandUse()==LandUse.RICE||c.getNextToRice())
			    		//   {   
		    		//  if( c.getLandUse()==LandUse.RICE||c.getNextToRice())
		    		 if(c.getLandUse()==LandUse.RICE|| c.getNextToRice()) 
		    		 {   planningRiceCells.add(c);
		    		      count++;
		    		      }
			    		       
			    	  else 
			    		  if (c.getLandUse()==LandUse.SOY
			    	//	  ||c.getSProb()>c.getCProb() 
			    			  )
			    			//	   &&
			    			//	   (double) planningSoyCells.size()/numberOfCells < soyProportion)
					      {
			    			   planningSoyCells.add(c);
			    			   count++;
					    		 }
					     else {
					    		 planningCornCells.add(c);
					    		 count++;
					    	 }
			    		 }
		    			 
		            else if ( planningSoyCells.size() < soyCellCount 
		            		&&      		 count<numberOfCells) 
		         {
		        	 planningSoyCells.add(c);
		        	 count ++;
		         } else if(count<numberOfCells)
		                  { 
		        	        planningCornCells.add(c);
		                    count ++; 
		                    }
		         else
		         {
		        	 planningOtherCells.add(c);
		        	 count++;
		         }
		        }
		     
		     count = 0;
		     
		     Collections.sort(planningRiceCells, new SortByRcount());
		//     Collections.sort(planningSoyCells, new SortBySoyProbability());
		     for(int i=0; i<planningRiceCells.size(); i++) {
		    	 if(planningRiceCells.get(i).getRProb()<riceCutOff)
		    	 { 
		    		 listToChange.add(i);
		    		 if(planningRiceCells.get(i).getSProb()
		    				 <planningRiceCells.get(i).getCProb())
		             planningCornCells.add(planningRiceCells.get(i)); 
		    		 else
		    			 planningSoyCells.add(planningRiceCells.get(i));
		    	 }
		     }
		     planningRiceCells.remove(listToChange);
		     
		     listToChange.clear();
		     
		     Collections.sort(planningSoyCells, new SortBySoyProbability());
		     
		     
		     for (int k=0;k<planningSoyCells.size();k++) {
		    	// if(k<(int) soyProportion*tenureCells.size()) {
		    	 if(k < soyCellCount) { 
		    		 listNotChange.add(k);		    	
		    	 } else
		    	  { listToChange.add(k);
		    	    planningCornCells.add(planningSoyCells.get(k));
		    	  }
		     }
		     
		     planningSoyCells.remove(listToChange);		    
		     
		     listToChange.clear();
		     
		     for (int k=0; k<planningCornCells.size(); k++) {
		    	 if(k>cornCellCount) {
		    		 listToChange.add(k);
		    		 planningSoyCells.add(planningCornCells.get(k));
		    	 }
		     }
		     planningCornCells.remove(listToChange);
		    
		 }
		 
		
		 
		 
		 count=0;
		 
		 listToChange.clear();
		 listNotChange.clear();
		 
		 if (highest == 0 )    //soy highest		
		 { 
			 tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			/* if(soyProportion<cornProportion )
			 {   double temp = soyProportion;
				 soyProportion = cornProportion;
				 cornProportion = temp;
			 }*/
		//	 if(	Math.abs((soyProportion-lastYearSoyProportion)/lastYearSoyProportion)>0.3  )
		//	 { cornProportion = 0.352334 
		//		                  + 0.137903*cornSoldToTraderAgent.getCommodityPrice(LandUse.CORN)
		//		                  + RandomHelper.nextDoubleFromTo(-0.1, 0.2);
		//	   soyProportion = 1-cornProportion-riceProportion;}
			
			// soyProportion = soyProportion+RandomHelper.nextDoubleFromTo(-0.05, 0.05);
	
		//"justnow"			 cornProportion = 1-riceProportion-soyProportion;
		 		
		  //      planningSoyCells.addAll(soyCells);
		 //     if(tick==1)
		//    	  planningRiceCells.addAll(riceCells);
			 
			  double riceCutOff=0.0;
				 if(tick==1)
					 riceCutOff=0.03;
				 else riceCutOff=0.15;
		      
			 Collections.sort(this.tenureCells, new SortBySoyProbability());
			 
			 for ( int n=0; n< numberOfCells; n++)	    
		     {   
		    	 LandCell c = this.tenureCells.get(n);
		    	 
		    	
		    		 if(planningSoyCells.size() <=  soyCellCount) 
		    		 { planningSoyCells.add(c);
		    		   count++;
		    		 }
		    	    else if (planningRiceCells.size() <= riceCellCount)
		    			 //riceProportion)
		    	     {	
		               if (c.getLandUse()==LandUse.RICE 
		            		   || c.getNextToRice())
		               {  if(c.getRProb()>riceCutOff)
		    		        planningRiceCells.add(c);
		            	   else 
		            	   {if(c.getCProb()>c.getSProb()) planningCornCells.add(c);
		            	        else planningSoyCells.add(c); } }
		               else {
		            	//   if(c.getCProb()>c.getSProb()) 
		            	//	   planningCornCells.add(c);
           	              //   else 
           	                	 planningSoyCells.add(c); }
		               
		               count++;
		    	   }
		    	 else if(count<numberOfCells)
		    	       { planningCornCells.add(c);count++;}
		    	       else {planningOtherCells.add(c);count++;}
		       }
		     
			 
		     count = 0;
		     Collections.sort(planningRiceCells, new SortByRcount());
  
		     
		     for (int k=0;k<planningRiceCells.size();k++) {
		    	 if(k< riceCellCount) {
		    		 listNotChange.add(k);		    	
		    	 } else
		    	  { listToChange.add(k);
		    	    planningSoyCells.add(planningRiceCells.get(k));
		    	  }
		     }
		     
		     planningRiceCells.remove(listToChange);
		    
		     Collections.sort(planningSoyCells,  new SortBySoyProbability());
		     
		     for( int k=0; k< planningSoyCells.size(); k++){
		    	 if(k>soyCellCount) {
		    		 planningCornCells.add(planningSoyCells.get(k));
		    		 listToChange.add(k);
		    	 }
		     }
		     planningSoyCells.remove(listToChange);
		 }

		 listToChange.clear();
		 listNotChange.clear();
		 
		 if (highest == 1 )    //corn highest		
		 { 
			 tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		//	 cornProportion = 1-riceProportion-soyProportion;
		//	 Collections.sort(this.tenureCells, new SortBySoyProbability());
	//		 System.out.println("soy price "+soySoldToTraderAgent.getCommodityPrice(LandUse.SOY) 
	//		 +"//"+cornProportion);
		//	 if(tick==1)
		//		 planningRiceCells.addAll(riceCells);
			  double riceCutOff=0.0;
				 if(tick==1)
					 riceCutOff=0.03;
				 else riceCutOff=0.15;
				 
				 
		if (maxProb == 1 && (!grownRice) )
		{
			 if( tick>1)    //[1] abandon soybean
		//			 || maxProb == 0) 
			 { 
			//	 System.out.println("almost abandoned soybean "+cornProportion+" s: "+
			//	 soyProportion);
			//	 soyProportion = 0.0+RandomHelper.nextDoubleFromTo(0.0, 0.1);
			//	 cornProportion =1-soyProportion-riceProportion;
				Random r = new Random();
				
				 cornProportion=r.nextGaussian()*0.1+0.9;
				 soyProportion = 1-cornProportion;
				 riceProportion = 0.0;
			//forgot to update the count first time, because now I'm using int
				 //count to control not the proportions.
				 
				 cornCellCount = (int) cornProportion*numberOfCells;
				 soyCellCount = (int) soyProportion * numberOfCells;
				 riceCellCount = (int) riceProportion*numberOfCells;
			  			  
			 }
			 
		//	 else {
			//	 double random=RandomHelper.nextDoubleFromTo(0.05, 0.4);
			//	 cornProportion=cornProportion*(1-random);
			//	 soyProportion = soyProportion*(1+random);
				 
			//	 cornCellCount = (int) (cornProportion*numberOfCells*0.8);
			//	 soyCellCount = (int) (soyProportion * numberOfCells*1.3);
		
				 //left here at 01/10/2019
		//	 }
		}
			count=0;
			 for ( int n=0; n<numberOfCells; n++)	    
		     {   
		    	 LandCell c = this.tenureCells.get(n);
		    	 
		    	 if( planningCornCells.size() <= cornCellCount) 
		    		 {
		    		  planningCornCells.add(c);
		    		  count++;
		    		 }
		    	 else if ( planningRiceCells.size() < riceCellCount)
		    			 //riceProportion)
		    	   {	
		               if (c.getLandUse()==LandUse.RICE 
		            		   || c.getNextToRice())
		    		        planningRiceCells.add(c);                
		               else  planningSoyCells.add(c);
		                    
		               
		               count++;
		    	   }
		    	 else if(count<numberOfCells)
	    	       { planningSoyCells.add(c); count++;}
	    	       else planningOtherCells.add(c);
		     }
		     count = 0;
         Collections.sort(planningRiceCells, new SortByRcount());

	     for(int i=0; i<planningRiceCells.size(); i++) {
	    	 if(planningRiceCells.get(i).getRProb()>riceCutOff)
	    	 { 
	    		 listNotChange.add(i);
	    	 }
	    	 else 
	    		// if(planningRiceCells.get(i).getSProb()>planningRiceCells.get(i).getCProb())
	    		 //{
	    		  //listToChange.add(i);
	              //planningSoyCells.add(planningRiceCells.get(i));
	              //}
	    	      //else 
	    		 {
	    	    	  listToChange.add(i);
	    	    	  planningCornCells.add(planningRiceCells.get(i));
	    	            }
	     }
	     planningRiceCells.remove(listToChange);
	     
	     listToChange.clear();
	     Collections.sort(planningSoyCells, new SortBySoyProbability());
	     
	     for (int k=0;k<planningSoyCells.size();k++) {
		    	// if(k<(int) soyProportion*tenureCells.size()) {
		    	 if(k > soyCellCount) 
		    	  { listToChange.add(k);
		    	    planningCornCells.add(planningSoyCells.get(k));
		    	  }
		     }
		     
		     planningSoyCells.remove(listToChange);		    
		     
		     listToChange.clear();
		     
		     for (int k=0; k<planningCornCells.size(); k++) {
		    	 if(k>cornCellCount) {
		    		 listToChange.add(k);
		    		 planningSoyCells.add(planningCornCells.get(k));
		    	 }
		     }
		     
		     planningCornCells.remove(listToChange);
		    
		 }
		 
		 //the rest of cells are allocated for others; 
		 for(int n=numberOfCells; n < this.getTenureCellSize() ; n++)	    
	     {   
	    	 LandCell c = this.tenureCells.get(n);
	    	 
	    	 planningOtherCells.add(c);
	    	 }
//}
			 
/*		 System.out.println("rice proportion "+riceProportion);
		 System.out.println(soyProportion);
		 System.out.println(cornProportion);*/
		 
}
		 
		 
		
		
		 
		 
	
	
	public double getPriceDelta(LandUse commodity){
		double cPrice = 0;
		double priceDelta = 0;
		double soyPriceDelta = 0;
		double cornPriceDelta = 0;
		double ricePriceDelta = 0;
		
		if(commodity == LandUse.SOY)
		{ 
		//	cPrice = soySoldToTraderAgent.getCommodityPrice(commodity);	
	    //	soyPrices.add(cPrice);
			//don't need to add or remove because it was add at the update price section
	  
	 
   
		   if(soyPrices.size()==1){
	        	soyPriceDelta = soyPrices.get(0).doubleValue();
	   // 	cornPriceDelta = cornPrices.get(0).doubleValue();
	 //   	ricePriceDelta = ricePrices.get(0).doubleValue();
	        } else if(soyPrices.size()==2){
	            	soyPriceDelta = soyPrices.get(1).doubleValue() - 
	    			soyPrices.get(0).doubleValue();
	          
	   // 	cornPriceDelta = cornPrices.get(1).doubleValue() - 
	  //  			cornPrices.get(0).doubleValue();
	  //  	ricePriceDelta = ricePrices.get(1).doubleValue()-
	  //  			ricePrices.get(0).doubleValue();
	         } else{
	            	soyPriceDelta = soyPrices.get(2).doubleValue()-
			           soyPrices.get(1).doubleValue();
	            	
	            	}
		//System.out.println("to test tick ");
			priceDelta = soyPriceDelta;
       	}
		if(commodity == LandUse.CORN) {
		
		   if(cornPrices.size()==1){
	        	cornPriceDelta = cornPrices.get(0).doubleValue();
	   // 	cornPriceDelta = cornPrices.get(0).doubleValue();
	 //   	ricePriceDelta = ricePrices.get(0).doubleValue();
	        } else if(cornPrices.size()==2){
	            	cornPriceDelta = cornPrices.get(1).doubleValue() - 
	    			cornPrices.get(0).doubleValue();
	   // 	cornPriceDelta = cornPrices.get(1).doubleValue() - 
	  //  			cornPrices.get(0).doubleValue();
	  //  	ricePriceDelta = ricePrices.get(1).doubleValue()-
	  //  			ricePrices.get(0).doubleValue();
	         } else{
	            	cornPriceDelta = cornPrices.get(2).doubleValue()-
			           cornPrices.get(1).doubleValue();
	            	}
		   
		   priceDelta = cornPriceDelta;
		}
		
		if(commodity == LandUse.RICE) {
		
		   if(ricePrices.size()==1){
	        	ricePriceDelta = ricePrices.get(0).doubleValue();
	   // 	cornPriceDelta = cornPrices.get(0).doubleValue();
	 //   	ricePriceDelta = ricePrices.get(0).doubleValue();
	        } else if(ricePrices.size()==2){
	        	ricePriceDelta = ricePrices.get(1).doubleValue() - 
	            			ricePrices.get(0).doubleValue();
	   // 	cornPriceDelta = cornPrices.get(1).doubleValue() - 
	  //  			cornPrices.get(0).doubleValue();
	  //  	ricePriceDelta = ricePrices.get(1).doubleValue()-
	  //  			ricePrices.get(0).doubleValue();
	         } else{
	        	 ricePriceDelta = ricePrices.get(2).doubleValue()-
	        			 ricePrices.get(1).doubleValue();
	            	}
		   
		   priceDelta = ricePriceDelta;
		}
		
		return priceDelta;
	}

	public void setCornProportion(double cornProportion) {
		this.cornProportion = cornProportion;
	}
	
	public void setSoyProportion(double soyProportion) {
		this.soyProportion = soyProportion;
	}
	
    public double getCornProportion(){
			return (double) cornCells.size()/this.getTenureCellSize();
		//   return this.cornProportion;
		}
		
		public double getSoyProportion(){
		//    return this.soyProportion;
			return (double) soyCells.size()/this.getTenureCellSize();
		}
		
		public double getRiceProportion() {
			return (double) riceCells.size()/(soyCells.size()+cornCells.size()
			+riceCells.size());
		//	return this.riceProportion;
		}

	


}
