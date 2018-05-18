/**
 * 
 */
package teleABM;

import java.io.BufferedReader;
import java.util.concurrent.atomic.AtomicInteger;


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

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

import repast.simphony.engine.environment.RunEnvironment;



/**
 * @author geododo
 *
 */
public class TraderAgent {
//extends SimpleAgent{

	private int id;
    private static final AtomicInteger idGenerator = new AtomicInteger (0);
    protected Point location;
   

	protected Context<?> context;
    protected int vision;
   

	protected double capital;
	
	protected LinkedList<LandUse> commodityType =
			       new LinkedList<LandUse>();
	
    private double soyAmount=0;
	private double cornAmount=0;
    private double riceAmount=0;
    private double otherAmount=0;
    private Map<LandUse, ArrayList<Double>> prices = new HashMap<LandUse, ArrayList<Double>>();
    Map<LandUse, InputStream> priceStreams;
    MarketPrices marketPrices = new MarketPrices();
    
    
	protected LinkedList<Integer> soybeanSellers = new LinkedList<Integer>();
	protected LinkedList<SoybeanAgent> purchasingfromSoybeanAgents = new LinkedList<SoybeanAgent>();

	public TraderAgent() {
		this(idGenerator.getAndIncrement());
	
//		initialize();
	}


	public TraderAgent(int id) {
		this.id = id;
//		System.out.println(" initialization agents "+this.getID());
//		initialize();
		
	}
	
	public int getID(){
		return id;
	}
	
	public void initialize(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
		setVision(RandomHelper.nextIntFromTo(50,1000));
		setCapital(RandomHelper.nextDoubleFromTo(1000000,1000000000));
	//	int rand = RandomHelper.nextIntFromTo(1, 9);
		int rand = 9;
		//this is to test the price difference; 
		if (rand==1) {
			setCommodityType(LandUse.SOY);
		} else if(rand==2) {
			setCommodityType(LandUse.CORN);
		} else if (rand==3){
		setCommodityType(LandUse.RICE);
		} else if (rand==4){
			setCommodityType(LandUse.OTHERCROPS);
		} else if (rand==5){
		  setCommodityType(LandUse.SOY);
		  setCommodityType(LandUse.CORN);
		} else if (rand==6){
			setCommodityType(LandUse.SOY);
			setCommodityType(LandUse.RICE);			
		} else if(rand==7){
			setCommodityType(LandUse.CORN);
			setCommodityType(LandUse.RICE);
		}  else if(rand==8) {
			setCommodityType(LandUse.SOY);
			setCommodityType(LandUse.OTHERCROPS);
		} else {
			setCommodityType(LandUse.SOY);
			setCommodityType(LandUse.CORN);
			setCommodityType(LandUse.RICE);
		}
		//overwrite
	//	setCommodityType(LandUse.RICE);
		
		
		setCommodityPrices();
	//	System.out.println("trader agent initiliaze price"+this.getCommodityPrice(LandUse.SOY));
		findSoybeanAgent(organicSpace);
	}
	
	private void findSoybeanAgent(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub		
	    int xLook, yLook;
        Grid<Object> grid = (Grid) organicSpace.getProjection("Grid");
		
	    GridPoint point = grid.getLocation(this);
       
	    int x = point.getX();

	    int y = point.getY();
	    
	//    System.out.println("traderAgent Location "+ x +" "+ y);

	    
	    boolean neighborExists = true;
	    yLook = y;

		 for (xLook = x - vision/2; xLook <= x + vision/2; xLook++) {
			 for (yLook = y-vision/2;yLook<=y+vision/2;yLook++){
		
              organicSpace.setTraderAgent(id, xLook, yLook);
              if(organicSpace.getLandHolder(xLook, yLook)>0) {
            	  if(!soybeanSellers.contains(organicSpace.getLandHolder(xLook, yLook)))
            	  {
            	  soybeanSellers.add(organicSpace.getLandHolder(xLook, yLook));
            	  //soybeanSellers is a new list that contains the soybean farmer agents 
            	  //within the vision of a trader;
            	  //this is initialized at step 1;
            	  
              }
            	 }
		 }
		 }
		 
		// int count =0;
		 for (xLook = x - vision/2; xLook <= x + vision/2; xLook++) {
			 for (yLook = y-vision/2;yLook<=y+vision/2;yLook++){
				
			Iterable neighbors = grid.getObjectsAt(xLook, yLook);
			 if (!neighbors.iterator().hasNext())
			        neighborExists = false;

			      else for (Object o : neighbors) {
			        if (o instanceof SoybeanAgent)
			        	{ 
			        	  if (((SoybeanAgent) o).getTenureCells().size()>0)
			        	  { 
			//        		  count++;
			        		  ((SoybeanAgent) o).addTraderAgent(this);
			        		  this.purchasingfromSoybeanAgents.add((SoybeanAgent) o);
		//	  System.out.println("trader: "+this.getID()+" "+((SoybeanAgent) o).getID());
			 //      		+" tenure size"+  ((SoybeanAgent) o).getTenureCells().size());
			        	  }
			        	}
			        //  break;
			      }
				 neighborExists=false;
			 }
			 }
	//	System.out.println(count);		 	
		
	}


	protected void addTraderAgentAtRandom(OrganicSpace organicSpace) {	
		Grid<Object> grid = (Grid) organicSpace.getProjection("Grid");
		
	}
	


	
   public LinkedList<LandUse> getCommodityType() {
	return commodityType;
   }


   public void setCommodityType(LandUse commodityT) {
	   //if commodityType ==1 soy
	   //if commodity type==2 corn
	   //if commodity type ==3 rice
	//  this.commodityType = commodityType;
	  this.commodityType.add(commodityT);
   }
   
   public int getVision() {
		return vision;
	}


	public void setVision(int vision) {
		this.vision = vision;
	}


	public double getCapital() {
		return capital;
	}


	public void setCapital(double capital) {
		this.capital = capital;
	}
	

	public void purchaseCommodity(double transaction){
     
	capital-=transaction;
//	if(transaction>0) System.out.println("capital "+capital+ " transaction "+transaction);
	 
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
				priceLists.put(LandUse.SOY, new FileInputStream("auxdata/prices/soyPrice.txt"));
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
   // 	System.out.println(this.getID()+" rice price "+prices.get(LandUse.RICE));
    }
    
    public double getCommodityPrice(LandUse landuse){
    //	return MarketPrices.get(landuse);
    	//here is only static price;
    	//needs to solve dynamic price;
    	double tempPrice=0;
    	int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
    	
    //	System.out.println("tick at trader "+tick);
    	if(commodityType.contains(landuse)) {
 //   		System.out.println("commodity type "+commodityType.contains(landuse));
    	 if(prices.containsKey(landuse))  
    			 {
    	     
    		 tempPrice=prices.get(landuse).get(tick);
    		
    			 } 
    	 else 
    	 {   
    		 tempPrice=marketPrices.getPrice(landuse)+
    			 RandomHelper.nextDoubleFromTo(-0.01, 0.05);
   // 	 System.out.println("land use static prices: "+tempPrice); 
    	 }
    	 }
    
    	if (capital<0) 
    		 tempPrice=0;
    	
    	return tempPrice;
    	
    	
    }
    
    
    
    public GridPoint getLocation() {

	    OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
		Grid<Object> grid = (Grid) organicSpace.getProjection("Grid");
		
	    GridPoint point = grid.getLocation(this);
    	
		return point;
	}


	public void setLocation(Point location) {
		this.location = location;
	}
	
    public double getSoyAmount() {
		return soyAmount;
	}


	public void addSoyAmount(double soyAmount) {
		this.soyAmount+= soyAmount;
	}


	public double getCornAmount() {
		return cornAmount;
	}


	public void addCornAmount(double cornAmount) {
		this.cornAmount+= cornAmount;
	}


	public double getRiceAmount() {
		return riceAmount;
	}


	public void addRiceAmount(double riceAmount) {
		this.riceAmount+= riceAmount;
	}


	public double getOtherAmount() {
		return otherAmount;
	}


	public void addOtherAmount(double otherAmount) {
		this.otherAmount+= otherAmount;
	}


}
