/**
 * 
 */
package teleABM;

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
      
  	List<ReceivingSoybeanAgent> receivingSoybeanAgents =
			new LinkedList<ReceivingSoybeanAgent>();
  	List<SendingSoybeanAgent> sendingSoybeanAgents =
			new LinkedList<SendingSoybeanAgent>();
    
	protected WeightedSelector<Range<Integer>> dependentRatioSelector;
	protected WeightedSelector<Range<Integer>> genderRatioSelector;
        //this is to indicate which system the instatnce is representing, it's read through the parameter.
	
		public Context<Object> build(Context<Object> context) {
			
		  // The sugarFile contains the initial/max sugar values for every point
			// on the 2D sugarspace.
			String organicFile = "misc/organicmatter.asc";
			
			Parameters p = RunEnvironment.getInstance().getParameters();
			
			int numAgents = (Integer)p.getValue("initialNumAgents");
			int numTradeAgents = (Integer) p.getValue("initialNumTradeAgents");
			
			modeAddAgents = (Integer)p.getValue("mode of adding agents");
	//		System.out.println(p.getValue("sending system representation"));
		
			dependentRatioSelector = new 
					 WeightedSelector<Range<Integer>>("dependentRatioSelector");
			
			genderRatioSelector = new 
					 WeightedSelector<Range<Integer>>("genderRatioSelector");
			
			if ( (boolean) p.getValue("sending system representation"))  {
				 sendingSystem=true;
				 receivingSystem = false;
				 System.out.println("only sending System");
			 }
			 if ( (boolean) p.getValue("receiving system representation")) {
				 receivingSystem=true;
				 sendingSystem=false;
				 System.out.println("only receiving System");
				 
			 }
			 if ( !(boolean) p.getValue("sending system representation") &&
					 !(boolean) p.getValue("receiving system representation")) {
				 receivingSystem=false;
				 sendingSystem=false;
					System.err.println("TeleABM Creator: Skipping run: invalid parameters.");
					System.exit(1);
			 }
			 
			 setUpRandomDistributions();
			 
			OrganicSpace organicSpace = new OrganicSpace(organicFile);
			context.addSubContext(organicSpace);
			context.add(organicSpace);
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
						
				
				for(int i =0; i<numAgents; i++){					
			    ReceivingSoybeanAgent h = 
					new ReceivingSoybeanAgent(i);
			    receivingSoybeanAgents.add(h);
				}				
				ArrayList<Point> corners = setXYcorner();
				System.out.println(corners.size());				
														
				
				if (modeAddAgents ==0) //add agents covering whole landscape
														
					for(int i =0; i<numAgents && i<corners.size(); i++){
				 //      System.out.println("i = "+i);
						receivingSoybeanAgents.get(i).initialize();							
						organicSpace.add(receivingSoybeanAgents.get(i));	
						
						receivingSoybeanAgents.get(i).addSoybeanAgentFromLandscape(organicSpace, corners.get(i));
				//		System.out.println("i = "+i+" add soybean agents: "+
				//		                        corners.get(i).x+" "+corners.get(i).y);
						
						receivingSoybeanAgents.get(i).addLandUseFromField(organicSpace);
				//		System.out.println("i = "+i+" add land use from field");
						
					}
					else if (modeAddAgents==1) //add agents from the land use maps
					{
						for(int i =0; i<numAgents; i++)
						{
							
							receivingSoybeanAgents.get(i).initialize();
						
							organicSpace.add(receivingSoybeanAgents.get(i));
							receivingSoybeanAgents.get(i).addSoybeanAgentFromField(organicSpace, corners.get(i));
							receivingSoybeanAgents.get(i).addLandUseFromField(organicSpace);												
						}
												
					}
				
		//	System.out.println("how many agents: "+receivingSoybeanAgents.size());	
               for( int i = 0; i<receivingSoybeanAgents.size(); i++)
				if (receivingSoybeanAgents.get(i).getTenureCells().size()==0)
				{
			//	System.out.println("remove some agents "+i);
				organicSpace.remove(receivingSoybeanAgents.get(i));
				receivingSoybeanAgents.remove(i);
			//	break;
				}
              for ( ReceivingSoybeanAgent h:receivingSoybeanAgents) {
            	  Range<Integer> ws = dependentRatioSelector.sample();
				  Integer dependentRatio = RandomHelper.nextIntFromTo(ws.getLower(), ws.getUpper());
				  h.setDependentRatio((double) dependentRatio/1000);
				  Integer genderRatio = RandomHelper.nextIntFromTo(ws.getLower(), ws.getUpper());
				  h.setGenderRatio((double) genderRatio/1000);
		//		  System.out.println("dependent ratio of this agent: "+h.getDependentRatio() +
        //                			"  "+ dependentRatio	  );
				  
              }
               
         //      System.out.println("after removing "+receivingSoybeanAgents.);	
			}  
			
				if (sendingSystem) {
				
				
				for(int i =0; i<numAgents; i++){
					SendingSoybeanAgent h =
					  new SendingSoybeanAgent(i);
			        sendingSoybeanAgents.add(h);
				}				
				ArrayList<Point> corners = setXYcorner();
				if (modeAddAgents ==0) //add agents covering whole landscape
					for(int i =0; i<numAgents; i++){
						sendingSoybeanAgents.get(i).initialize();							
						organicSpace.add(sendingSoybeanAgents.get(i));			
						sendingSoybeanAgents.get(i).addSoybeanAgentFromLandscape(organicSpace,							                                  
								                                    corners.get(i));
						sendingSoybeanAgents.get(i).addLandUseFromField(organicSpace);
					 
					}
					else if (modeAddAgents==1) //add agents from the land use maps
					{
						for(int i =0; i<numAgents; i++){
				//			soybeanAgents.get(i).initialize();
				//			organicSpace.add(soybeanAgents.get(i));
				//			soybeanAgents.get(i).addSoybeanAgentFromField(organicSpace, corners.get(i));
				//			
							sendingSoybeanAgents.get(i).initialize();
							organicSpace.add(sendingSoybeanAgents.get(i));
							sendingSoybeanAgents.get(i).addSoybeanAgentFromField(organicSpace, corners.get(i));
							sendingSoybeanAgents.get(i).addLandUseFromField(organicSpace);
							

						}
					}
				
				for(int i=0; i<sendingSoybeanAgents.size();i++)
				 if (sendingSoybeanAgents.get(i).getTenureCells().size()==0)
					{  
					  organicSpace.remove(sendingSoybeanAgents.get(i));
					  sendingSoybeanAgents.remove(i);
					}
					
			}
	
			System.out.println("add agents finished");
			
			//add trade agents
			for (int i = 0; i<numTradeAgents; i++){
				TraderAgent traderAgent = new TraderAgent(i);		
				organicSpace.add(traderAgent);
				traderAgent.initialize(organicSpace);
			}
		//	context.add(soybeanAgents);
		//	System.out.println(2);
			
			
			// If running in batch mode, schedule the sim stop time
			double endTime = 10.0;
			
			if(RunEnvironment.getInstance().isBatch())
				RunEnvironment.getInstance().endAt(endTime);
			
			RunEnvironment.getInstance().endAt(endTime);

			
		
			return context;
		}
		
		public void setUpRandomDistributions() {
			
		  if (receivingSystem) {
			RandomHelper.registerDistribution("farmCost", RandomHelper.createUniform(100.0,500.0));
			
			RandomHelper.registerDistribution("capital", RandomHelper.createNormal(10000, 500000));
			RandomHelper.registerDistribution("labour", RandomHelper.createUniform(2,10));
			RandomHelper.registerDistribution("elevationRange", RandomHelper.createUniform(1, 50));
			RandomHelper.registerDistribution("hectares", RandomHelper.createUniform(30,200));
		  //http://repast.sourceforge.net/docs/api/repastjava/repast/simphony/random/RandomHelper.html 
			//lots of different distribution
			RandomHelper.registerDistribution("soyYield", RandomHelper.createUniform(2000,3000));
			//overwrite with empirical data
			RandomHelper.registerDistribution("cornYield", RandomHelper.createUniform(4000,5000));
			RandomHelper.registerDistribution("riceYield", RandomHelper.createUniform(5000,6000));
			RandomHelper.registerDistribution("otherYield", RandomHelper.createUniform(2000,3000));
			
		  }
		  else{
			  
				RandomHelper.registerDistribution("farmCost", RandomHelper.createUniform(100d,500d));
				
				RandomHelper.registerDistribution("capital", RandomHelper.createUniform(100000, 500000));
				RandomHelper.registerDistribution("labour", RandomHelper.createUniform(4, 54));
				
				RandomHelper.registerDistribution("hectares", RandomHelper.createUniform(800,1000));
				
				RandomHelper.registerDistribution("soyYield", RandomHelper.createUniform(3000,4000));
				//overwrite with empirical data
				RandomHelper.registerDistribution("cornYield", RandomHelper.createUniform(4000,5000));
				RandomHelper.registerDistribution("riceYield", RandomHelper.createUniform(5000,6000));
				RandomHelper.registerDistribution("otherYield", RandomHelper.createUniform(2000,3000));
			  
		  }
	}
		
		public ArrayList<Point> setXYcorner () {
			   //find the corner where agents start to iterate
			
			  Parameters para = RunEnvironment.getInstance().getParameters();
			  int xdim = (Integer)para.getValue("worldWidth");
			  int ydim = (Integer)para.getValue("worldHeight");
				int numAgents = (Integer)para.getValue("initialNumAgents");
				if(receivingSystem)
				numAgents=this.receivingSoybeanAgents.size();
				else 
				numAgents=this.sendingSoybeanAgents.size();
				
				
			ArrayList<Point> corners = new ArrayList<Point>();

			   double xboundary;
			   double yboundary;
				    
			   double perAgentArea;
			   
			perAgentArea = (double) xdim*ydim/(numAgents);
				    
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
						
					//	Point p = new Point(xtick*xcount*(int)xboundary, ytick*ycount*(int)yboundary);
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
		//		System.out.println(count+" : "+p.x+" "+p.y);
		/*	for (int i=0;i<numAgents;i++){	    	
				System.out.println(i+" "+corners.get(i).x+" "+corners.get(i).y);
			}
			*/
		//		System.out.println("finished "+count);
			   return corners;
		//	    grid.moveTo(this, goodx[chosenSpotIndex], goody[chosenSpotIndex]);
			    
			    
		   }
		  
		public SoybeanAgent getAllSoybeanAgent(){
			if(receivingSystem)
			return (SoybeanAgent) receivingSoybeanAgents;
			else 
				return (SoybeanAgent) sendingSoybeanAgents;
		}
	}

