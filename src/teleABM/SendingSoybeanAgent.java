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
	
	protected  FastTable<LandCell> planningSoyCells = new FastTable<LandCell>();
	protected  FastTable<LandCell> planningSoyMaizeCells = new FastTable<LandCell>();
	protected FastTable<LandCell> planningCottonCells = new FastTable<LandCell>();
	protected  FastTable<LandCell> planningSoyCottonCells = new FastTable<LandCell>();	
	//these are to record all planning cells
	
	protected double soyMProduction;	
	//this is to record the soy production when it's followed by corn
	protected double soyCProduction;
	//this is to record the soy production when it's followed by cotton
	protected double cottonSProduction;
	
	protected double soyMPerHaYield;
	protected double soyCPerHaYield;
	protected double cottonSPerHaYield;
	
	protected double lastYearSoyCPerHaProfit;
	protected double lastYearSoyMPerHaProfit;
//	protected double lastYearCottonSPerHaProfit;
	
	protected double lastYearSoyPerHaCost;
	protected double lastYearSoyMPerHaCost;
	protected double lastYearSoyCPerHaCost;
	protected double lastYearCottonPerHaCost;
	
	
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
		this.setCornPerHaFuelInput(97.1);
		
		double random = RandomHelper.nextDoubleFromTo(0, 1);
		
		if (random > 0.5) {
			this.proDiversifying = true;
		} else this.proDiversifying = false;
//		System.out.println(random+" this farmer is prof diversifying: "+ proDiversifying);
		
		
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
				soyPrice=traderAgents.get(i).getCommodityPrice(LandUse.SOY);
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
		
		planningSoyCells.clear();
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
				planningSoyCells.add(c);
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
						planningSoyCells.add(c);
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
				 planningSoyCells.add(c);				
				
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
		   this.soyCells.clear();
	//	   this.cornCells.clear();
		   this.cottonCells.clear();
		   this.soyMaizeCells.clear();
		   this.soyCottonCells.clear();
		   
		   this.cornProduction = 0;
		   this.soyProduction = 0;
		   this.soyMProduction = 0;
		   this.soyCProduction = 0;
		   this.cottonProduction = 0;
		   this.cottonSProduction = 0;
		   
		   
		   for(int i =0;i< this.tenureCells.size();i++) {
			    LandCell c =   this.getTenureCells().get(i);
		      
			    c.transition();
			
			 //   if (c.getLastLandUse()==LandUse.RICE) {
			    if (c.getLandUse()==LandUse.SINGLESOY) { 	
			    	this.soyCells.add(c);
			    	soyProduction+=c.getSoyYield();
			    
			//    	System.out.println("single soy = "+soyProduction);
			    } else if (c.getLandUse()==LandUse.DOUBLESOY){
			//    	this.soyCells.add(c);
			        this.soyMaizeCells.add(c);
			    	soyMProduction+=c.getSoyYield();			    	
			//    	this.cornCells.add(c);
			    	cornProduction+=c.getCornYield();
			//    	System.out.println("soy="+c.getSoyYield()+" corn="+c.getCornYield());
			    } else if(c.getLandUse()==LandUse.SOYCOTTON){
			//    	this.soyCells.add(c);
			//    	this.cottonCells.add(c);
			    	this.soyCottonCells.add(c);
			    	
			    	soyCProduction+=c.getSoyYield();
			    	cottonSProduction+=c.getCottonYield();
			    }  else if(c.getLandUse()==LandUse.COTTON) {  //COTTON
			    	this.cottonCells.add(c);
			    	cottonProduction+=c.getCottonYield();
		    	
			    } else {  //others, right now don't record.
			    
			    }
			    
			
			   if(soyCells.size() > 0) { 
			    soyPerHaYield = soyProduction/soyCells.size();
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
			this.setSoyProduction(soyProduction);
			this.setCornProduction(cornProduction);
			this.setCottonProduction(cottonProduction);
			

	}

	@Override
	public void updateLandUse(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
	 boolean nextToAgricultural = false;
	 int countChange = 0;	
	 
	// SimUtilities.shuffle(this.tenureCells, RandomHelper.getUniform());
	//   SimUtilities.shuffle(this.planningSoyCells, RandomHelper.getUniform());
	//   this.planningSoyCells.sort();
	   
	   Collections.sort(this.tenureCells, new SortbyRoll());
	   
	
	    for(LandCell c:this.tenureCells) {
			     
	        	for(GridCell<LandCell> cell:c.nghCell) 
	        	{
			    
	        		int landuseNumber = organicSpace.getLandUseAt(cell.getPoint().getX(), cell.getPoint().getY());
		        
			         if(landuseNumber == 1||landuseNumber ==2)
			           { 
				            nextToAgricultural = true;
			            	break;
			        	}
			         else if(landuseNumber == 3||landuseNumber ==5) 
			         {
			            	nextToAgricultural = true;
				            break;
			         } else 
			        	 nextToAgricultural = false;		
		
		          }
		
		    if(agriculturalCells.contains(c))
		     {
		    	  
		    	  if(planningSoyCells.contains(c))
		    	  {    		  
		    			//	c.setLastLandUse(c.getLandUse());
		    			c.setLandUse(LandUse.SINGLESOY);
		    			organicSpace.setLandUse(2, c.getXlocation(), c.getYlocation());
		    			c.setFertilizerInput(LandUse.SINGLESOY);
		    			c.setFuelInput((soyPerHaFuelInput/10000.0)
		    							*(cellsizeSending*cellsizeSending));
		    					//the soy per ha fuel is in initialization function
		    				  //	c.setWaterRequirement(LandUse.SOY);		
		    			capital-= (c.getFertilizerInput()*fertilizerUnitCost +
		    							  c.getFuelInput()*fuelUnitCost)/0.34;
		    	 
		          } else
		    	  
		    	  if(planningSoyMaizeCells.contains(c))
		    	  {
		    			c.setLandUse(LandUse.DOUBLESOY);
		    			organicSpace.setLandUse(1, c.getXlocation(), c.getYlocation());
		    			c.setFertilizerInput(LandUse.DOUBLESOY);
		    			c.setFuelInput(((soyPerHaFuelInput+cornPerHaFuelInput)/10000.0)
		    					*(cellsizeSending*cellsizeSending));
		    		
		    			capital-= (c.getFertilizerInput()*fertilizerUnitCost +
		    					  c.getFuelInput()*fuelUnitCost)/0.36;
		    	  }
		    	  
		    	  else if(planningCottonCells.contains(c))
		    	  {
		    		  c.setLandUse(LandUse.COTTON);
		  			  organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
		  			  c.setFertilizerInput(LandUse.COTTON);
		  			  c.setFuelInput((cottonPerHaFuelInput/10000.0)
		  					  *(cellsizeSending*cellsizeSending));
		  			//the soy and corn per ha fuel is in initialization function
		  		//	c.setWaterRequirement(LandUse.SOY);		
		  			  capital-= (c.getFertilizerInput()*fertilizerUnitCost +
		  					  c.getFuelInput()*fuelUnitCost)/0.28;
		    	  }
		    	  
		    	  else  if(planningSoyCottonCells.contains(c))
		    	  {
		    		  c.setLandUse(LandUse.SOYCOTTON);
		  		      organicSpace.setLandUse(9, c.getXlocation(), c.getYlocation());
		  			  c.setFertilizerInput(LandUse.SOYCOTTON);
		  			  c.setFuelInput(((soyPerHaFuelInput+cottonPerHaFuelInput)/10000.0)
		  					      *(cellsizeSending*cellsizeSending));
		  			  capital-= (c.getFertilizerInput()*fertilizerUnitCost +
		  					   c.getFuelInput()*fuelUnitCost) /0.30; 
		    	  }
		    	  else {
		    		  c.setLandUse(LandUse.GRASSLAND);
		    		  agriculturalCells.remove(c);
		    	  }
		    	  
		     } //else is when c does not belong to agricultural cells
		    else 
		    {	
		    	if((double) countChange/this.agriculturalCells.size()<0.1) 
		    		
		    	   if( nextToAgricultural && 
		    			   c.getSuitability() > 0)
		    	   {   
		    		   if(planningSoyCells.contains(c) && capital > 0)
				    	  {    		  
				    			//	c.setLastLandUse(c.getLandUse());
				    			c.setLandUse(LandUse.SINGLESOY);
				    			organicSpace.setLandUse(2, c.getXlocation(), c.getYlocation());
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
				  			  c.setFertilizerInput(LandUse.SOYCOTTON);
				  			  c.setFuelInput(((soyPerHaFuelInput+cottonPerHaFuelInput)/10000.0)
				  					      *(cellsizeSending*cellsizeSending));
				  			  capital-= (c.getFertilizerInput()*fertilizerUnitCost +
				  					   c.getFuelInput()*fuelUnitCost) /0.30; 
				  	          countChange++;
			    			  agriculturalCells.add(c);
				    	  }
		    	   }
		    	   else
		    	   {
		    		   if (c.getLastLandUse()==LandUse.FOREST)
		    		   {
		    			   c.setLastLastLandUse(c.getLastLandUse());
			    		   c.setLastLandUse(c.getLandUse());
			    		   c.setLandUse(LandUse.FOREST);
			    		   organicSpace.setLandUse(5,c.getXlocation(), c.getYlocation());
		    		   }
		    		   else 
		    		   {
		    			   c.setLastLastLandUse(c.getLastLandUse());
			    		   c.setLastLandUse(c.getLandUse());
			    		   c.setLandUse(LandUse.GRASSLAND);
			    		   organicSpace.setLandUse(4,c.getXlocation(), c.getYlocation());
		    		   }
		    		  
		    		   
		    	   }
		    		
		    }
		
	
		}
	    if(capital < 0) System.out.println("capital of sending "+capital);
	}

	@Override
	public void updateCost(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
		double cost = 0;
		double totalCost = 0;
		double totalfertilizeruse=0;
		
		if(soyCells.size()>0){
		for (int i=0; i<soyCells.size();i++){
			//setFertilizerInput() has already been put at land use decision ();
			cost+=soyCells.get(i).getFertilizerInput()*fertilizerUnitCost; 
			cost+=soyCells.get(i).getFuelInput()*fuelUnitCost;
			totalfertilizeruse+= soyCells.get(i).getFertilizerInput();
		//	System.out.println("cell no. "+i+" use fertilizer "+soyCells.get(i).getFertilizerInput());
		}
		cost/=soyCells.size();
		cost/=0.34;  
		//this is to convert the above calculated fertilizer and fuel cost to total cost, 
		//based on empirical proportion
		//title: sinop soybean price and production, sheet 2, 
		//location: google drive spreadsheet
		cost/=(cellsizeSending*cellsizeSending)/10000.0;
		setLastYearSoyPerHaCost(cost);
		totalCost+=cost;
		soyPerHaFertilizerInput=totalfertilizeruse/soyCells.size();    					
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
	    	cPrice = soySoldToTraderAgent.getCommodityPrice(LandUse.SOY);
	    	profit+= cPrice*soyProduction;
	    	lastYearSoyPerHaProfit = soyPerHaYield*cPrice-getSoyPerHaFuelInput()*fuelUnitCost
	       //          -soyPerHaFertilizerInput*fertilizerUnitCost 
	        //         - 121 //seed cost
	                 ;
	    } else lastYearSoyPerHaProfit = 0.701*3071; //2010 price and average yield
	    
	    if (soyMProduction > 0) {
	    //	cPrice = this.getCommodityPrice(LandUse.SINGLESOY);
	    	cPrice = soySoldToTraderAgent.getCommodityPrice(LandUse.SOY);
	    	profit+=cPrice*soyMProduction;
	    //	profit+= this.getCommodityPrice(LandUse.CORN)
	    	csecondPrice = cornSoldToTraderAgent.getCommodityPrice(LandUse.CORN);
	    	profit+= csecondPrice
	    			 *
	    			cornProduction;
	    	lastYearSoyMPerHaProfit = soyMPerHaYield*cPrice
	    			//-getSoyPerHaFuelInput()*fuelUnitCost
	               //  -soyPerHaFertilizerInput*fertilizerUnitCost 
	               //  - 121 
	                 //corn profit
	                 + cornPerHaYield*csecondPrice - getCornPerHaFuelInput()*fuelUnitCost
	               //  - cornPerHaFertilizerInput * fertilizerUnitCost
	               //  - 238.0
	                 //seed cost
	                 ;
	    	
	    }  else lastYearSoyMPerHaProfit = 0.359*4200+0.701*3071; //2010 price and average yield
		
	    if (soyCProduction > 0) {
	    	cPrice = soySoldToTraderAgent.getCommodityPrice(LandUse.SOY);
	    	profit+=cPrice*soyCProduction;
	    	csecondPrice = cottonSoldToTraderAgent.getCommodityPrice(LandUse.COTTON);
	    	profit+=csecondPrice*cottonSProduction;
	    	lastYearSoyCPerHaProfit = soyCPerHaYield*cPrice
	    			//-getSoyPerHaFuelInput()*fuelUnitCost
	               //  -soyPerHaFertilizerInput*fertilizerUnitCost 
	               //  - 121 
	                 //corn profit
	                 + cottonSPerHaYield*csecondPrice - getCottonPerHaFuelInput()*fuelUnitCost
	             //    - cottonPerHaFertilizerInput * fertilizerUnitCost
	              //   - 134.25
	                 //seed cost
	                 ;
	    	
	    }   else lastYearSoyCPerHaProfit = 3.14*3000+0.701*3071; 
	    
		
		
	    if(cottonProduction > 0){
	    	cPrice = cottonSoldToTraderAgent.getCommodityPrice(LandUse.COTTON);
	    	profit+=cPrice*cottonProduction;
	    	lastYearCottonPerHaProfit = cottonPerHaYield*cPrice
	    			//- getCottonPerHaFuelInput()*fuelUnitCost
	    			//-cottonPerHaFertilizerInput*fertilizerUnitCost
	    			//-134.25
	    			;
	    } else lastYearCottonPerHaProfit = 3.14*3000;
	    
	    this.soySoldToTraderAgent.addSoyAmount(soyProduction+soyMProduction+soyCProduction);
		this.soySoldToTraderAgent.purchaseCommodity((soyProduction+soyMProduction+soyCProduction)*
				soySoldToTraderAgent.getCommodityPrice(LandUse.SOY));
		
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
		
		planningSoyCells.clear();
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
				planningSoyCells.add(c);
			if(highestLandUse == LandUse.DOUBLESOY)
				planningSoyMaizeCells.add(c);
			if(highestLandUse == LandUse.COTTON)
				planningCottonCells.add(c);
			if(highestLandUse == LandUse.SOYCOTTON)
				planningSoyCottonCells.add(c);
			
		 }
		
		
	}
	
	
	public void landUseDecisionLogisticRegression(OrganicSpace organicSpace){
		
		int changeCount = 0;
		//this is to count how many cells are agricultural, it can't extend more than 70% of the tenure size;
		
		planningSoyCells.clear();
        planningSoyMaizeCells.clear();
        planningCottonCells.clear();
        planningSoyCottonCells.clear();
        
	
		
		
		double[] profit = new double[4];
		
		profit[0] = lastYearSoyPerHaProfit;   //single soy profit , 2
		profit[1] = lastYearSoyMPerHaProfit;   //soy+maize profit, 1
		profit[2] = lastYearSoyCPerHaProfit;   //soy+cotton profit, 9
		profit[3] = lastYearCottonPerHaProfit;  //single cotton profit, 3
		
		
		double[] coef_1= new double[22];
		double[] coef_2= new double[22];
		double[] coef_3= new double[22];
		double[] coef_4= new double[22];
		double[] coef_9= new double[22];
	
		        //title: sending logistic regression
		        // sheet 4
		        // base ref is forest
				//location: google drive
		
		       coef_1[0]=2.7; coef_1[1]=3.38;coef_1[2]=16.57;coef_1[3]=0.71;
		       coef_1[4]=1.4; coef_1[5]=-0.43;coef_1[6]=-0.23;coef_1[7]=-22.99;
		       coef_1[8]=0.14; coef_1[9]=-5.63;coef_1[10]=1.16;coef_1[11]=0.33;
		       coef_1[12]=21.33; coef_1[13]=1.52;coef_1[14]=0.0005;coef_1[15]=0.0009;
		       //coef_1[16]=-0.0004; 
		       coef_1[16]=-0.0001;
		       coef_1[17]=0.0003;coef_1[18]=-0.0002;coef_1[19]=-0.0005;		       
		       coef_1[20]=-0.0006;coef_1[21]=-0.0009;    
		    
		       
		    	coef_2[0]=1.87; coef_2[1]=2.3;coef_2[2]=14.24;coef_2[3]=-0.31;
			 	coef_2[4]=-118.72; coef_2[5]=-0.06;coef_2[6]=-0.65;coef_2[7]=-22.75;
				coef_2[8]=0.35; coef_2[9]=-3.28;coef_2[10]=0.59;coef_2[11]=0.73;
				coef_2[12]=21.51; coef_2[13]=0.28;coef_2[14]=0.0001;coef_2[15]=0.0002;
				coef_2[16]=0.0003; coef_2[17]=0;coef_2[18]=-0.0002;coef_2[19]=-0.0002;
				coef_2[20]=-0.0005;coef_2[21]=-0.0005;
				    
				    
				coef_3[0]=6.07; coef_3[1]=2.74;coef_3[2]=-10.59;coef_3[3]=0.46;
				coef_3[4]=8.14; coef_3[5]=0.08;coef_3[6]=-7.01;coef_3[7]=-52.7;
				coef_3[8]=-4.59; coef_3[9]=-5.9;coef_3[10]=-0.28;coef_3[11]=0.15;
				coef_3[12]=26.76; coef_3[13]=1.21;coef_3[14]=0.0005;coef_3[15]=0.0009;
				coef_3[16]=-0.0003; coef_3[17]=0.0002;coef_3[18]=-0.0001;coef_3[19]=-0.0004;
				coef_3[20]=-0.0007;coef_3[21]=-0.0011;
				    
				
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
				coef_9[16]=-0.0004; coef_9[17]=-0.0009;coef_9[18]=0;coef_9[19]=-0.0001;
				coef_9[20]=-0.0001;coef_9[21]=-0.0003;
		
		
	    for(int i =0; i< this.tenureCells.size(); i++){
				
	 //     for (int i = 0; i < this.agriculturalCells.size(); i++)    {
	    	double[] exp= new double[6];
			double[] probability = new double[6];
	    	
	    	int[] landcover = new int[10];
	    	
	    	for(int j=0;j<10;j++)
	    		landcover[j]=0;
	    	
	    	
	    	LandCell c = this.tenureCells.get(i);
	    	
	//    	for(int r = 0; r<this.agriculturalCells.size();r++)	{
	  //  	LandCell c = this.getAgriculturalCells().get(i);
	    	
	    	// 1=doublesoy 
	//    	if(c.getSCCount()>0) {System.out.println("should grow sc "+c.getSCCount());}
	    	
	    	
	    	if(c.getLastLandUse()==LandUse.DOUBLESOY) //1
	    		landcover[0] = 1;
	    	if(c.getLastLandUse()==LandUse.SINGLESOY)  //2
	    		landcover[1] = 1;
	    	if(c.getLastLandUse()==LandUse.COTTON) //3
	    		landcover[2] = 1;
	    	if(c.getLastLandUse()==LandUse.GRASSLAND)  //4
	    		landcover[3] = 1;
	    	if(c.getLastLandUse()==LandUse.SOYCOTTON)   //9
	    		landcover[4] = 1;
	    	
	    	
	    	if(c.getLastLastLandUse()==LandUse.DOUBLESOY) //1
	    		landcover[5] = 1;
	    	if(c.getLastLastLandUse()==LandUse.SINGLESOY)  //2
	    		landcover[6] = 1;
	    	if(c.getLastLastLandUse()==LandUse.COTTON) //3
	    		landcover[7] = 1;
	    	if(c.getLastLastLandUse()==LandUse.GRASSLAND)  //4
	    		landcover[8] = 1;
	    	if(c.getLastLastLandUse()==LandUse.SOYCOTTON)   //9
	    		landcover[9] = 1;
	    	   	
	            
	    	
	    	
	    	exp[1] = coef_1[0]*landcover[0] +
	    			coef_1[1]*landcover[1] +
	    			coef_1[2]*landcover[2] +
	    			coef_1[3]*landcover[3] +
	    			coef_1[4]*landcover[4] +
	    			coef_1[5]*landcover[5] +
	    			coef_1[6]*landcover[6] +
	    			coef_1[7]*landcover[7] +
	    			coef_1[8]*landcover[8] +
	    			coef_1[9]*landcover[9] +
	    			coef_1[10]*c.getSSCount() +
	    			coef_1[11]*c.getDSCount() +
	    			coef_1[12]*c.getCCount() +
	    			coef_1[13]*c.getSCCount() +
	    			coef_1[14]*lastYearSoyPerHaProfit +
	    			coef_1[15]*lastYearSoyMPerHaProfit +
	    			coef_1[16]*lastYearCottonPerHaProfit +
	    			coef_1[17]*lastYearSoyCPerHaProfit + 
	    			coef_1[18]*lastYearSoyPerHaCost +
	    			coef_1[19]*lastYearSoyMPerHaCost +
	    			coef_1[20]*lastYearCottonPerHaCost +
	    			coef_1[21]*lastYearSoyCPerHaCost;	
	    	
	    	
	    	exp[2] = 
	    			 coef_2[0]*landcover[0] +
	    			 coef_2[1]*landcover[1] +
	    			 coef_2[2]*landcover[2] +
	    			 coef_2[3]*landcover[3] +
	    			 coef_2[4]*landcover[4] +
	    			 coef_2[5]*landcover[5] +
	    			 coef_2[6]*landcover[6] +
	    			 coef_2[7]*landcover[7] +
	    			 coef_2[8]*landcover[8] +
	    			 coef_2[9]*landcover[9] +
	    			 coef_2[10]*c.getSSCount() +
	    			 coef_2[11]*c.getDSCount() +
	    			 coef_2[12]*c.getCCount() +
	    			 coef_2[13]*c.getSCCount() +
	    			 coef_2[14]*lastYearSoyPerHaProfit +
	    			 coef_2[15]*lastYearSoyMPerHaProfit +
	    			 coef_2[16]*lastYearCottonPerHaProfit +
	    			 coef_2[17]*lastYearSoyCPerHaProfit + 
	    			 coef_2[18]*lastYearSoyPerHaCost +
	    			 coef_2[19]*lastYearSoyMPerHaCost +
	    			 coef_2[20]*lastYearCottonPerHaCost +
	    			 coef_2[21]*lastYearSoyCPerHaCost;
	    	
	    	exp[3] = coef_3[0]*landcover[0] +
	    			coef_3[1]*landcover[1] +
	    			coef_3[2]*landcover[2] +
	    			coef_3[3]*landcover[3] +
	    			coef_3[4]*landcover[4] +
	    			coef_3[5]*landcover[5] +
	    			coef_3[6]*landcover[6] +
	    			coef_3[7]*landcover[7] +
	    			coef_3[8]*landcover[8] +
	    			coef_3[9]*landcover[9] +
	    			coef_3[10]*c.getSSCount() +
	    			coef_3[11]*c.getDSCount() +
	    			coef_3[12]*c.getCCount() +
	    			coef_3[13]*c.getSCCount() +
	    			coef_3[14]*lastYearSoyPerHaProfit +
	    			coef_3[15]*lastYearSoyMPerHaProfit +
	    			coef_3[16]*lastYearCottonPerHaProfit +
	    			coef_3[17]*lastYearSoyCPerHaProfit + 
	    			coef_3[18]*lastYearSoyPerHaCost +
	    			coef_3[19]*lastYearSoyMPerHaCost +
	    			coef_3[20]*lastYearCottonPerHaCost +
	    			coef_3[21]*lastYearSoyCPerHaCost;		 
		
	    	exp[4] = coef_4[0]*landcover[0] +
	    			coef_4[1]*landcover[1] +
	    			coef_4[2]*landcover[2] +
	    			coef_4[3]*landcover[3] +
	    			coef_4[4]*landcover[4] +
	    			coef_4[5]*landcover[5] +
	    			coef_4[6]*landcover[6] +
	    			coef_4[7]*landcover[7] +
	    			coef_4[8]*landcover[8] +
	    			coef_4[9]*landcover[9] +
	    			coef_4[10]*c.getSSCount() +
	    			coef_4[11]*c.getDSCount() +
	    			coef_4[12]*c.getCCount() +
	    			coef_4[13]*c.getSCCount() +
	    			coef_4[14]*lastYearSoyPerHaProfit +
	    			coef_4[15]*lastYearSoyMPerHaProfit +
	    			coef_4[16]*lastYearCottonPerHaProfit +
	    			coef_4[17]*lastYearSoyCPerHaProfit + 
	    			coef_4[18]*lastYearSoyPerHaCost +
	    			coef_4[19]*lastYearSoyMPerHaCost +
	    			coef_4[20]*lastYearCottonPerHaCost +
	    			coef_4[21]*lastYearSoyCPerHaCost;	
	    	
	    	
	    	exp[5] = coef_9[0]*landcover[0] +
	    			coef_9[1]*landcover[1] +
	    			coef_9[2]*landcover[2] +
	    			coef_9[3]*landcover[3] +
	    			coef_9[4]*landcover[4] +
	    			coef_9[5]*landcover[5] +
	    			coef_9[6]*landcover[6] +
	    			coef_9[7]*landcover[7] +
	    			coef_9[8]*landcover[8] +
	    			coef_9[9]*landcover[9] +
	    			coef_9[10]*c.getSSCount() +
	    			coef_9[11]*c.getDSCount() +
	    			coef_9[12]*c.getCCount() +
	    			coef_9[13]*c.getSCCount() +
	    			coef_9[14]*lastYearSoyPerHaProfit +
	    			coef_9[15]*lastYearSoyMPerHaProfit +
	    			coef_9[16]*lastYearCottonPerHaProfit +
	    			coef_9[17]*lastYearSoyCPerHaProfit + 
	    			coef_9[18]*lastYearSoyPerHaCost +
	    			coef_9[19]*lastYearSoyMPerHaCost +
	    			coef_9[20]*lastYearCottonPerHaCost +
	    			coef_9[21]*lastYearSoyCPerHaCost;	
	    	
	    	
	    	double expSum= Math.exp(exp[1])+
	    			       Math.exp(exp[2])+
	    			       Math.exp(exp[3])+
	    			       Math.exp(exp[4])+
	    			       Math.exp(exp[5])+
	    			       1;
			probability[0]=1/expSum;
			probability[1]=Math.exp(exp[1])/expSum;
			probability[2]=Math.exp(exp[2])/expSum;
			probability[3]=Math.exp(exp[3])/expSum;
			probability[4]=Math.exp(exp[4])/expSum;
			probability[5]=Math.exp(exp[5])/expSum;
			
			
		  if (this.proDiversifying) {
			  probability[1]+=0.05;
			  //pro double soy+maize
			  probability[5]+=0.1;
			  //pro soy cotton;
		  }
			
			
			double max = 0;
			double maxProb = probability[0] ;
			double second = 0;
			double secondProb = 0;
			
		
			
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
			}
			
//			System.out.println("max prob "+max);
//			System.out.println("second "+second);
			
//			System.out.println("last last land use "+c.getLastLastLandUse());
//			System.out.println("last land use: "+c.getLastLandUse());
/*		    if(c.getSSCount()+c.getDSCount()+c.getCCount()+c.getSCCount() > 0 && max==0) {
		     System.out.println("max prob: "+ max);
			 System.out.println("sscount "+c.getSSCount()+" "+c.getDSCount()+" "+c.getCCount()+" "+c.getSCCount());

             for(int k=0;k<10;k++)
             	System.out.println(landcover[k]);
             
             System.out.println(lastYearSoyPerHaProfit);
             System.out.println(lastYearSoyMPerHaProfit);
             System.out.println(lastYearCottonPerHaProfit);
             System.out.println(lastYearSoyCPerHaProfit);
             System.out.println(lastYearSoyPerHaCost);
             System.out.println(lastYearSoyMPerHaCost);
             System.out.println(lastYearCottonPerHaCost);
             System.out.println(lastYearSoyCPerHaCost);
		    }
		    */
			
		//	c.setLastLastLandUse(c.getLastLandUse());
	    //	c.setLastLandUse(c.getLandUse());
		if(agriculturalCells.contains(c))  //if c was an agricultural cells already;
		   {		
	    		if (max == 0||max == 4)
	    		{
				     if (second == 1) 
   			        	  planningSoyMaizeCells.add(c);
   			         if (second == 2)
   			        	  planningSoyCells.add(c);
   			         if (second == 3)
   				          planningCottonCells.add(c);
   			         if (second == 5)
   			        	  planningSoyCottonCells.add(c);    
			      }
			    else 
			    {
			       	if(max == 1)
				        	planningSoyMaizeCells.add(c);
				    if(max == 2)
				        	planningSoyCells.add(c);
				    if(max ==3)
				        	planningCottonCells.add(c);
				    if(max == 5)
				        	planningSoyCottonCells.add(c);
			    }
		}
		else {
						
          if (tick < 3) {
        	  if (max == 0) {  		  
        			  if (second == 1 ) 
        				  planningSoyMaizeCells.add(c);
        			  else
        			  {
        			    if (second == 1 || second == 2)
        				   planningSoyCells.add(c);
        			    if (second == 3)
        				   planningCottonCells.add(c);
        			    if (second == 5)
        				   planningSoyCottonCells.add(c); 
        			  }
        		  }
        	  
        	  
        	  if (max == 4) {
        		     if (second == 1 ) 
    				      planningSoyMaizeCells.add(c);
        		     else 
        		     {	 
    			         if (second == 1 || second == 2)
    			        	  planningSoyCells.add(c);
    			         if (second == 3)
    			        	  planningCottonCells.add(c);
    		   	         if (second == 5)
    				          planningSoyCottonCells.add(c); 
        		     }
    			    
        	  }
        	  
        	  if(max == 1)
        		  planningSoyMaizeCells.add(c);
        	  if(max == 2)
        		  planningSoyCells.add(c);
        	  if(max == 3)
        		  planningCottonCells.add(c);
        	  if(max == 5)
        		  planningSoyCottonCells.add(c);
        	    	  
          }
          else {
        	  if(c.getLastLandUse()==LandUse.FOREST ) {
        		  if( max ==0||max ==4 ) {
        		  //soyMoratorium
        		  c.setLastLastLandUse(c.getLastLandUse());
 			      c.setLastLandUse(c.getLandUse());
                  c.setLandUse(LandUse.FOREST);
 			      organicSpace.setLandUse(5, c.getXlocation(), c.getYlocation());
        		  }
        		  else {
        			  if(max == 1)
                		  planningSoyMaizeCells.add(c);
                	  if(max == 2)
                		  planningSoyCells.add(c);
                	  if(max == 3)
                		  planningCottonCells.add(c);
                	  if(max == 5)
                		  planningSoyCottonCells.add(c);
        		  }
        	  }
        	  else {
        		  if( max ==0||max ==4 ) 
        		  {
            		  //soyMoratorium
            		  c.setLastLastLandUse(c.getLastLandUse());
     			      c.setLastLandUse(c.getLandUse());
                      c.setLandUse(LandUse.GRASSLAND);
     			      organicSpace.setLandUse(4, c.getXlocation(), c.getYlocation());
            	   }
        		  else {
        			  if(max == 1)
                		  planningSoyMaizeCells.add(c);
                	  if(max == 2)
                		  planningSoyCells.add(c);
                	  if(max == 3)
                		  planningCottonCells.add(c);
                	  if(max == 5)
                		  planningSoyCottonCells.add(c);
        		  }
        	  }
          }
	    
	}
	    
	//	System.out.println("how many changed to agricultural cells "+changeCount);
	    }
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
   
	public int getSoySize(){
	//for data collection at accounting stage
		return soyCells.size();
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
	
	
}
