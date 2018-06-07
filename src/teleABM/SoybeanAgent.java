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
    private static final AtomicInteger idGenerator = new AtomicInteger (1);
    protected Point location;
    protected Context<?> context;
    Parameters p = RunEnvironment.getInstance().getParameters();
	int numReceivingAgents = (Integer)p.getValue("initialReceivingNumAgents");
    int numSendingAgents = (Integer)p.getValue("initialSendingNumAgents");
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

	protected double fertilizerUnitCost; //price of fertilizer 
	
	protected double fuelUnitCost;  //unit price for diesel,
	
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
		  int receivingXdim = (Integer)para.getValue("receivingWorldWidth");
		  int receivingYdim = (Integer)para.getValue("receivingWorldHeight");
		  int sendingXdim = (Integer)para.getValue("sendingWorldWidth");
		  int sendingYdim = (Integer)para.getValue("sendingWorldHeight");
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
		
		protected double soyPerHaFuelInput;		
		protected double cornPerHaFuelInput;
		protected double ricePerHaFuelInput;
		protected double otherPerHaFuelInput;
		//above fuel unit cost are all from direct fossil fuel energy cost (in tab energy in Equations.xlsx) 
		//
		
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
			//	setCommodityType(LandUse.COTTON);
				setCommodityPrices();
			}
			
		//	if(TeleABMBuilder.sendingSystem){
			if(organicSpace.getTypeID()=="organicSpaceSending"){	
				setFarmCost(RandomHelper.getDistribution("farmCostSending").nextDouble());	
			    setCapital(RandomHelper.getDistribution("capitalSending").nextDouble());
		        setLabour(RandomHelper.getDistribution("labourSending").nextDouble());
			    setProTeleCoupling(true);
			    setOrganicFarm(true);
			    
			    setCommodityType(LandUse.SOY);
				setCommodityType(LandUse.CORN);
				setCommodityType(LandUse.RICE);
				setCommodityType(LandUse.COTTON);
				setCommodityPrices();
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
	  //  	System.out.println(this.getID()+" soy price "+prices.get(LandUse.SOY));
	 //the following should only be temperary solution because this is 
	 //for receiving systems 
	   	   soyPrices.add(0, 1.31+RandomHelper.nextDoubleFromTo(-0.01, 0.05));
	       cornPrices.add(0, 0.6+RandomHelper.nextDoubleFromTo(-0.01, 0.05));
	       ricePrices.add(0, 1.3+RandomHelper.nextDoubleFromTo(-0.01, 0.05));
	   //have to assign the first year price here becuase the price is only updated 
	       //at "update profit"
	       //which won't happen after land use decision
	       
	  /*  	soyPrices.add(1, 2.27);
	    	soyPrices.add(2, 3.20);
	    	
	    	cornPrices.add(1, 0.59);
	    	cornPrices.add(2, 0.56);
	    	
	    	ricePrices.add(1, 1.32);
	    	ricePrices.add(2, 1.32);*/
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
	    	     
	    		 tempPrice=prices.get(landuse).get(tick);
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
		
	   OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
	 //   Grid grid = (Grid) organicSpace.getProjection("Grid");
	   
	   if(organicSpace.getTypeID()=="organicSpaceSending") {
			this.landUseDecision(organicSpace);
		}
		  
		
		if(organicSpace.getTypeID()=="organicSpaceReceiving") 
		{
	//		 System.out.println("come on");
		   this.soyCells.clear();
		   this.cornCells.clear();
		   this.riceCells.clear();
		   this.otherCells.clear();
		   
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
	    System.out.println("receiving tenure "+this.tenureCells.size());
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
		    	riceProduction+=c.getCropYield();
		//    	System.out.println(c.getCropYield());
		    	
		  //  	System.out.println("rice++");
		    } else if (c.getLandUse()==LandUse.CORN) {
		    	this.cornCells.add(c);
		    	cornProduction+=c.getCropYield();
		    	
		    } else if (c.getLandUse()==LandUse.SOY) {
		    	this.soyCells.add(c);
		    	soyProduction+=c.getCropYield();
		    	
		//   	  System.out.println("soy yiled= "+c.getCropYield());
		    	
		    } else if(c.getLandUse()==LandUse.OTHERCROPS){
		   	    this.otherCells.add(c);
		    	otherProduction+=c.getCropYield();
		    	
		    }
	   }
	   
	 
	//   System.out.println(riceProduction);
	   if (riceProduction>0) this.grownRice=true;
	   if (cornProduction>0) this.grownCorn=true;
	   if (soyProduction>0) { this.grownSoy=true; this.grownSoyYears=grownSoyYears+1;
	   System.out.println("has soy production "+grownSoyYears);}
	   
	   if (otherProduction>0) this.grownOther=true;
	   
	   updateCost();
//	   setCommodityPrices();
//	   decidingTradingPartner();
	  
	   //also update the price memory list
	   this.setGrownSoyYears(grownSoyYears);
	this.setSoyProduction(soyProduction);
	this.setCornProduction(cornProduction);
	this.setRiceProduction(riceProduction);
	this.setOtherProduction(otherProduction);
	
	 
	//   System.out.println("this year profit="+profit);
	//   updateLandUse();
	//   System.out.println(tick+" "+this.getID()+ this.soyCells.size()+this.cornCells.size()+this.riceCells.size());
//	   System.out.println(tick+" "+this.getID()+ this.getSoyCellSize()+this.getCornCellSize()+this.getRiceCellSize());
	//   System.out.println(" corn Production "+cornProduction);
	//   System.out.println(" soy Production "+soyProduction);
	//   System.out.println(tick+" "+this.getID()+" other Production "+otherProduction);
	//  System.out.println("soy Size last year = "+this.soyCells.size());
	//  System.out.println("corn size last year="+this.cornCells.size());
	//  System.out.println("rice size last year="+this.riceCells.size());
	
	  
	  updateProfit();
	//   System.out.println("grown soy years = "+this.getGrownSoyYears());
//	  if(TeleABMBuilder.receivingSystem)
//	  OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
	  this.landUseDecision(organicSpace);
		}
		
	
	  
	}
	

	
public int getGrownSoyYears() {
		return grownSoyYears;
	}


	public void setGrownSoyYears(int grownSoyYears) {
		this.grownSoyYears = grownSoyYears;
	}


	//	}
	public void landUseDecision(OrganicSpace organicSpace) {
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
		//perHaFertilizerInput is the average fertilizer usage by the farmer. 
		double totalCost = 0;
	//	double cost=0;
		double totalfertilizeruse=0;
		if(soyCells.size()>0){
		for (int i=0; i<soyCells.size();i++){
			//setFertilizerInput() has already been put at land use decision ();
			totalCost+=soyCells.get(i).getFertilizerInput()*fertilizerUnitCost;
			//		cost+=soyCells.get(i).getFertilizerInput()*fertilizerUnitCost;
			
			totalfertilizeruse+= soyCells.get(i).getFertilizerInput();
		//	System.out.println("cell no. "+i+" use fertilizer "+soyCells.get(i).getFertilizerInput());
		}
		soyPerHaFertilizerInput=totalfertilizeruse/soyCells.size();
				
		soyPerHaFertilizerInput = soyPerHaFertilizerInput/(cellsize*cellsize)*10000.0;
		
		setSoyPerHaFertilizerInput(soyPerHaFertilizerInput);
	//	System.out.println("soy cell size = "+soyCells.size());
	//	System.out.println(totalfertilizeruse);
        //soyPerHaFertilizerInput is the average fertilizer amount for all soy cells.
		
		//	System.out.println("soy average cost"+ soyUnitFertilizerInput);
	//	cost=0;
		totalfertilizeruse = 0;
		}
		if(cornCells.size()>0){
			for (int i=0; i<cornCells.size();i++){
				totalCost+=cornCells.get(i).getFertilizerInput()*fertilizerUnitCost;
		//		cost+=cornCells.get(i).getFertilizerInput();
				totalfertilizeruse+=cornCells.get(i).getFertilizerInput();
				if(cornCells.get(i).getFertilizerInput()<0)
				  {
					System.out.println(cornCells.get(i).getObservedCornPerHaFertilizerUse());
					System.out.println("cell no. "+i+" use fertilizer "+cornCells.get(i).getFertilizerInput());
				  }
				}
			cornPerHaFertilizerInput=totalfertilizeruse/cornCells.size();
			cornPerHaFertilizerInput = cornPerHaFertilizerInput/(cellsize*cellsize)*10000.0;
			setCornPerHaFertilizerInput(cornPerHaFertilizerInput);
	//		System.out.println("soy average cost"+ cornUnitFertilizerInput);
	//		cost=0;
			totalfertilizeruse=0;
			}
		
		if(riceCells.size()>0){
			for (int i=0; i<riceCells.size();i++){
				totalCost+=riceCells.get(i).getFertilizerInput()*fertilizerUnitCost;
	//			cost+=riceCells.get(i).getFertilizerInput();
				totalfertilizeruse+=riceCells.get(i).getFertilizerInput();
			}
			ricePerHaFertilizerInput=totalfertilizeruse/riceCells.size();
			ricePerHaFertilizerInput = ricePerHaFertilizerInput/(cellsize*cellsize)*10000.0;
			setRicePerHaFertilizerInput(ricePerHaFertilizerInput);
	//		System.out.println("rice average cost"+ riceUnitFertilizerInput);
	//		cost=0;
			totalfertilizeruse = 0;
			}
		
		if(otherCells.size()>0){
			for (int i=0; i<otherCells.size();i++){
				totalCost+=otherCells.get(i).getFertilizerInput()*fertilizerUnitCost;
	//			cost+=otherCells.get(i).getFertilizerInput();
			    totalfertilizeruse+=otherCells.get(i).getFertilizerInput();
			}
			otherPerHaFertilizerInput=totalfertilizeruse/otherCells.size();
			otherPerHaFertilizerInput = otherPerHaFertilizerInput/(cellsize*cellsize)*10000.0;
			setOtherPerHaFertilizerInput(otherPerHaFertilizerInput);
	//		System.out.println("other average cost"+ otherUnitFertilizerInput);
	//		cost=0;
			}
		
		
		
	//	capital-=totalCost;
		//cost has been taken out at decisionmaking() at receiving soybean agent class
		
		
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
	   
	int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
	//	tick has to be called everytime it is used;
	double cPrice=0;
	
//	lastYearSoyPerHaProfit=0;
//	lastYearCornPerHaProfit=0;
//	lastYearRicePerHaProfit=0;
//	lastYearOtherPerHaProfit=0;
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
	
//	if (grownSoy && soyCells.size()>0) {
	if(soyProduction > 0 && soyCells.size()>0)	{
//		double soyYield=0;
//		for (int i=0; i < soyCells.size();i++) {
//			yield = soyCells.get(i).getCropYield();
//			soyYield+=yield;
		//	cPrice=this.soySoldToTraderAgent.getCommodityPrice(LandUse.SOY);
			
			cPrice=this.getCommodityPrice(LandUse.SOY);

			
	//		System.out.println("update profit: "+tick+ " "+
		//	         cPrice+" yield "+yield);
			profit+=soyProduction*cPrice;		
	//		this.soySoldToTraderAgent.addSoyAmount(yield);
	//		this.soySoldToTraderAgent.purchaseCommodity(yield*cPrice);
			
		
		soyPerHaYield = soyProduction/soyCells.size();
		soyPerHaYield = soyPerHaYield /(cellsize*cellsize)*10000.0;
		//has to convert this to from cell yield to per ha yield
	//	System.out.println(soyYield+" cell "+soyCells.size());
		lastYearSoyPerHaProfit = soyPerHaYield*cPrice-getSoyPerHaFuelInput()*fuelUnitCost
				                 -soyPerHaFertilizerInput*fertilizerUnitCost;
	//	System.out.println("soy per ha yield="+soyPerHaYield+" per ha fuel input="+soyPerHaFuelInput+
	//			"soyPerHaFertilizerInput = "+soyPerHaFertilizerInput);
	//	System.out.println("capital = "+capital+" soy profit = "+lastYearSoyPerHaProfit);
		
	}
	
//	System.out.println("soy yield: "
 //           +this.getCommodityPrice(LandUse.CORN));
	if (cornProduction>0) {
			
			cPrice=this.getCommodityPrice(LandUse.CORN);

			profit+=cornProduction*cPrice;
	//		this.cornSoldToTraderAgent.addCornAmount(yield);
	//		this.cornSoldToTraderAgent.purchaseCommodity(yield*cPrice);
			
		cornPerHaYield=cornProduction/cornCells.size();
		cornPerHaYield = (cornPerHaYield/(cellsize*cellsize))*10000.0;
		lastYearCornPerHaProfit = cornPerHaYield*cPrice-cornPerHaFuelInput*fuelUnitCost
                -cornPerHaFertilizerInput*fertilizerUnitCost;
	//	System.out.println("corn size="+cornCells.size()*900/10000.0+" corn per ha yield="+cornPerHaYield
	//			+" corn per ha fuel inpu=" +cornPerHaFuelInput+
	//			" corn fertilizer input "+cornPerHaFertilizerInput);
	//	System.out.println("capital = "+capital+" corn profit = "+ lastYearCornPerHaProfit);
	}
	if (riceProduction>0) {
		
			cPrice=this.getCommodityPrice(LandUse.RICE);
			profit+=riceProduction*cPrice;
	//		this.riceSoldToTraderAgent.addRiceAmount(yield);
	//		this.riceSoldToTraderAgent.purchaseCommodity(yield*cPrice);
			
		ricePerHaYield=riceProduction/riceCells.size();
		ricePerHaYield=(ricePerHaYield/(cellsize*cellsize))*10000.0;
		lastYearRicePerHaProfit = ricePerHaYield*cPrice-ricePerHaFuelInput*fuelUnitCost
                -ricePerHaFertilizerInput*fertilizerUnitCost;
	//	System.out.println("capital = "+capital+"rice profit = "+ lastYearRicePerHaProfit);
	}
	if (grownOther) {
		
			cPrice=this.getCommodityPrice(LandUse.OTHERCROPS);
			otherPrices.add(cPrice);
			if (otherPrices.size()>priceMemoryLimit) {
				otherPrices.remove(0); //remove least recent price
			
			profit+=otherProduction*cPrice;
	//		this.otherSoldToTraderAgent.addOtherAmount(yield);
	//		this.otherSoldToTraderAgent.purchaseCommodity(yield*cPrice);
			}
		otherPerHaYield=otherProduction/otherCells.size();
		lastYearOtherPerHaProfit = otherPerHaYield*cPrice-otherPerHaFuelInput*fuelUnitCost
                -otherPerHaFertilizerInput*fertilizerUnitCost;
	//	System.out.println("capital = "+capital+"other profit = "+lastYearOtherPerHaProfit);
	}
	
	capital+=profit;
	
//	System.out.println("capital: = "+profit);
//	System.out.println(tick +" "+this.getID()+"soy profit "+lastYearSoyPerHaProfit);
//	System.out.println(lastYearCornPerHaProfit);
//	System.out.println(lastYearRicePerHaProfit);
//	  System.out.println(" ");
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
	
	// 	System.out.println("tenure Size "+this.getTenureCells().size());
	 	int count=0;
		   for (int i=0; i< this.getTenureCells().size(); i++) {
		    	int x = this.getTenureCells().get(i).getXlocation();
		    	int y = this.getTenureCells().get(i).getYlocation();
		    	
		  //  	double random = RandomHelper.nextDouble();
		  //  	System.out.println("thislandcell "+ organicSpace.getLandUseAt(x, y));
		    	 //problem here is it puts all kinds of land use to the agent
		    	
		    	if (organicSpace.getTypeID()=="organicSpaceReceiving"){
		    	if (organicSpace.getLandUseAt(x, y)==2){
		    		this.getTenureCells().get(i).setLandUse(LandUse.SOY);
		    	//	this.getTenureCells().get(i).setLastLandUse(LandUse.SOY);
	
		    	} else if (organicSpace.getLandUseAt(x, y)==3) {
		    		this.getTenureCells().get(i).setLandUse(LandUse.RICE);
		    	//	this.getTenureCells().get(i).setLastLandUse(LandUse.RICE);
		    	} else if (organicSpace.getLandUseAt(x, y)==6) {
		    		this.getTenureCells().get(i).setLandUse(LandUse.CORN);
		    	//	this.getTenureCells().get(i).setLastLandUse(LandUse.CORN);
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
			    		this.getTenureCells().get(i).setLandUse(LandUse.SOY);
			//    		count++;
			    	} else if (organicSpace.getLandUseAt(x, y)==2) {
			    		this.getTenureCells().get(i).setLandUse(LandUse.SECONDSOY);
			    	} else if (organicSpace.getLandUseAt(x, y)==3) {
			    		this.getTenureCells().get(i).setLandUse(LandUse.COTTON);
			    	} else {
			    		this.getTenureCells().get(i).setLandUse(LandUse.OTHERCROPS);//Cotton
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
		    
		    List<Point> agriculturalCells = new LinkedList<Point>();
		    
	//	   System.out.println(corner.x+" "+(corner.x+xboundary));
	//	   System.out.println(corner.y+" "+(corner.y+yboundary));
		    
		    
		 //first, go through all agricultural cells in this sub-section   
if(organicSpace.getTypeID()=="organicSpaceReceiving"){
		 for (int i=corner.x; i<=(corner.x+xboundary);i++)  {
			 for (int j=corner.y;j<=(corner.y+yboundary);j++){
				 Point p=new Point(i,j);
				 LandCell c = new LandCell(organicSpace,grid,p.x,p.y,
	 					 organicSpace.getElevationAt(p.x, p.y),
	 					 organicSpace.getOrganicAt(p.x, p.y));
			//	 System.out.println("organic space "+organicSpace.getId()+" land cell is "+c.getYlocation());
				
					if(c.isTaken()==true || organicSpace.getLandHolder(i, j)>0)
					//this is to check if land cell is taken.
						{
						
						}
					else
				{ 
					 if(organicSpace.getLandUseAt(i, j) ==2) 
					 {  
					//	 System.out.println("land use field works"); 
						 //this worked
						 c.setLandUse(LandUse.SOY);
						 {
							 TeleABMBuilder.total++;
							 c.setLandHolder( true,this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);
							 //fertilizer use
							 c.setRecommendedSoyPerHaFertilizerUse(10.0);
							 c.setObservedSoyPerHaFertilizerUse(63.0);
							 
							 c.setFertilizerInput(LandUse.SOY);
							 //have to add recommended and observed for all crops when iterate through all cells.
							 c.setRecommendedCornPerHaFertilizerUse(200.0);
							 c.setObservedCornPerHaFertilizerUse(224.0);
							 c.setRecommendedRicePerHaFertilizerUse(150.0);
							 c.setObservedRicePerHaFertilizerUse(146.0);
							 
						 }
					 }
					 if(organicSpace.getLandUseAt(i, j)==3) {
						 c.setLandUse(LandUse.RICE);
					
							 TeleABMBuilder.total++;
							 c.setLandHolder(true, this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);
							 c.setRecommendedRicePerHaFertilizerUse(150.0); //kg/ha
							 c.setObservedRicePerHaFertilizerUse(146.0 );
							 
							 c.setFertilizerInput(LandUse.RICE);
							 //have to add recommended and observed for all crops when iterate through all cells.
							 c.setRecommendedSoyPerHaFertilizerUse(10.0);
							 c.setObservedSoyPerHaFertilizerUse(63.0);
							 c.setRecommendedCornPerHaFertilizerUse(200.0);
							 c.setObservedCornPerHaFertilizerUse(224.0);
						 
					 }
					 if(organicSpace.getLandUseAt(i, j)==6){
						 c.setLandUse(LandUse.CORN);
						
							 TeleABMBuilder.total++;
							 c.setLandHolder(true,this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);
							 c.setRecommendedCornPerHaFertilizerUse(200.0);
							 c.setObservedCornPerHaFertilizerUse(224.0);
							 
							 c.setFertilizerInput(LandUse.CORN);
							 //have to add recommended and observed for all crops when iterate through all cells.
							 c.setRecommendedSoyPerHaFertilizerUse(10.0);
							 c.setObservedSoyPerHaFertilizerUse(63.0);
							 c.setRecommendedRicePerHaFertilizerUse(150.0);
							 c.setObservedRicePerHaFertilizerUse(146.0);
						
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
							 
							 
					 if(c.isTaken()==true || organicSpace.getLandHolder(i, j)>0){
						 
					 }
					 else {
					 if (organicSpace.getLandUseAt(i, j)>=1&&organicSpace.getLandUseAt(i, j)<=2)
						{
							
							agriculturalCells.add(p);
							TeleABMBuilder.total++;
							
							c.setLandUse(LandUse.SOY);
							 c.setLandHolder(true,this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);
							 c.setRecommendedSoyPerHaFertilizerUse(100);
							 c.setObservedSoyPerHaFertilizerUse(200);
							 
							 c.setFertilizerInput(LandUse.SOY);
							 
							 c.setRecommendedCornPerHaFertilizerUse(500);
							 c.setObservedCornPerHaFertilizerUse(700);
					//		System.out.println("soy");
						}
					 else if(organicSpace.getLandUseAt(i, j)==3) {
						 agriculturalCells.add(p);
							TeleABMBuilder.total++;
							
							c.setLandUse(LandUse.COTTON);
							 c.setLandHolder(true,this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);
							 
					 }
					 else if (organicSpace.getLandUseAt(i, j)==9){
							
							agriculturalCells.add(p);
							TeleABMBuilder.total++;
							c.setLandUse(LandUse.CORN);
							 c.setLandHolder(true,this);
							 organicSpace.setLandHolder((double) this.getID(), c.getXlocation(), c.getYlocation());
							 this.tenureCells.add(c);
							 c.setRecommendedCornPerHaFertilizerUse(500);
							 c.setObservedCornPerHaFertilizerUse(700);
							 
					         c.setFertilizerInput(LandUse.CORN);
					         
					         c.setRecommendedSoyPerHaFertilizerUse(100);
							 c.setObservedSoyPerHaFertilizerUse(200);
						} else {
							
						}
				 }
				}
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
			
}
	
	
	
