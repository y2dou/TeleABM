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

import org.apache.bsf.util.LangCell;

import teleABM.OrganicSpace;
import javolution.util.FastMap;
import javolution.util.FastTable;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialException;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridDimensions;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.OpenLongToDoubleHashMap.Iterator;
import repast.simphony.valueLayer.GridValueLayer;

import teleABM.Point;

import teleABM.SimpleAgent;



/**
 * @author DOU Yue
 *
 */
public class SoybeanAgent extends SimpleAgent{
	
    private Integer id;
    private static final AtomicInteger idGenerator = new AtomicInteger (0);
    protected Point location;
    protected Context<?> context;
    Parameters p = RunEnvironment.getInstance().getParameters();
	int numAgents = (Integer)p.getValue("initialNumAgents");
    
	   protected List<TraderAgent> traderAgents = new LinkedList<TraderAgent>();
	   public TraderAgent soySoldToTraderAgent ;
	   public TraderAgent cornSoldToTraderAgent;
	   public TraderAgent riceSoldToTraderAgent;
	   public TraderAgent otherSoldToTraderAgent;
	   
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

	protected double fertilizerUnitCost=100; //price of fertilizer
	protected double fuelUnitCost=5;  //unit price for fuel
	
	protected double riceProduction = 0;
	protected double soyProduction = 0;	
	protected double cornProduction = 0;
	protected double otherProduction = 0;
	protected boolean grownRice = false;
	protected boolean grownSoy = false;
	protected int grownSoyYears=0;
	protected boolean grownCorn = false;
	protected boolean grownOther = false;
	   
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
		  int xdim = (Integer)para.getValue("worldWidth");
		  int ydim = (Integer)para.getValue("worldHeight");
		  int cellsize = (Integer) para.getValue("cellSize");
		protected  int vision = (Integer) para.getValue("vision");
		
		protected List<LandCell> tenureCells = new LinkedList<LandCell>();
		protected  FastTable<LandCell> soyCells = new FastTable<LandCell>();
		protected  FastTable<LandCell> cornCells = new FastTable<LandCell>();
		protected FastTable<LandCell> riceCells = new FastTable<LandCell>();
		protected  FastTable<LandCell> otherCells = new FastTable<LandCell>();
	//	protected MyLandCell tenure;
		 
		  //to place agents at best location
		
		protected double soyPerHaYield;
		protected double cornPerHaYield;
		protected double ricePerHaYield;
		protected double otherPerHaYield;
		
		protected double soyPerHaFertilizerInput;
		protected double cornPerHaFertilizerInput;
		protected double ricePerHaFertilizerInput;
		protected double otherPerHaFertilizerInput;
		
		protected double soyPerHaFuelInput=10;
		protected double cornPerHaFuelInput=50;
		protected double ricePerHaFuelInput=20;
		protected double otherPerHaFuelInput=30;
		//above fuel unit cost are all simplifed
		
		protected double lastYearSoyPrice;
		protected double lastYearCornPrice;
		protected double lastYearRicePrice;
		protected double lastYearOtherPrice;
		
		protected double lastYearSoyPerHaProfit;
		protected double lastYearCornPerHaProfit;
		protected double lastYearRicePerHaProfit;
		protected double lastYearOtherPerHaProfit;
		
		protected List<Double> soyPrices = new LinkedList<Double>();
		protected List<Double> cornPrices = new LinkedList<Double>();
		protected List<Double> ricePrices = new LinkedList<Double>();
		protected List<Double> otherPrices = new LinkedList<Double>();
		
		protected int priceMemoryLimit = 3;
		
		 private Map<LandUse, ArrayList<Double>> prices = new HashMap<LandUse, ArrayList<Double>>();
		    Map<LandUse, InputStream> priceStreams;
		    MarketPrices marketPrices = new MarketPrices();
		    protected LinkedList<LandUse> commodityType =
				       new LinkedList<LandUse>();
	   
		 public SoybeanAgent() {
			this(idGenerator.getAndIncrement());

		}


		public SoybeanAgent(int id) {
			this.id = id;

		}
		

		public void initialize() {
		// TODO Auto-generated method stub			   
			
			setFarmCost(RandomHelper.getDistribution("farmCost").nextDouble());	
			 
			    setCapital(RandomHelper.getDistribution("capital").nextDouble());
			  
			    setLabour(RandomHelper.getDistribution("labour").nextDouble());
			    
		//	    setCredit(RandomHelper.getDistribution("credit").nextDouble());		
		//	    System.out.println("initialization done" + this.getCredit());
			    setProTeleCoupling(true);
			    setOrganicFarm(true);
			    
			    setCommodityType(LandUse.SOY);
				setCommodityType(LandUse.CORN);
				setCommodityType(LandUse.RICE);
				setCommodityPrices();
//				System.out.println("initialization general "+
	//			this.getCommodityPrice(LandUse.SOY));
						
			   
	}
		
  
	


		public void setCommodityPrices(){
	    	 Parameters p = RunEnvironment.getInstance().getParameters();
	    	// System.out.println ("soyPrice =" + (Double) p.getValue("soyPrice"));
			// load data: prices
			Map<LandUse, InputStream> priceLists = new HashMap<LandUse, InputStream>();
					
			//check if soyprice is static or dynamic
			try {
				double staticPrice = (Double) p.getValue("soyPrice");
				
				
				if (staticPrice >= 0) {
					marketPrices.setPrice(LandUse.SOY, staticPrice);
			//		System.out.println ("soyPrice =" + staticPrice);
				} else {
				//	priceLists.put(LandUse.SOY, new FileInputStream("auxdata/prices/soyPrice.txt"));
					priceLists.put(LandUse.SOY, new FileInputStream("auxdata/prices/soyPriceTest.txt"));
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			
			//check if corn price is static or dynamic
			try {
				double staticPrice = (Double) p.getValue("cornPrice");
				if (staticPrice >= 0) {
					marketPrices.setPrice(LandUse.CORN, staticPrice);
				} else {
					priceLists.put(LandUse.CORN, new FileInputStream("auxdata/prices/corn.prices.txt"));
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			//check if rice price is static or dynamic
			try {
				double staticPrice = (Double) p.getValue("ricePrice");
				if (staticPrice >= 0) {
					marketPrices.setPrice(LandUse.RICE, staticPrice);
				} else {
					priceLists.put(LandUse.RICE, new FileInputStream("auxdata/prices/rice.prices.txt"));
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			//check if other price is static or dynamic
			try {
				double staticPrice = (Double) p.getValue("otherPrice");
				if (staticPrice >= 0) {
					marketPrices.setPrice(LandUse.OTHERCROPS, staticPrice);
				} else {
					priceLists.put(LandUse.OTHERCROPS, new FileInputStream("auxdata/prices/other.prices.txt"));
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			
	    	if (!priceLists.isEmpty()) {
	    		Map<LandUse, InputStream> priceStreams = priceLists;
	    	//	System.out.println(priceLists.size());
	    	for (Map.Entry<LandUse, InputStream> e : priceStreams.entrySet()) {
				ArrayList<Double> cPrices = new ArrayList<Double>(60);
				
				
				try {
					Reader r = new BufferedReader(new InputStreamReader(e.getValue()));
					StreamTokenizer st = new StreamTokenizer(r);
					
					// initialize parser
					st.parseNumbers();
					st.eolIsSignificant(false);
					st.whitespaceChars(',', ',');
					
					while (true) {
						st.nextToken();
						if (st.ttype == StreamTokenizer.TT_EOF)
							break;
						else if (st.ttype == StreamTokenizer.TT_NUMBER) {
							cPrices.add(st.nval +RandomHelper.nextDoubleFromTo(-0.01, 0.05));
							//this is to add some randomness of each agent's price offer
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					try {
						e.getValue().close();
					} catch (IOException e1) {}
				}
				prices.put(e.getKey(), cPrices);
						}
	    	}
	//    	System.out.println(this.getID()+" rice price "+prices.get(LandUse.RICE));
	 //the following should only be temperary solution because this is 
	 //for receiving systems 
	    	soyPrices.add(0, 1.31);
	    	soyPrices.add(1, 2.27);
	    	soyPrices.add(2, 3.20);
	    	cornPrices.add(0, 0.6);
	    	cornPrices.add(1, 0.59);
	    	cornPrices.add(2, 0.56);
	    	ricePrices.add(0, 1.3);
	    	ricePrices.add(1, 1.32);
	    	ricePrices.add(2, 1.32);
	    }
	    
	    public double getCommodityPrice(LandUse landuse){
	    //	return MarketPrices.get(landuse);
	    	//here is only static price;
	    	//needs to solve dynamic price;
	    	double tempPrice=0;
	    	int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	    	
	    //	System.out.println("tick at trader "+tick);
	    	if(commodityType.contains(landuse)) {
	    //		System.out.println("commodity type "+commodityType.contains(landuse));
	    	 if(prices.containsKey(landuse))  
	    			 {
	    	     
	    		 tempPrice=prices.get(landuse).get(tick-1);
	    		 //note that it has to be get(tick-1), otherwise it's more than what it needs.
	    		 double r = new Random().nextDouble();
	    		 tempPrice= tempPrice+ (2*r-1.0)*0.05;  
	    		 //this gives p a natural 0.05% variation of the price
	    			 } 
	    	 else 
	    	 {   
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
	    
	    
	    
	
	@ScheduledMethod(start = 1, interval = 1)
	  public void step() {
	// this is to calculate last year's profit and make this year's decision
		
	//   OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
	 //   Grid grid = (Grid) organicSpace.getProjection("Grid");
		
		   riceProduction = 0;
		   cornProduction = 0;
		   soyProduction = 0;
		   otherProduction = 0;
		   //every year, set all production = 0;
		   
		   //clear last year's crop cells. 
		   
		   
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		//	tick has to be called everytime it is used;
	//	 System.out.println("tick "+tick);
	    int count=0;
	 
	  
	    
	//    updateLabor();
	  // to start maybe not count labour;
	    landUseDecision();
	//make decision first
	    //following are actual land use change;
	    
	   for(int i =0;i< tenureCells.size();i++) {
		    LandCell c = tenureCells.get(i);
		 //   updateLandUse(c);
		 //   tenureCells.get(i).
		//    landUseDecision();
		    c.transition();
		   	
		 //   if (c.getLastLandUse()==LandUse.RICE) {
		    if (c.getLandUse()==LandUse.RICE) { 	
		 //   	riceCells.add(c);
		    	riceProduction+=c.getCropYield();
		//    	System.out.println(c.getCropYield());
		    	
		  //  	System.out.println("rice++");
		    } else if (c.getLandUse()==LandUse.CORN) {
		  //  	cornCells.add(c);
		    	cornProduction+=c.getCropYield();
		    	
		    } else if (c.getLandUse()==LandUse.SOY) {
		   // 	soyCells.add(c);
		    	soyProduction+=c.getCropYield();
		    //	  System.out.println("soyyiled= "+c.getCropYield());
		    	
		    } else if(c.getLandUse()==LandUse.OTHERCROPS){
		   // 	otherCells.add(c);
		    	otherProduction+=c.getCropYield();
		    	
		    }
	   }
	   
	 
	//   System.out.println(riceProduction);
	   if (riceProduction>0) grownRice=true;
	   if (cornProduction>0) grownCorn=true;
	   if (soyProduction>0) { grownSoy=true; grownSoyYears=grownSoyYears+1;
	//   System.out.println("has soy production"+grownSoyYears);}
	   }
	   if (otherProduction>0) grownOther=true;
	   
	   updateCost();
//	   setCommodityPrices();
//	   decidingTradingPartner();
	   updateProfit();
	   //also update the price memory list
	   setGrownSoyYears(grownSoyYears);
	setSoyProduction(soyProduction);
	setCornProduction(cornProduction);
	setRiceProduction(riceProduction);
	setOtherProduction(otherProduction);
	//   System.out.println("this year profit="+profit);
	//   updateLandUse();
	//   System.out.println(tick+" "+this.getID()+" rice Production "+riceProduction);
	//   System.out.println(tick+" "+this.getID()+" corn Production "+cornProduction);
	//   System.out.println(tick+" "+this.getID()+" soy Production "+soyProduction);
	//   System.out.println(tick+" "+this.getID()+" other Production "+otherProduction);
	//  System.out.println("soySize = "+this.soyCells.size());
	//   System.out.println("grown soy years = "+this.getGrownSoyYears());
	}
	

	
public int getGrownSoyYears() {
		return grownSoyYears;
	}


	public void setGrownSoyYears(int grownSoyYears) {
		this.grownSoyYears = grownSoyYears;
	}


	//	}
	public void landUseDecision() {
		//redirect to sending/receiving soybeanagent;
	}
	public void updateLandUse(LandCell c) {
//	c.setLandUse(LandUse.RICE);
		
	}
	
	private void updateLabor() {
		// TODO Auto-generated method stub
		//to represent the household demography dynamics
		double random = RandomHelper.nextDouble();
		Random r = new Random();
		//double randomValue = -0.03 + 2 * r.nextDouble();
		double randomValue = r.nextDouble();
		double labourCost;
		labourCost=
				this.soyCells.size()*0.002+
				this.cornCells.size()*0.001+
				this.riceCells.size()*0.003+
				this.otherCells.size()*0.002;
	
		this.setLabour(this.getLabour()-labourCost+randomValue);
		
		//here can implement the machinery, if they can hire more and bigger machines, 
		//then the labor use or requirement is lower. 
	}

	private void updateCost(){
		
		double totalCost = 0;
		double cost=0;
		if(soyCells.size()>0){
		for (int i=0; i<soyCells.size();i++){
			//setFertilizerInput() has already been put at land use decision ();
			totalCost+=soyCells.get(i).getFertilizerInput()*fertilizerUnitCost;
			cost+=soyCells.get(i).getFertilizerInput();
		}
		soyPerHaFertilizerInput=cost/soyCells.size();
	//	System.out.println("soy average cost"+ soyUnitFertilizerInput);
		cost=0;
		}
		if(cornCells.size()>0){
			for (int i=0; i<cornCells.size();i++){
				totalCost+=cornCells.get(i).getFertilizerInput()*fertilizerUnitCost;
				cost+=cornCells.get(i).getFertilizerInput();
			}
			cornPerHaFertilizerInput=cost/cornCells.size();
	//		System.out.println("soy average cost"+ cornUnitFertilizerInput);
			cost=0;
			}
		
		if(riceCells.size()>0){
			for (int i=0; i<riceCells.size();i++){
				totalCost+=riceCells.get(i).getFertilizerInput()*fertilizerUnitCost;
				cost+=riceCells.get(i).getFertilizerInput();
			}
			ricePerHaFertilizerInput=cost/riceCells.size();
	//		System.out.println("rice average cost"+ riceUnitFertilizerInput);
			cost=0;
			}
		
		if(otherCells.size()>0){
			for (int i=0; i<otherCells.size();i++){
				totalCost+=otherCells.get(i).getFertilizerInput()*fertilizerUnitCost;
				cost+=otherCells.get(i).getFertilizerInput();
			}
			otherPerHaFertilizerInput=cost/otherCells.size();
	//		System.out.println("other average cost"+ otherUnitFertilizerInput);
			cost=0;
			}
		
		
		
	//	capital-=totalCost;
		
		
	}
	
	
	public void decidingTradingPartner(){
		
	}
	
 public void setCommodityType(LandUse commodityT) {
		   //if commodityType ==1 soy
		   //if commodity type==2 corn
		   //if commodity type ==3 rice
		//  this.commodityType = commodityType;
		  this.commodityType.add(commodityT);
	   }

private void updateProfit(){
	
	double yield=0;
	double cPrice=0;
	//current year price	
	profit=0;
	
//	System.out.println("update profit at soybean abstract: "
//	                +tick+": "+ this.getCommodityPrice(LandUse.SOY));
	
	cPrice=this.getCommodityPrice(LandUse.SOY);
	soyPrices.add(cPrice);
	if (soyPrices.size()>priceMemoryLimit) {
		soyPrices.remove(0); //remove least recent price
	   
	}
	
	cPrice=this.getCommodityPrice(LandUse.CORN);
	cornPrices.add(cPrice);
	if (cornPrices.size()>priceMemoryLimit) {
		cornPrices.remove(0); //remove least recent price
	}
	cPrice=this.getCommodityPrice(LandUse.RICE);
	ricePrices.add(cPrice);
	if (ricePrices.size()>priceMemoryLimit) {
		ricePrices.remove(0); //remove least recent price
	}
	
	if (grownSoy) {
		double soyYield=0;
		for (int i=0; i < soyCells.size();i++) {
			yield = soyCells.get(i).getCropYield();
			soyYield+=yield;
		//	cPrice=this.soySoldToTraderAgent.getCommodityPrice(LandUse.SOY);
			
			cPrice=this.getCommodityPrice(LandUse.SOY);

			
	//		System.out.println("update profit: "+tick+ " "+
	//		         cPrice);
			profit+=yield*cPrice;		
	//		this.soySoldToTraderAgent.addSoyAmount(yield);
	//		this.soySoldToTraderAgent.purchaseCommodity(yield*cPrice);
			
		}
		soyPerHaYield = soyYield/soyCells.size();
		lastYearSoyPerHaProfit = soyPerHaYield*cPrice-soyPerHaFuelInput*fuelUnitCost
				                 -soyPerHaFertilizerInput*fertilizerUnitCost;
	}
	
//	System.out.println("soy yield: "
 //           +this.getCommodityPrice(LandUse.CORN));
	if (grownCorn) {
		yield=0;
		double cornYield=0;
		for (int i =0; i <cornCells.size();i++) {
			yield=cornCells.get(i).getCropYield();
			cornYield+=yield;
	//		cPrice=this.cornSoldToTraderAgent.getCommodityPrice(LandUse.CORN);		
			cPrice=this.getCommodityPrice(LandUse.CORN);

			profit+=yield*cPrice;
	//		this.cornSoldToTraderAgent.addCornAmount(yield);
	//		this.cornSoldToTraderAgent.purchaseCommodity(yield*cPrice);
		}		
		cornPerHaYield=cornYield/cornCells.size();
		lastYearCornPerHaProfit = cornPerHaYield*cPrice-cornPerHaFuelInput*fuelUnitCost
                -cornPerHaFertilizerInput*fertilizerUnitCost;
	}
	if (grownRice) {
		yield=0;
		double riceYield=0;
	//	System.out.println("rice cells: "+riceCells.size());
		for (int i =0; i <riceCells.size();i++) {
			yield=riceCells.get(i).getCropYield();
			riceYield+=yield;
	//		cPrice=this.riceSoldToTraderAgent.getCommodityPrice(LandUse.RICE);
			cPrice=this.getCommodityPrice(LandUse.RICE);
			profit+=yield*cPrice;
	//		this.riceSoldToTraderAgent.addRiceAmount(yield);
	//		this.riceSoldToTraderAgent.purchaseCommodity(yield*cPrice);
		}	
		ricePerHaYield=riceYield/riceCells.size();
		lastYearRicePerHaProfit = ricePerHaYield*cPrice-ricePerHaFuelInput*fuelUnitCost
                -ricePerHaFertilizerInput*fertilizerUnitCost;
	}
	if (grownOther) {
		yield=0;
		double otherYield=0;
		for (int i=0; i<otherCells.size();i++) {
			yield=otherCells.get(i).getCropYield();
			otherYield+=yield;
	//		cPrice=this.otherSoldToTraderAgent.getCommodityPrice(LandUse.OTHERCROPS);
			cPrice=this.getCommodityPrice(LandUse.OTHERCROPS);
			otherPrices.add(cPrice);
			if (otherPrices.size()>priceMemoryLimit) {
				otherPrices.remove(0); //remove least recent price
			}
			profit+=yield*cPrice;
	//		this.otherSoldToTraderAgent.addOtherAmount(yield);
	//		this.otherSoldToTraderAgent.purchaseCommodity(yield*cPrice);
		}
		otherPerHaYield=otherYield/otherCells.size();
		lastYearOtherPerHaProfit = otherPerHaYield*cPrice-otherPerHaFuelInput*fuelUnitCost
                -otherPerHaFertilizerInput*fertilizerUnitCost;
	}
	
	capital+=profit;
	}
	
	

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
		    	double random = RandomHelper.nextDouble();
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
	        
	    	double tenureSize =  Math.round(RandomHelper.getDistribution("hectares").nextDouble()*
	    			10000d/ (cellsize * cellsize));
	    	
	   // 	System.out.println(this.getID()+" get hectares "+tenureSize);
	    	
			 for (int i=corner.x+randomX; i<(corner.x+Math.sqrt(tenureSize)+randomX);i++)  {
				 for (int j=corner.y+randomY;j<(corner.y+Math.sqrt(tenureSize)+randomY);j++){
					 Point p=new Point(i,j);
				
					 
					 if (TeleABMBuilder.receivingSystem&&!TeleABMBuilder.sendingSystem){
					
					 if (organicSpace.getLandUseAt(i, j)>=2 && organicSpace.getLandUseAt(i, j)<=4)
						{		
							agriculturalCells.add(p);
					
					
						}
					 else if (organicSpace.getLandUseAt(i, j)==6){
							
							agriculturalCells.add(p);
		
						} else {
							
						}
					}
					 
					 if (TeleABMBuilder.sendingSystem && !TeleABMBuilder.receivingSystem){
				
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
             
              addLandUseFromField(organicSpace);
        
        		
           
	}
	
	
	public void addLandUseFromField(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
	
	// 	System.out.println("tenure Size "+this.getTenureCells().size());
	 	int count=0;
		   for (int i=0; i< this.getTenureCells().size(); i++) {
		    	int x = this.getTenureCells().get(i).getXlocation();
		    	int y = this.getTenureCells().get(i).getYlocation();
		    	
		  //  	double random = RandomHelper.nextDouble();
		  //  	System.out.println("thislandcell "+ organicSpace.getLandUseAt(x, y));
		    	 //problem here is it puts all kinds of land use to the agent
		    	
		    	if (TeleABMBuilder.receivingSystem){
		    	if (organicSpace.getLandUseAt(x, y)==2){
		    		this.getTenureCells().get(i).setLandUse(LandUse.SOY);
		    		this.getTenureCells().get(i).setLastLandUse(LandUse.SOY);
	
		    	} else if (organicSpace.getLandUseAt(x, y)==3) {
		    		this.getTenureCells().get(i).setLandUse(LandUse.RICE);
		    		this.getTenureCells().get(i).setLastLandUse(LandUse.RICE);
		    	} else if (organicSpace.getLandUseAt(x, y)==6) {
		    		this.getTenureCells().get(i).setLandUse(LandUse.CORN);
		    		this.getTenureCells().get(i).setLastLandUse(LandUse.CORN);
		    	} else if (organicSpace.getLandUseAt(x, y)==4) {
		    		this.getTenureCells().get(i).setLandUse(LandUse.OTHERCROPS);
		    		this.getTenureCells().get(i).setLastLandUse(LandUse.OTHERCROPS);
		    	} else if (organicSpace.getLandUseAt(x, y)==5){
		    		this.getTenureCells().get(i).setLandUse(LandUse.FOREST); 
		    	} else if (organicSpace.getLandUseAt(x, y)==7){		    		
		    		this.getTenureCells().get(i).setLandUse(LandUse.BUILDING); 
		    	} else {
		    		this.getTenureCells().get(i).setLandUse(LandUse.WATER); 
		    	}}
		    	
		    	if (TeleABMBuilder.sendingSystem){
		    		if (organicSpace.getLandUseAt(x, y)==1){
			    		this.getTenureCells().get(i).setLandUse(LandUse.SOY);
			//    		count++;
			    	} else if (organicSpace.getLandUseAt(x, y)==2) {
			    		this.getTenureCells().get(i).setLandUse(LandUse.SECONDSOY);
			    	} else if (organicSpace.getLandUseAt(x, y)==3) {
			    		this.getTenureCells().get(i).setLandUse(LandUse.COTTON);
			    	} else {
			    		this.getTenureCells().get(i).setLandUse(LandUse.OTHERCROPS);//cutton
			    	}
		    	}
	//	    	System.out.println("this agent "+i+" has "+this.getTenureCells().get(i).getLandUse());
		    		
		    }
	//	   if(count>0&&this.getID()>30) System.out.println(this.getID()+" "+count);
	}


	public void addSoybeanAgentFromLandscape(OrganicSpace organicSpace,
			Point corner) {
		// TODO Auto-generated method stub
		//this is to count all avaialbe land use cells, and put them in a list,
		//and randomly assign them to different hhd agents
		
		  double xboundary;
		   double yboundary;
			    
		
		   double perAgentArea;
		   
		perAgentArea = (double) xdim*ydim/numAgents;
			    
		   xboundary = Math.sqrt(perAgentArea*((double)xdim/(double)ydim));
		   yboundary = xboundary*ydim/xdim;
		   
		 
		Grid<Object> grid = (Grid) organicSpace.getProjection("Grid");
		
		    GridPoint point = grid.getLocation(this);
		    grid.moveTo(this, corner.x, corner.y);
		    
		    List<Point> agriculturalCells = new LinkedList<Point>();
		    
	//	   System.out.println(corner.x+" "+(corner.x+xboundary));
	//	   System.out.println(corner.y+" "+(corner.y+yboundary));
		 //first, go through all agricultural cells in this sub-section   
		 for (int i=corner.x; i<(corner.x+xboundary);i++)  {
			 for (int j=corner.y;j<(corner.y+yboundary);j++){
				 Point p=new Point(i,j);
			//	 System.out.println("point"+organicSpace.getLandUseAt(p.x, p.y));
				 if (TeleABMBuilder.receivingSystem){
				 if (organicSpace.getLandUseAt(i, j)>=2&&organicSpace.getLandUseAt(i, j)<=4)
					{
						
						agriculturalCells.add(p);
				//		System.out.println("soy");
					}
				 else if (organicSpace.getLandUseAt(i, j)==6){
						
						agriculturalCells.add(p);
				//		System.out.println("corn");
					} else {
						//System.out.println("nothing");
					}
				 }
				 
				 if (TeleABMBuilder.sendingSystem){
					 if (organicSpace.getLandUseAt(i, j)>=1&&organicSpace.getLandUseAt(i, j)<=3)
						{
							
							agriculturalCells.add(p);
					//		System.out.println("soy");
						}
					 else if (organicSpace.getLandUseAt(i, j)==9){
							
							agriculturalCells.add(p);
					//		System.out.println("corn");
						} else {
							//System.out.println("nothing");
						}
				 }
				}
		//	 System.out.println("yboundary finished once "+i);
		 }
		  
	
		    
		     for (int i=0; i<agriculturalCells.size();i++){
		    	 Point p = agriculturalCells.get(i);
		    	 LandCell c = new LandCell(organicSpace,grid,p.x,p.y,
	 					 organicSpace.getElevationAt(p.x, p.y),
	 					 organicSpace.getOrganicAt(p.x, p.y));
		  //  	 System.out.println("initial soc-- "+organicSpace.getOrganicAt(p.x, p.y));    
				  c.setLandHolder(true,this);
		//		  c.setSoc(organicSpace.getOrganicAt(p.x, p.y));
			      
				  c.setLastLandUse(c.getLandUse());
				  this.tenureCells.add(c);
				  organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
				  
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
			return soyProduction;
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
				this.soyProduction = soyProduction;
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
}
	
	
	
