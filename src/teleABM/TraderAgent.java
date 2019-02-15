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
import java.util.Iterator;
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
import repast.simphony.valueLayer.GridValueLayer;
import repast.simphony.engine.environment.RunEnvironment;



/**
 * @author geododo
 *
 */
public abstract class TraderAgent {
//extends SimpleAgent{

	private int id;
    private static final AtomicInteger idGenerator = new AtomicInteger (0);
    protected Point location;
    
    
	 Parameters para = RunEnvironment.getInstance().getParameters();
	  int receivingXdim = (Integer)para.getValue("receivingWorldWidth");
	  int receivingYdim = (Integer)para.getValue("receivingWorldHeight");
	  int sendingXdim = (Integer)para.getValue("sendingWorldWidth");
	  int sendingYdim = (Integer)para.getValue("sendingWorldHeight");
//	    Parameters p = RunEnvironment.getInstance().getParameters();
	  int numReceivingAgents = (Integer)para.getValue("initialReceivingNumAgents");
		int numTradeAgents = (Integer) para.getValue("initialNumTradeAgents");
	protected Context<?> context;
    protected int vision;
   

	protected double capital;
	
	protected LinkedList<LandUse> commodityType =
			       new LinkedList<LandUse>();
	
    protected double soyAmount=0;
	protected double cornAmount=0;
    protected double riceAmount=0;
    protected double cottonAmount=0;
    protected double otherAmount=0;
    
    protected Map<LandUse, ArrayList<Double>> prices = new HashMap<LandUse, ArrayList<Double>>();
    Map<LandUse, InputStream> priceStreams;
    MarketPrices marketPrices = new MarketPrices();
    
    
	
	protected LinkedList<SoybeanAgent> purchasingfromSoybeanAgents = new LinkedList<SoybeanAgent>();
//	protected LinkedList<SoybeanAgent> purchasingfromSoybeanAgentsTEST = new LinkedList<SoybeanAgent>();
	protected double internationalTradeSoyPrice = 0;    
	protected double internationalTradeBrazilSoyPrice = 0;
	
	protected List<Double> soyPrices = new LinkedList<Double>();
	protected List<Double> cornPrices = new LinkedList<Double>();
	protected List<Double> ricePrices = new LinkedList<Double>();
	protected List<Double> cottonPrices = new LinkedList<Double>();
	protected List<Double> otherPrices = new LinkedList<Double>();

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
		if(organicSpace.getTypeID()=="organicSpaceReceiving")
	{
		if(receivingXdim>receivingYdim)
			setVision(receivingXdim);
		else 
			setVision(receivingYdim);
	//	setVision(RandomHelper.nextIntFromTo(500,100000));
	//	setCapital(RandomHelper.nextDoubleFromTo(10000,10000000));
		setCapital(10000000000.0);
	//	setCommodityPrices();
		//commodity price is set at the teleABMBuilder
		}
		else 
	{
	//		setVision(RandomHelper.nextIntFromTo(5000,1000000));
			if(sendingXdim>sendingYdim)
				setVision(sendingXdim);
			else 
				setVision(sendingYdim);
	  //      setVision(sendingXdim/2);
	//		setVision(20000);
	//		setCapital(RandomHelper.nextDoubleFromTo(1000000, 1000000000));
			setCapital(100000000.0);
	//		setCommodityPrices();
			//commodity price is set at the teleABMBuilder
		}

		findSoybeanAgent(organicSpace);
//		System.out.println("ok size "+this.getID()+" "+this.purchasingfromSoybeanAgents.size());
	//	if(TeleABMBuilder.sendingSystem)
		//	findSoybeanAgent(organicSpace);
	}
	
	private void findSoybeanAgent(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub		
		LinkedList<Integer> tradedFarmers = new LinkedList<Integer>();
		//
	    int xLook, yLook;
	   
	    Grid<Object> grid; 
	 
	    List<ReceivingSoybeanAgent> receivingSoybeanAgents;
	    List<SendingSoybeanAgent> sendingSoybeanAgents;
	    double xdim;
	    double ydim;
	    int x;
	    int y;
	    
	    if(organicSpace.getId()=="organicSpaceReceiving") {
	    	grid = (Grid) organicSpace.getProjection("Grid");
	//        xdim = receivingXdim;
	//        ydim = receivingYdim;
	        x= (int) (id*(receivingXdim/numTradeAgents)+RandomHelper.nextIntFromTo(-100,100));
	        y= (int) (id*(receivingYdim/numTradeAgents)+RandomHelper.nextIntFromTo(-100,100));
	        
	    	
	    } else {
	    	grid = (Grid) organicSpace.getProjection("gridSending");
//	        xdim = sendingXdim;
//	        ydim = sendingYdim;
//	    	System.out.println(id);
	        x= (int) ((id)*(sendingXdim/numTradeAgents)+RandomHelper.nextIntFromTo(-100,100));
	        y= (int) ((id)*(sendingYdim/numTradeAgents)+RandomHelper.nextIntFromTo(-100,100));
	//    	System.out.println("grid hahahah");
	    }
	    
	    //this is to make sure all soybean agents can be found by at least one trader agent
	    
	  
	    
	//    tradedFarmers.replaceAll(operator);;
		
		
//	    GridPoint point = grid.getLocation(this);
       
//	    int x = point.getX();

//	    int y = point.getY();
	    	       
	    organicSpace.setTraderAgent(id, x - vision/2, y-vision/2);
	//        System.out.println("traderAgent Location "+ x +" "+ y);
	 
    //    boolean neighborExists = true;
	    yLook = y;

		 for (xLook = x - vision/2; xLook <= x + vision/2; xLook++) {
			 for (yLook = y-vision/2;yLook<=y+vision/2;yLook++){
		
         //     organicSpace.setTraderAgent(id, xLook, yLook);
              if(organicSpace.getLandHolder(xLook, yLook)>0) {
            	//  System.out.println(organicSpace.getLandHolder(xLook, yLook));
            	  
            	  if(!tradedFarmers.contains(organicSpace.getLandHolder(xLook, yLook)))
            	  {
            		 int i = organicSpace.getLandHolder(xLook, yLook);
            	//	  System.out.println(organicSpace.getLandHolder(xLook, yLook));
            	      tradedFarmers.add(i);
            	
            	  //soybeanSellers is a new list that contains the soybean farmer agents 
            	  //within the vision of a trader;
            	  //this is initialized at step 1;
            	  //however, soybeanSellers not seem to be used?
            	  
                }
            	 }
		 }
		 }
//		 System.out.println("all farmers: "+tradedFarmers.size());
		 
	/*     soybeanAgents = organicSpace.getSoybeanAgents();
	     System.out.println("soybean agents: "+soybeanAgents.size());
		 
	     Iterator it = tradedFarmers.iterator();
	     
	     while(it.hasNext()) {
	    	 SoybeanAgent o = soybeanAgents.get((int) it.next());
	    	 if(!this.purchasingfromSoybeanAgents.contains(o)) {
   			  //this if is to control that soybean agents not been added more than once
				  
   		//	  SoybeanAgent o = this.purchasingfromSoybeanAgentsTEST.get(tradedFarmers.iterator().next());
   			  o.addTraderAgent(this);
       		  this.purchasingfromSoybeanAgents.add((SoybeanAgent) o);
       	//	  System.out.println("add once "+((SoybeanAgent) o).getID() );
   		    }
	    			 
	     }
	     
	     System.out.println("purchasing from soybean agents "+purchasingfromSoybeanAgents.size());	*/
	     
	     
		 if(organicSpace.getId()=="organicSpaceReceiving") 
		 {			 			 
			  	receivingSoybeanAgents = organicSpace.getReceivingSoybeanAgents();
	
			  	Iterator it = tradedFarmers.iterator();
			  	
		   while(it.hasNext()){
			   
			   
			 ReceivingSoybeanAgent o = receivingSoybeanAgents.get((int)it.next());
		//	 tradedFarmers.iterator().remove();
		//	 System.out.println(count++);
		//	 System.out.println(o.getTenureCells().size());
			 
			  if(!this.purchasingfromSoybeanAgents.contains(o)) {
    			  //this if is to control that soybean agents not been added more than once
				  
    		//	  SoybeanAgent o = this.purchasingfromSoybeanAgentsTEST.get(tradedFarmers.iterator().next());
    			  o.addTraderAgent(this);
        		  this.purchasingfromSoybeanAgents.add((SoybeanAgent) o);
        	//	  System.out.println("add once "+((SoybeanAgent) o).getID() );
    		    }
		      }
		 }
		 else {
		        sendingSoybeanAgents = organicSpace.getSendingSoybeanAgents();
		        
			  	Iterator it = tradedFarmers.iterator();
//			  	System.out.println(it.next());
			  	//the problem when having both receiving and sending soybean agents are the soybean agent id
			     while(it.hasNext()){
			 //   	 System.out.println("is it wrong");
					SendingSoybeanAgent o = sendingSoybeanAgents.get((int)it.next());
			//				-numReceivingAgents);
				
					  if(!this.purchasingfromSoybeanAgents.contains(o)) {
		    			  //this if is to control that soybean agents not been added more than once
						  
		    		//	  SoybeanAgent o = this.purchasingfromSoybeanAgentsTEST.get(tradedFarmers.iterator().next());
		    			  o.addTraderAgent(this);
		        		  this.purchasingfromSoybeanAgents.add((SoybeanAgent) o);
		        //		  System.out.println("not added more than once "+((SoybeanAgent) o).getID());
		    		    }
				      }
		 }
		 
		
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
				marketPrices.setPrice(LandUse.SINGLESOY, staticPrice);
		//		System.out.println ("soyPrice =" + staticPrice);
			} else {
//				priceLists.put(LandUse.SOY, new FileInputStream("auxdata/prices/soyGannanPrice.txt"));
					priceLists.put(LandUse.SOY, new FileInputStream("./data/prices/soyPriceTest.txt"));
					priceLists.put(LandUse.SINGLESOY, new FileInputStream("./data/prices/soySinopPrice.txt"));
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
				priceLists.put(LandUse.CORN, new FileInputStream("./data/prices/cornGannanPricesCPIAdjusted.txt"));
				//	priceLists.put(LandUse.CORN, new FileInputStream("auxdata/prices/cornGannanPrices.txt"));
				//priceLists.put(LandUse.CORN, new FileInputStream("auxdata/prices/corn.prices.txt"));
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
				priceLists.put(LandUse.RICE, new FileInputStream("./data/prices/rice.pricesCPIAdjusted.txt"));
				//	priceLists.put(LandUse.RICE, new FileInputStream("auxdata/prices/rice.prices2.txt"));
				//	priceLists.put(LandUse.RICE, new FileInputStream("auxdata/prices/rice.prices.txt"));
				
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
				priceLists.put(LandUse.OTHERCROPS, new FileInputStream("./data/prices/other.prices.txt"));
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
						cPrices.add(st.nval +RandomHelper.nextDoubleFromTo(-0.01, 0.01));
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
    	
    //	if(landuse==LandUse.DOUBLESOY)
    //		landuse = LandUse.CORN;
    //	if(landuse==LandUse.SOYCOTTON)
    	
    	if(TeleABMBuilder.internationalTradeMode)
		{
		 if(landuse==LandUse.SOY)
		 {
	//		 tempPrice=marketPrices.getPrice(landuse)+
	//    			 RandomHelper.nextDoubleFromTo(-0.01, 0.05);
	//	     System.out.println(tick+": land use "+landuse+" price "+tempPrice);
			 tempPrice = getInternationalTradeSoyPrice();
		 } 
		 if(landuse==LandUse.SINGLESOY)
				tempPrice = getInternationalTradeBrazilSoyPrice();
		} 
		
		else if(commodityType.contains(landuse)) {
 //   		System.out.println("commodity type "+commodityType.contains(landuse));
    	 if(prices.containsKey(landuse))  
    			 {
    	     
    	       	 tempPrice=prices.get(landuse).get(tick);
    		
    			 } 
    	 else 
    	 {   
    		 tempPrice=marketPrices.getPrice(landuse)+
    			 RandomHelper.nextDoubleFromTo(-0.01, 0.01);
   // 	 System.out.println("land use static prices: "+tempPrice); 
    	 }
    	 }
    
    //	if (capital < 0) 
    //		 tempPrice=RandomHelper.nextDoubleFromTo(0, 0.1);
    	
    	//this means trader agent went bankrupt
    	
    //	System.out.println("trader agent: "+tempPrice);
    	return tempPrice;
    	
    	
    }
    
    
	public double getInternationalTradeSoyPrice() {
		return internationalTradeSoyPrice;
	}
    
	public double getInternationalTradeBrazilSoyPrice(){
		return internationalTradeBrazilSoyPrice;
	}

    

    
    
    
    public GridPoint getLocation() {

	    OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
		Grid<Object> grid = (Grid) organicSpace.getProjection("Grid");
		
	    GridPoint point = grid.getLocation(this);
    	
		return point;
	}


	
    public double getSoyAmount() {
		return soyAmount;
	}


	public void addSoyAmount(double soyAmount) {
		this.soyAmount+= soyAmount;
	}
	
	public void addCottonAmount(double cottonAmount) {
		this.cottonAmount+=cottonAmount;
	}

	public double getCottonAmount(){
	    return cottonAmount;
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
	
    public void setInternationalTradeSoyPrice(double cprice){
    	internationalTradeSoyPrice = cprice;
    }
	
    public void setInternationalTradeBrazilSoyPrice(double bprice){
    	internationalTradeBrazilSoyPrice = bprice;
    }
	

	public void setSendingStaticCommodityPrices(LandUse landuse, double tempPrice){
    	marketPrices.setPrice(landuse, tempPrice);
	
	}
	public void setSendingDynamicCommodityPrices(LandUse landuse, ArrayList cropprices){
		prices.put(landuse, cropprices);
		
		 soyPrices.add(0, 0.501+RandomHelper.nextDoubleFromTo(-0.01, 0.01));
	       cornPrices.add(0, 0.302+RandomHelper.nextDoubleFromTo(-0.01, 0.01));
	       cottonPrices.add(0, 1.93+RandomHelper.nextDoubleFromTo(-0.01, 0.01));
	}
	
	public void setReceivingStaticCommodityPrices(LandUse landuse, double tempPrice){
    	marketPrices.setPrice(landuse, tempPrice);
	
	}
	public void setReceivingDynamicCommodityPrices(LandUse landuse, ArrayList cropprices){
		   prices.put(landuse, cropprices);
//		System.out.println("set receive dynamic "+prices.get(landuse));
		   //following is to add the first step price, 
		   //this is only used at initialization stage.
		   soyPrices.add(0, 1.31+RandomHelper.nextDoubleFromTo(-0.01, 0.01));
	       cornPrices.add(0, 0.6+RandomHelper.nextDoubleFromTo(-0.01, 0.01));
	       ricePrices.add(0, 1.3+RandomHelper.nextDoubleFromTo(-0.01, 0.01));
	}


}
