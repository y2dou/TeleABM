/**
 * 
 */
package teleABM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
public class SendingSoybeanAgent extends SoybeanAgent {
	
	protected  FastTable<LandCell> planningSingleSoyCells = new FastTable<LandCell>();
	protected  FastTable<LandCell> planningSoyMaizeCells = new FastTable<LandCell>();
	protected FastTable<LandCell> planningCottonCells = new FastTable<LandCell>();
	protected  FastTable<LandCell> planningSoyCottonCells = new FastTable<LandCell>();	
	//these are to record all planning cells
	
	protected double soyMProduction=0;	
	//this is to record the soy production when it's followed by corn
	protected double soyCProduction=0;
	//this is to record the soy production when it's followed by cotton
	protected double cottonSProduction=0;
	
	protected double soyMPerHaYield=0;
	protected double soyCPerHaYield=0;
	protected double cottonSPerHaYield=0;
	
	protected double lastYearSoyCPerHaProfit=0;
	protected double lastYearSoyMPerHaProfit=0;
//	protected double lastYearCottonSPerHaProfit;
	
	protected double lastYearSoyPerHaCost=0;
	protected double lastYearSoyMPerHaCost=0;
	protected double lastYearSoyCPerHaCost=0;
	protected double lastYearCottonPerHaCost=0;
	
	
	protected boolean proDiversifying = false;
	
	
	
	
	//protected double lastYearCottonPerHaProfit;
	//protected double lastYearOtherPerHaProfit;

	
	public SendingSoybeanAgent() {
		super();
	}
	
	public SendingSoybeanAgent(int id) {
		super(id);
	    initializeSending();
	}
	
	public void initializeSending(){
		
		this.setFertilizerUnitCost(1.398);
	
		//R$1398 per metric ton
		//which is R$1.398 per kg
		
		this.setFuelUnitCost(15.46);
		// 15.46 r$/unit
	//	this.setCornPerHaFuelInput(10.54);
	//	this.setSoyPerHaFuelInput(7.1);
	//    this.setCottonPerHaFuelInput(15.0);
	//	this.setOtherPerHaFuelInput(65.46);
		this.setSoyPerHaFuelInput(10.6);
		this.setCornPerHaFuelInput(10.76);
	//	this.setCornPerHaFuelInput(97.1);
		 this.setCottonPerHaFuelInput(97.1);
		 //error found, 10/28, there were two setCornPerHaFuelInput
		 
		double random = RandomHelper.nextDoubleFromTo(0, 1);
		
		if (random > 0.5) {
			this.proDiversifying = true;
		} else this.proDiversifying = false;
//		System.out.println(random+" this farmer is prof diversifying: "+ proDiversifying);
		
	//	this.proDiversifying=true;  //0209, this was not here.
	/*	planningSingleSoyCells.clear();
		planningSoyMaizeCells.clear();
		planningCottonCells.clear();
		planningSoyCottonCells.clear();
		
	//	singleSoyCells.clear();
	//	soyMaizeCells.clear();
	//	soyCottonCells.clear();
	//	cottonCells.clear();
		
		 soyMProduction=0;	
         soyCProduction=0;
	      cottonSProduction=0;
		
	     soyMPerHaYield=0;
		 soyCPerHaYield=0;
		 cottonSPerHaYield=0;
		
		 lastYearSoyCPerHaProfit=0;
		lastYearSoyMPerHaProfit=0;

		 lastYearSoyPerHaCost=0;
	     lastYearSoyMPerHaCost=0;
		 lastYearSoyCPerHaCost=0;
		 lastYearCottonPerHaCost=0;
		*/
	}
	
	public void decidingTradingPartner(){
		double highestPrice=0;
		double soyPrice=0;
		int soySoldToTraderAgentID=0;
		double cornPrice=0;
		int cornSoldToTraderAgentID=0;
		double cottonPrice=0;
		int cottonSoldToTraderAgentID=0;
		double otherPrice=0;
		int otherSoldToTraderAgentID=0;
		
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
//		System.out.println(tick+ " trader size: "+this.traderAgents.size());
		
	//	if (grownSoy) {
			//check who has the highest price
			for (int i=0; i<this.traderAgents.size();i++) {
				
				soyPrice=traderAgents.get(i).getCommodityPrice(LandUse.SINGLESOY);
				
				if (soyPrice>highestPrice) {
					highestPrice=soyPrice;
					soySoldToTraderAgentID = i;
				//	System.out.println(highestPrice);
				}				
			}			
			soySoldToTraderAgent = this.traderAgents.get(soySoldToTraderAgentID);
			lastYearSoyPrice = highestPrice;
			
	//		System.out.println("sending soy agent "+this.getID()+" trade with "+
     //               soySoldToTraderAgent.getID() +" at tick "+tick);

           // System.out.println("sending; big or small trader"+soySoldToTraderAgent.capital);
		
			
	//	System.out.println("sending trader agent: "+soySoldToTraderAgent.getCapital());

		
	//	if (grownCorn) {
			highestPrice=0;
			for (int i=0;i<this.traderAgents.size();i++){
				cornPrice=traderAgents.get(i).getCommodityPrice(LandUse.CORN);
				if (cornPrice>highestPrice) {
					highestPrice=cornPrice;
					cornSoldToTraderAgentID = i;
				}				
			}			
			cornSoldToTraderAgent = this.traderAgents.get(cornSoldToTraderAgentID);
			lastYearCornPrice = highestPrice;
	//	}
		
	//	if (grownCotton) {
			highestPrice=0;
			for (int i=0;i<this.traderAgents.size();i++){
				cottonPrice=traderAgents.get(i).getCommodityPrice(LandUse.COTTON);
	//			System.out.println("commodity type: "+traderAgents.get(i).getCommodityType());
	//			System.out.println("commodity price: "+traderAgents.get(i).getCommodityPrice(LandUse.RICE));
				if ( cottonPrice>highestPrice) {
					highestPrice=cottonPrice;
					cottonSoldToTraderAgentID = i;
				}				
			}			
			lastYearCottonPrice = highestPrice;
			cottonSoldToTraderAgent = this.traderAgents.get(cottonSoldToTraderAgentID);
//			System.out.println("sold to cotton agent: "+cottonSoldToTraderAgentID+" "+highestPrice);
	//	}
	
		
			
	}
	
	
	
	public void landUseDecision(OrganicSpace organicSpace){
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		//	
	//	 OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
//		 System.out.println("sending organic space: "+organicSpace.getTypeID());
		LandUse highestLandUse=LandUse.SINGLESOY;
		
        int landUseNumber=0;
 //       List<Integer> listToChange = new ArrayList<Integer>();
//		List<Integer> listNotChange = new ArrayList<Integer>();
		
        planningSingleSoyCells.clear();
        planningSoyMaizeCells.clear();
        planningCottonCells.clear();
        planningSoyCottonCells.clear();
        
		for(int i =0; i< this.tenureCells.size(); i++){
			
		}
		
		
		double[] profit = new double[4];
		
		profit[0] = lastYearSoyPerHaProfit;   //single soy profit , 2
		profit[1] = lastYearSoyMPerHaProfit;   //soy+maize profit, 1
		profit[2] = lastYearSoyCPerHaProfit;   //soy+cotton profit, 9
		profit[3] = lastYearCottonPerHaProfit;  //single cotton profit, 3
		
		double highestProfit = profit[0];
		int count=0;
		for (int i = 0; i<4; i++){
		//	System.out.println(i+" = "+profit[i]);
			if (highestProfit<profit[i])
				{ highestProfit = profit[i];
		          count = i;
				}
	        }
		
		if(count == 0)  { highestLandUse = LandUse.SINGLESOY; landUseNumber = 2;}
		if(count == 1)  { highestLandUse = LandUse.DOUBLESOY; landUseNumber = 1;}
		if(count == 2)  { highestLandUse = LandUse.SOYCOTTON; landUseNumber = 9;}
		if(count == 3)  { highestLandUse = LandUse.COTTON; landUseNumber = 3;}
		
	//    System.out.println("tick: "+tick+" hhd id:"+ this.getID()+
	 //   		              " highest land use = "+highestLandUse+" "+profit[count]);
		
		
	//	landUseNumber=2;
	    if (this.proDiversifying) {
		 for (int i =0; i< Math.round(this.tenureCells.size()*0.5); i++)
		 { 
			LandCell c = this.tenureCells.get(i);
			c.setLastLandUse(c.getLandUse());
		//	c.setLandUse(highestLandUse);
		//	organicSpace.setLandUse(landUseNumber, c.getXlocation(), c.getYlocation());
			if(highestLandUse == LandUse.SINGLESOY)
				planningSingleSoyCells.add(c);
			if(highestLandUse == LandUse.DOUBLESOY)
				planningSoyMaizeCells.add(c);
			if(highestLandUse == LandUse.COTTON)
				planningCottonCells.add(c);
			if(highestLandUse == LandUse.SOYCOTTON)
				planningSoyCottonCells.add(c);
			
		 }
		 
		 for (int i = (int) Math.round(this.tenureCells.size()*0.5); i<this.tenureCells.size();i++ ){
			    LandCell c = this.tenureCells.get(i);
				c.setLastLandUse(c.getLandUse());
				if (highestLandUse == LandUse.SINGLESOY||highestLandUse == LandUse.DOUBLESOY) {
					if(RandomHelper.nextDoubleFromTo(0, 1)>0.5) {
						planningCottonCells.add(c);
			//		c.setLandUse(LandUse.COTTON);
			//		organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
					} else {
			//			c.setLandUse(LandUse.SOYCOTTON);
			//			organicSpace.setLandUse(9, c.getXlocation(), c.getYlocation());
						planningSoyCottonCells.add(c);
					}
				} else {
					if (RandomHelper.nextDoubleFromTo(0, 1)>0.5) 
					{
			//			c.setLandUse(LandUse.SINGLESOY);
			//			organicSpace.setLandUse(2, c.getXlocation(), c.getYlocation());
						planningSingleSoyCells.add(c);
					} else {
			//			c.setLandUse(LandUse.DOUBLESOY);
			//			organicSpace.setLandUse(1, c.getXlocation(), c.getYlocation());
						planningSoyMaizeCells.add(c);
					}
					
					
				}
				
		 }
	    } else {
	    	if(highestLandUse==LandUse.SINGLESOY)
	    	 for (int i =0; i< this.tenureCells.size(); i++)
			 { 
				LandCell c = this.tenureCells.get(i);
				c.setLastLandUse(c.getLandUse());
				planningSingleSoyCells.add(c);				
				
			 }
	    	if(highestLandUse == LandUse.DOUBLESOY) 
	    		for (int i = 0 ; i<this.tenureCells.size();i++) {
	    			LandCell c = this.tenureCells.get(i);
	    			c.setLastLandUse(c.getLandUse());
	    			planningSoyMaizeCells.add(c);
	    		}
	    	if(highestLandUse == LandUse.SOYCOTTON) 
	    		for (int i = 0 ; i<this.tenureCells.size();i++) {
	    			LandCell c = this.tenureCells.get(i);
	    			c.setLastLandUse(c.getLandUse());
	    			planningSoyCottonCells.add(c);
	    		}
	    	if(highestLandUse == LandUse.COTTON) 
	    		for (int i = 0 ; i<this.tenureCells.size();i++) {
	    			LandCell c = this.tenureCells.get(i);
	    			c.setLastLandUse(c.getLandUse());
	    			planningCottonCells.add(c);
	    		}
	    }
	    
//	    System.out.println("soy: "+ planningSoyCells.size()+" double soy: "+planningSoyMaizeCells.size()
//	                         +" cotton: "+planningCottonCells.size()
//	                         +"soy cotton: "+planningSoyCottonCells.size());
		
		
	}

	@Override
	public void updateProduction(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
		//   this.soyCells.clear();
	//	   this.cornCells.clear();
		//   this.cottonCells.clear();
		 //  this.soyMaizeCells.clear();
		 //  this.soyCottonCells.clear();
		   
		   this.cornProduction = 0;
		   this.soyProduction = 0;
		   this.soyMProduction = 0;
		   this.soyCProduction = 0;
		   this.cottonProduction = 0;
		   this.cottonSProduction = 0;
		   
		   singleSoyCells.clear();
		    soyMaizeCells.clear();
		    cottonCells.clear();
		    soyCottonCells.clear();
		    
		    
		    for(LandCell c:this.tenureCells) {
		    	if(c.getLandUse()==LandUse.SINGLESOY)
		    		singleSoyCells.add(c);
		    	if(c.getLandUse()==LandUse.DOUBLESOY)
		    		soyMaizeCells.add(c);
		    	if(c.getLandUse()==LandUse.COTTON)
		    		cottonCells.add(c);
		    	if(c.getLandUse()==LandUse.SOYCOTTON)
		    		soyCottonCells.add(c);
		    }
		   
		
		   for(LandCell c: this.tenureCells) 
		   {  
			    c.transition();
			   if( singleSoyCells.contains(c))
			    	soyProduction+=c.getSoyYield();
			     else if (soyMaizeCells.contains(c)){	   
			    	soyMProduction+=c.getSoyYield();			    			
			    	cornProduction+=c.getCornYield();
			    } else if(soyCottonCells.contains(c)){
			    	soyCProduction+=c.getSoyYield();
			    	cottonSProduction+=c.getCottonYield();
			    }  else if(cottonCells.add(c)) 
			    
			    	cottonProduction+=c.getCottonYield();	    	
			     else {  //others, right now don't record.
			    
			    }		    
		   }
			
			   if(singleSoyCells.size() > 0) { 
			    soyPerHaYield = soyProduction/singleSoyCells.size();
				soyPerHaYield = soyPerHaYield /(cellsizeSending*cellsizeSending)*10000.0;
			   }
			   if(soyMaizeCells.size() > 0){
				   soyMPerHaYield= soyMProduction/soyMaizeCells.size();
				   soyMPerHaYield = soyMPerHaYield/(cellsizeSending*cellsizeSending)*10000.0;
				   cornPerHaYield = cornProduction/soyMaizeCells.size();
				   cornPerHaYield = cornPerHaYield/(cellsizeSending*cellsizeSending)*10000.0;
			   }
			   if(cottonCells.size() > 0) {
				   cottonPerHaYield = cottonProduction/cottonCells.size();
				   cottonPerHaYield = cottonPerHaYield/(cellsizeSending*cellsizeSending)*10000.0;
			   }
			   
			   if(soyCottonCells.size() > 0) {
				   soyCPerHaYield = soyCProduction/soyCottonCells.size();
				   soyCPerHaYield = soyCPerHaYield/(cellsizeSending*cellsizeSending)*10000.0;
				   cottonSPerHaYield = cottonSProduction/soyCottonCells.size();
				   cottonSPerHaYield = cottonSPerHaYield / (cellsizeSending*cellsizeSending)*10000.0;
			   }
				
		   
		   
		 
		//   System.out.println(riceProduction);
		   if (cornProduction>0) this.grownCorn=true;
		   if (cottonProduction>0) this.grownCotton=true;
		   
		   if (soyProduction>0) 
		   {
			   this.grownSoy=true; this.grownSoyYears=grownSoyYears+1;
	//	       System.out.println("has soy production "+soyProduction);
		   }
		   
		 		   
		    this.setGrownSoyYears(grownSoyYears);
			this.setSoyProduction(soyProduction+soyMProduction+soyCProduction);
			this.setCornProduction(cornProduction);
			this.setCottonProduction(cottonProduction+cottonSProduction);
	
	}

	@Override
	public void updateLandUse(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
	 boolean nextToAgricultural = false;
	 int countChange = 0;	
	  int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	  double soyPriceDelta = getPriceDelta(LandUse.SINGLESOY);
	  double cornPriceDelta = getPriceDelta(LandUse.CORN);
	  double cottonPriceDelta = getPriceDelta(LandUse.COTTON);	
		
     double threshold = 0.1;
	
     if((soyPriceDelta+cornPriceDelta)/2 <-0.04  || cottonPriceDelta<-1.0  ) 
	 {
		 threshold = 0.3;
	 }
	 else threshold = (soyPriceDelta + cornPriceDelta)/2 + RandomHelper.nextDoubleFromTo(-0.005, 0.005);
	// SimUtilities.shuffle(this.tenureCells, RandomHelper.getUniform());
	//   SimUtilities.shuffle(this.planningSoyCells, RandomHelper.getUniform());
	//   this.planningSoyCells.sort();
	   
	   Collections.sort(this.tenureCells, new SortbyRoll());
	   Collections.sort(this.agriculturalCells, new SortbyRoll());
	   Collections.sort(planningSingleSoyCells, new SortbyRoll());
  	 
  /*	 for(int i=(int) (tick/20.0)*planningSoyCells.size();i<planningSoyCells.size();i++) {
  		 LandCell c = planningSoyCells.get(i);
  		 planningSoyMaizeCells.add(c);
  		 planningSoyCells.remove(c);
  	 }*/
	
	    for(LandCell c:this.tenureCells) {
			     
	        	for(GridCell<LandCell> cell:c.nghCell) 
	        	{
			    
	        		int landuseNumber = organicSpace.getLandUseAt(cell.getPoint().getX(), cell.getPoint().getY());
		        
			         if(landuseNumber == 1||landuseNumber ==2)
			           { 
				            nextToAgricultural = true;
				 //           System.out.println("ngh is correct");
			            	break;
			        	}
			         else if(landuseNumber == 3||landuseNumber ==9) 
			         {
			            	nextToAgricultural = true;
				            break;
			         } else 
			        	 nextToAgricultural = false;		
		
		          }
	        	
	//	if(!c.isChangedThisTime()) {
	        	
	        	 
		    if(agriculturalCells.contains(c) )
		     {

	        	c.setLastLastLandUse(c.getLastLandUse());
	        	c.setLastLandUse(c.getLandUse());
	        	//because nonagricultural cells have been updated in logistic regression
		    	 
		    	
		    	  
		    	  if(planningSoyMaizeCells.contains(c) && (capital/agriculturalCells.size()) > 1.5* lastYearSoyMPerHaCost*(cellsize*cellsize)/10000)
		    	  {
		    			c.setLandUse(LandUse.DOUBLESOY);
		    			organicSpace.setLandUse(1, c.getXlocation(), c.getYlocation());
		    			  c.setChangedThisTime(true);
		    			c.setFertilizerInput(LandUse.DOUBLESOY);
		    			c.setFuelInput(((soyPerHaFuelInput+cornPerHaFuelInput)/10000.0)
		    					*(cellsizeSending*cellsizeSending));
		    		
		    			capital-= (c.getFertilizerInput()*fertilizerUnitCost +
		    					  c.getFuelInput()*fuelUnitCost)/0.36;
		    	  }
		    //	  else if(planningSoyCells.contains(c) && capital> (tick)*lastYearSoyPerHaCost*(cellsize*cellsize)/10000)
		    		  else if(planningSingleSoyCells.contains(c) && (capital/agriculturalCells.size())> 2.0*lastYearSoyPerHaCost*(cellsize*cellsize)/10000)
		    	  {    		  
		    			//	c.setLastLandUse(c.getLandUse());
		    			c.setLandUse(LandUse.SINGLESOY);
		    			organicSpace.setLandUse(2, c.getXlocation(), c.getYlocation());
		    			c.setChangedThisTime(true);
		    			c.setFertilizerInput(LandUse.SINGLESOY);
		    			c.setFuelInput((soyPerHaFuelInput/10000.0)
		    							*(cellsizeSending*cellsizeSending));
		    					//the soy per ha fuel is in initialization function
		    				  //	c.setWaterRequirement(LandUse.SOY);		
		    			capital-= (c.getFertilizerInput()*fertilizerUnitCost +
		    							  c.getFuelInput()*fuelUnitCost)/0.34;
		    	 
		          } 
		    	  else if(planningCottonCells.contains(c) && (capital/agriculturalCells.size())> 2.0* lastYearCottonPerHaCost*(cellsize*cellsize)/10000)
		    	  {
		    		  c.setLandUse(LandUse.COTTON);
		  			  organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
		  			  c.setChangedThisTime(true);
		  			  c.setFertilizerInput(LandUse.COTTON);
		  			  c.setFuelInput((cottonPerHaFuelInput/10000.0)
		  					  *(cellsizeSending*cellsizeSending));
		  			//the soy and corn per ha fuel is in initialization function
		  		//	c.setWaterRequirement(LandUse.SOY);		
		  			  capital-= (c.getFertilizerInput()*fertilizerUnitCost +
		  					  c.getFuelInput()*fuelUnitCost)/0.28;
		    	  }
		    	  else 
		    	 if  (planningSoyCottonCells.contains(c) && (capital/agriculturalCells.size())> 1.5* lastYearSoyCPerHaCost*(cellsize*cellsize)/10000)
		    	  {
		    		  c.setLandUse(LandUse.SOYCOTTON);
		  		      organicSpace.setLandUse(9, c.getXlocation(), c.getYlocation());
		  		      c.setChangedThisTime(true);
		  			  c.setFertilizerInput(LandUse.SOYCOTTON);
		  			  c.setFuelInput(((soyPerHaFuelInput+cottonPerHaFuelInput)/10000.0)
		  					      *(cellsizeSending*cellsizeSending));
		  			  capital-= (c.getFertilizerInput()*fertilizerUnitCost +
		  					   c.getFuelInput()*fuelUnitCost) /0.30; 
		    	  } else {
		    		  c.setLandUse(LandUse.GRASSLAND);
		    		  organicSpace.setLandUse(4, c.getXlocation(), c.getYlocation());
		    		  c.setChangedThisTime(true);
		    		  agriculturalCells.remove(c);
		    	  }
		    	 
		     } //else is when c does not belong to agricultural cells
		    else 
		    {	
		       
		    	if((double) countChange/this.agriculturalCells.size()<threshold) 
		    	{	
		    	   if( nextToAgricultural && c.getSuitability()>0)
		    		   //has to have getSuitability....
		    		   //otherwise the property shows up 
		    	   {   
		    		   
				    //	  if(planningSoyMaizeCells.contains(c) && capital > lastYearSoyMPerHaCost*(cellsize*cellsize)/10000)
		    		   if(planningSoyMaizeCells.contains(c) && lastYearSoyMPerHaProfit> 1.5*lastYearSoyMPerHaCost)
				    	  {
				    			c.setLandUse(LandUse.DOUBLESOY);
				    			organicSpace.setLandUse(1, c.getXlocation(), c.getYlocation());
				    			  c.setChangedThisTime(true);
				    			c.setFertilizerInput(LandUse.DOUBLESOY);
				    			c.setFuelInput(((soyPerHaFuelInput+cornPerHaFuelInput)/10000.0)
				    					*(cellsizeSending*cellsizeSending));
				    		
				    			capital-= (c.getFertilizerInput()*fertilizerUnitCost +
				    					  c.getFuelInput()*fuelUnitCost)/0.36;
				    			countChange++;
				    			agriculturalCells.add(c);
				    	  }
		    		   
		    		   if(planningSingleSoyCells.contains(c) )
		    		   {
		    			   if( lastYearSoyPerHaProfit> 1.5*lastYearSoyPerHaCost)
		    			   {    		  
		    		  
				    			//	c.setLastLandUse(c.getLandUse());
				    			c.setLandUse(LandUse.SINGLESOY);
				    			organicSpace.setLandUse(2, c.getXlocation(), c.getYlocation());
				    			  c.setChangedThisTime(true);
				    			c.setFertilizerInput(LandUse.SINGLESOY);
				    			c.setFuelInput((soyPerHaFuelInput/10000.0)
				    							*(cellsizeSending*cellsizeSending));
				    					//the soy per ha fuel is in initialization function
				    				  //	c.setWaterRequirement(LandUse.SOY);		
				    			capital-= (c.getFertilizerInput()*fertilizerUnitCost +
				    							  c.getFuelInput()*fuelUnitCost)/0.34;
				    			countChange++;
				    			agriculturalCells.add(c);
				    	 
				          }	
		    			   
		    			   else {
		    				   c.setLandUse(LandUse.DOUBLESOY);
				    			organicSpace.setLandUse(1, c.getXlocation(), c.getYlocation());
				    			  c.setChangedThisTime(true);
				    			c.setFertilizerInput(LandUse.DOUBLESOY);
				    			c.setFuelInput(((soyPerHaFuelInput+cornPerHaFuelInput)/10000.0)
				    					*(cellsizeSending*cellsizeSending));
				    		
				    			capital-= (c.getFertilizerInput()*fertilizerUnitCost +
				    					  c.getFuelInput()*fuelUnitCost)/0.36;
				    			countChange++;
				    			agriculturalCells.add(c);
		    			   }
		    	        }
				    	  
				    	  if(planningCottonCells.contains(c) && capital > lastYearCottonPerHaCost*(cellsize*cellsize)/10000)
				    	  {
				    		  c.setLandUse(LandUse.COTTON);
				  			  organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
				  			  c.setChangedThisTime(true);
				  			  c.setFertilizerInput(LandUse.COTTON);
				  			  c.setFuelInput((cottonPerHaFuelInput/10000.0)
				  					  *(cellsizeSending*cellsizeSending));
				  			//the soy and corn per ha fuel is in initialization function
				  		//	c.setWaterRequirement(LandUse.SOY);		
				  			  capital-= (c.getFertilizerInput()*fertilizerUnitCost +
				  					  c.getFuelInput()*fuelUnitCost)/0.28;
				  			  countChange++;
			    			  agriculturalCells.add(c);
				    	  }
				    	  
				    	//  if(planningSoyCottonCells.contains(c) && capital > lastYearSoyCPerHaCost*(cellsize*cellsize)/10000)
				    	  if(planningSoyCottonCells.contains(c) && lastYearSoyCPerHaProfit> 1.5*lastYearSoyCPerHaCost)
				    	  {
				    		  c.setLandUse(LandUse.SOYCOTTON);
				  		      organicSpace.setLandUse(9, c.getXlocation(), c.getYlocation());
				  		    c.setChangedThisTime(true);
				  			  c.setFertilizerInput(LandUse.SOYCOTTON);
				  			  c.setFuelInput(((soyPerHaFuelInput+cottonPerHaFuelInput)/10000.0)
				  					      *(cellsizeSending*cellsizeSending));
				  			  capital-= (c.getFertilizerInput()*fertilizerUnitCost +
				  					   c.getFuelInput()*fuelUnitCost) /0.30; 
				  	          countChange++;
			    			  agriculturalCells.add(c);
				    	  }
		    	   }
		    	   else  //this else is for  if( nextToAgricultural && c.getSuitability()>0)
		    	   {
		    		   if(c.getSuitability()>0) 
		    		   {
		    			   if(planningSingleSoyCells.contains(c) && lastYearSoyPerHaProfit> 1.5*lastYearSoyPerHaCost)
		    			//   if(planningSoyCells.contains(c) && lastYearSoyPerHaProfit> tick*lastYearSoyPerHaCost)
					    	  {    		  
					    			//	c.setLastLandUse(c.getLandUse());
					    			c.setLandUse(LandUse.SINGLESOY);
					    			organicSpace.setLandUse(2, c.getXlocation(), c.getYlocation());
					    			  c.setChangedThisTime(true);
					    			c.setFertilizerInput(LandUse.SINGLESOY);
					    			c.setFuelInput((soyPerHaFuelInput/10000.0)
					    							*(cellsizeSending*cellsizeSending));
					    					//the soy per ha fuel is in initialization function
					    				  //	c.setWaterRequirement(LandUse.SOY);		
					    			capital-= (c.getFertilizerInput()*fertilizerUnitCost +
					    							  c.getFuelInput()*fuelUnitCost)/0.34;
					    			countChange++;
					    			agriculturalCells.add(c);
					    	 
					          }			    	  
					    	  if(planningSoyMaizeCells.contains(c) && capital > 0)
					    	  {
					    			c.setLandUse(LandUse.DOUBLESOY);
					    			organicSpace.setLandUse(1, c.getXlocation(), c.getYlocation());
					    			  c.setChangedThisTime(true);
					    			c.setFertilizerInput(LandUse.DOUBLESOY);
					    			c.setFuelInput(((soyPerHaFuelInput+cornPerHaFuelInput)/10000.0)
					    					*(cellsizeSending*cellsizeSending));
					    		
					    			capital-= (c.getFertilizerInput()*fertilizerUnitCost +
					    					  c.getFuelInput()*fuelUnitCost)/0.36;
					    			countChange++;
					    			agriculturalCells.add(c);
					    	  }
					    	  
					    	  if(planningCottonCells.contains(c) && capital > 0)
					    	  {
					    		  c.setLandUse(LandUse.COTTON);
					  			  organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
					  			  c.setChangedThisTime(true);
					  			  c.setFertilizerInput(LandUse.COTTON);
					  			  c.setFuelInput((cottonPerHaFuelInput/10000.0)
					  					  *(cellsizeSending*cellsizeSending));
					  			//the soy and corn per ha fuel is in initialization function
					  		//	c.setWaterRequirement(LandUse.SOY);		
					  			  capital-= (c.getFertilizerInput()*fertilizerUnitCost +
					  					  c.getFuelInput()*fuelUnitCost)/0.28;
					  			  countChange++;
				    			  agriculturalCells.add(c);
					    	  }
					    	  
					    	  if(planningSoyCottonCells.contains(c) && capital > 0)
					    	  {
					    		  c.setLandUse(LandUse.SOYCOTTON);
					  		      organicSpace.setLandUse(9, c.getXlocation(), c.getYlocation());
					  		    c.setChangedThisTime(true);
					  			  c.setFertilizerInput(LandUse.SOYCOTTON);
					  			  c.setFuelInput(((soyPerHaFuelInput+cottonPerHaFuelInput)/10000.0)
					  					      *(cellsizeSending*cellsizeSending));
					  			  capital-= (c.getFertilizerInput()*fertilizerUnitCost +
					  					   c.getFuelInput()*fuelUnitCost) /0.30; 
					  	          countChange++;
				    			  agriculturalCells.add(c);
					    	  }
		    		   }
		    		   else if (c.getLastLandUse()==LandUse.FOREST)
		    		   {
		    			   c.setLastLastLandUse(c.getLastLandUse());
			    		   c.setLastLandUse(c.getLandUse());
			    		   c.setLandUse(LandUse.FOREST);
			    		   organicSpace.setLandUse(5,c.getXlocation(), c.getYlocation());
			    		   c.setChangedThisTime(true);
		    		   }
		    		   else 
		    		   {
		    			   c.setLastLastLandUse(c.getLastLandUse());
			    		   c.setLastLandUse(c.getLandUse());
			    		   c.setLandUse(LandUse.GRASSLAND);
			    		   organicSpace.setLandUse(4,c.getXlocation(), c.getYlocation());
			    		   c.setChangedThisTime(true);
		    		   }
		    		  
		    		   
		    	   }
		    		
		       } //if countchange/agcells>0.1
		    	else 
		    	{
		    		  
		    		   if(c.getLastLandUse()==LandUse.GRASSLAND) {
		    			   c.setLastLastLandUse(c.getLastLandUse());
			    		   c.setLastLandUse(c.getLandUse());
		    		   c.setLandUse(LandUse.GRASSLAND);
		    		   organicSpace.setLandUse(4,c.getXlocation(), c.getYlocation());
		    		   c.setChangedThisTime(true);
		    		   }
		    		   else {
		    			   c.setLastLastLandUse(c.getLastLandUse());
			    		   c.setLastLandUse(c.getLandUse());
		    			   c.setLandUse(LandUse.FOREST);
		    			   organicSpace.setLandUse(5,c.getXlocation(), c.getYlocation());
		    			   c.setChangedThisTime(true);
		    		   }
		    	}
		    }	
		
	
	//	}
	  //  if(capital < 0) System.out.println("capital of sending "+capital);
	    }
	    
	    singleSoyCells.clear();
	    soyMaizeCells.clear();
	    cottonCells.clear();
	    soyCottonCells.clear();
	    
	    
	    for(LandCell c:this.tenureCells) {
	    	if(c.getLandUse()==LandUse.SINGLESOY)
	    		singleSoyCells.add(c);
	    	if(c.getLandUse()==LandUse.DOUBLESOY)
	    		soyMaizeCells.add(c);
	    	if(c.getLandUse()==LandUse.COTTON)
	    		cottonCells.add(c);
	    	if(c.getLandUse()==LandUse.SOYCOTTON)
	    		soyCottonCells.add(c);
	    }
	  
	//    System.out.println(tick+" soy "+soyCells.size()+" soymaize="+soyMaizeCells.size());
	 //   System.out.println(cottonCells.size()+" "+soyCottonCells.size());
	}

	@Override
	public void updateCost(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
		double cost = 0;
		double totalCost = 0;
		double totalfertilizeruse=0;
		
		if(singleSoyCells.size()>0){
		for (int i=0; i<singleSoyCells.size();i++){
			//setFertilizerInput() has already been put at land use decision ();
			cost+=singleSoyCells.get(i).getFertilizerInput()*fertilizerUnitCost; 
			cost+=singleSoyCells.get(i).getFuelInput()*fuelUnitCost;
			totalfertilizeruse+= singleSoyCells.get(i).getFertilizerInput();
		//	System.out.println("cell no. "+i+" use fertilizer "+soyCells.get(i).getFertilizerInput());
		}
		cost/=singleSoyCells.size();
		cost/=0.34;  
		//this is to convert the above calculated fertilizer and fuel cost to total cost, 
		//based on empirical proportion
		//title: sinop soybean price and production, sheet 2, 
		//location: google drive spreadsheet
		cost/=(cellsizeSending*cellsizeSending)/10000.0;
		setLastYearSoyPerHaCost(cost);
		totalCost+=cost;
		soyPerHaFertilizerInput=totalfertilizeruse/singleSoyCells.size();    					
		soyPerHaFertilizerInput = soyPerHaFertilizerInput/(cellsizeSending*cellsizeSending)*10000.0;    			
		setSoyPerHaFertilizerInput(soyPerHaFertilizerInput);
		totalfertilizeruse = 0;
		cost=0;
		} else setLastYearSoyPerHaCost(1611.4);
		
		if(soyMaizeCells.size()>0){
			for (int i=0; i<soyMaizeCells.size();i++){
				cost+=soyMaizeCells.get(i).getFertilizerInput()*fertilizerUnitCost;
				//this getFertilizerInput has both, soy and maize
				cost+=soyMaizeCells.get(i).getFuelInput()*fuelUnitCost;
				totalfertilizeruse+=soyMaizeCells.get(i).getFertilizerInput();			
				
				}
			cost/=soyMaizeCells.size();
			cost/=0.36;  
			//this is to convert the above calculated fertilizer and fuel cost to total cost, 
			//based on empirical proportion
			cost/=(cellsizeSending*cellsizeSending)/10000.0;
			setLastYearSoyMPerHaCost(cost);
			totalCost+=cost;
			soyMaizePerHaFertilizerInput=totalfertilizeruse/soyMaizeCells.size();
			soyMaizePerHaFertilizerInput = soyMaizePerHaFertilizerInput/(cellsizeSending*cellsizeSending)*10000.0;
			setSoyMaizePerHaFertilizerInput(soyMaizePerHaFertilizerInput);
			totalfertilizeruse=0;
			cost=0;
			} else setLastYearSoyMPerHaCost(3531);
		
		if(cottonCells.size()>0){
			for (int i=0; i<cottonCells.size();i++){
				cost+=cottonCells.get(i).getFertilizerInput()*fertilizerUnitCost;
				cost+=cottonCells.get(i).getFuelInput()*fuelUnitCost;
				totalfertilizeruse+=cottonCells.get(i).getFertilizerInput();
			}
			cost/=cottonCells.size();
			cost/=0.28;  
			//this is to convert the above calculated fertilizer and fuel cost to total cost, 
			//based on empirical proportion
			cost/=(cellsizeSending*cellsizeSending)/10000.0;
			setLastYearCottonPerHaCost(cost);
			totalCost+=cost;
			cottonPerHaFertilizerInput=totalfertilizeruse/cottonCells.size();
			cottonPerHaFertilizerInput = cottonPerHaFertilizerInput/(cellsizeSending*cellsizeSending)*10000.0;
			setCottonPerHaFertilizerInput(cottonPerHaFertilizerInput);
			totalfertilizeruse = 0;
			cost = 0;
			}   else setLastYearCottonPerHaCost(4918);
		
		if (soyCottonCells.size()>0){
			for (int i=0;i<soyCottonCells.size();i++){
				cost+=soyCottonCells.get(i).getFertilizerInput()*fertilizerUnitCost;
				cost+=soyCottonCells.get(i).getFuelInput()*fuelUnitCost;
				totalfertilizeruse+=soyCottonCells.get(i).getFertilizerInput();
			}
			cost/=soyCottonCells.size();
			cost/=0.30;  
			//this is to convert the above calculated fertilizer and fuel cost to total cost, 
			//based on empirical proportion
			cost/=(cellsizeSending*cellsizeSending)/10000.0;
			setLastYearSoyCPerHaCost(cost);
			totalCost+=cost;
			soyCottonPerHaFertilizerInput=totalfertilizeruse/soyCottonCells.size();
			soyCottonPerHaFertilizerInput = soyCottonPerHaFertilizerInput/(cellsizeSending*cellsizeSending)*10000.0;
			setSoyCottonPerHaFertilizerInput(soyCottonPerHaFertilizerInput);
			totalfertilizeruse=0;
			cost = 0;
		}  else setLastYearSoyCPerHaCost(6529);
	
		capital-=totalCost;
//		System.out.println("total cost = "+totalCost);
	}

	@Override
	public void updateProfit() {
		// TODO Auto-generated method stub
	    double cPrice=0;
	    double csecondPrice=0;
	    
	  /*  if(soyProduction>0){
	    	
	    	soyPerHaYield = soyProduction/soyCells.size();
			soyPerHaYield = soyPerHaYield /(cellsize*cellsize)*10000.0;
			cPrice = this.getCommodityPrice(LandUse.SINGLESOY);
		//	System.out.println("is price signed?"+cPrice);
			profit+= cPrice*soyProduction;
			
			lastYearSoyPerHaProfit = soyPerHaYield*cPrice-getSoyPerHaFuelInput()*fuelUnitCost
	                 -soyPerHaFertilizerInput*fertilizerUnitCost 
	                 - 121 //seed cost
	                 ;
	    
	    }
		
		if(cornProduction > 0) {
			
		cPrice = this.getCommodityPrice(LandUse.DOUBLESOY);
		profit+= cPrice*cornProduction;
		
		cornPerHaYield=cornProduction/cornCells.size();
		cornPerHaYield = (cornPerHaYield/(cellsize*cellsize))*10000.0;
		lastYearCornPerHaProfit = cornPerHaYield*cPrice-cornPerHaFuelInput*fuelUnitCost
                -cornPerHaFertilizerInput*fertilizerUnitCost 
                - 238.0 //seed cost
                ;
		}
		
		if(cottonProduction > 0){
			cPrice = this.getCommodityPrice(LandUse.COTTON);
			
			cottonPerHaYield = cottonProduction/cottonCells.size();
			cottonPerHaYield = (cottonPerHaYield/(cellsize*cellsize))*10000.0;
			lastYearCottonPerHaProfit = cottonPerHaYield*cPrice - cottonPerHaFuelInput*fuelUnitCost 
					-cottonPerHaFertilizerInput*fertilizerUnitCost
					-134.25; //seed cost
		}*/
	    if(soyProduction >0) {
	//    	cPrice = this.getCommodityPrice(LandUse.SINGLESOY);
	    	cPrice = soySoldToTraderAgent.getCommodityPrice(LandUse.SINGLESOY);
	//    	System.out.println("sending soy price="+cPrice);
	    	profit+= cPrice*soyProduction;
	    	lastYearSoyPerHaProfit = soyPerHaYield*cPrice-getSoyPerHaFuelInput()*fuelUnitCost
	                 -soyPerHaFertilizerInput*fertilizerUnitCost 
	        //         - 121 //seed cost
	                 ;
	    } else lastYearSoyPerHaProfit = 0.701*3071; //2010 price and average yield
//	    lastYearSoyPerHaProfit = 0.701*3071; 
	    if (soyMProduction > 0) {
	    //	cPrice = this.getCommodityPrice(LandUse.SINGLESOY);
	    	cPrice = soySoldToTraderAgent.getCommodityPrice(LandUse.SINGLESOY);
	    	profit+=cPrice*soyMProduction;
	    //	profit+= this.getCommodityPrice(LandUse.CORN)
	    	csecondPrice = cornSoldToTraderAgent.getCommodityPrice(LandUse.CORN);
	  //  	System.out.println(cPrice+"  price//"+csecondPrice);
	    	profit+= csecondPrice
	    			 *
	    			cornProduction;
	    	lastYearSoyMPerHaProfit = soyMPerHaYield*cPrice
	    			-getSoyPerHaFuelInput()*fuelUnitCost
	                 -soyPerHaFertilizerInput*fertilizerUnitCost 
	               //  - 121 
	                 //corn profit
	                 + cornPerHaYield*csecondPrice - getCornPerHaFuelInput()*fuelUnitCost
	                 - cornPerHaFertilizerInput * fertilizerUnitCost
	               //  - 238.0
	                 //seed cost
	                 ;
	    	
	    }  else lastYearSoyMPerHaProfit = 0.359*4200+0.701*3071; //2010 price and average yield
	//    lastYearSoyMPerHaProfit = 0.359*4200+0.701*3071; 
	    if (soyCProduction > 0) {
	    	cPrice = soySoldToTraderAgent.getCommodityPrice(LandUse.SINGLESOY);
	    	profit+=cPrice*soyCProduction;
	    	csecondPrice = cottonSoldToTraderAgent.getCommodityPrice(LandUse.COTTON);
	    	profit+=csecondPrice*cottonSProduction;
	    	lastYearSoyCPerHaProfit = soyCPerHaYield*cPrice
	    			-getSoyPerHaFuelInput()*fuelUnitCost
	                 -soyPerHaFertilizerInput*fertilizerUnitCost 
	               //  - 121 
	                 //corn profit
	                 + cottonSPerHaYield*csecondPrice - getCottonPerHaFuelInput()*fuelUnitCost
	                 - cottonPerHaFertilizerInput * fertilizerUnitCost
	              //   - 134.25
	                 //seed cost
	                 ;
	    	
	    }   else lastYearSoyCPerHaProfit = 3.14*3000+0.701*3071; 
	    
	//    lastYearSoyCPerHaProfit = 3.14*3000+0.701*3071; 
		
	    if(cottonProduction > 0){
	    	cPrice = cottonSoldToTraderAgent.getCommodityPrice(LandUse.COTTON);
	    	profit+=cPrice*cottonProduction;
	    	lastYearCottonPerHaProfit = cottonPerHaYield*cPrice
	    			//- getCottonPerHaFuelInput()*fuelUnitCost
	    			//-cottonPerHaFertilizerInput*fertilizerUnitCost
	    			//-134.25
	    			;
	    } else lastYearCottonPerHaProfit = 3.14*3000;
	    
	//    lastYearCottonPerHaProfit = 3.14*3000;
	    
	    this.soySoldToTraderAgent.addSoyAmount(soyProduction+soyMProduction+soyCProduction);
	
	    this.soySoldToTraderAgent.purchaseCommodity((soyProduction+soyMProduction+soyCProduction)*
				soySoldToTraderAgent.getCommodityPrice(LandUse.SINGLESOY));
		
		this.cornSoldToTraderAgent.addCornAmount(cornProduction);
		this.cornSoldToTraderAgent.purchaseCommodity(cornProduction*
				cornSoldToTraderAgent.getCommodityPrice(LandUse.CORN));
		
		this.cottonSoldToTraderAgent.addCottonAmount(cottonSProduction+cottonProduction);
		this.cottonSoldToTraderAgent.purchaseCommodity((cottonSProduction+cottonProduction)*
				cottonSoldToTraderAgent.getCommodityPrice((LandUse.COTTON)));
		
		capital+=profit;
//	System.out.println("single soy="+lastYearSoyPerHaProfit+"// double soy = "+lastYearSoyCPerHaProfit+
	//		"  //cotton="+lastYearCottonPerHaProfit+" //soy Cotton="+lastYearSoyCPerHaProfit);
//	System.out.println("sending hhd capital="+capital);
	
	}
	
	public void landUseDecisionBeta(OrganicSpace organicSpace){
	   //how to combine two land uses at one cell for profit?
		LandUse highestLandUse=LandUse.SINGLESOY;
		
        int landUseNumber=0;
 //       List<Integer> listToChange = new ArrayList<Integer>();
//		List<Integer> listNotChange = new ArrayList<Integer>();
		
        planningSingleSoyCells.clear();
        planningSoyMaizeCells.clear();
        planningCottonCells.clear();
        planningSoyCottonCells.clear();
        
		for(int i =0; i< this.tenureCells.size(); i++){
			
		}
		
		
		double[] profit = new double[4];
		
		profit[0] = lastYearSoyPerHaProfit;   //single soy profit , 2
		profit[1] = lastYearSoyMPerHaProfit;   //soy+maize profit, 1
		profit[2] = lastYearSoyCPerHaProfit;   //soy+cotton profit, 9
		profit[3] = lastYearCottonPerHaProfit;  //single cotton profit, 3
		
		double highestProfit = profit[0];
		int count=0;
		for (int i = 0; i<4; i++){
		//	System.out.println(i+" = "+profit[i]);
			if (highestProfit<profit[i])
				{ highestProfit = profit[i];
		          count = i;
				}
	        }
		
		if(count == 0)  { highestLandUse = LandUse.SINGLESOY; landUseNumber = 2;}
		if(count == 1)  { highestLandUse = LandUse.DOUBLESOY; landUseNumber = 1;}
		if(count == 2)  { highestLandUse = LandUse.SOYCOTTON; landUseNumber = 9;}
		if(count == 3)  { highestLandUse = LandUse.COTTON; landUseNumber = 3;}
		
	//	highestLandUse = LandUse.COTTON;
		
	//	System.out.println("highest land use "+highestLandUse);
		
		 for (int i =0; i< this.tenureCells.size(); i++)
		 { 
			LandCell c = this.tenureCells.get(i);
			c.setLastLandUse(c.getLandUse());
		//	c.setLandUse(highestLandUse);
		//	organicSpace.setLandUse(landUseNumber, c.getXlocation(), c.getYlocation());
			if(highestLandUse == LandUse.SINGLESOY)
				planningSingleSoyCells.add(c);
			if(highestLandUse == LandUse.DOUBLESOY)
				planningSoyMaizeCells.add(c);
			if(highestLandUse == LandUse.COTTON)
				planningCottonCells.add(c);
			if(highestLandUse == LandUse.SOYCOTTON)
				planningSoyCottonCells.add(c);
			
		 }
		
		
	}
	
	
	public void landUseDecisionLogisticRegression(OrganicSpace organicSpace){
		
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		
		planningSingleSoyCells.clear();
        planningSoyMaizeCells.clear();
        planningCottonCells.clear();
        planningSoyCottonCells.clear();
        agriculturalCells.clear();
        		
		double[] profit = new double[4];
		
		profit[0] = lastYearSoyPerHaProfit;   //single soy profit , 2
		profit[1] = lastYearSoyMPerHaProfit;   //soy+maize profit, 1
		profit[2] = lastYearSoyCPerHaProfit;   //soy+cotton profit, 9
		profit[3] = lastYearCottonPerHaProfit;  //single cotton profit, 3
		
		
	/*	double[] coef_1= new double[22];
		double[] coef_2= new double[22];
		double[] coef_3= new double[22];
		double[] coef_4= new double[22];
		double[] coef_9= new double[22];
	
		        //title: sending logistic regression
		        // sheet4: forest ref
		        // base ref is forest
				//location: google drive
		
		       coef_1[0]=2.7; coef_1[1]=3.38;coef_1[2]=16.57;coef_1[3]=0.71;
		       coef_1[4]=1.4; coef_1[5]=-0.43;coef_1[6]=-0.23;coef_1[7]=-22.99;
		       coef_1[8]=0.14; coef_1[9]=-5.63;coef_1[10]=1.16;coef_1[11]=0.33;
		       coef_1[12]=21.33; coef_1[13]=1.52;
		       coef_1[14]=0.0005;coef_1[15]=0.0009;
		       
		       coef_1[16]=-0.0004; //-0.0004 is the original value
		     //  coef_1[16]=-0.0001;
		       coef_1[17]=0.0003;coef_1[18]=-0.0002;coef_1[19]=-0.0003;		       
		       coef_1[20]=-0.0006;coef_1[21]=-0.0009;   
		       
		       coef_1[16]+= tick*0.00003;
		       //add time scale
		       
		    	coef_2[0]=1.87; coef_2[1]=2.3;coef_2[2]=14.24;coef_2[3]=-0.31;
			 	coef_2[4]=-118.72; coef_2[5]=-0.06;coef_2[6]=-0.65;coef_2[7]=-22.75;
				coef_2[8]=0.35; coef_2[9]=-3.28;coef_2[10]=0.59;coef_2[11]=0.73;
				coef_2[12]=21.51; coef_2[13]=0.28;
				coef_2[14]=0.0001;coef_2[15]=0.0002;
				coef_2[16]=0.0003; coef_2[17]=0;coef_2[18]=-0.0005;
			//	coef_2[18]=-0.0002
				coef_2[18]+=tick*0.00002;
				
				coef_2[19]=-0.0002;
				coef_2[20]=-0.0005;coef_2[21]=-0.0005;
				    
				    
				coef_3[0]=6.07; coef_3[1]=2.74;coef_3[2]=-10.59;coef_3[3]=0.46;
				coef_3[4]=8.14; coef_3[5]=0.08;coef_3[6]=-7.01;coef_3[7]=-52.7;
				coef_3[8]=-4.59; coef_3[9]=-5.9;coef_3[10]=-0.28;coef_3[11]=0.15;
				coef_3[12]=26.76; coef_3[13]=1.21;coef_3[14]=0.0005;coef_3[15]=0.0009;
				coef_3[16]=-0.0003; coef_3[17]=0.0002;coef_3[18]=-0.0001;coef_3[19]=-0.0004;
				//coef_3[20]=-0.0007;
				coef_3[20]=-0.0008;
				coef_3[21]=-0.0011;
				    
				
				coef_4[0]=-10.01; coef_4[1]=-11.06;coef_4[2]=-13.01;coef_4[3]=-13.68;
				coef_4[4]=-7.51; coef_4[5]=-0.26;coef_4[6]=1.61;coef_4[7]=-31.23;
				coef_4[8]=0.39; coef_4[9]=1.85;coef_4[10]=-0.08;coef_4[11]=-0.7;
				coef_4[12]=22.91; coef_4[13]=-1.63;coef_4[14]=-0.0005;coef_4[15]=-0.0008;
				coef_4[16]=0.0003; coef_4[17]=-0.0003;coef_4[18]=0.0001;coef_4[19]=0.0002;
				coef_4[20]=0.0004;coef_4[21]=0.0002;
				
				coef_9[0]=6.13; coef_9[1]=6.46;coef_9[2]=12.83;coef_9[3]=4.82;
				coef_9[4]=4.99; coef_9[5]=3.6;coef_9[6]=3.73;coef_9[7]=-27.06;
				coef_9[8]=3.57; coef_9[9]=2.4;coef_9[10]=0.97;coef_9[11]=-0.75;
				coef_9[12]=-13.25; coef_9[13]=2.33;coef_9[14]=-0.0005;coef_9[15]=-0.0007;
				coef_9[16]=-0.0004; coef_9[17]=-0.0009;
				
				coef_9[17]+=tick*0.00008;
				
				coef_9[18]=0;coef_9[19]=-0.0001;
				coef_9[20]=-0.0001;coef_9[21]=-0.0003;*/
				
		double[] coef_1= new double[24];
		double[] coef_2= new double[24];
		double[] coef_3= new double[24];
		
		coef_1[0] = 0; coef_1[1] = 3.76; coef_1[2] = 4.18; coef_1[3] = 17.82;
		coef_1[4] =1.48; coef_1[5] = 5.51; coef_1[6] = 0.04; coef_1[7] = 0.17;
		coef_1[8] = -2.79; coef_1[9]=0.24;coef_1[10]=-4.486;coef_1[11]=1.025;
		coef_1[12]=0.331;coef_1[13]=0.627;coef_1[14]=0.00044;coef_1[15]=0.00079;coef_1[16]=-0.00041;
		coef_1[17]=-0.00026;coef_1[18]=-0.00063;coef_1[19]=-0.00115;coef_1[20]=-0.00001;
		coef_1[21] = 0;coef_1[22]=0.0095;coef_1[23]=-0.0417;
		
		coef_2[0] = 0; coef_2[1] = 2.53; coef_2[2] = 2.88; coef_2[3] = 16.52;
		coef_2[4] =0.27; coef_2[5] = -35.62; coef_2[6] = -0.02; coef_2[7] = -0.59;
		coef_2[8] = -3.07; coef_2[9]=0.24;coef_2[10]=-3.869;coef_2[11]=0.564;
		coef_2[12]=0.779;coef_2[13]=0.515;coef_2[14]=0.00018;coef_2[15]=0.00028;coef_2[16]=-0.00022;
		coef_2[17]=-0.00011;coef_2[18]=-0.00028;coef_2[19]=-0.00050;coef_2[20]=0;
		coef_2[21] = 0;coef_2[22]=0.001;coef_2[23]=-0.034;
		
		coef_3[0] = 0; coef_3[1] = -0.004; coef_3[2] = -1.010; coef_3[3] = 10.324;
		coef_3[4] =-1.857; coef_3[5] = 1.919; coef_3[6] = 7.107; coef_3[7] = 5.842;
		coef_3[8] = -15.612; coef_3[9]=6.966;coef_3[10]=1.533;coef_3[11]=1.270;
		coef_3[12]=0.001;coef_3[13]=1.992;coef_3[14]=0.00079;coef_3[15]=0.00136;coef_3[16]=0.00020;
		coef_3[17]=-0.00029;coef_3[18]=-0.00072;coef_3[19]=-0.00132;coef_3[20]=0;
		coef_3[21] = 0;coef_3[22]=-0.046;coef_3[23]=0.663;
				
				double soyPriceDelta = 0;
				
			
				
	    for(int i =0; i< this.tenureCells.size(); i++){
				
	 //     for (int i = 0; i < this.agriculturalCells.size(); i++)    {
	    	double[] exp= new double[4];
			double[] probability = new double[4];
	    	
	    	int[] landcover = new int[10];
	    	
	    	for(int j=0;j<10;j++)
	    		landcover[j]=0;
	    		    	
	    	LandCell c = this.tenureCells.get(i);
	    	c.setChangedThisTime(false);
	 //       System.out.println(c.getElevation()+" :?"+c.getDisToUrban()+"?? "+c.getDisToRoad()+"~~"+c.getSlope());
	    	boolean nextToCorn = false;
	    	boolean nextToCotton = false;
	//    	for(int r = 0; r<this.agriculturalCells.size();r++)	{
	  //  	LandCell c = this.getAgriculturalCells().get(i);
	    	
	    	// 1=doublesoy 
	//    	if(c.getSCCount()>0) {System.out.println("should grow sc "+c.getSCCount());}
	    	for(GridCell<LandCell> cell:c.nghCell) 
        	{
		         
        		int landuseNumber = organicSpace.getLandUseAt(cell.getPoint().getX(), cell.getPoint().getY());
        		if(landuseNumber==1)
        		{
        			nextToCorn = true;
        //			System.out.println("next to soy");
        		}
        		if(landuseNumber==3||landuseNumber==9){
        			nextToCotton = true;
        		}
        	}
	    	
	    	if(c.getLastLandUse()==LandUse.DOUBLESOY) //1
	    		landcover[0] = 1; else landcover[0]=0;
	    	if(c.getLastLandUse()==LandUse.SINGLESOY)  //2
	    		landcover[1] = 1; else landcover[1]=0;
	    	if(c.getLastLandUse()==LandUse.COTTON) //3
	    		landcover[2] = 1; else landcover[2]=0;
	    	if(c.getLastLandUse()==LandUse.GRASSLAND)  //4
	    		landcover[3] = 1;else landcover[3]=0;
	    	if(c.getLastLandUse()==LandUse.SOYCOTTON)   //9
	    		landcover[4] = 1; else landcover[4]=0;
	    	
	    	
	    	if(c.getLastLastLandUse()==LandUse.DOUBLESOY) //1
	    		landcover[5] = 1; else landcover[5]=0;
	    	if(c.getLastLastLandUse()==LandUse.SINGLESOY)  //2
	    		landcover[6] = 1; else landcover[6]=0;
	    	if(c.getLastLastLandUse()==LandUse.COTTON) //3
	    		landcover[7] = 1; else landcover[7]=0;
	    	if(c.getLastLastLandUse()==LandUse.GRASSLAND)  //4
	    		landcover[8] = 1; else landcover[8]=0;
	    	if(c.getLastLastLandUse()==LandUse.SOYCOTTON)   //9
	    		landcover[9] = 1; else landcover[9]=0;
	    	   		    	
	    	exp[1] = coef_1[0]+
	    			coef_1[1]*landcover[0] +
	    			coef_1[2]*landcover[1] +
	    			coef_1[3]*landcover[2] +
	    			coef_1[4]*landcover[3] +
	    			coef_1[5]*landcover[4] +
	    			coef_1[6]*landcover[5] +
	    			coef_1[7]*landcover[6] +
	    			coef_1[8]*landcover[7] +
	    			coef_1[9]*landcover[8] +
	    			coef_1[10]*landcover[9] +
	    			coef_1[11]*c.getSSCount() +
	    			coef_1[12]*c.getDSCount() +
	    			coef_1[13]*(c.getCCount()+c.getSCCount()) +
	    			coef_1[14]*lastYearSoyPerHaProfit +
	    			coef_1[15]*lastYearSoyMPerHaProfit +
	    			coef_1[16]*lastYearSoyCPerHaProfit +
	    			coef_1[17]*lastYearSoyPerHaCost +
	    			coef_1[18]*lastYearSoyMPerHaCost + 
	    			coef_1[19]*lastYearSoyCPerHaCost +
	    			coef_1[20]* c.getDisToUrban() +
	    			coef_1[21]*c.getDisToRoad() +
	    			coef_1[22]* c.getElevation()+
	    			coef_1[23] * c.getSlope();	
	    	
	    	
	    	exp[2] =  coef_2[0]+
	    			coef_2[1]*landcover[0] +
	    			coef_2[2]*landcover[1] +
	    			coef_2[3]*landcover[2] +
	    			coef_2[4]*landcover[3] +
	    			coef_2[5]*landcover[4] +
	    			coef_2[6]*landcover[5] +
	    			coef_2[7]*landcover[6] +
	    			coef_2[8]*landcover[7] +
	    			coef_2[9]*landcover[8] +
	    			coef_2[10]*landcover[9] +
	    			coef_2[11]*c.getSSCount() +
	    			coef_2[12]*c.getDSCount() +
	    			coef_2[13]*(c.getCCount()+c.getSCCount()) +
	    			coef_2[14]*lastYearSoyPerHaProfit +
	    			coef_2[15]*lastYearSoyMPerHaProfit +
	    			coef_2[16]*lastYearSoyCPerHaProfit +
	    			coef_2[17]*lastYearSoyPerHaCost +
	    			coef_2[18]*lastYearSoyMPerHaCost + 
	    			coef_2[19]*lastYearSoyCPerHaCost +
	    			coef_2[20]* c.getDisToUrban() +
	    			coef_2[21]*c.getDisToRoad() +
	    			coef_2[22]* c.getElevation()+
	    			coef_2[23] * c.getSlope();	
	    	
	    	exp[3] =  coef_3[0]+
	    			coef_3[1]*landcover[0] +
	    			coef_3[2]*landcover[1] +
	    			coef_3[3]*landcover[2] +
	    			coef_3[4]*landcover[3] +
	    			coef_3[5]*landcover[4] +
	    			coef_3[6]*landcover[5] +
	    			coef_3[7]*landcover[6] +
	    			coef_3[8]*landcover[7] +
	    			coef_3[9]*landcover[8] +
	    			coef_3[10]*landcover[9] +
	    			coef_3[11]*c.getSSCount() +
	    			coef_3[12]*c.getDSCount() +
	    			coef_3[13]*(c.getCCount()+c.getSCCount()) +
	    			coef_3[14]*lastYearSoyPerHaProfit +
	    			coef_3[15]*lastYearSoyMPerHaProfit +
	    			coef_3[16]*lastYearSoyCPerHaProfit +
	    			coef_3[17]*lastYearSoyPerHaCost +
	    			coef_3[18]*lastYearSoyMPerHaCost + 
	    			coef_3[19]*lastYearSoyCPerHaCost +
	    			coef_3[20]* c.getDisToUrban() +
	    			coef_3[21]*c.getDisToRoad() +
	    			coef_3[22]* c.getElevation()+
	    			coef_3[23] * c.getSlope();			 
		
	    	    	
	    	double expSum= Math.exp(exp[1])+
	    			       Math.exp(exp[2])+
	    			       Math.exp(exp[3])+
	    		//	       Math.exp(exp[4])+
	    			//       Math.exp(exp[5])+
	    			       1;
			probability[0]=1/expSum;
			probability[1]=Math.exp(exp[1])/expSum;  //land use 1=double soy
			probability[2]=Math.exp(exp[2])/expSum;  //land use 2=single soy
			probability[3]=Math.exp(exp[3])/expSum;  //land use 3 = soy cotton
		//	probability[4]=Math.exp(exp[4])/expSum;
		//	probability[5]=Math.exp(exp[5])/expSum;
			
	//     if(soyPriceDelta/soyPrices.get(soyPrices.size()-1)<-0.05 )
		   soyPriceDelta = getPriceDelta(LandUse.SINGLESOY)	;
		
		/*	 if(soyPriceDelta < 0 )
		        {   
	    	           probability[1]-=0.025;
		          //     probability[4]+=0.1;
	    	           probability[2]-=0.05;
	    	      //     probability[5]-=0.05;
	    	        
	    	           probability[3]-=0.025;
	    	           probability[0]+=0.1;
		        }		 */
		 
		  if ( this.proDiversifying) {
		
			  probability[2]-=0.05*(tick-1);
			  //pro soy cotton;
		  }
	//	  System.out.println(probability[1]+"really..."+probability[2]);
	//	  probability[0] = probability[0] - (tick-1)*0.05;
		 if(tick==1)
		  {
		//     probability[0] = probability[0] *Math.pow(0.7, tick-1);	   
		//     probability[2] = probability[2] *Math.pow(0.9, tick-1);
		//     probability[1] = probability[1] * Math.pow(1.10,tick);
			  probability[2] = probability[2] + 0.2;
		  }
		 else {
		//	if(tick<11) 
			{
				  probability[2] = probability[2] - (tick-1)*0.02;
				  probability[0] = probability[0] - (tick-1)*0.05;
				  probability[1] = probability[1] + (tick-1)* 0.02;
				  }
			
				
			 
		 }
     //     probability[2] =0;
	//	if(tick>11)
	//	 System.out.println("now after "+probability[1]+"really..."+probability[2]);		
		
			double max = 0;
			double maxProb = probability[0] ;
		//	double second = 0;
		//	double secondProb = 0;
			
		/*		
			for (int k=1;k<probability.length;k++) 
			{
				
				if(probability[k]>maxProb) {
                    secondProb = maxProb;
                    second = max;
					maxProb = probability[k];
					max = k;
				}	else if(probability[k]>secondProb){
					  secondProb = probability[k];
					  second = k;
				}
			}*/
			
           for(int k=0;k<probability.length;k++){
        	   if(probability[k]>maxProb) {
        		   max=k;
        		   maxProb = probability[k];
        	   } 
           }
    //       max=1;
	//	if(tick>5&&max==2)
	//	System.out.println("max "+max);
	//		System.out.println("second "+second);
	//	max = 0;
	//	second =4;
			
	   if(max==0) {
		   if(c.getLandUse()==LandUse.FOREST)
		   {   c.setLastLastLandUse(c.getLastLandUse());
	           c.setLastLandUse(c.getLandUse());
               c.setLandUse(LandUse.FOREST);
	           organicSpace.setLandUse(5, c.getXlocation(), c.getYlocation());
	           c.setChangedThisTime(true);
	    //       agriculturalCells.remove(c);
	           }
		   else 
		   {
			   c.setLastLastLandUse(c.getLastLandUse());
			      c.setLastLandUse(c.getLandUse());
                  c.setLandUse(LandUse.GRASSLAND);
			      organicSpace.setLandUse(4, c.getXlocation(), c.getYlocation());
			      c.setChangedThisTime(true); 
		//	      agriculturalCells.remove(c);
		   } 
		   		   
	   }
	   
	    if(max==1 ) 
	    {
	    	
	    	if(tick==1)
	    	{
		
			   if(c.getLandUse()==LandUse.FOREST)
			   {   c.setLastLastLandUse(c.getLastLandUse());
		           c.setLastLandUse(c.getLandUse());
	               c.setLandUse(LandUse.FOREST);
		           organicSpace.setLandUse(5, c.getXlocation(), c.getYlocation());
		           c.setChangedThisTime(true);
		    //       agriculturalCells.remove(c);
		           }
			   else if(c.getLandUse()==LandUse.GRASSLAND){
				   c.setLastLastLandUse(c.getLastLandUse());
				      c.setLastLandUse(c.getLandUse());
	                  c.setLandUse(LandUse.GRASSLAND);
				      organicSpace.setLandUse(4, c.getXlocation(), c.getYlocation());
				      c.setChangedThisTime(true); 
			   }
			   else {
				    if(nextToCorn||c.getLandUse()==LandUse.DOUBLESOY)
			       planningSoyMaizeCells.add(c);
			        else 
				   planningSingleSoyCells.add(c);
				 
				    agriculturalCells.add(c);
			   }
				   
		   }
	    	
	    	else
		      {  
			      if(nextToCorn||c.getLandUse()==LandUse.DOUBLESOY)
			       planningSoyMaizeCells.add(c);
			      else if(c.getDSCount()>c.getSSCount())
				   planningSoyMaizeCells.add(c);
			      else
			    	  planningSingleSoyCells.add(c);
			   
			      agriculturalCells.add(c);	 
		      }
		   
		   
	   }
		
	  
		   if(max==2) 
		    { 	
			   planningSingleSoyCells.add(c);
				   agriculturalCells.add(c);
		     }
		   
	   if(max==3) {
		   planningSoyCottonCells.add(c);
		   agriculturalCells.add(c);
	   }
	   
		/*
		 * if(agriculturalCells.contains(c))  //if c was an agricultural cells already;
		   {	
	    		if (max == 0)
	    		{   
	    			if(tick<11) //was tick<7
	    		  {
				     if (second == 1) 
				     {  // if(RandomHelper.nextDoubleFromTo(0.0, 1.0)<0.8)
				   // 	if( c.getSSCount()>c.getDSCount() )
				    	if(nextToCorn)
				    //	 if(c.getLastLandUse())
				    		 planningSoyMaizeCells.add(c);
				         else 
				        	 planningSoyCells.add(c);
				    	
				     }
   			         if (second == 2)
   			        	  planningSoyCells.add(c);
   			         if (second == 3)
   			        		 //&& nextToCotton)
   			        	  planningSoyCottonCells.add(c);    
	    			
	    			if(c.getLastLandUse()==LandUse.FOREST)
	    			{
	    				c.setLastLastLandUse(c.getLastLandUse());
	 			        c.setLastLandUse(c.getLandUse());
	                    c.setLandUse(LandUse.FOREST);
	 			        organicSpace.setLandUse(5, c.getXlocation(), c.getYlocation());
	 			        c.setChangedThisTime(true);
	 			        agriculturalCells.remove(c);
	 			//        System.out.println("changed back to");
	    			}
	    			else {
	    				 c.setLastLastLandUse(c.getLastLandUse());
       			      c.setLastLandUse(c.getLandUse());
                         c.setLandUse(LandUse.GRASSLAND);
       			      organicSpace.setLandUse(4, c.getXlocation(), c.getYlocation());
       			      c.setChangedThisTime(true); 
       			      agriculturalCells.remove(c);
       		//	   System.out.println("removed an agricultural cell");
    			     
	    			}
	    		    } 
	    		
	    		else { //this else is for tick <6
	    		    	 if (second == 1 && c.getDSCount()>0) 
	    		    	       // {   if(nextToCorn)
	    		    	  	          planningSoyMaizeCells.add(c);
	    		    	 else  if (second == 2)
	    	   			        	  planningSoyCells.add(c);
	    		    	 else 
	    	   		//			   && nextToCotton)
	    	   				          planningSoyCottonCells.add(c);
	    	   			 
	    		    }
	    		
			      }
			    else //this else is for when max for agricultural 
			    {
			    	if(tick==1)  //was tick<7
			    	{  
			        	if(max == 1 && c.getDSCount()>0) 
			        			//&& nextToCorn)			        
			        			planningSoyMaizeCells.add(c);
				
				       if(max == 2)
				        	planningSoyCells.add(c);
			        	if(max ==3 )
			        	//		&& nextToCotton)
				        	planningSoyCottonCells.add(c);
			        	}
			    	else {
			    		if(max==1)
			    			planningSoyMaizeCells.add(c);
			    		if(max==2)
			    			planningSoyCells.add(c);
			    		if(max ==3)
				        	planningSoyCottonCells.add(c);
			    	}
				    
			    }
		}
		else {  //if c was NOT an agricultural cells ;
        	if(tick < 4)
          {  //before Soy Moratorium
        	  if (max == 0 ) 
        	  {
        		  if(c.getLandUse()==LandUse.FOREST) 
        		  {
        		    c.setLastLastLandUse(c.getLastLandUse());
 			        c.setLastLandUse(c.getLandUse());
                    c.setLandUse(LandUse.FOREST);
 			        organicSpace.setLandUse(5, c.getXlocation(), c.getYlocation());
 			        c.setChangedThisTime(true);
        	  
        		  }
        	      else 
        	      {        			  
        				  c.setLastLastLandUse(c.getLastLandUse());
           			      c.setLastLandUse(c.getLandUse());
                             c.setLandUse(LandUse.GRASSLAND);
           			      organicSpace.setLandUse(4, c.getXlocation(), c.getYlocation());
           			      c.setChangedThisTime(true); 
        			  //}
        	        }
        	  } //finish if max==0
                   	  
        	  if(max == 1)
        		  planningSoyMaizeCells.add(c);
        	  if(max == 2)
        		  planningSoyCells.add(c);
        	  if(max == 3)
        		  planningSoyCottonCells.add(c);
        	    	  
          }
          else {  //tick>3
        	  if(c.getLastLandUse()==LandUse.FOREST ) {
        		  c.setLastLastLandUse(c.getLastLandUse());
 			      c.setLastLandUse(c.getLandUse());
                  c.setLandUse(LandUse.FOREST);
 			      organicSpace.setLandUse(5, c.getXlocation(), c.getYlocation());
 			     c.setChangedThisTime(true);
      
        	  }
        	  else {
        		  if( max ==0 ) 
        		  {
            		  //soyMoratorium
            		  c.setLastLastLandUse(c.getLastLandUse());
     			      c.setLastLandUse(c.getLandUse());
                      c.setLandUse(LandUse.GRASSLAND);
     			      organicSpace.setLandUse(4, c.getXlocation(), c.getYlocation());
     			      c.setChangedThisTime(true);
            	   }
        		  else {
        			  if(max == 1)
                		  planningSoyMaizeCells.add(c);
                	  if(max == 2)
                		  planningSoyCells.add(c);
                	  if(max == 3)
                		  planningSoyCottonCells.add(c);
        		  }
        	  }
          }
	    
	}*/
	    
	    }
	//   if(tick>8)
	//		System.out.println("count "+planningSoyCells.size()+"?? 1: "+planningSoyMaizeCells.size());
	}
	
	public void landUseDecisionCelluar(OrganicSpace organicSpace){
		
	}
	
	
	public double getLastYearSoyPerHaCost() {
		return lastYearSoyPerHaCost;
	}

	public void setLastYearSoyPerHaCost(double lastYearSoyPerHaCost) {
		this.lastYearSoyPerHaCost = lastYearSoyPerHaCost;
	}

	public double getLastYearSoyMPerHaCost() {
		return lastYearSoyMPerHaCost;
	}

	public void setLastYearSoyMPerHaCost(double lastYearSoyMPerHaCost) {
		this.lastYearSoyMPerHaCost = lastYearSoyMPerHaCost;
	}

	public double getLastYearSoyCPerHaCost() {
		return lastYearSoyCPerHaCost;
	}

	public void setLastYearSoyCPerHaCost(double lastYearSoyCPerHaCost) {
		this.lastYearSoyCPerHaCost = lastYearSoyCPerHaCost;
	}

	public double getLastYearCottonPerHaCost() {
		return lastYearCottonPerHaCost;
	}

	public void setLastYearCottonPerHaCost(double lastYearCottonPerHaCost) {
		this.lastYearCottonPerHaCost = lastYearCottonPerHaCost;
	}
   
	public int getSingleSoySize(){
	//for data collection at accounting stage
		return singleSoyCells.size();
	}
	public int getSoyMaizeSize(){
		return soyMaizeCells.size();
	}
	public int getCottonSize(){
		return cottonCells.size();
	}
	public int getSoyCottonSize(){
		return soyCottonCells.size();
	}
	
	public double getPriceDelta(LandUse commodity){
		double cPrice = 0;
		double priceDelta = 0;
		double soyPriceDelta = 0;
		double cornPriceDelta = 0;
		double cottonPriceDelta = 0;
		if(commodity == LandUse.SINGLESOY)
		{ 
			cPrice = soySoldToTraderAgent.getCommodityPrice(commodity);	
	    	soyPrices.add(cPrice);
		   if (soyPrices.size()>priceMemoryLimit) 
		     {
					soyPrices.remove(0); //remove least recent price	   
		     }
   
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
	//	System.out.println("to test tick "+tick);
			priceDelta = soyPriceDelta;
       	}
		if(commodity == LandUse.CORN) {
			cPrice = cornSoldToTraderAgent.getCommodityPrice(commodity);	
	    	cornPrices.add(cPrice);
		   if (cornPrices.size()>priceMemoryLimit) 
		     {
					cornPrices.remove(0); //remove least recent price	   
		     }
   
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
		
		if(commodity == LandUse.COTTON) {
			cPrice = cottonSoldToTraderAgent.getCommodityPrice(commodity);	
	    	cottonPrices.add(cPrice);
		   if (cottonPrices.size()>priceMemoryLimit) 
		     {
			   cottonPrices.remove(0); //remove least recent price	   
		     }
   
		   if(cottonPrices.size()==1){
	        	cottonPriceDelta = cottonPrices.get(0).doubleValue();
	   // 	cornPriceDelta = cornPrices.get(0).doubleValue();
	 //   	ricePriceDelta = ricePrices.get(0).doubleValue();
	        } else if(cottonPrices.size()==2){
	            	cottonPriceDelta = cottonPrices.get(1).doubleValue() - 
	            			cottonPrices.get(0).doubleValue();
	   // 	cornPriceDelta = cornPrices.get(1).doubleValue() - 
	  //  			cornPrices.get(0).doubleValue();
	  //  	ricePriceDelta = ricePrices.get(1).doubleValue()-
	  //  			ricePrices.get(0).doubleValue();
	         } else{
	        	 cottonPriceDelta = cottonPrices.get(2).doubleValue()-
	            			cottonPrices.get(1).doubleValue();
	            	}
		   
		   priceDelta = cottonPriceDelta;
		}
		
		return priceDelta;
	}
	
	   public double getSoyPrice() {
       	return soySoldToTraderAgent.getCommodityPrice(LandUse.SINGLESOY);
       }

	
}
