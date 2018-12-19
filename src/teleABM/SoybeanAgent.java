/**
 * 
 */
package teleABM;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Iterator;

import org.apache.bsf.util.LangCell;

import teleABM.OrganicSpace;
import javolution.util.FastMap;
import javolution.util.FastTable;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.query.space.grid.VNQuery;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialException;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridDimensions;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;
import repast.simphony.query.space.grid.GridCell;
//import repast.simphony.util.collections.OpenLongToDoubleHashMap.Iterator;
//import repast.simphony.valueLayer.GridCell;
import repast.simphony.valueLayer.GridValueLayer;

import teleABM.Point;

import teleABM.SimpleAgent;



/**
 * @author DOU Yue
 *
 */
public abstract class SoybeanAgent {
//extends SimpleAgent{
	
    private Integer id;
    private static final AtomicInteger idGenerator = new AtomicInteger (0);
    protected Point location;
    protected Context<?> context;
    Parameters p = RunEnvironment.getInstance().getParameters();
	int numReceivingAgents = (Integer)p.getValue("initialReceivingNumAgents");
    int numSendingAgents = (Integer)p.getValue("initialSendingNumAgents");
    
	   public List<TraderAgent> traderAgents = new LinkedList<TraderAgent>();
	   //this is the list of soybean agents
	   public TraderAgent soySoldToTraderAgent ;
	   public TraderAgent cornSoldToTraderAgent;
	   public TraderAgent riceSoldToTraderAgent;
	   public TraderAgent otherSoldToTraderAgent;
	   public TraderAgent cottonSoldToTraderAgent;
	   
	   protected double capital;
	//   protected int vision;
	   protected double farmCost;
	   protected int currentYear, maxYear;
	   protected int x, y; //to keep tracking their location;
	   protected double profit;
	   protected double labour;
	   protected int xcorner, ycorner;
	   protected boolean organicFarm; //type of farmer and how they use fertilizerInput
	   //if true, they use less fertilizer
	   //if false, they use as much as possible

	protected double fertilizerUnitCost; //price of fertilizer 
	
	protected double fuelUnitCost;  //unit price for diesel,
	
	protected double riceProduction;
	protected double soyProduction;	
	protected double cornProduction;
	protected double otherProduction;
	protected double cottonProduction;
	protected double soyTotalProduction;
	

	protected boolean grownRice = false;
	protected boolean grownSoy = false;
	protected int grownSoyYears=0;
	protected boolean grownCorn = false;
	protected boolean grownOther = false;
	protected boolean grownCotton = false;
	   
	   protected boolean proTeleCoupling;
	
   /* protected int soyCellsCount;
    protected int cornCellsCount;
    protected int riceCellsCount;
    protected int otherCellsCount;*/
	//   protected boolean isDead;	
	   protected double credit;
	//	int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	//	double tick = RunState.getInstance().getScheduleRegistry().getModelSchedule().getTickCount();
	//	double tick
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	//	int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
    	
		Map<Integer, Point> agentLocations = new HashMap<Integer, Point>();
		 Parameters para = RunEnvironment.getInstance().getParameters();
		  int receivingXdim = (Integer)para.getValue("receivingWorldWidth");
		  int receivingYdim = (Integer)para.getValue("receivingWorldHeight");
		  int sendingXdim = (Integer)para.getValue("sendingWorldWidth");
		  int sendingYdim = (Integer)para.getValue("sendingWorldHeight");
		  
		  int cellsizeSending = (Integer) p.getValue("cellSizeSending");
		  int cellsizeReceiving = (Integer) p.getValue("cellSizeReceiving");
			//because sending and receiving have different cellzie;
		  int cellsize; 
	//	  int cellsize = (Integer) para.getValue("cellSize");
		  
		protected  int vision = (Integer) para.getValue("vision");
		
		protected List<LandCell> tenureCells = new LinkedList<LandCell>();
		protected List<LandCell> agriculturalCells = new LinkedList<LandCell>();
		protected  FastTable<LandCell> soyCells = new FastTable<LandCell>();
		protected  FastTable<LandCell> cornCells = new FastTable<LandCell>();
		protected FastTable<LandCell> riceCells = new FastTable<LandCell>();
		protected  FastTable<LandCell> otherCells = new FastTable<LandCell>();
		
		protected FastTable<LandCell> soyMaizeCells = new FastTable<LandCell>();
		protected FastTable<LandCell> soyCottonCells = new FastTable<LandCell>();
		protected FastTable<LandCell> cottonCells = new FastTable<LandCell>();
		
		
	//	protected MyLandCell tenure;
		 
		  //to place agents at best location
		
		protected double soyPerHaYield;
		protected double cornPerHaYield;
		protected double ricePerHaYield;
		protected double cottonPerHaYield;
		
		protected double otherPerHaYield;
		
		protected double soyPerHaFertilizerInput;
		protected double cornPerHaFertilizerInput;
		protected double ricePerHaFertilizerInput;
		protected double cottonPerHaFertilizerInput;
		protected double soyMaizePerHaFertilizerInput;
		protected double soyCottonPerHaFertilizerInput;
		protected double otherPerHaFertilizerInput;
		
		protected double soyPerHaFuelInput;		
		protected double cornPerHaFuelInput;
		protected double ricePerHaFuelInput;
		protected double cottonPerHaFuelInput;
		
		protected double otherPerHaFuelInput;
		//above fuel unit cost are all from direct fossil fuel energy cost (in tab energy in Equations.xlsx) 
		//
		
		protected double lastYearSoyPrice;
		protected double lastYearCornPrice;
		protected double lastYearRicePrice;
		protected double lastYearCottonPrice;
		protected double lastYearOtherPrice;
		
		protected double lastYearSoyPerHaProfit;
		protected double lastYearCornPerHaProfit;
		protected double lastYearRicePerHaProfit;
		protected double lastYearCottonPerHaProfit;
		protected double lastYearOtherPerHaProfit;
		
		protected List<Double> soyPrices = new LinkedList<Double>();
		protected List<Double> cornPrices = new LinkedList<Double>();
		protected List<Double> ricePrices = new LinkedList<Double>();
		protected List<Double> cottonPrices = new LinkedList<Double>();
		protected List<Double> otherPrices = new LinkedList<Double>();
		
		protected int priceMemoryLimit = 3;
		
		 private Map<LandUse, ArrayList<Double>> prices = new HashMap<LandUse, ArrayList<Double>>();
		    Map<LandUse, InputStream> priceStreams;
		    MarketPrices marketPrices = new MarketPrices();
		    protected LinkedList<LandUse> commodityType =
				       new LinkedList<LandUse>();
	   
		protected double internationalTradeSoyPrice = 0;    
		    
		 public SoybeanAgent() {
			this(idGenerator.getAndIncrement());

		}


		public SoybeanAgent(int id) {
			this.id = id;

		}
		

		public void initialize(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub	
			
	//		 OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
			 
		//	if(TeleABMBuilder.receivingSystem){
			if(organicSpace.getTypeID()=="organicSpaceReceiving"){
			    setFarmCost(RandomHelper.getDistribution("farmCostReceiving").nextDouble());	
			    setCapital(RandomHelper.getDistribution("capitalReceiving").nextDouble());
		        setLabour(RandomHelper.getDistribution("labourReceiving").nextDouble());
			    setProTeleCoupling(true);
			    setOrganicFarm(true);
			    
			    setCommodityType(LandUse.SOY);
				setCommodityType(LandUse.CORN);
				setCommodityType(LandUse.RICE);
			
	//			setReceivingCommodityPrices();
			}
			
		//	if(TeleABMBuilder.sendingSystem){
			if(organicSpace.getTypeID()=="organicSpaceSending"){	
				setFarmCost(RandomHelper.getDistribution("farmCostSending").nextDouble());	
			    setCapital(RandomHelper.getDistribution("capitalSending").nextDouble());
		        setLabour(RandomHelper.getDistribution("labourSending").nextDouble());
			    setProTeleCoupling(true);
			    setOrganicFarm(true);
			    
		//	    setCommodityType(LandUse.SINGLESOY);
		//		setCommodityType(LandUse.DOUBLESOY);
		//		setCommodityType(LandUse.COTTON);
				
			    setCommodityType(LandUse.SOY);
			    setCommodityType(LandUse.CORN);
			    setCommodityType(LandUse.COTTON);
		//		setSendingCommodityPrices();
			}

	}
		
  
	


		public double getFertilizerUnitCost() {
			return fertilizerUnitCost;
		}


		public void setFertilizerUnitCost(double fertilizerUnitCost) {
			this.fertilizerUnitCost = fertilizerUnitCost;
		}


		public double getFuelUnitCost() {
			return fuelUnitCost;
		}


		public void setFuelUnitCost(double fuelUnitCost) {
			this.fuelUnitCost = fuelUnitCost;
		}


	    public void setInternationalTradeSoyPrice(double cprice){
	    	internationalTradeSoyPrice = cprice;
	    }
		
		

		public void setSendingStaticCommodityPrices(LandUse landuse, double tempPrice){
	    	marketPrices.setPrice(landuse, tempPrice);
		
		}
		public void setSendingDynamicCommodityPrices(LandUse landuse, ArrayList cropprices){
			prices.put(landuse, cropprices);
			 soyPrices.add(0, 0.501+RandomHelper.nextDoubleFromTo(-0.01, 0.05));
		       cornPrices.add(0, 0.302+RandomHelper.nextDoubleFromTo(-0.01, 0.05));
		       cottonPrices.add(0, 1.93+RandomHelper.nextDoubleFromTo(-0.01, 0.05));
		}
		
		public void setReceivingStaticCommodityPrices(LandUse landuse, double tempPrice){
	    	marketPrices.setPrice(landuse, tempPrice);
		
		}
		public void setReceivingDynamicCommodityPrices(LandUse landuse, ArrayList cropprices){
			   prices.put(landuse, cropprices);
	//		System.out.println("set receive dynamic "+prices.get(landuse));
			   soyPrices.add(0, 1.31+RandomHelper.nextDoubleFromTo(-0.01, 0.05));
		       cornPrices.add(0, 0.6+RandomHelper.nextDoubleFromTo(-0.01, 0.05));
		       ricePrices.add(0, 1.3+RandomHelper.nextDoubleFromTo(-0.01, 0.05));
		}
		
		
		
	    
	    public double getCommodityPrice(LandUse landuse){
	    //	return MarketPrices.get(landuse);
	    	//here is only static price;
	    	//needs to solve dynamic price;
	    	double tempPrice=0;
	    	int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	    	
	    //	System.out.println("tick at trader "+tick);
	    	if(TeleABMBuilder.internationalTradeMode)
	    		{
	    		 if(landuse==LandUse.SOY||landuse==LandUse.SINGLESOY){
	    	//		 tempPrice=marketPrices.getPrice(landuse)+
			//    			 RandomHelper.nextDoubleFromTo(-0.01, 0.05);
	    	//	     System.out.println(tick+": land use "+landuse+" price "+tempPrice);
	    			 tempPrice = getInternationalTradeSoyPrice();
	    		 }
	    		
	    		
	    	} else  if(commodityType.contains(landuse)) {
	   // 		System.out.println("commodity type "+commodityType.contains(landuse));
	    		
	    	 if(prices.containsKey(landuse))  
	    			 {
	    	     
	    		 tempPrice=prices.get(landuse).get(tick);
	    		 //note that it has to be get(tick-1), otherwise it's more than what it needs.
	    		 double r = new Random().nextDouble();
	    		 tempPrice= tempPrice+ (2*r-1.0)*0.05;  
	    		 //this gives p a natural 0.05% variation of the price
	    			 } 
	    	 else 
	    	 {   
	    		//here add the interactions of price
	   		     		 
	    		 tempPrice=marketPrices.getPrice(landuse)+
	    			 RandomHelper.nextDoubleFromTo(-0.01, 0.05);
	   // 	 System.out.println("land use static prices: "+tempPrice); 
	    	 }
	    	 }
	    
	   // 	if (capital<0) 
	   // 		 tempPrice=0;
	   // 	System.out.println("tempPrice: "+tempPrice);
	    	
	    	
	    	return tempPrice;
	    	
	    	
	    }
	    
	    
	    
	//bigger the priority number, more ahead.
	@ScheduledMethod(start = 1, interval = 1, priority = 2)
	  public void step() {
	// this is to calculate last year's profit and make this year's decision
	//	System.out.println("which step is first");
	   OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
	 //   Grid grid = (Grid) organicSpace.getProjection("Grid");
	   
	   if(organicSpace.getTypeID()=="organicSpaceSending") {
		       this.decidingTradingPartner();
			   this.updateProduction(organicSpace);
			   this.updateCost(organicSpace);
			   this.updateProfit();
		    //   this.landUseDecision(organicSpace);
			//   this.landUseDecisionBeta(organicSpace);
			   this.landUseDecisionLogisticRegression(organicSpace);
		       this.updateLandUse(organicSpace);
		}
		  
		
		if(organicSpace.getTypeID()=="organicSpaceReceiving") 
		{
	//		 System.out.println("come on");
			  this.decidingTradingPartner();
	           this.updateProduction(organicSpace);
	           this.updateCost(organicSpace);
	           this.updateProfit();
	           this.landUseDecisionLogisticRegression(organicSpace);
	           this.landUseDecision(organicSpace);
	       //    this.landUseDecisionBeta(organicSpace);
	      //     this.landUseDecisionLogisticRegression(organicSpace);
	           this.updateLandUse(organicSpace);
	          
		}
		
	
	  
	}
	

public abstract void landUseDecisionLogisticRegression (OrganicSpace organicSpace);	

public abstract void landUseDecisionBeta(OrganicSpace organicSpace) ;


public abstract void updateProduction(OrganicSpace organicSpace);


public int getGrownSoyYears() {
		return grownSoyYears;
	}


public void setGrownSoyYears(int grownSoyYears) {
		this.grownSoyYears = grownSoyYears;
	}

public abstract void landUseDecision(OrganicSpace organicSpace);
	
public abstract void updateLandUse(OrganicSpace organicSpace);
	
	private void updateLabor() {
		// TODO Auto-generated method stub
		//to represent the household demography dynamics
		double random = RandomHelper.nextDoubleFromTo(0.0, 0.1);
	//	Random r = new Random();
		//double randomValue = -0.03 + 2 * r.nextDouble();
	//	double randomValue = r.ne;
		double labourCost;
		labourCost=
				this.soyCells.size()*0.002+
				this.cornCells.size()*0.001+
				this.riceCells.size()*0.003+
				this.otherCells.size()*0.002;
	
		this.setLabour(this.getLabour()-labourCost+random);
		
		//here can implement the machinery, if they can hire more and bigger machines, 
		//then the labor use or requirement is lower. 
	}

public abstract void updateCost(OrganicSpace organicSpace);
	
	
	public void decidingTradingPartner(){
		
	}
	
 public void setCommodityType(LandUse commodityT) {
		   //if commodityType ==1 soy
		   //if commodity type==2 corn
		   //if commodity type ==3 rice
		//  this.commodityType = commodityType;
		  this.commodityType.add(commodityT);
	   }

public abstract void updateProfit();
	
	
	//initialization
	protected void addSoybeanAgentAtRandom(OrganicSpace organicSpace) {
		//add farmers spatially and to the best location in the organic field
		 int bestOrganic = -1;
		    int bestDistance = -9999;
		    int goodx[] = new int[16];
		    int goody[] = new int[16];
		    int bestSpots = 0;

		    int xLook, yLook;
		    
		    List<LandCell> neighborCells = new LinkedList<LandCell>();
		    
		  Grid<Object> grid = (Grid) organicSpace.getProjection("Grid");

		    GridPoint point = grid.getLocation(this);
		    
		    int x = point.getX();
		    int y = point.getY();
            System.out.println("location of Agents "+x+" "+y);
            
		    boolean neighborExists = true;
		    yLook = y;

		    for (xLook = x - vision; xLook <= x + vision; xLook++) {
            //  neighborCells.add(organicSpace.get)
	/*	    	LandCell c = 
		    			new LandCell(organicSpace,grid,
		  		    		  xLook, yLook,
		  		    		  organicSpace.getElevationAt(goodx[chosenSpotIndex], goody[chosenSpotIndex]),
		  		    		  organicSpace.getOrganicAt(goodx[chosenSpotIndex], goody[chosenSpotIndex]));*/
		      Iterable neighbors = grid.getObjectsAt(xLook, yLook);
       //       Iterable neighbor = organicSpace.getValueLayer("Land Holder Field").get(coordinates);
              
		      if (!neighbors.iterator().hasNext())
		        neighborExists = false;

		      else for (Object o : neighbors) {
		        if (o instanceof SoybeanAgent)
		          break;

		        neighborExists = false;
		      }
		      
		      if (neighborExists == false) {
		          if (organicSpace.getOrganicAt(xLook, yLook) > bestOrganic) {
		            bestOrganic = organicSpace.getOrganicAt(xLook, yLook);
		            bestDistance = Math.abs(x - xLook);
		            bestSpots = 0;
		            goodx[0] = xLook;
		            goody[0] = yLook;
		            bestSpots++;
		          } else if (organicSpace.getOrganicAt(xLook, yLook) == bestOrganic) {
		            if (Math.abs(x - xLook) < bestDistance) {
		              bestDistance = Math.abs(x - xLook);
		              bestSpots = 0;
		              goodx[0] = xLook;
		              goody[0] = yLook;
		              bestSpots++;
		            } else if (Math.abs(x - xLook) == bestDistance) {
		              goodx[bestSpots] = xLook;
		              goody[bestSpots] = yLook;
		              bestSpots++;
		            }
		          }
		        }
		      }

		      neighborExists = true;
		      xLook = x;

		      for (yLook = y - vision; yLook <= y + vision; yLook++) {

		        Iterable neighbors = grid.getObjectsAt(xLook, yLook);

		        if (!neighbors.iterator().hasNext())
		          neighborExists = false;

		        else for (Object o : neighbors) {
		          if (o instanceof SoybeanAgent)
		            break;

		          neighborExists = false;
		        }

		        if (neighborExists == false) {
		          if (organicSpace.getOrganicAt(xLook, yLook) > bestOrganic) {
		            bestOrganic = organicSpace.getOrganicAt(xLook, yLook);
		            bestDistance = Math.abs(y - yLook);
		            bestSpots = 0;
		            goodx[0] = xLook;
		            goody[0] = yLook;
		            bestSpots++;
		          } else if (organicSpace.getOrganicAt(xLook, yLook) == bestOrganic) {
		            if (Math.abs(y - yLook) < bestDistance) {
		              bestDistance = Math.abs(y - yLook);
		              bestSpots = 0;
		              goodx[0] = xLook;
		              goody[0] = yLook;
		              bestSpots++;
		            } else if (Math.abs(y - yLook) == bestDistance) {
		              goodx[bestSpots] = xLook;
		              goody[bestSpots] = yLook;
		              bestSpots++;
		            }
		          }
		        }
		      }

		      int chosenSpotIndex = 0;
		      // agent go to the best spot
		      if (bestSpots != 0) {
		        if (bestSpots == 1) {
		          chosenSpotIndex = 0;
		        } else {
		          chosenSpotIndex = RandomHelper.nextIntFromTo(0, bestSpots - 1);
		        }

		        grid.moveTo(this, goodx[chosenSpotIndex], goody[chosenSpotIndex]);
		      }
		      LandCell landCell = new LandCell(organicSpace,grid,
		    		  goodx[chosenSpotIndex], goody[chosenSpotIndex],
		    		  organicSpace.getElevationAt(goodx[chosenSpotIndex], goody[chosenSpotIndex]),
		    		  organicSpace.getOrganicAt(goodx[chosenSpotIndex], goody[chosenSpotIndex]));
	//	      System.out.println("initial soc: "+landCell.getInitialSoc());    

		    	landCell.setLandHolder(true,this);
		    	this.tenureCells.add(landCell);
		    	organicSpace.setLandHolder(this.getID(), goodx[chosenSpotIndex], goody[chosenSpotIndex]);
		    	
		    	
		    	   if(organicSpace.getTypeID() == "organicSpaceReceiving")
			        	cellsize = cellsizeReceiving;
			        if(organicSpace.getTypeID() == "organicSpaceSending")
			        	cellsize = cellsizeSending;
			        
		  //  	RandomHelper.registerDistribution("hectares", RandomHelper.createUniform(1.2, 50));
	            
		    	int tenureSize = (int) Math.round(RandomHelper.getDistribution("hectares").nextDouble()*
		    			10000d/ (cellsize * cellsize));
		    	
		    	
		//    	System.out.println("get hectares "+tenureSize);
		    	
		    	for ( int i = landCell.getXlocation()-(int) (Math.sqrt(tenureSize)/2); 
		    			i < landCell.getXlocation() + (int) (Math.sqrt(tenureSize)/2) ;i++){
		    		
		    		for (int j = landCell.getYlocation() - (int) (Math.sqrt(tenureSize)/2) ; 
		    				j<landCell.getYlocation() + (int) (Math.sqrt(tenureSize)/2); j++){
		    	
		    		
		    			LandCell landCellneighbor = new LandCell(organicSpace,grid,i,j,
		    					 organicSpace.getElevationAt(i, j),
		    					 organicSpace.getOrganicAt(i, j));
		    			  
		    			if (!landCellneighbor.isTaken())
		    					{ landCellneighbor.setLandHolder(true, this);
		    					  this.tenureCells.add(landCellneighbor);
		    					  organicSpace.setLandHolder(this.getID(), i, j);
		    					}
		    			}
		    	
		    		
		    	}
		    }


	public void addLandUseAtRandom(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
	//	  Grid<Object> grid = (Grid) organicSpace.getProjection("Grid");

	//	    GridPoint point = grid.getLocation(this);
		
		   //initialize land use, randomly
		    for (int i=0; i< this.getTenureCells().size(); i++) {
		    	int x = this.getTenureCells().get(i).getXlocation();
		    	int y = this.getTenureCells().get(i).getYlocation();
		//    	System.out.println("tenure Size "+this.getTenureCells().size()+" "+
		//    	x+" "+y);
		    	double random = RandomHelper.nextDoubleFromTo(0.0, 1.0);
			    if (random <0.5) {
			    	this.getTenureCells().get(i).setLandUse(LandUse.SOY);
			//    	System.out.println("SOY1");
			    	organicSpace.setLandUse(1, x, 
			    			y);
			//    	System.out.println("SOY2");
			    }
			    
			    if (random >=0.5 && random<0.7) {
			    	this.getTenureCells().get(i).setLandUse(LandUse.CORN);
			    	organicSpace.setLandUse(2, x,y);
			    }
			    
			    if (random >0.7 ) {
			    	this.getTenureCells().get(i).setLandUse(LandUse.RICE);
			    	organicSpace.setLandUse(3, x,y);
			    }
			    
		    	
		    }
		   
		    		
	}
	
	
	protected void addSoybeanAgentFromField(OrganicSpace organicSpace, Point corner) {
		   
		  Grid<Object> grid = (Grid) organicSpace.getProjection("Grid");

		    GridPoint point = grid.getLocation(this);
		    
		    int randomX = ThreadLocalRandom.current().nextInt(1, 30);
		    int randomY = ThreadLocalRandom.current().nextInt(1, 50);
            grid.moveTo(this, corner.x+randomX, corner.y+randomY);
		    
		    List<Point> agriculturalCells = new LinkedList<Point>();
		    
		 //   RandomHelper.registerDistribution("hectares", RandomHelper.createUniform(30,200));
	        if(organicSpace.getTypeID() == "organicSpaceReceiving")
	        	cellsize = cellsizeReceiving;
	        if(organicSpace.getTypeID() == "organicSpaceSending")
	        	cellsize = cellsizeSending;
	        
	        
	    	double tenureSize =  Math.round(RandomHelper.getDistribution("hectares").nextDouble()*
	    			10000d/ (cellsize * cellsize));
	    	
	   // 	System.out.println(this.getID()+" get hectares "+tenureSize);
	    	
			 for (int i=corner.x+randomX; i<(corner.x+Math.sqrt(tenureSize)+randomX);i++)  {
				 for (int j=corner.y+randomY;j<(corner.y+Math.sqrt(tenureSize)+randomY);j++){
					 Point p=new Point(i,j);
				
					 
				//	 if (TeleABMBuilder.receivingSystem&&!TeleABMBuilder.sendingSystem){
					 if(organicSpace.getTypeID()=="organicSpaceReceiving") {
						
						 
					 if (organicSpace.getLandUseAt(i, j)>=2 && organicSpace.getLandUseAt(i, j)<=4)
						{		
							agriculturalCells.add(p);
					
					
						}
					 else if (organicSpace.getLandUseAt(i, j)==6){
							
							agriculturalCells.add(p);
		
						} else {
							
						}
					}
					 
					 if (organicSpace.getTypeID()=="organicSpaceSending"){
				
					 if (organicSpace.getLandUseAt(i, j)>=1&&organicSpace.getLandUseAt(i, j)<=3)
						{	//	 System.out.println("added soy/corn");
							agriculturalCells.add(p);
					//       System.out.println("added soy/corn");
						}
					 else if (organicSpace.getLandUseAt(i, j)==9){
							
							agriculturalCells.add(p);
						//	System.out.println("cotton");
						} else {
							//System.out.println("nothing");
						}
					}}
			//	 System.out.println("yboundary finished once "+i);
			 }
       
			 int count = 0;
			 int i = 0;
			 while(count<tenureSize && i<agriculturalCells.size()) {
			  
			    	 Point p = agriculturalCells.get(i);
			    	 LandCell c = new LandCell(organicSpace,grid,p.x,p.y,
		 					 organicSpace.getElevationAt(p.x, p.y),
		 					 organicSpace.getOrganicAt(p.x, p.y));
					 count++;
					 i++;
					  c.setLandHolder(true,this);
					  this.tenureCells.add(c);
					  organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
		//			  System.out.println("initial soc: "+c.getInitialSoc());    
			 }
			
         //     System.out.println("tenure size "+count);   
             
          //    addLandUseFromField(organicSpace);
        
        		
           
	}
	
	
	public void addLandUseFromField(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
	//this is called at the building stage
		
	// 	System.out.println("tenure Size "+this.getTenureCells().size());
	 	int count=0;
		   for (int i=0; i< this.getTenureCells().size(); i++) {
		    	int x = this.getTenureCells().get(i).getXlocation();
		    	int y = this.getTenureCells().get(i).getYlocation();
		    	
		  //  	double random = RandomHelper.nextDouble();
		  //  	System.out.println("thislandcell "+ organicSpace.getLandUseAt(x, y));
		    	 //problem here is it puts all kinds of land use to the agent
		    	
		    	if (organicSpace.getTypeID()=="organicSpaceReceiving"){
		    //	if (organicSpace.getLandUseAt(x, y)==2){
		    	if (organicSpace.getLandUseAt(x, y)==40) {
		    		//sun jing
		    		this.getTenureCells().get(i).setLandUse(LandUse.SOY);
		    	//	this.getTenureCells().get(i).setLastLandUse(LandUse.SOY);
	
		  //  	} else if (organicSpace.getLandUseAt(x, y)==3) {
		    	} else if (organicSpace.getLandUseAt(x, y)==41) {		
		    		this.getTenureCells().get(i).setLandUse(LandUse.RICE);
		    	//	this.getTenureCells().get(i).setLastLandUse(LandUse.RICE);
		   // 	} else if (organicSpace.getLandUseAt(x, y)==6) {
		    	} else if (organicSpace.getLandUseAt(x, y)==42) {
		    		this.getTenureCells().get(i).setLandUse(LandUse.CORN);
		    	//	this.getTenureCells().get(i).setLastLandUse(LandUse.CORN);
		    	} else if (organicSpace.getLandUseAt(x, y) == 4) {
		    		this.getTenureCells().get(i).setLandUse(LandUse.OTHERCROPS);
		    	}
		    
		    	
		    	
		    	/*else if (organicSpace.getLandUseAt(x, y)==4) {
		    		this.getTenureCells().get(i).setLandUse(LandUse.OTHERCROPS);
		    		this.getTenureCells().get(i).setLastLandUse(LandUse.OTHERCROPS);
		    	} else if (organicSpace.getLandUseAt(x, y)==5){
		    		this.getTenureCells().get(i).setLandUse(LandUse.FOREST); 
		    	} else if (organicSpace.getLandUseAt(x, y)==7){		    		
		    		this.getTenureCells().get(i).setLandUse(LandUse.BUILDING); 
		    	} else {
		    		this.getTenureCells().get(i).setLandUse(LandUse.WATER); 
		    	}*/
		    	}
		    	
		    	if (organicSpace.getTypeID()=="organicSpacSending"){
		    		if (organicSpace.getLandUseAt(x, y)==1){
			    		this.getTenureCells().get(i).setLandUse(LandUse.DOUBLESOY);
			//    		count++;
			    	} else if (organicSpace.getLandUseAt(x, y)==2) {
			    		this.getTenureCells().get(i).setLandUse(LandUse.SINGLESOY);
			    	} else if (organicSpace.getLandUseAt(x, y)==3) {
			    		this.getTenureCells().get(i).setLandUse(LandUse.COTTON);
			    	} else {
			    		this.getTenureCells().get(i).setLandUse(LandUse.SOYCOTTON);//Cotton
			    	}
		    	}
		   // 	System.out.println("this agent "+i+" has "+this.getTenureCells().get(i).getLandUse());
		    		
		    }
	//	   if(count>0&&this.getID()>30) System.out.println(this.getID()+" "+count);
	}


	public void addSoybeanAgentFromLandscape(OrganicSpace organicSpace,
			Point corner) {
		// TODO Auto-generated method stub
		//this is to count all available land use cells, and put them in a list,
		//and randomly assign them to different hhd agents
		
		  double xboundary;
		   double yboundary;
			    
		
		   double perAgentArea;
		  double xdim;
		  double ydim;
		  int numAgents ;
		   if(organicSpace.getTypeID()=="organicSpaceReceiving") {
			   xdim = receivingXdim;
			   ydim = receivingYdim;
			   numAgents = numReceivingAgents;
		   } else if(organicSpace.getTypeID()=="organicSpaceSending"){
			   xdim = sendingXdim;
			   ydim = sendingYdim;
			   numAgents = numSendingAgents;
		   } else {
			   xdim=1000;
			   ydim=1000;
			   numAgents=100;
		   }
		   
		  perAgentArea = (double) xdim*ydim/numAgents;
			    
		   xboundary = Math.sqrt(perAgentArea*((double)xdim/(double)ydim));
		   yboundary = xboundary*ydim/xdim;
		   
		 
		Grid<Object> grid;
	

		if(organicSpace.getId()=="organicSpaceReceiving") {
			grid = (Grid) organicSpace.getProjection("Grid");
	//		System.out.println("organic space receiving "+grid.getName());
			
		} else {
			grid = (Grid) organicSpace.getProjection("gridSending");
	//		System.out.println("organic space sending"+grid.getName());
		}
		
	//	System.out.println(grid.getName());
		    GridPoint point = grid.getLocation(this);
		    grid.moveTo(this, corner.x, corner.y);
		    
		   
		    
	//	   System.out.println(corner.x+" "+(corner.x+xboundary));
	//	   System.out.println(corner.y+" "+(corner.y+yboundary));
		    
		    
		 //first, go through all agricultural cells in this sub-section   
if(organicSpace.getTypeID()=="organicSpaceReceiving"){
	
		 for (int i=corner.x; i<=(corner.x+xboundary);i++)  {
			 for (int j=corner.y;j<=(corner.y+yboundary);j++){
				 Point p=new Point(i,j);
				 GridPoint pt = grid.getLocation(this);		
				 //    System.out.println(pt.getX()+" "+pt.getY());	     
				     GridCellNgh<LandCell> nghCreator = new GridCellNgh<LandCell>(grid, pt, LandCell.class,2,2);     
				     List<GridCell<LandCell>> gridCells = nghCreator.getNeighborhood(true); 
				     
				 LandCell c = new LandCell(organicSpace,grid,p.x,p.y,
	 					 organicSpace.getElevationAt(p.x, p.y),
	 					 organicSpace.getOrganicAt(p.x, p.y));
									     
				 c.setNgh(gridCells);
		/*		 GridPoint pp = grid.getLocation(p);
				 GridCellNgh<LandCell> nghCreator = new GridCellNgh<LandCell>(grid, pp, LandCell.class, 3,3);
				 
				 List<repast.simphony.query.space.grid.GridCell<LandCell>> ngh = nghCreator.getNeighborhood(true);
				 
				 for (int k=0;k<ngh.size();k++) {
					 System.out.println(ngh.iterator().next().getClass());
				 }*/
				 
			//	 VNQuery<LandCell> query = new VNQuery<LandCell>(grid, c, 3);
				 
				 c.setCellSize(cellsizeReceiving);
				 
				 //c.setDSCount(organicSpace.getSoyCornCountAt(i, j));
				// c.setRCount(organicSpace.getRCountAt(i, j));
				 //above two for now I change them as random, dec 18, for simplicity
			
				
				 int randomA = ThreadLocalRandom.current().nextInt(1, 6);
				 int randomB = ThreadLocalRandom.current().nextInt(1,3);
				 c.setDSCount(randomA);
				 c.setRCount(randomB);
				 
				 c.setSuitability(c.getDSCount());
			//	 System.out.println("organic space "+organicSpace.getId()+" land cell is "+c.getRCount());
				 c.setRecommendedSoyPerHaFertilizerUse(10.0);
				 c.setObservedSoyPerHaFertilizerUse(63.0);
				 c.setRecommendedRicePerHaFertilizerUse(150.0);
				 c.setObservedRicePerHaFertilizerUse(146.0);
				 c.setRecommendedCornPerHaFertilizerUse(200.0);
				 c.setObservedCornPerHaFertilizerUse(224.0);
				 c.setObservedOtherPerHaFertilizerUse(300.0);
				 //have to add recommended and observed for all crops when iterate through all cells.
				 
					if(c.isTaken()==true || organicSpace.getLandHolder(i, j)>0)
					//this is to check if land cell is taken.
						{
						
						}
					else
				{ 
					// if(organicSpace.getLandUseAt(i, j) ==2) 
						if(organicSpace.getLandUseAt(i, j) ==40)
							
					 {  
					//	 System.out.println("land use field works"); 
						 //this worked
						 c.setLandUse(LandUse.SOY);
						 {
							 
							 c.setLandHolder( true,this);
							 //here the id is soybean id, it's different from receiving/sending 
							 //soybean id							 
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);
							 //fertilizer use							 
							 c.setFertilizerInput(LandUse.SOY);
							 //have to add recommended and observed for all crops when iterate through all cells.
							 
						 }
					 }
				//	 if(organicSpace.getLandUseAt(i, j)==3) {
						if(organicSpace.getLandUseAt(i, j)==41) {
							//sun jing
						 c.setLandUse(LandUse.RICE);
					
							 c.setLandHolder(true, this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);													 
							 c.setFertilizerInput(LandUse.RICE);						
						 
					 }
				//	 if(organicSpace.getLandUseAt(i, j)==6){
						 if(organicSpace.getLandUseAt(i, j)==42){
						 c.setLandUse(LandUse.CORN);											
							 c.setLandHolder(true,this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);							 
							 c.setFertilizerInput(LandUse.CORN);
							 //have to add recommended and observed for all crops when iterate through all cells.
							 
						
					 }
					 
					 if(organicSpace.getLandUseAt(i, j)==4){
						 c.setLandUse(LandUse.OTHERCROPS);											
							 c.setLandHolder(true,this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);							 
							 c.setFertilizerInput(LandUse.OTHERCROPS);
							 //have to add recommended and observed for all crops when iterate through all cells.
							 
						
					 }
					 
					 if(organicSpace.getLandUseAt(i, j)==5) {
						 c.setLandUse(LandUse.FOREST);
					 }
					 if (organicSpace.getLandUseAt(i, j)==7) {
						 c.setLandUse(LandUse.BUILDING);
					 }
					 if(organicSpace.getLandUseAt(i, j)==1) {
						 c.setLandUse(LandUse.WATER);
					 }
						 
					 
				 }
				 } //first for loop
		 } //second for loop
}
				  
	
				 
if (organicSpace.getTypeID()=="organicSpaceSending"){
					 
					 for (int i=corner.x; i<=(corner.x+xboundary);i++)  {
						 for (int j=corner.y;j<=(corner.y+yboundary);j++){
							 Point p=new Point(i,j);
							 LandCell c = new LandCell(organicSpace,grid,p.x,p.y,
				 					 organicSpace.getElevationAt(p.x, p.y),
				 					 organicSpace.getOrganicAt(p.x, p.y));
							 
							 c.setCellSize(cellsizeSending);
							 
							
							 
					//		 GridCellNgh<LandCell> nghCreator = new GridCellNgh<LandCell>(grid, pp, LandCell.class, 1 ,1);
							 
				//			 VNQuery<GridPoint> queryPP = new VNQuery<GridPoint>(grid, pp,3, 3);
							 
							 
							
					//		 List<repast.simphony.query.space.grid.GridCell<LandCell>> ngh = nghCreator.getNeighborhood(true);
							 
					//		 for (int k=0;k<ngh.size();k++) {
					//			 System.out.println(ngh.iterator().next().getClass());
					//		 }
							 
							 //suitability
							 c.setSSCount(organicSpace.getSSCountAt(i, j));
							 //single soy
							 c.setDSCount(organicSpace.getDSCountAt(i, j));
							 //double soy
							 c.setCCount(organicSpace.getCCountAt(i, j));
							 //cotton
							 c.setSCCount(organicSpace.getSCCountAt(i, j));
							 //soy cotton
							 
							 c.setSuitability(c.getSSCount()+c.getDSCount()+c.getCCount()+c.getSCCount());
							 
							 c.setRecommendedSoyPerHaFertilizerUse(300);
						//	 c.setObservedSoyPerHaFertilizerUse(243.5);
							 c.setObservedSoyPerHaFertilizerUse(138);
							//http://www.fao.org/docrep/007/y5376e/y5376e08.htm#bm08.2 
							 c.setRecommendedCornPerHaFertilizerUse(500);
						//	 c.setObservedCornPerHaFertilizerUse(395.5);
							 c.setObservedCornPerHaFertilizerUse(119); 
							 //ref://http://www.fao.org/docrep/007/y5376e/y5376e08.htm#bm08.2 
							 
							 c.setObservedCottonPerHaFertilizerUse(242.2);		
							 
							 //last year land cover
							 if(organicSpace.getLastYearLandUseAt(i, j)==1)
								 c.setLastLandUse(LandUse.DOUBLESOY);
							 if(organicSpace.getLastYearLandUseAt(i, j)==2)
								 c.setLastLandUse(LandUse.SINGLESOY);
							 if(organicSpace.getLastYearLandUseAt(i, j)==3)
								 c.setLastLandUse(LandUse.COTTON);
							 if(organicSpace.getLastYearLandUseAt(i, j)==4)
								 c.setLastLandUse(LandUse.GRASSLAND); //this is pasture
							 if(organicSpace.getLastYearLandUseAt(i, j)==5)
								 c.setLastLandUse(LandUse.FOREST);
							 if(organicSpace.getLastYearLandUseAt(i, j)==6)
								 c.setLastLandUse(LandUse.WATER);
							 if(organicSpace.getLastYearLandUseAt(i, j)==7)
								 c.setLastLandUse(LandUse.BUILDING);
							 if(organicSpace.getLastYearLandUseAt(i, j)==9)
								 c.setLastLandUse(LandUse.SOYCOTTON);
							 
							 //two years ago land cover
							 if(organicSpace.getTwoYearsAgoLandUseAt(i, j)==1)
								 c.setLastLastLandUse(LandUse.DOUBLESOY);
							 if(organicSpace.getTwoYearsAgoLandUseAt(i, j)==2)
								 c.setLastLastLandUse(LandUse.SINGLESOY);
							 if(organicSpace.getTwoYearsAgoLandUseAt(i, j)==3)
								 c.setLastLastLandUse(LandUse.COTTON);
							 if(organicSpace.getTwoYearsAgoLandUseAt(i, j)==4)
								 c.setLastLastLandUse(LandUse.GRASSLAND); //this is pasture
							 if(organicSpace.getTwoYearsAgoLandUseAt(i, j)==5)
								 c.setLastLastLandUse(LandUse.FOREST);
							 if(organicSpace.getTwoYearsAgoLandUseAt(i, j)==6)
								 c.setLastLastLandUse(LandUse.WATER);
							 if(organicSpace.getTwoYearsAgoLandUseAt(i, j)==7)
								 c.setLastLastLandUse(LandUse.BUILDING);
							 if(organicSpace.getTwoYearsAgoLandUseAt(i, j)==9)
								 c.setLastLandUse(LandUse.SOYCOTTON);
							 
							 
					
						/*	 while(query.query().iterator().hasNext()){
								 LandCell x = query.query().iterator().next();
								 System.out.println("big? "+x.cellsize);
								 query.query().iterator().remove();
							 }*/
						//	 Iterator nghIterator = query.query(LandCell.class).iterator();
						
						//	 for(LandCell x: query.query().iterator()){
						//		 System.out.println(query.query().iterator());
						//		 System.out.println(" ");
						//	 }
						//	 System.out.println("created ngh "+query.query(iter));
							 
					 if(c.isTaken()==true || organicSpace.getLandHolder(i, j)>0){
						 
					 }
					 else {
					 if (organicSpace.getLandUseAt(i, j)==1)
					 {
						 agriculturalCells.add(c);
				
							c.setLandUse(LandUse.DOUBLESOY);
							 c.setLandHolder(true,this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);							 
							 c.setFertilizerInput(LandUse.DOUBLESOY);
							 
							
							 
							 
					//		System.out.println("soy");
					 }
						 
						 
					 else if(organicSpace.getLandUseAt(i, j)==2)
						{
						 agriculturalCells.add(c);
							
							c.setLandUse(LandUse.SINGLESOY);
							 c.setLandHolder(true,this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);
						//	 c.setRecommendedSoyPerHaFertilizerUse(300);
							 //from GOEDERT-1983-Journal of soil science
							 //fig.6
							 //400 kg P/ha
							 //it's when soybean yield reaches plateau.
						//	 c.setObservedSoyPerHaFertilizerUse(600);
							 
							 c.setFertilizerInput(LandUse.SINGLESOY);
							 //has to add recommended and observed too
						//	 c.setRecommendedCornPerHaFertilizerUse(500);
						//	 c.setObservedCornPerHaFertilizerUse(700);
					//		System.out.println("soy");
						//	 c.setObservedCottonPerHaFertilizerUse(2290.1);
							
						}
					 else if(organicSpace.getLandUseAt(i, j)==3) {
					        agriculturalCells.add(c);
							
							c.setLandUse(LandUse.COTTON);
							 c.setLandHolder(true,this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);
							 
						//	 c.setRecommendedSoyPerHaFertilizerUse(300);
						//	 c.setObservedSoyPerHaFertilizerUse(600);
							 //has to add recommended and observed too
						//	 c.setRecommendedCornPerHaFertilizerUse(500);
						//	 c.setObservedCornPerHaFertilizerUse(700);
						//	 c.setObservedCottonPerHaFertilizerUse(2290.1);
							 
							 c.setFertilizerInput(LandUse.COTTON);
							 
					 }
					 else if(organicSpace.getLandUseAt(i, j)==4){
				//		 agriculturalCells.add(p);
						 c.setLandUse(LandUse.GRASSLAND);
						 c.setLandHolder(true,this);
						 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
						 this.tenureCells.add(c);
					 } else if(organicSpace.getLandUseAt(i, j)==5){
				//		 agriculturalCells.add(p);
						 c.setLandUse(LandUse.FOREST);
						 c.setLandHolder(true,this);
						 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
						 this.tenureCells.add(c);
					 }
					 else	 if (organicSpace.getLandUseAt(i, j)==9){
							
							agriculturalCells.add(c);
		
							c.setLandUse(LandUse.SOYCOTTON);
							 c.setLandHolder(true,this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);
						//	 c.setRecommendedCornPerHaFertilizerUse(500);
						//	 c.setObservedCornPerHaFertilizerUse(700);					         
					    //     c.setRecommendedSoyPerHaFertilizerUse(300);
						//	 c.setObservedSoyPerHaFertilizerUse(600);
						//	 c.setObservedCottonPerHaFertilizerUse(2290.1);							 
							  c.setFertilizerInput(LandUse.SOYCOTTON);
						} else {
							
						}
				 }
					
					 
					
				}
			 }
					 
				for(int k =0; k<this.tenureCells.size();k++){
					//	 Grid<LandCell> gridCells = (Grid) organicSpace.getProjection("gridSending");
					
						 Grid gridTest = (Grid) organicSpace.getProjection("gridSending");
				//		 System.out.println(this.tenureCells.size());
					    GridPoint pt = gridTest.getLocation(this);
					
					 //    System.out.println(pt.getX()+" "+pt.getY());
					     
					     GridCellNgh<LandCell> nghCreator = new GridCellNgh<LandCell>(gridTest, pt, LandCell.class,2,2);
					     
					     List<GridCell<LandCell>> gridCells = nghCreator.getNeighborhood(true); 
					//     SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
					 
					     this.tenureCells.get(k).setNgh(gridCells);
					  
					     
					/*     System.out.println("ngh "+gridCells.size()+" "+gridCells.get(0).getPoint());
					     
					     
					     for(GridCell<LandCell> cell:gridCells) {
							    	
					    	System.out.println(organicSpace.getLandUseAt(cell.getPoint().getX(), cell.getPoint().getY()));
					    		//	 .iterator().next().getSSCount());
					     }
					   */
					  
					   
	                      
						 VNQuery<LandCell> query = new VNQuery<LandCell>(gridTest,this.tenureCells.get(k), 3,3);
						
						
						 
				}
					}
		
		
			
			 	
	}
	
	   public boolean isOrganicFarm() {
			return organicFarm;
		}


		public void setOrganicFarm(boolean organicFarm) {
			this.organicFarm = organicFarm;
		}


		public boolean isProteleCoupling() {
			return proTeleCoupling;
		}


		public void setProTeleCoupling(boolean proteleCoupling) {
			this.proTeleCoupling = proteleCoupling;
		}

		 public double getCapital() {
				return capital;
			}


			public void setCapital(double capital) {
				this.capital = capital;
			}


			public double getFarmCost() {
				return farmCost;
			}


			public void setFarmCost(double d) {
				this.farmCost = d;
			}


		public double getCredit() {
			return credit;
		}

		public void setCredit(double credit) {
			this.credit = credit;
		}

		   
		   public double getProfit() {
			return profit;
		}

		public void setProfit(double profit) {
			this.profit = profit;
		}
		
		public double getRiceProduction() {
			return riceProduction;
		}

		public double getSoyProduction() {
			return soyTotalProduction;
		}

		public double getCornProduction() {
			return cornProduction;
		}

		public double getOtherProduction() {
			return otherProduction;
		}
		
		public void addTraderAgent(TraderAgent ta){
			this.traderAgents.add(ta);
		}
		   
		   public List<LandCell> getTenureCells() {
				return tenureCells;
			}
		   
		   public List<LandCell> getAgriculturalCells(){
			   return agriculturalCells;
		   }
			
			public double getLabour() {
				return labour;
			}

			public void setLabour(double labour) {
				this.labour = labour;
			}
	
			public void setRiceProduction(double riceProduction) {
				this.riceProduction = riceProduction;
			}


			public void setSoyProduction(double soyProduction) {
				this.soyTotalProduction = soyProduction;
			}


			public void setCornProduction(double cornProduction) {
				this.cornProduction = cornProduction;
			}


			public void setOtherProduction(double otherProduction) {
				this.otherProduction = otherProduction;
			}
			
			public int getSoyCellSize (){
				return soyCells.size();
			}
			
			public int getCornCellSize (){
				return cornCells.size();
			}
			
			public int getRiceCellSize (){
				return riceCells.size();
			}
			
			public int getOtherCellSize(){
				return otherCells.size();
			}
			public int getTenureCellSize(){
				return tenureCells.size();
			}
			
			public double getSoyPerHaYield() {
				return soyPerHaYield;
			}


			public void setSoyPerHaYield(double soyPerHaYield) {
				this.soyPerHaYield = soyPerHaYield;
			}


			public double getCornPerHaYield() {
				return cornPerHaYield;
			}


			public void setCornPerHaYield(double cornPerHaYield) {
				this.cornPerHaYield = cornPerHaYield;
			}


			public double getRicePerHaYield() {
				return ricePerHaYield;
			}


			public void setRicePerHaYield(double ricePerHaYield) {
				this.ricePerHaYield = ricePerHaYield;
			}


			public double getOtherPerHaYield() {
				return otherPerHaYield;
			}


			public void setOtherPerHaYield(double otherPerHaYield) {
				this.otherPerHaYield = otherPerHaYield;
			}


			public double getSoyPerHaFertilizerInput() {
				return soyPerHaFertilizerInput;
			}


			public void setSoyPerHaFertilizerInput(double soyPerHaFertilizerInput) {
				this.soyPerHaFertilizerInput = soyPerHaFertilizerInput;
			}


			public double getCornPerHaFertilizerInput() {
				return cornPerHaFertilizerInput;
			}


			public void setCornPerHaFertilizerInput(double cornPerHaFertilizerInput) {
				this.cornPerHaFertilizerInput = cornPerHaFertilizerInput;
			}


			public double getRicePerHaFertilizerInput() {
				return ricePerHaFertilizerInput;
			}


			public void setRicePerHaFertilizerInput(double ricePerHaFertilizerInput) {
				this.ricePerHaFertilizerInput = ricePerHaFertilizerInput;
			}


			public double getOtherPerHaFertilizerInput() {
				return otherPerHaFertilizerInput;
			}


			public void setOtherPerHaFertilizerInput(double otherPerHaFertilizerInput) {
				this.otherPerHaFertilizerInput = otherPerHaFertilizerInput;
			}


			public double getSoyPerHaFuelInput() {
				return soyPerHaFuelInput;
			}


			public void setSoyPerHaFuelInput(double soyPerHaFuelInput) {
				this.soyPerHaFuelInput = soyPerHaFuelInput;
			}
			
			public void setCottonPerHaFuelInput(double cottonPerHaFuelInput){
				this.cottonPerHaFuelInput = cottonPerHaFuelInput;
			}
			
			public double getCottonPerHaFuelInput(){
				return cottonPerHaFuelInput;
			}


			public double getCornPerHaFuelInput() {
				return cornPerHaFuelInput;
			}


			public void setCornPerHaFuelInput(double cornPerHaFuelInput) {
				this.cornPerHaFuelInput = cornPerHaFuelInput;
			}


			public double getRicePerHaFuelInput() {
				return ricePerHaFuelInput;
			}


			public void setRicePerHaFuelInput(double ricePerHaFuelInput) {
				this.ricePerHaFuelInput = ricePerHaFuelInput;
			}


			public double getOtherPerHaFuelInput() {
				return otherPerHaFuelInput;
			}


			public void setOtherPerHaFuelInput(double otherPerHaFuelInput) {
				this.otherPerHaFuelInput = otherPerHaFuelInput;
			}
			
		
			
			public void setCottonProduction(double cottonProduction) {
				this.cottonProduction = cottonProduction;
			}

			public void setCottonPerHaFertilizerInput(double cottonPerHaFertilizerInput) {
				this.cottonPerHaFertilizerInput = cottonPerHaFertilizerInput;
			}

			public double getInternationalTradeSoyPrice() {
				return internationalTradeSoyPrice;
			}
			
			public double getSoyMaizePerHaFertilizerInput() {
				return soyMaizePerHaFertilizerInput;
			}


			public void setSoyMaizePerHaFertilizerInput(double soyMaizePerHaFertilizerInput) {
				this.soyMaizePerHaFertilizerInput = soyMaizePerHaFertilizerInput;
			}


			public double getSoyCottonPerHaFertilizerInput() {
				return soyCottonPerHaFertilizerInput;
			}


			public void setSoyCottonPerHaFertilizerInput(double soyCottonPerHaFertilizerInput) {
				this.soyCottonPerHaFertilizerInput = soyCottonPerHaFertilizerInput;
			}

           public int getID(){
        	   return this.id;
           }

}
	
	
	
