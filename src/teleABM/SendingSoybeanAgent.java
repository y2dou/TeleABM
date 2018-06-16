/**
 * 
 */
package teleABM;

import java.util.ArrayList;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;

/**
 * @author geododo
 *
 */
public class SendingSoybeanAgent extends SoybeanAgent {
	
	
	
	public SendingSoybeanAgent() {
		super();
	}
	
	public SendingSoybeanAgent(int id) {
		super(id);
	    initializeSending();
	}
	
	public void initializeSending(){
		
		this.setFertilizerUnitCost(1.06);
	
		//$380 per metric ton
		//which is $0.38 per kg
		
		this.setFuelUnitCost(15.46);
		// 6.21 yuan/litre
		this.setCornPerHaFuelInput(10.54);
		this.setSoyPerHaFuelInput(7.1);
	    
		this.setOtherPerHaFuelInput(65.46);
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
		if (grownSoy) {
			//check who has the highest price
			for (int i=0; i<this.traderAgents.size();i++) {
				soyPrice=traderAgents.get(i).getCommodityPrice(LandUse.SOY);
				if (soyPrice>highestPrice) {
					highestPrice=soyPrice;
					soySoldToTraderAgentID = i;
				}				
			}			
			soySoldToTraderAgent = this.traderAgents.get(soySoldToTraderAgentID);
			lastYearSoyPrice = highestPrice;
		}
		
		if (grownCorn) {
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
		}
		
		if (grownRice) {
			highestPrice=0;
			for (int i=0;i<this.traderAgents.size();i++){
				ricePrice=traderAgents.get(i).getCommodityPrice(LandUse.RICE);
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
		}
		if (grownOther) {
			highestPrice=0;
			for (int i=0;i<this.traderAgents.size();i++){
				otherPrice=traderAgents.get(i).getCommodityPrice(LandUse.OTHERCROPS);
				if (otherPrice>highestPrice) {
					highestPrice=ricePrice;
					otherSoldToTraderAgentID = i;
				}				
			}			
			lastYearOtherPrice = highestPrice;
			otherSoldToTraderAgent = this.traderAgents.get(otherSoldToTraderAgentID);
		}
		
			
	}
	
	
	
	public void landUseDecision(OrganicSpace organicSpace){
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		//	
	//	 OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
//		 System.out.println("sending organic space: "+organicSpace.getTypeID());
		LandUse highestLandUse=LandUse.SOY;
		
        int landUseNumber=0;
		
	
		
		double soyProfit;
		double otherProfit;
		if (tick<=0){
	
			soyProfit=RandomHelper.nextDoubleFromTo(1, 100);
			otherProfit=RandomHelper.nextDoubleFromTo(1, 100);
		}
		else {
				soyProfit=
				soyPerHaYield*lastYearSoyPrice-soyPerHaFertilizerInput*fertilizerUnitCost;
		otherProfit=
				otherPerHaYield*lastYearOtherPrice-otherPerHaFertilizerInput*fertilizerUnitCost;
		}
		
		if (soyProfit>otherProfit) {
			highestLandUse=LandUse.SOY;
			landUseNumber=1;
		}
		else {
			highestLandUse = LandUse.COTTON;
			landUseNumber=3;
		}
		
		
		double x=RandomHelper.nextDoubleFromTo(0.0, 1.0);
		if(x < 0.25){
		   landUseNumber = 1;	
		   highestLandUse=LandUse.SINGLESOY;
		} else if (x<0.5){
			landUseNumber = 2;
			  highestLandUse=LandUse.DOUBLESOY;
		} else if(x<0.75){
			landUseNumber = 3;
			highestLandUse=LandUse.COTTON;
		} else {
			landUseNumber =9;
			highestLandUse=LandUse.SOYCOTTON;
		}
	//	landUseNumber=2;
		for (int i =0; i< this.tenureCells.size(); i++){
			LandCell c = this.tenureCells.get(i);
			c.setLastLandUse(c.getLandUse());
			c.setLandUse(highestLandUse);
			organicSpace.setLandUse(landUseNumber, c.getXlocation(), c.getYlocation());
			
		}
		
		/*for (int i=0; i<this.tenureCells.size();i++){
			LandCell c = this.tenureCells.get(i);
			double count=capital;
			 if (count>0) {
				 c.setLastLandUse(c.getLandUse());
				 c.setLandUse(highestLandUse);
				 organicSpace.setLandUse(landUseNumber, c.getXlocation(), c.getYlocation());
				 count-=soyPerHaFertilizerInput*fertilizerUnitCost;
			 }
			 else {
				 c.setLastLandUse(c.getLandUse());
				 c.setLandUse(LandUse.FOREST);
				 organicSpace.setLandUse(5, c.getXlocation(), c.getYlocation());
			 }
		}*/
	
		
	}

	@Override
	public void updateProduction(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
		   this.soyCells.clear();
		   this.cornCells.clear();
		   this.cottonCells.clear();
		   
		   this.cornProduction = 0;
		   this.soyProduction = 0;
		   this.cottonProduction = 0;
		   
		   
		   for(int i =0;i< this.tenureCells.size();i++) {
			    LandCell c =   this.getTenureCells().get(i);
		      
			    c.transition();
			
			 //   if (c.getLastLandUse()==LandUse.RICE) {
			    if (c.getLandUse()==LandUse.SINGLESOY) { 	
			    	this.soyCells.add(c);
			    	soyProduction+=c.getSoyYield();
			//    	System.out.println("single soy = "+soyProduction);
			    } else if (c.getLandUse()==LandUse.DOUBLESOY){
			    	this.soyCells.add(c);
			    	soyProduction+=c.getSoyYield();			    	
			    	this.cornCells.add(c);
			    	cornProduction+=c.getCornYield();
			//    	System.out.println("soy="+c.getSoyYield()+" corn="+c.getCornYield());
			    } else if(c.getLandUse()==LandUse.SOYCOTTON){
			    	this.soyCells.add(c);
			    	this.cottonCells.add(c);
			    	soyProduction+=c.getSoyYield();
			    	cottonProduction+=c.getCottonYield();
			    }  else  {  //COTTON
			    	this.cottonCells.add(c);
			    	cottonProduction+=c.getCottonYield();
		    	
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
		
	}

	@Override
	public void updateCost(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
		double totalCost = 0;
		double totalfertilizeruse=0;
		
		if(soyCells.size()>0){
		for (int i=0; i<soyCells.size();i++){
			//setFertilizerInput() has already been put at land use decision ();
			totalCost+=soyCells.get(i).getFertilizerInput()*fertilizerUnitCost; 
			totalCost+=soyCells.get(i).getFuelInput()*fuelUnitCost;
			totalfertilizeruse+= soyCells.get(i).getFertilizerInput();
		//	System.out.println("cell no. "+i+" use fertilizer "+soyCells.get(i).getFertilizerInput());
		}
		
		soyPerHaFertilizerInput=totalfertilizeruse/soyCells.size();    					
		soyPerHaFertilizerInput = soyPerHaFertilizerInput/(cellsize*cellsize)*10000.0;    			
		setSoyPerHaFertilizerInput(soyPerHaFertilizerInput);
		totalfertilizeruse = 0;
		}
		
		if(cornCells.size()>0){
			for (int i=0; i<cornCells.size();i++){
				totalCost+=cornCells.get(i).getFertilizerInput()*fertilizerUnitCost;
				totalCost+=cornCells.get(i).getFuelInput()*fuelUnitCost;
				totalfertilizeruse+=cornCells.get(i).getFertilizerInput();
				}
			cornPerHaFertilizerInput=totalfertilizeruse/cornCells.size();
			cornPerHaFertilizerInput = cornPerHaFertilizerInput/(cellsize*cellsize)*10000.0;
			setCornPerHaFertilizerInput(cornPerHaFertilizerInput);
			totalfertilizeruse=0;
			}
		
		if(cottonCells.size()>0){
			for (int i=0; i<cottonCells.size();i++){
				totalCost+=cottonCells.get(i).getFertilizerInput()*fertilizerUnitCost;
				totalCost+=cottonCells.get(i).getFuelInput()*fuelUnitCost;
				totalfertilizeruse+=cottonCells.get(i).getFertilizerInput();
			}
			cottonPerHaFertilizerInput=totalfertilizeruse/cottonCells.size();
			cottonPerHaFertilizerInput = cottonPerHaFertilizerInput/(cellsize*cellsize)*10000.0;
			setCottonPerHaFertilizerInput(cottonPerHaFertilizerInput);
			totalfertilizeruse = 0;
			}   			
	
		capital-=totalCost;
//		System.out.println("total cost = "+totalCost);
	}

	@Override
	public void updateProfit() {
		// TODO Auto-generated method stub
	    double cPrice=0;
	    
	    if(soyProduction>0){
	    	
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
		}
		capital+=profit;
	System.out.println(lastYearSoyPerHaProfit+"// maize = "+lastYearCornPerHaProfit+"  //cotton="+lastYearCottonPerHaProfit);
//	System.out.println("sending hhd capital="+capital);
	
	}
	
	public void landUseDecisionBeta(OrganicSpace organicSpace){
	   LandUse highestLandUse=LandUse.SOY;
		
        int landUseNumber=0;
		
        
		for (int i =0; i< this.tenureCells.size(); i++){
			LandCell c = this.tenureCells.get(i);
			c.setLastLandUse(c.getLandUse());
			c.setLandUse(highestLandUse);
			organicSpace.setLandUse(landUseNumber, c.getXlocation(), c.getYlocation());
			
		}
	}
	
}
