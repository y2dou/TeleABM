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

/**
 * @author DOU Yue
 *  version 2, sep 20, 2017
 */

	
	
	import repast.simphony.context.Context;
	import repast.simphony.dataLoader.ContextBuilder;
	import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;

import teleABM.Point;
import teleABM.SoybeanAgent;

	/**
	 * A partial implementation of the teleabm simulation.
	 * 
	 * The source code is heavily annotated as example of a simulation built using
	 * the Repast toolkit. See the html API documentation for the details of the
	 * framework objects.
	 * 
	
	 */
	public class TeleABMBuilder implements ContextBuilder<Object>{
		
	  public static int count=0;
	  public static boolean sendingSystem = false;
      public static boolean receivingSystem = false;
      public static int modeAddAgents = 0;
      public static boolean internationalTradeMode = false;
      
      
     InternationalTradeAgent internationalTradeAgent = new InternationalTradeAgent();
     ReceivingGovernment governmentAgent = new ReceivingGovernment();
      
  	List<ReceivingSoybeanAgent> receivingSoybeanAgents =
			new LinkedList<ReceivingSoybeanAgent>();
  	List<SendingSoybeanAgent> sendingSoybeanAgents =
			new LinkedList<SendingSoybeanAgent>();
  	
  	List<ReceivingTraderAgent> receivingTraderAgents = new LinkedList<ReceivingTraderAgent>();
	List<SendingTraderAgent> sendingTraderAgents = new LinkedList<SendingTraderAgent>();
    
	protected WeightedSelector<Range<Integer>> dependentRatioSelector;
	protected WeightedSelector<Range<Integer>> genderRatioSelector;
        //this is to indicate which system the instatnce is representing, it's read through the parameter.
	
//	Map<LandUse, ArrayList<Double>> prices = new HashMap<LandUse, ArrayList<Double>>();
	
		public Context<Object> build(Context<Object> context) {
			
		  // The sugarFile contains the initial/max sugar values for every point
			// on the 2D sugarspace.
			String organicFile = "misc/organicmatter.asc";
		//	String organicFileForSending = "misc/2005sinop.asc";
			String organicFileForSending = "misc/sinop/sinop_2005.asc";
			
			Parameters p = RunEnvironment.getInstance().getParameters();
			
			int numReceivingAgents = (Integer)p.getValue("initialReceivingNumAgents");
			int numSendingAgents = (Integer) p.getValue("initialSendingNumAgents");
			
			int numTradeAgents = (Integer) p.getValue("initialNumTradeAgents");
			context.add(internationalTradeAgent);
			context.add(governmentAgent);
			
			modeAddAgents = (Integer)p.getValue("mode of adding agents");
	//		System.out.println(p.getValue("sending system representation"));
		
			dependentRatioSelector = new 
					 WeightedSelector<Range<Integer>>("dependentRatioSelector");
			
			genderRatioSelector = new 
					 WeightedSelector<Range<Integer>>("genderRatioSelector");
			
			OrganicSpace organicSpaceReceiving;
			OrganicSpace organicSpaceSending;
			
			//create both organicSpace no matter what. 
			//but add them to context according to system setting
			
			if ( (boolean) p.getValue("sending system representation") && 
					!(boolean) p.getValue("receiving system representation"))  {
				 sendingSystem=true;
				 receivingSystem = false;
				 organicSpaceSending = new OrganicSpace(organicFileForSending);
				     context.add(organicSpaceSending);
			    	 context.addSubContext(organicSpaceSending);
			    	 System.out.println("sending context being build="+context.getId());
					 System.out.println("contains sending: "+context.contains(organicSpaceSending));
				//	 System.out.println("contains receiving: "+context.contains(organicSpaceReceiving));
				     System.out.println("only sending System");
			 }
			 if ( !(boolean) p.getValue("sending system representation") && 
			 (boolean) p.getValue("receiving system representation")) {
				 receivingSystem=true;
				 sendingSystem=false;
					
				 organicSpaceReceiving = new OrganicSpace(organicFile);
		//		 context.add(internationalTradeAgent);
						context.add(organicSpaceReceiving);
						context.addSubContext(organicSpaceReceiving);
						System.out.println("receiving context being build="+context.getId());
						System.out.println("contains receiving: "+context.contains(organicSpaceReceiving));
				//		 System.out.println("contains sending: "+context.contains(organicSpaceSending));
				 System.out.println("only receiving System");
				 
			 }
			 if ( !(boolean) p.getValue("sending system representation") &&
					 !(boolean) p.getValue("receiving system representation")) {
				 receivingSystem=false;
				 sendingSystem=false;
					System.err.println("TeleABM Creator: Skipping run: invalid parameters.");
					System.exit(1);
			 }
			 
			 if ( (boolean) p.getValue("sending system representation") &&
					 (boolean) p.getValue("receiving system representation")) {
				 receivingSystem=true;
				 organicSpaceReceiving = new OrganicSpace(organicFile);
				 
						context.add(organicSpaceReceiving);
						context.addSubContext(organicSpaceReceiving);
						
				 sendingSystem=true;
				 organicSpaceSending = new OrganicSpace(organicFileForSending);	
				        context.add(organicSpaceSending);
				        context.addSubContext(organicSpaceSending);
				    System.out.println("receiving context being build="+context.getId());
					System.out.println("contains receiving: "+context.contains(organicSpaceReceiving));
					 System.out.println("contains sending: "+context.contains(organicSpaceSending));
				     System.out.println("receiving & sending System");
			 }
			 
			 setUpRandomDistributions();
			 
		
			//organicSpace is for receiving system
			
		
		//	context.addSubContext(organicSpaceSending);
			
		//	context.add(organicSpaceSending);
			//organicSpaceSending is for sending System
			
		//	System.out.println(organicSpace.getValueLayer("Current Organic").get(10,10));
		//	System.out.println(1);
			// Create the initial agents and add to the sugar space.
	
		
			
			if (receivingSystem) {	
				 				
				 dependentRatioSelector.add(new Range<Integer>(0, 100), 147);
				 dependentRatioSelector.add(new Range<Integer>(101, 200), 6);
				 dependentRatioSelector.add(new Range<Integer>(201, 300), 261);
				 dependentRatioSelector.add(new Range<Integer>(301, 400), 171);
				 dependentRatioSelector.add(new Range<Integer>(401, 500), 76);
				 dependentRatioSelector.add(new Range<Integer>(501, 600), 154);
				 dependentRatioSelector.add(new Range<Integer>(601, 700), 42);
				 dependentRatioSelector.add(new Range<Integer>(701, 800), 3);
				 dependentRatioSelector.add(new Range<Integer>(801, 900), 2);
				 dependentRatioSelector.add(new Range<Integer>(901, 1000), 11);		
				
				 
				 genderRatioSelector.add(new Range<Integer>(0, 100), 131);
				 genderRatioSelector.add(new Range<Integer>(101, 200), 6);
				 genderRatioSelector.add(new Range<Integer>(201, 300), 53);
				 genderRatioSelector.add(new Range<Integer>(301, 400), 43);
				 genderRatioSelector.add(new Range<Integer>(401, 500), 7);
				 genderRatioSelector.add(new Range<Integer>(501, 600), 15);
				 genderRatioSelector.add(new Range<Integer>(601, 700), 5);
				 genderRatioSelector.add(new Range<Integer>(701, 800), 1);
						
				
				for(int i =0; i<numReceivingAgents; i++){					
			    ReceivingSoybeanAgent h = 
					new ReceivingSoybeanAgent(i);
			    receivingSoybeanAgents.add(h);
				}		
				
				ArrayList<Point> receivingCorners = setXYcornerOfReceivingSystem();
		//		System.out.println(receivingCorners.size());				
														
				
				if (modeAddAgents ==0) //add agents covering whole landscape
				//this is default.										
					for(int i =0; i<numReceivingAgents && i<receivingCorners.size(); i++){
				 //      System.out.println("i = "+i);
				//		receivingSoybeanAgents.get(i).initialize();		
						organicSpaceReceiving = (OrganicSpace) context.findContext("organicSpaceReceiving");
						organicSpaceReceiving.add(receivingSoybeanAgents.get(i));	
						
						receivingSoybeanAgents.get(i).addSoybeanAgentFromLandscape(
								organicSpaceReceiving, receivingCorners.get(i));
					//	System.out.println("i = "+i+" add soybean agents: "+
					//	                        receivingCorners.get(i).x+" "+receivingCorners.get(i).y);
						
						receivingSoybeanAgents.get(i).addLandUseFromField(organicSpaceReceiving);
			//			System.out.println(receivingSoybeanAgents.get(i).getID()+
			//					       " receiving agents tenure size: "+
			//		             receivingSoybeanAgents.get(i).getTenureCells().size());
				//		System.out.println("i = "+i+receivingSoybeanAgents.get(i));
						
					}
					else if (modeAddAgents==1) //add agents from the land use maps
					{
						for(int i =0; i<numReceivingAgents; i++)
						{
							
					//		receivingSoybeanAgents.get(i).initialize();
							organicSpaceReceiving = (OrganicSpace) context.findContext("organicSpaceReceiving");
							organicSpaceReceiving.add(receivingSoybeanAgents.get(i));
							receivingSoybeanAgents.get(i).addSoybeanAgentFromField(
									organicSpaceReceiving, receivingCorners.get(i));
							receivingSoybeanAgents.get(i).addLandUseFromField(organicSpaceReceiving);		
				//			System.out.println("receiving agents tenure size: "+
				//			             receivingSoybeanAgents.get(i).getTenureCells().size());
						}
												
					}
				
		//	System.out.println("how many agents: "+receivingSoybeanAgents.size());	
				organicSpaceReceiving = (OrganicSpace) context.findContext("organicSpaceReceiving");
				List<Integer> listToRemove = new ArrayList<Integer>();
				 for( int i = 0; i<receivingSoybeanAgents.size(); i++) {
				if (receivingSoybeanAgents.get(i).getTenureCells().size()==0)
				  {
					listToRemove.add(i);
				   }
				else receivingSoybeanAgents.get(i).initialize(organicSpaceReceiving);
				 }
				 
				 for(Integer i:listToRemove){
				//	 organicSpaceReceiving = (OrganicSpace) context.findContext("organicSpaceReceiving");
					 organicSpaceReceiving.remove(receivingSoybeanAgents.get(i));
				 }
				 receivingSoybeanAgents.removeAll(listToRemove);
				 
		//		 organicSpaceReceiving.add(internationalTradeAgent);
		//		 System.out.println(listToRemove);
		//		 System.out.println(organicSpaceReceiving.numAgents);
              /* for( int i = 0; i<receivingSoybeanAgents.size(); i++)
				if (receivingSoybeanAgents.get(i).getTenureCells().size()==0)
				{
			//	
				organicSpace.remove(receivingSoybeanAgents.get(i));
				receivingSoybeanAgents.remove(i);
			//	receivingSoybeanAgents.
				System.out.println("remove some agents "+i+" "+receivingSoybeanAgents.size());
			//	break;
				} else {
					receivingSoybeanAgents.get(i).initialize();	
				
				        }	*/
              for ( ReceivingSoybeanAgent h:receivingSoybeanAgents) {
            	  Range<Integer> ws = dependentRatioSelector.sample();
				  Integer dependentRatio = RandomHelper.nextIntFromTo(ws.getLower(), ws.getUpper());
				  h.setDependentRatio((double) dependentRatio/1000);
				  Integer genderRatio = RandomHelper.nextIntFromTo(ws.getLower(), ws.getUpper());
				  h.setGenderRatio((double) genderRatio/1000);
		//		  System.out.println("dependent ratio of this agent: "+h.getDependentRatio() +
        //                			"  "+ dependentRatio	  );
				  organicSpaceReceiving.addReceivingSoybeanAgent(h);
		//		  setReceivingPrice(h);
              }
               
         //      System.out.println("after removing "+receivingSoybeanAgents.);	
        //      System.out.println("total land use cells="+total);
			}  
			
				if (sendingSystem) {
								
				for(int i =0; i<numSendingAgents; i++){
					SendingSoybeanAgent h =
					  new SendingSoybeanAgent(i);
			        sendingSoybeanAgents.add(h);
			  //      System.out.println(i+" =? "+h.getID());
				}				
				organicSpaceSending = (OrganicSpace) context.findContext("organicSpaceSending");
				ArrayList<Point> sendingCorners = setXYcornerOfSendingSystem();
				if (modeAddAgents ==0) //add agents covering whole landscape
					for(int i =0; i<numSendingAgents; i++){
						sendingSoybeanAgents.get(i).initialize(organicSpaceSending);										
						organicSpaceSending.add(sendingSoybeanAgents.get(i));			
						sendingSoybeanAgents.get(i).addSoybeanAgentFromLandscape(organicSpaceSending,							                                  
								sendingCorners.get(i));
						sendingSoybeanAgents.get(i).addLandUseFromField(organicSpaceSending);
					 
					}
					else if (modeAddAgents==1) //add agents from the land use maps
					{
						for(int i =0; i<numSendingAgents; i++){
				//			soybeanAgents.get(i).initialize();
				//			organicSpace.add(soybeanAgents.get(i));
				//			soybeanAgents.get(i).addSoybeanAgentFromField(organicSpace, corners.get(i));
				//			
							sendingSoybeanAgents.get(i).initialize(organicSpaceSending);
							organicSpaceSending.add(sendingSoybeanAgents.get(i));
							System.out.println("organic space sending="+organicSpaceSending.numAgents);
							sendingSoybeanAgents.get(i).addSoybeanAgentFromField(organicSpaceSending,
									sendingCorners.get(i));
							sendingSoybeanAgents.get(i).addLandUseFromField(organicSpaceSending);
							

						}
					}
				
				/*for(int i=0; i<sendingSoybeanAgents.size();i++)
				 if (sendingSoybeanAgents.get(i).getTenureCells().size()==0)
					{  
					 organicSpaceSending.remove(sendingSoybeanAgents.get(i));
					  sendingSoybeanAgents.remove(i);
					}*/
				
				
				organicSpaceSending = (OrganicSpace) context.findContext("organicSpaceSending");
				List<Integer> listToRemove = new ArrayList<Integer>();
				 for( int i = 0; i<sendingSoybeanAgents.size(); i++) {
				//if (sendingSoybeanAgents.get(i).getTenureCells().size()==0)
					if(sendingSoybeanAgents.get(i).getAgriculturalCells().size()==0)
				  {
					listToRemove.add(i);
				   }
				else sendingSoybeanAgents.get(i).initialize(organicSpaceSending);
				 }
				 
				 for(Integer i:listToRemove){
				//	 organicSpaceReceiving = (OrganicSpace) context.findContext("organicSpaceReceiving");
					 organicSpaceSending.remove(sendingSoybeanAgents.get(i));
				 }
				 sendingSoybeanAgents.removeAll(listToRemove);
				 
				  for ( SendingSoybeanAgent h:sendingSoybeanAgents) {
				  organicSpaceSending.addSendingSoybeanAgent(h);
		//		  setSendingPrice(h);
				  }
				 
				//	System.out.println(organicSpaceSending.numAgents);
			}
	
			System.out.println("add soybean agents finished");
			
			//add trade agents
			if(receivingSystem){
				organicSpaceReceiving = (OrganicSpace) context.findContext("organicSpaceReceiving");
		 	for (int i = 0; i<numTradeAgents; i++){
			 	ReceivingTraderAgent receivingTraderAgent = new ReceivingTraderAgent(i);	
				
			 	organicSpaceReceiving.add(receivingTraderAgent);				
			 	receivingTraderAgent.initialize(organicSpaceReceiving);
				setReceivingPrice(receivingTraderAgent);
				
				receivingTraderAgents.add(receivingTraderAgent);
			}
			   organicSpaceReceiving.add(internationalTradeAgent);
			   organicSpaceReceiving.add(governmentAgent);
			}
			if(sendingSystem){
				organicSpaceSending = (OrganicSpace) context.findContext("organicSpaceSending");
				for (int i = 0; i<numTradeAgents; i++){
			//	for (int i=0; i< 2; i++)	{
					SendingTraderAgent sendingTraderAgent = new SendingTraderAgent(i);		
		//		    System.out.println(i+" sta? "+sendingTraderAgent.getID());
				    
					organicSpaceSending.add(sendingTraderAgent);
							
					sendingTraderAgent.initialize(organicSpaceSending);		
			//		System.out.println("add trader agent  "+i);
					setSendingPrice(sendingTraderAgent);
			//		System.out.println("add trader agent to sending "+i);
					sendingTraderAgents.add(sendingTraderAgent);
									
				}
				
				
				organicSpaceSending.add(internationalTradeAgent);
				System.out.println("internationa agent added to sending system" );
			}
		//	context.add(soybeanAgents);
		//	System.out.println(2);
			
			
			// If running in batch mode, schedule the sim stop time
			double endTime = 10.0;
			
			if(RunEnvironment.getInstance().isBatch())
				RunEnvironment.getInstance().endAt(endTime);
			
			RunEnvironment.getInstance().endAt(endTime);

		//	internationalTradeAgent.marketTrading();
			
			ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
			ScheduleParameters  generate = ScheduleParameters.createRepeating(1, 1,1);
		
			schedule.schedule(generate , this ,"tradeAction");
			
			
	//		tradeAction();
			/*ISchedule schedule = RunEnvironment.init(schedule, scheduleRunner, parameters, isBatch);
					.getCurrentSchedule();
					RunEnviroment. getCurrentSchedule () ;
			ScheduleParameters params = ScheduleParameters . createOneTime
			(1) ;
			schedule . schedule ( params , this , " step ") ;*/

		
			return context;
		}
		
		public void setUpRandomDistributions() {
			
		  if (receivingSystem) {
			RandomHelper.registerDistribution("farmCostReceiving", RandomHelper.createUniform(100.0,500.0));
			
			RandomHelper.registerDistribution("capitalReceiving", RandomHelper.createNormal(10000, 500));
			RandomHelper.registerDistribution("labourReceiving", RandomHelper.createUniform(2,10));
			RandomHelper.registerDistribution("elevationRangeReceiving", RandomHelper.createUniform(1, 50));
			RandomHelper.registerDistribution("hectaresReceiving", RandomHelper.createUniform(30,2000));
		  //http://repast.sourceforge.net/docs/api/repastjava/repast/simphony/random/RandomHelper.html 
			//lots of different distribution
			RandomHelper.registerDistribution("soyYieldReceiving", RandomHelper.createUniform(2000,3000));
			//overwrite with empirical data
			RandomHelper.registerDistribution("cornYieldReceiving", RandomHelper.createUniform(4000,5000));
			RandomHelper.registerDistribution("riceYieldReceiving", RandomHelper.createUniform(5000,6000));
			RandomHelper.registerDistribution("otherYieldReceiving", RandomHelper.createUniform(2000,3000));
			
		  }
		  if(sendingSystem){
			  
				RandomHelper.registerDistribution("farmCostSending", RandomHelper.createUniform(100d,500d));
				
				RandomHelper.registerDistribution("capitalSending", RandomHelper.createUniform(10000000, 50000000));
				RandomHelper.registerDistribution("labourSending", RandomHelper.createUniform(4, 54));
				RandomHelper.registerDistribution("elevationRangeSending", RandomHelper.createUniform(1, 10));
				RandomHelper.registerDistribution("hectaresSending", RandomHelper.createUniform(800,1000));
				
				RandomHelper.registerDistribution("soyYieldSending", RandomHelper.createUniform(3000,4000));
				//overwrite with empirical data
				RandomHelper.registerDistribution("cornYieldSending", RandomHelper.createUniform(4000,5000));
				RandomHelper.registerDistribution("riceYieldSending", RandomHelper.createUniform(5000,6000));
				RandomHelper.registerDistribution("otherYieldSending", RandomHelper.createUniform(2000,3000));
				
			  
		  }
	}
		
		public ArrayList<Point> setXYcornerOfReceivingSystem() {
			   //find the corner where agents start to iterate
			
			  Parameters para = RunEnvironment.getInstance().getParameters();
			  int xdim = (Integer)para.getValue("receivingWorldWidth");
			  int ydim = (Integer)para.getValue("receivingWorldHeight");
				int numReceivingAgents = (Integer)para.getValue("initialReceivingNumAgents");
			
				numReceivingAgents=this.receivingSoybeanAgents.size();
				
			ArrayList<Point> corners = new ArrayList<Point>();

			   double xboundary;
			   double yboundary;
				    
			   double perAgentArea;
			   
			
			perAgentArea = (double) xdim*ydim/(numReceivingAgents);
				    
			   xboundary = Math.sqrt(perAgentArea*((double)xdim/(double)ydim));
			   yboundary = xboundary*ydim/xdim;
				
				int xcount = xdim/(int) xboundary;
				int ycount = ydim/(int) yboundary;
				
				System.out.println("perAgentArea "+perAgentArea);
				System.out.println("ydim "+ydim+" xdim "+ xdim+" ydim/xdim "+(double)ydim/xdim);
				System.out.println("xcount " + xcount);
				System.out.println("ycount " + ycount);
				System.out.println("xboundary="+xboundary+ " yboundary="+ yboundary);
				int count=0;
				
				for (int ytick=0; ytick<ycount+1;ytick++) {
					for (int xtick=0;xtick<xcount+1;xtick++){					
						Point p = new Point(xtick*(int)xboundary, ytick*(int)yboundary);			
						corners.add(count++, p);
					}
				}
				Point p = new Point(xcount*(int)xboundary,ycount*(int)yboundary);
				corners.add(count++,p);
			     
	          //end of receiving system			
	        
			   return corners;			
		}
		
		public ArrayList<Point> setXYcornerOfSendingSystem() {
			 //find the corner where agents start to iterate
			
			  Parameters para = RunEnvironment.getInstance().getParameters();
			  int xdim = (Integer)para.getValue("sendingWorldWidth");
			  int ydim = (Integer)para.getValue("sendingWorldHeight");
				
				int numSendingAgents = (Integer) para.getValue("initialSendingNumAgents");
				
			
					numSendingAgents=this.sendingSoybeanAgents.size();
				
  	
					
				ArrayList<Point> corners = new ArrayList<Point>();

				   double xboundary;
				   double yboundary;
					    
				   double perAgentArea;
 
				   if(sendingSystem) {
	               perAgentArea = (double) xdim*ydim/(numSendingAgents);
		    
	          xboundary = Math.sqrt(perAgentArea*((double)xdim/(double)ydim));
	         yboundary = xboundary*ydim/xdim;
		
		int xcount = xdim/(int) xboundary;
		int ycount = ydim/(int) yboundary;
		System.out.println("SENDING SYSTEM");
		System.out.println("perAgentArea "+perAgentArea);
		System.out.println("ydim "+ydim+" xdim "+ xdim+" ydim/xdim "+(double)ydim/xdim);
		System.out.println("xcount " + xcount);
		System.out.println("ycount " + ycount);
		System.out.println("xboundary="+xboundary+ " yboundary="+ yboundary);
		int count=0;
		
		for (int ytick=0; ytick<ycount+1;ytick++) {
			for (int xtick=0;xtick<xcount+1;xtick++){
				
			
				Point p = new Point(xtick*(int)xboundary, ytick*(int)yboundary);
	//			System.out.println(count+" : "+p.x+" "+p.y);
				corners.add(count++, p);
			//	count++;
				//write the left upper corner coordinates to the list;
			//	count++;
			}
		}
		Point p = new Point(xcount*(int)xboundary,ycount*(int)yboundary);
		corners.add(count++,p);
	  
//	    grid.moveTo(this, goodx[chosenSpotIndex], goody[chosenSpotIndex]);
	    
//end of sending system			
	
} 
                 return corners;
		}

		 
		
	//	internationalTradeAgent.marketTrading();
		//priority higher means it excute earlier.
		
  @ScheduledMethod(start = 0, interval = 1, priority = 2)		
  public void tradeAction(){
	  boolean tariff;
	  double receivingSoyProduction = 0;
	  double sendingSoyProduction = 0;
	  double soySubsidy;
	  
	  if(receivingSystem) {
	   internationalTradeAgent.setReceivingTotalSoyProduction(0);
		for ( ReceivingTraderAgent rta:receivingTraderAgents) 
		        {	
                 internationalTradeAgent.receivingMarketProduction(rta);   
                 receivingSoyProduction+=rta.getSoyAmount();
            //     if(rta.getSoyAmount()>0)
          //      System.out.println("rta: "+rta.getSoyAmount());
	            }
		
	  }
	  internationalTradeAgent.setReceivingTotalSoyProduction(receivingSoyProduction);
	  System.out.println( "rta total: "+internationalTradeAgent.getReceivingTotalSoyProduction());
	  
	  if(sendingSystem){
		  internationalTradeAgent.setSendingTotalSoyProduction(0);
		  	for ( SendingTraderAgent sta:sendingTraderAgents) 
		      {
			      internationalTradeAgent.sendingMarketProduction(sta);
			      sendingSoyProduction+=sta.getSoyAmount();
			   //   if(sta.getSoyAmount()>0)
	//	                 System.out.println("sta: "+sta.getSoyAmount());
		      }
	  }
	 
	  System.out.println("sta: "+internationalTradeAgent.getSendingTotalSoyProduction());
	//  System.out.println( "rta: "+internationalTradeAgent.getReceivingTotalSoyProduction());
	  
	  if(receivingSystem && sendingSystem)
	  {
		  TeleABMBuilder.internationalTradeMode = true;
	      tariff = governmentAgent.decideTariff();
	      tariff=false;
	      //
		  internationalTradeAgent.priceSetting(receivingTraderAgents, sendingTraderAgents,tariff);
		
	  }
	  
//	  internationalTradeAgent.setSendingTotalSoyProduction(sendingSoyProduction);
//	  internationalTradeAgent.setReceivingTotalSoyProduction(receivingSoyProduction);
//	  receivingSoyProduction = internationalTradeAgent.getReceivingTotalSoyProduction();
	
	  System.out.println("test receiving soy production "+receivingSoyProduction);
		 
		  if (receivingSoyProduction<2.0E8)
		      soySubsidy = 400;
		  else 
			  soySubsidy = 100;
		  
		  for ( ReceivingSoybeanAgent h:receivingSoybeanAgents) 
		    { 
			  h.setSoySubsidy(soySubsidy);
    	    }
		  System.out.println(receivingSoyProduction+" has soy subsidy "+ soySubsidy);	
		
	}
  
  
//  public void setSendingPrice(SendingSoybeanAgent h){
  public void setSendingPrice(SendingTraderAgent h){
	     Parameters p = RunEnvironment.getInstance().getParameters();
  	// System.out.println ("soyPrice =" + (Double) p.getValue("soyPrice"));
		// load data: prices
		Map<LandUse, InputStream> priceLists = new HashMap<LandUse, InputStream>();
						
		//check if soyprice is static or dynamic
		try {
			double staticPrice = (Double) p.getValue("soyPrice");
			
			
			if (staticPrice >= 0) {
	
		//		 h.setSendingStaticCommodityPrices(LandUse.SINGLESOY,staticPrice);
				 h.setSendingStaticCommodityPrices(LandUse.SOY, staticPrice);
	
			} else {
		//		priceLists.put(LandUse.SINGLESOY, new FileInputStream("auxdata/prices/soySinopPrice.txt"));
				priceLists.put(LandUse.SOY, new FileInputStream("auxdata/prices/soySinopPrice.txt"));
			//	priceLists.put(LandUse.SOY, new FileInputStream("auxdata/prices/soyScenarioSinopPrice"));
			//	priceLists.put(LandUse.SOY, new FileInputStream("auxdata/prices/soyPriceTest.txt"));
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		//check if corn price is static or dynamic
		try {
			double staticPrice = (Double) p.getValue("cornPrice");
			if (staticPrice >= 0) {
			//	 h.setSendingStaticCommodityPrices(LandUse.DOUBLESOY,staticPrice);
				 h.setSendingStaticCommodityPrices(LandUse.CORN,staticPrice);
				
			} else {
			//	priceLists.put(LandUse.DOUBLESOY, new FileInputStream("auxdata/prices/cornSinopPrice.txt"));
				priceLists.put(LandUse.CORN, new FileInputStream("auxdata/prices/cornSinopPrice.txt"));
				
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	
		//check if other price is static or dynamic
		try {
			double staticPrice = (Double) p.getValue("otherPrice");
			if (staticPrice >= 0) {
		//		h.setSendingStaticCommodityPrices(LandUse.OTHERCROPS, staticPrice);
				h.setSendingStaticCommodityPrices(LandUse.OTHERCROPS, staticPrice);
			} else {
				priceLists.put(LandUse.OTHERCROPS, new FileInputStream("auxdata/prices/other.prices.txt"));
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		//get cotton price
		try {
			double staticPrice = (Double) p.getValue("cottonPrice");
			if (staticPrice >= 0){
				 h.setSendingStaticCommodityPrices(LandUse.COTTON,staticPrice);
			}
		 else {
			priceLists.put(LandUse.COTTON, new FileInputStream("auxdata/prices/cottonSinopPrice.txt"));
		 }
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
  	if (!priceLists.isEmpty()) {
  		Map<LandUse, InputStream> priceStreams = priceLists;
 
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
			h.setSendingDynamicCommodityPrices(e.getKey(),cPrices);
					}
  	        
  	        }
    
        }
   }

//  public void setReceivingPrice(ReceivingSoybeanAgent h){
	  public void setReceivingPrice(ReceivingTraderAgent h){	  
	     Parameters p = RunEnvironment.getInstance().getParameters();
	// System.out.println ("soyPrice =" + (Double) p.getValue("soyPrice"));
		// load data: prices
		Map<LandUse, InputStream> priceLists = new HashMap<LandUse, InputStream>();
						
		//check if soyprice is static or dynamic
		try {
			double staticPrice = (Double) p.getValue("soyPrice");
						
			if (staticPrice >= 0) {
	
				 h.setReceivingStaticCommodityPrices(LandUse.SOY,staticPrice);
	
			} else {
				priceLists.put(LandUse.SOY, new FileInputStream("auxdata/prices/soyGannanPrice.txt"));
			//	priceLists.put(LandUse.SOY, new FileInputStream("auxdata/prices/soyPriceTest.txt"));
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		//check if corn price is static or dynamic
		try {
			double staticPrice = (Double) p.getValue("cornPrice");
			if (staticPrice >= 0) {
				 h.setReceivingStaticCommodityPrices(LandUse.CORN,staticPrice);
				
			} else {
				priceLists.put(LandUse.CORN, new FileInputStream("auxdata/prices/cornGannanPrices.txt"));
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		//check if rice price is static or dynamic
		try {
			double staticPrice = (Double) p.getValue("ricePrice");
			if (staticPrice >= 0) {
				h.setReceivingStaticCommodityPrices(LandUse.RICE, staticPrice);
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
				h.setReceivingStaticCommodityPrices(LandUse.OTHERCROPS, staticPrice);
			} else {
				priceLists.put(LandUse.OTHERCROPS, new FileInputStream("auxdata/prices/other.prices.txt"));
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		
		
	if (!priceLists.isEmpty()) {
		Map<LandUse, InputStream> priceStreams = priceLists;

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
			h.setReceivingDynamicCommodityPrices(e.getKey(),cPrices);
					}
	        
	        }
 
     }
	
	 
}
  
  
  
	}

