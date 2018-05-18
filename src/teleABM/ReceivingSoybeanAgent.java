/**
 * 
 */
package teleABM;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;

/**
 * @author geododo
 *
 */
public class ReceivingSoybeanAgent extends SoybeanAgent{
	
	
	private int familyPopulation;
	//average is 3.65
	private boolean hhdHeadMale;
	private int age;
	//household head age, use average 45 (minus 10 to meet the 2005 time) for now
	private double dependentRatio;
	//average is 0.21
	private double genderRatio;
	

	private boolean hhdHeadHealth;
	//411 is healthy, 28 is unhealty
	private int occupation;
	//full time farmer, 2=part time farmer, 3=non time farmer
	
	private double unhealthProportion;
	
	boolean joinedCooperatives=false;
	protected double lastYearSoyPrice;
	protected double lastYearCornPrice;
	protected double lastYearRicePrice;
	protected double lastYearOtherPrice;
	protected double knowInternationalTrade;
	protected double whetherknow_soybean_ixYes;
	protected double whether_pericoupledperi;
	protected double whetherknow_transgeneYes;
	protected double whether_know_import_gmoYes;
	 
	protected int costConvertToRicePaddy=2000;
	
	protected double totalFertilizerInput=0;
	
	protected double totalFuelInput=0;
	


	public ReceivingSoybeanAgent() {
		super();
	}
	
	public ReceivingSoybeanAgent(int id) {
		super(id);
		initializeReceiving();

	}
	
	public void initializeReceiving(){
		//special initialize for receiving systems;
//dependent ratio, gender ratio are not normal distribution, 
//so they have to be put in teleABMBuilder
		
		
	    Random r = new Random();
	    
// to initialize household head		
		if (r.nextDouble()<=0.073) {
			//99/(99+1256)
			hhdHeadMale=false;		  
		} else hhdHeadMale=true;
//to initialize family population		
		//normal distribution, 1.15 is SD, 3.46 is mean value
	    familyPopulation = (int) Math.round(r.nextGaussian()*1.15+3.46);
		//average is 3.46, almost look like normal distribution
	    
	    //to initialize hhdHead Age, normal distribution 
	 //   r = new Random(); //no need to create a random object every time
	    age = (int) Math.round(r.nextGaussian()*10.8+38.5);   
		//household head age, use average 38.5 (minus 10 to meet the 2005 time) for now
		//almost normal distribution
	     
	 //   r = new Random();
	 //   genderRadio = r.nextGaussian()*0.18+0.160; 
	    //mean is 0.1597 sd=0.18
	    
	    
	//    r = new Random();
		if (r.nextDouble()<0.716) hhdHeadHealth = true;
		else hhdHeadHealth = false;
		//970 is healthy, 384 is unhealthy
		
	//	 r = new Random();
		 double tempProb = r.nextDouble();
			if (tempProb<0.04) occupation = 3;
			else {if (tempProb<0.18) occupation =2;
			else occupation = 1;}
		//1116 full time farmer, 56 non farmer, 184 part-time farmer =1356 total	
			
	//	 r = new Random();
		unhealthProportion = r.nextGaussian()*0.188+0.11;
		
		
		if (r.nextDouble()<0.540) 	knowInternationalTrade=1.0;
		else knowInternationalTrade=0.0;
		
		if (r.nextDouble()<0.281)  whetherknow_soybean_ixYes=1.0;
		else whetherknow_soybean_ixYes=0.0;
		
		if (r.nextDouble()<0.022) whether_pericoupledperi = 1.0;
		else whether_pericoupledperi=0.0;
		
		if (r.nextDouble()<0.709) whetherknow_transgeneYes = 1.0;
		else whetherknow_transgeneYes=0.0;
		
		if (r.nextDouble()<0.312) whether_know_import_gmoYes = 1.0;
		else whether_know_import_gmoYes=0.0;
		
		
	
	//	System.out.println(hhdHeadHealth);
	//	System.out.println(genderRadio);
	//	System.out.println(dependentRadio);
	//	System.out.println(age);
	}
	
	/*public double[] readCoef(){
		
		InputStream[] coefLists = new InputStream[4];
		
		
		double[] coef_1= new double[20];
		
		File file = new File("auxdata/prices/soyPrice.txt");
		try(FileInputStream fis = new FileInputStream(file)){
			System.out.println("Total file size to read:"+fis.available());
			if(fis.available()>0) {
				for(int i=0;i<20;i++){
					coef_1[i]=fis.e
				}
			}
		}catch (FileNotFoundException e1) {
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
		return coef_1;
		
	}*/
	
	public void landUseDecision() {
		//this function is to make current year land use decisions
		
//		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		//	
		setTotalFertilizerInput(0);
		setTotalFuelInput(0);
		
		int landUseNumber;
		 OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
		LandUse highestLandUse=LandUse.SOY;
/*		System.out.println("current soy price: "+cornPrices.get(2));
		System.out.println("last year soy price: "+cornPrices.get(1));
		System.out.println("two years ago soy price: "+cornPrices.get(0));	*/
		
		double[] exp= new double[4];
		double[] probability = new double[4];
		//[1] abandon soybean
		//[2] continue grow
		//[3] new grower		
		//[0] never grow soybean
		//to use mean to start with
	
		
		double[] coef_1= new double[20];
		double[] coef_2= new double[20];
		double[] coef_3= new double[20];
		
		double meanTemp=2.6;
		double meanSoila=22.02;
		double meanSoilb=57.14;
		double meanSoilc = 20.83;
//		double meanAge=48.48;
		double meanEducationYear=7.75;
	//	double meanDependentRatio=0.198;
	//	double meanGenderRatio=0.16;
		double meanAllSchoolYear = 6.29;
		double meanNoOffFarmIncome = 0.45;
		double meanNoBigMachine = 1.02;
//		double meanSoyPriceDelta=-0.04;
//		double meanCornPriceDelta=-0.0006;
//		double meanRicePriceDelta=0.388;
		
	    coef_1[0]=8.55; coef_1[1]=0.976;coef_1[2]=-0.193;coef_1[3]=-0.125;
	    coef_1[4]=-0.137; coef_1[5]=0.009;coef_1[6]=-0.033;coef_1[7]=-0.053;
	    coef_1[8]=-0.206; coef_1[9]=0.099;coef_1[10]=0.192;coef_1[11]=0.390;
	    coef_1[12]=-0.012; coef_1[13]=0.757;coef_1[14]=-0.264;coef_1[15]=-0.115;
	    coef_1[16]=0.564; coef_1[17]=-1.324;coef_1[18]=-0.041;coef_1[19]=1.999;
	    
	    
	    
	    coef_2[0]=50.76; coef_2[1]=1.994;coef_2[2]=-0.708;coef_2[3]=-0.518;
	    coef_2[4]=-0.658; coef_2[5]=0.009;coef_2[6]=-0.007;coef_2[7]=-0.451;
	    coef_2[8]=-0.138; coef_2[9]=-0.022;coef_2[10]=-0.019;coef_2[11]=0.225;
	    coef_2[12]=0.289; coef_2[13]=0.455;coef_2[14]=-0.606;coef_2[15]=0.441;
	    coef_2[16]=0.079; coef_2[17]=-1.835;coef_2[18]=-1.273;coef_2[19]=3.554;
	    
	    
	    coef_3[0]=-27.58; coef_3[1]=1.36;coef_3[2]=-0.011;coef_3[3]=0.275;
	    coef_3[4]=0.139; coef_3[5]=-0.047;coef_3[6]=-0.127;coef_3[7]=2.277;
	    coef_3[8]=4.323; coef_3[9]=0.129;coef_3[10]=-0.089;coef_3[11]=0.211;
	    coef_3[12]=0.520; coef_3[13]=0.402;coef_3[14]=0.943;coef_3[15]=1.224;
	    coef_3[16]=-1.237; coef_3[17]=1.826;coef_3[18]=-1.428;coef_3[19]=4.37;
	    
		exp[1] = // 1.05*age +
				         // 8.80*familyPopulation +
				         coef_1[0] +  //constant
				         coef_1[1]*meanTemp +  //temp
				         coef_1[2]*meanSoila + //soil.002
				         coef_1[3]*meanSoilb + //soil.002.02
				         coef_1[4]*meanSoilc + //soil.2
				         coef_1[5]*age + //husband age
				         coef_1[6]*meanEducationYear + //husband education
				         coef_1[7]*dependentRatio +
				         coef_1[8]*genderRatio +
				         coef_1[9]*meanAllSchoolYear +//mean average all school year
				         coef_1[10]*meanNoOffFarmIncome + 
				         coef_1[11] *  meanNoBigMachine +
				  //        (-0.32)*unhealthProportion+
				         coef_1[12]*knowInternationalTrade+ 
				         coef_1[13]*whetherknow_soybean_ixYes+
				         coef_1[14]*whether_pericoupledperi+
				         coef_1[15]*whetherknow_transgeneYes+
				         coef_1[16]*whether_know_import_gmoYes+
				      //    0.05*knowInternationalTrade +
				         coef_1[17]*
				          (soyPrices.get(2).doubleValue()-
				           soyPrices.get(1).doubleValue()) +
				          coef_1[18]*
				          (cornPrices.get(2).doubleValue()-
				           cornPrices.get(1).doubleValue())+
				          coef_1[19] *
				          (ricePrices.get(2).doubleValue()-
						  ricePrices.get(1).doubleValue())
				          ;
				          
		exp[2] = // 1.05*age +
		         // 8.80*familyPopulation +
		         coef_2[0] +  //constant
		         coef_2[1]*meanTemp +  //temp
		         coef_2[2]*meanSoila + //soil.002
		         coef_2[3]*meanSoilb + //soil.002.02
		         coef_2[4]*meanSoilc + //soil.2
		         coef_2[5]*age + //husband age
		         coef_2[6]*meanEducationYear + //husband education
		         coef_2[7]*dependentRatio +
		         coef_2[8]*genderRatio +
		         coef_2[9]*meanAllSchoolYear +//mean average all school year
		         coef_2[10]*meanNoOffFarmIncome + 
		         coef_2[11] *  meanNoBigMachine +
		  //        (-0.32)*unhealthProportion+
		         coef_2[12]*knowInternationalTrade+ 
		         coef_2[13]*whetherknow_soybean_ixYes+
		         coef_2[14]*whether_pericoupledperi+
		         coef_2[15]*whetherknow_transgeneYes+
		         coef_2[16]*whether_know_import_gmoYes+
		      //    0.05*knowInternationalTrade +
		         coef_2[17]*
		          (soyPrices.get(2).doubleValue()-
		           soyPrices.get(1).doubleValue()) +
		          coef_2[18]*
		          (cornPrices.get(2).doubleValue()-
		           cornPrices.get(1).doubleValue())+
		          coef_2[19] *
		          (ricePrices.get(2).doubleValue()-
				  ricePrices.get(1).doubleValue())
		          ;
	
		exp[3] = // 1.05*age +
		         // 8.80*familyPopulation +
		         coef_3[0] +  //constant
		         coef_3[1]*meanTemp +  //temp
		         coef_3[2]*meanSoila + //soil.002
		         coef_3[3]*meanSoilb + //soil.002.02
		         coef_3[4]*meanSoilc + //soil.2
		         coef_3[5]*age + //husband age
		         coef_3[6]*meanEducationYear + //husband education
		         coef_3[7]*dependentRatio +
		         coef_3[8]*genderRatio +
		         coef_3[9]*meanAllSchoolYear +//mean average all school year
		         coef_3[10]*meanNoOffFarmIncome + 
		         coef_3[11] *  meanNoBigMachine +
		  //        (-0.32)*unhealthProportion+
		         coef_3[12]*knowInternationalTrade+ 
		         coef_3[13]*whetherknow_soybean_ixYes+
		         coef_3[14]*whether_pericoupledperi+
		         coef_3[15]*whetherknow_transgeneYes+
		         coef_3[16]*whether_know_import_gmoYes+
		      //    0.05*knowInternationalTrade +
		         coef_3[17]*
		          (soyPrices.get(2).doubleValue()-
		           soyPrices.get(1).doubleValue()) +
		          coef_3[18]*
		          (cornPrices.get(2).doubleValue()-
		           cornPrices.get(1).doubleValue())+
		          coef_3[19] *
		          (ricePrices.get(2).doubleValue()-
				  ricePrices.get(1).doubleValue())
		          ;
/*		System.out.println("exp 1 = "+exp[1]);
		System.out.println("exp 2 = "+exp[2]);
		System.out.println("exp 3 = "+exp[3]);
		System.out.println("   ");*/
		double expSum=Math.exp(exp[1])+Math.exp(exp[2])+Math.exp(exp[3])+1;
		probability[0]=1/expSum;
		probability[1]=Math.exp(exp[1])/expSum;
		probability[2]=Math.exp(exp[2])/expSum;
		probability[3]=Math.exp(exp[3])/expSum;
		
	//	probability[0]=1-probability[1]-probability[2]-probability[3];
	//	System.out.println("prob 1 = "+probability[1]);
	//	System.out.println("prob 2 = "+probability[2]);
	//	System.out.println("prob 3 = "+probability[3]);
	//	System.out.println("prob 0 = "+probability[0]);
	//	System.out.println(" end ");
		
		//[1] abandon soybean
		//[2] continue grow
		//[3] new grower		
		//[0] never grow soybean
		int max=0;
		double maxProb=probability[0];
//to calculate the max probability
		   if(probability[1]>maxProb){
			   maxProb=probability[1];
			   max=1;
		   } else if(probability[2]>maxProb)
		         {
			       maxProb=probability[2];
			       max=2;
		          } else if(probability[3]>maxProb)
		             {  maxProb=probability[3];
		                max=3;} else{
		                	maxProb=probability[0];
		                	max=0;
		                }
//to calculate the max probability	   
	//	   System.out.println("most likely "+max);
	//	   System.out.println(this.getGrownSoyYears());
		if(max==0 && this.grownSoyYears==0 )   //this is the type that never grow soybeans
		{ 
			
			//if it is rice, then keep it rice, 
			//if it was corn, check if rice is way more profitable. convert to rice
			
			for(int i=0; i<this.getTenureCells().size();i++)
			  {
				
				LandCell c= this.getTenureCells().get(i);
				
				c.setLastLandUse(c.getLandUse());
				
				if(c.getLandUse()==LandUse.RICE){
				c.setLandUse(LandUse.RICE);
		//		c.setFertilizerInput(LandUse.RICE);
				c.setFuelInput(riceUnitFuelInput);
				organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
		        capital = capital - c.getFertilizerInput()*fertilizerUnitCost
		        	              - c.getFuelInput()*fuelUnitCost;}
				
				
				if(c.getLandUse()==LandUse.CORN){
					if(lastYearCornUnitProfit>lastYearRiceUnitProfit)
					{
						c.setLandUse(LandUse.CORN);
		//				c.setFertilizerInput(LandUse.CORN);
						c.setFuelInput(cornUnitFuelInput);
						organicSpace.setLandUse(6, c.getXlocation(), c.getYlocation());
						capital=capital- c.getFertilizerInput()*fertilizerUnitCost
								-c.getFuelInput()*fuelUnitCost;
					}
					else if (lastYearRiceUnitProfit>1.5*lastYearCornUnitProfit)
					{
						//convert to rice
						c.setLandUse(LandUse.RICE);
		//				c.setFertilizerInput(LandUse.RICE);
						c.setFuelInput(riceUnitFuelInput);
						organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
						capital=capital-costConvertToRicePaddy-c.getFertilizerInput()*fertilizerUnitCost
								-c.getFuelInput()*fuelUnitCost;
					} else {
						if(RandomHelper.nextDouble()>0.7&&(capital>costConvertToRicePaddy)) {
							c.setLandUse(LandUse.RICE);
		//					c.setFertilizerInput(LandUse.RICE);
							c.setFuelInput(riceUnitFuelInput);
							organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
							capital=capital-costConvertToRicePaddy-c.getFertilizerInput()*fertilizerUnitCost
									-c.getFuelInput()*fuelUnitCost;
						} else {
							c.setLandUse(LandUse.CORN);
		//					c.setFertilizerInput(LandUse.CORN);
							c.setFuelInput(cornUnitFuelInput);
							organicSpace.setLandUse(6, c.getXlocation(), c.getYlocation());
							capital=capital-c.getFertilizerInput()*fertilizerUnitCost
									-c.getFuelInput()*fuelUnitCost;
						}
						
					}
					
				}
			  totalFertilizerInput+=c.getFertilizerInput();
			  totalFuelInput+=c.getFuelInput();
			  
			  
			  } 
		//end of if(	max==0 && this.grownSoyYears==0 )
			System.out.println("when max==0 "+totalFertilizerInput);
		}
		
		if(max==0 && this.grownSoyYears>0 ) {
			
			for(int i=0; i<this.getTenureCells().size();i++)
			  {
				
				LandCell c= this.getTenureCells().get(i);
				
				c.setLastLandUse(c.getLandUse());
				
				if(c.getLandUse()==LandUse.CORN){
				c.setLandUse(LandUse.CORN);
		//		c.setFertilizerInput(LandUse.CORN);
				c.setFuelInput(cornUnitFuelInput);
				organicSpace.setLandUse(6, c.getXlocation(), c.getYlocation());
		        capital = capital - c.getFertilizerInput()*fertilizerUnitCost
		        	              - c.getFuelInput()*fuelUnitCost;}
				
				if(c.getLandUse()==LandUse.RICE){
					c.setLandUse(LandUse.RICE);
		//			c.setFertilizerInput(LandUse.RICE);
					c.setFuelInput(riceUnitFuelInput);
					organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
					capital=capital-c.getFertilizerInput()*fertilizerUnitCost
							-c.getFuelInput()*fuelUnitCost;}
				
				if(c.getLandUse()==LandUse.SOY){
					c.setLandUse(LandUse.CORN);
		//			c.setFertilizerInput(LandUse.CORN);
					c.setFuelInput(cornUnitFuelInput);
					organicSpace.setLandUse(6,c.getXlocation(),c.getYlocation());
					capital=capital-c.getFertilizerInput()*fertilizerUnitCost
							-c.getFuelInput()*fuelUnitCost;
				}
				if(c.getLandUse()==LandUse.OTHERCROPS){
					c.setLandUse(LandUse.OTHERCROPS);
		//			c.setFertilizerInput(LandUse.OTHERCROPS);
					c.setFuelInput(otherUnitFuelInput);
					
				}
				
				totalFertilizerInput+=c.getFertilizerInput();
				totalFuelInput+=c.getFuelInput();
				
			  }
		//	System.out.println("when max==0 but they have soy"+totalFertilizerInput);
		}
		
		if(max==1) {
			//abandon soy
			for(int i=0; i<this.getTenureCells().size();i++)
			  {
				LandCell c= this.getTenureCells().get(i);
				c.setLastLandUse(c.getLandUse());
				
				if(c.getLandUse()==LandUse.RICE){
					c.setLandUse(LandUse.RICE);
		//			c.setFertilizerInput(LandUse.RICE);
					c.setFuelInput(riceUnitFuelInput);
					organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
			        capital = capital - c.getFertilizerInput()*fertilizerUnitCost
	                                  - c.getFuelInput()*fuelUnitCost;
				}
				if(c.getLandUse()==LandUse.CORN||c.getLandUse()==LandUse.SOY){
					c.setLandUse(LandUse.CORN);
		//			c.setFertilizerInput(LandUse.CORN);
					c.setFuelInput(cornUnitFuelInput);
					organicSpace.setLandUse(6,c.getXlocation(),c.getYlocation());
					capital=capital-c.getFertilizerInput()*fertilizerUnitCost
							-c.getFuelInput()*fuelUnitCost;
				}
			
				totalFertilizerInput+=c.getFertilizerInput();
				totalFuelInput+=c.getFuelInput();
			  }
			System.out.println("when max = 1, fertilizer use =  "+totalFertilizerInput);
		}
		
		if(max==2){
			//continue grow soy, but may change proportion
			double soySize=this.soyCells.size()*0.8;
			
			for(int i=0; i<this.getTenureCells().size();i++)
			  {
				LandCell c= this.getTenureCells().get(i);
				c.setLastLandUse(c.getLandUse());
				
				if(c.getLandUse()==LandUse.RICE){
					c.setLandUse(LandUse.RICE);
		//			c.setFertilizerInput(LandUse.RICE);
					c.setFuelInput(riceUnitFuelInput);
					organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
			        capital = capital - c.getFertilizerInput()*fertilizerUnitCost
	                                  - c.getFuelInput()*fuelUnitCost;
				}
				
				
				if(c.getLandUse()==LandUse.SOY){
					if(soySize>0)
					{
					c.setLandUse(LandUse.SOY);
		//			c.setFertilizerInput(LandUse.SOY);
					c.setFuelInput(soyUnitFuelInput);
					organicSpace.setLandUse(2, c.getXlocation(), c.getYlocation());
			        capital = capital - c.getFertilizerInput()*fertilizerUnitCost
	                                  - c.getFuelInput()*fuelUnitCost;
			        soySize-=1;
			        } else {
			        	c.setLandUse(LandUse.CORN);
		//				c.setFertilizerInput(LandUse.CORN);
						c.setFuelInput(cornUnitFuelInput);
						organicSpace.setLandUse(6, c.getXlocation(), c.getYlocation());
				        capital = capital - c.getFertilizerInput()*fertilizerUnitCost
		                                  - c.getFuelInput()*fuelUnitCost;
			        }	
						
				}
				
				if(c.getLandUse()==LandUse.CORN){
					c.setLandUse(LandUse.CORN);
		//			c.setFertilizerInput(LandUse.CORN);
					c.setFuelInput(cornUnitFuelInput);
					organicSpace.setLandUse(6, c.getXlocation(), c.getYlocation());
			        capital = capital - c.getFertilizerInput()*fertilizerUnitCost
	                                  - c.getFuelInput()*fuelUnitCost;
				}
				
				totalFertilizerInput+=c.getFertilizerInput();
				totalFuelInput+=c.getFuelInput();
			  }
			System.out.println("when max = 2, fertilizer use =  "+totalFertilizerInput);
		}
		
		if(max==3){
			//new grower
			
			for(int i=0; i<this.getTenureCells().size();i++)
			  {
				LandCell c= this.getTenureCells().get(i);
				c.setLastLandUse(c.getLandUse());
				
				c.setLandUse(LandUse.SOY);
		//		c.setFertilizerInput(LandUse.SOY);
				c.setFuelInput(soyUnitFuelInput);
				organicSpace.setLandUse(2, c.getXlocation(), c.getYlocation());
		        capital = capital - c.getFertilizerInput()*fertilizerUnitCost
                                  - c.getFuelInput()*fuelUnitCost;
		        totalFertilizerInput+=c.getFertilizerInput();
				totalFuelInput+=c.getFuelInput();
			  }
			System.out.println("when max=3  fertilizer use ="+totalFertilizerInput);
		}
		
//	System.out.println("total fertilizer use = "+totalFertilizerInput);	
//	System.out.println("total fuel use = "+totalFuelInput);	
	System.out.println("  ");
	
	setTotalFertilizerInput(totalFertilizerInput);
	setTotalFuelInput(totalFuelInput);
	
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
		soyPrice=getCommodityPrice(LandUse.SOY);
	//	System.out.println("tick: "+tick+" "+soyPrice);
		
		if (grownSoy) {
			//check who has the highest price
			for (int i=0; i<this.traderAgents.size();i++) {
			//	soyPrice=traderAgents.get(i).getCommodityPrice(LandUse.SOY);
				
				if (soyPrice>highestPrice) {
					highestPrice=soyPrice;
					soySoldToTraderAgentID = i;
				}				
			}			
			soySoldToTraderAgent = this.traderAgents.get(soySoldToTraderAgentID);
			lastYearSoyPrice = highestPrice;
	//		System.out.println(tick+": last year price"+lastYearSoyPrice+
	//				" next year: "+ soyPrice);
		}
		
		if (grownCorn) {
			highestPrice=0;
			for (int i=0;i<this.traderAgents.size();i++){
				cornPrice=traderAgents.get(i).getCommodityPrice(LandUse.CORN);
				cornPrice=getCommodityPrice(LandUse.CORN);
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
				ricePrice=getCommodityPrice(LandUse.RICE);
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
				otherPrice=getCommodityPrice(LandUse.OTHERCROPS);
				if (otherPrice>highestPrice) {
					highestPrice=ricePrice;
					otherSoldToTraderAgentID = i;
				}				
			}			
			lastYearOtherPrice = highestPrice;
			otherSoldToTraderAgent = this.traderAgents.get(otherSoldToTraderAgentID);
		}
		
			
	}

	public double getDependentRatio() {
		return dependentRatio;
	}

	public void setDependentRatio(double dependentRatio) {
		this.dependentRatio = dependentRatio;
	}
	
	public double getGenderRatio() {
		return genderRatio;
	}

	public void setGenderRatio(double genderRatio) {
		this.genderRatio = genderRatio;
	}
	
	public double getTotalFertilizerInput() {
		return totalFertilizerInput;
	}

	public void setTotalFertilizerInput(double totalFertilizerInput) {
		this.totalFertilizerInput = totalFertilizerInput;
	}
	
	public double getTotalFuelInput() {
		return totalFuelInput;
	}

	public void setTotalFuelInput(double totalFuelInput) {
		this.totalFuelInput = totalFuelInput;
	}

}
