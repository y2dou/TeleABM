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
	
	protected ArrayList<Double> cornHistoricalPrices = new ArrayList<Double>();
	protected ArrayList<Double> soyHistoricalPrices = new ArrayList<Double>();
	protected ArrayList<Double> riceHistoricalPrices = new ArrayList<Double>();
	protected ArrayList<Double> otherHistoricalPrices = new ArrayList<Double>();
	
	public SendingSoybeanAgent() {
		super();
	}
	
	public SendingSoybeanAgent(int id) {
		super(id);
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
	
	
	
	public void landUseDecision(){
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		//	
		 OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
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
		
		for (int i=0; i<this.tenureCells.size();i++){
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
		}
	
		
	}
	
	
}
