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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javolution.util.FastTable;
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
	 
	protected double costConvertToRicePaddy=2000.0/(cellsize*cellsize);
	
	protected double totalFertilizerInput=0;
	
	protected double totalFuelInput=0;
	
	protected double totalWaterInput = 0;

	protected  FastTable<LandCell> planningSoyCells = new FastTable<LandCell>();
	protected  FastTable<LandCell> planningCornCells = new FastTable<LandCell>();
	protected FastTable<LandCell> planningRiceCells = new FastTable<LandCell>();
	protected  FastTable<LandCell> planningOtherCells = new FastTable<LandCell>();
	//these are to record all planning cells

	
	
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
		
		///environment cost
		//this.setFertilizerUnitCost(0.8);
		//n fertilizer price is 80 yuan/bag, which is 0.8yuan/kg
		//however, there are K, P fertilizer and other chemicals farmers need to buy,
		//according to data, corn per ha fertilizer cost is [1050-1875]
        //divided by fertilizer amount, the unit here should be 8 yuan/kg;
		this.setFertilizerUnitCost(8.0);
		
		//this.setFuelUnitCost(6.21);
		// 6.21 yuan/litre
		//same applies to fuel cost
		//
		this.setFuelUnitCost(16.2);
		
		
		this.setCornPerHaFuelInput(102.6);
		this.setSoyPerHaFuelInput(80.43);
		this.setRicePerHaFuelInput(163.6);
		this.setOtherPerHaFuelInput(30.0);				
	}
	
	@Override
    public void updateProduction(OrganicSpace organicSpace) {
    	 //here is to excute LUCC change,
    	//land use decision is only decision
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
	//    System.out.println("receiving tenure "+this.tenureCells.size());
	    
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
		    	riceProduction+=c.getRiceYield();
		//    	System.out.println(c.getCropYield());
		    	
		  //  	System.out.println("rice++");
		    } else if (c.getLandUse()==LandUse.CORN) {
		    	this.cornCells.add(c);
		    	cornProduction+=c.getCornYield();
		    	
		    } else if (c.getLandUse()==LandUse.SOY) {
		    	this.soyCells.add(c);
		    	soyProduction+=c.getSoyYield();
		    	
		//   	  System.out.println("soy yiled= "+c.getCropYield());
		    	
		    } else if(c.getLandUse()==LandUse.OTHERCROPS){
		   	    this.otherCells.add(c);
		    	otherProduction+=c.getOtherYield();
		    	
		    }
	   }
	   
	 
	//   System.out.println(riceProduction);
	   if (riceProduction>0) this.grownRice=true;
	   if (cornProduction>0) this.grownCorn=true;
	   if (soyProduction>0) { this.grownSoy=true; this.grownSoyYears=grownSoyYears+1;
	//   System.out.println("has soy production "+grownSoyYears);
	
	   }
	   
	   if (otherProduction>0) this.grownOther=true;
	   
	   this.setGrownSoyYears(grownSoyYears);
		this.setSoyProduction(soyProduction);
		this.setCornProduction(cornProduction);
		this.setRiceProduction(riceProduction);
		this.setOtherProduction(otherProduction);
		
    }
    
	@Override
    public void updateCost(OrganicSpace organicSpace){
    	//perHaFertilizerInput is the average fertilizer usage by the farmer. 
    			double totalCost = 0;
    			double totalfertilizeruse=0;
    			
    			if(soyCells.size()>0){
    			for (int i=0; i<soyCells.size();i++){
    				//setFertilizerInput() has already been put at land use decision ();
    				totalCost+=soyCells.get(i).getFertilizerInput()*fertilizerUnitCost;   				
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
    					totalfertilizeruse+=cornCells.get(i).getFertilizerInput();
    					}
    				cornPerHaFertilizerInput=totalfertilizeruse/cornCells.size();
    				cornPerHaFertilizerInput = cornPerHaFertilizerInput/(cellsize*cellsize)*10000.0;
    				setCornPerHaFertilizerInput(cornPerHaFertilizerInput);
    				totalfertilizeruse=0;
    				}
    			
    			if(riceCells.size()>0){
    				for (int i=0; i<riceCells.size();i++){
    					totalCost+=riceCells.get(i).getFertilizerInput()*fertilizerUnitCost;
    					totalfertilizeruse+=riceCells.get(i).getFertilizerInput();
    				}
    				ricePerHaFertilizerInput=totalfertilizeruse/riceCells.size();
    				ricePerHaFertilizerInput = ricePerHaFertilizerInput/(cellsize*cellsize)*10000.0;
    				setRicePerHaFertilizerInput(ricePerHaFertilizerInput);
    				totalfertilizeruse = 0;
    				}   			
    			if(otherCells.size()>0){
    				for (int i=0; i<otherCells.size();i++){
    					totalCost+=otherCells.get(i).getFertilizerInput()*fertilizerUnitCost;
    				    totalfertilizeruse+=otherCells.get(i).getFertilizerInput();
    				}
    				otherPerHaFertilizerInput=totalfertilizeruse/otherCells.size();
    				otherPerHaFertilizerInput = otherPerHaFertilizerInput/(cellsize*cellsize)*10000.0;
    				setOtherPerHaFertilizerInput(otherPerHaFertilizerInput);
    				}
    			
    }
	
	
	@Override
	public void landUseDecision(OrganicSpace organicSpace) {
		//this function is to make current year land use decisions
		
//		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		//tick has to be called everytime it is used. 	
		
		totalFuelInput = 0.0;
		totalFertilizerInput = 0.0;
		totalWaterInput = 0.0;
		
		planningSoyCells.clear();
		planningCornCells.clear();
		planningRiceCells.clear();
		planningOtherCells.clear();
		
//		int landUseNumber;
	//	 OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
//		 System.out.println("receiving land use decision: "+organicSpace.getTypeID());
		 soyPerHaFuelInput = this.getSoyPerHaFuelInput();
		 cornPerHaFuelInput = this.getCornPerHaFuelInput();
		 ricePerHaFuelInput = this.getRicePerHaFuelInput();
		 otherPerHaFuelInput = this.getOtherPerHaFuelInput();
		 fuelUnitCost = this.getFuelUnitCost();
		 
//		LandUse highestLandUse=LandUse.SOY;
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
	    
	//    int i=priceMemoryLimit -1; 
	    double soyPriceDelta;
	    double cornPriceDelta;
	    double ricePriceDelta;
	//    System.out.println(soyPrices.get(0));
	    //this is to make sure the first three years run well.
	    if(soyPrices.size()==1){
	    	soyPriceDelta = soyPrices.get(0).doubleValue();
	    	cornPriceDelta = cornPrices.get(0).doubleValue();
	    	ricePriceDelta = ricePrices.get(0).doubleValue();
	    } else if(soyPrices.size()==2){
	    	soyPriceDelta = soyPrices.get(1).doubleValue() - 
	    			soyPrices.get(0).doubleValue();
	    	cornPriceDelta = cornPrices.get(1).doubleValue() - 
	    			cornPrices.get(0).doubleValue();
	    	ricePriceDelta = ricePrices.get(1).doubleValue()-
	    			ricePrices.get(0).doubleValue();
	    } else{
	    	soyPriceDelta = soyPrices.get(2).doubleValue()-
			           soyPrices.get(1).doubleValue();
	    	cornPriceDelta = cornPrices.get(2).doubleValue() - 
	    			         cornPrices.get(1).doubleValue();
	    	ricePriceDelta = ricePrices.get(2).doubleValue() -
	    			         ricePrices.get(1).doubleValue();
	    }
	    
	//    System.out.println("soyprice different "+soyPriceDelta);
	    
	//    System.out.println("corn price different "+cornPriceDelta);
	    
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
				     //     (soyPrices.get(2).doubleValue()-
				      //     soyPrices.get(1).doubleValue()) +
				         soyPriceDelta+
				          coef_1[18]*
				     //     (cornPrices.get(2).doubleValue()-
				     //      cornPrices.get(1).doubleValue())+
				          cornPriceDelta+
				          coef_1[19] *
				     //     (ricePrices.get(2).doubleValue()-
					//	  ricePrices.get(1).doubleValue())
				          ricePriceDelta
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
		        soyPriceDelta+
		          coef_2[18]*
		        cornPriceDelta+
		          coef_2[19] *
		          ricePriceDelta
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
		         coef_2[17]*
			        soyPriceDelta+
			          coef_2[18]*
			        cornPriceDelta+
			          coef_2[19] *
			          ricePriceDelta
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
		int max=2;
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
//		   System.out.println("most likely "+max);
		   max=2;

		 if(max>0){
	//	   System.out.println("most likely "+max);
	//	   System.out.println("grow soy year: "+this.getGrownSoyYears());
		   }

			List<Integer> listToChange = new ArrayList<Integer>();
			List<Integer> listNotChange = new ArrayList<Integer>();
	
	//	this.grownSoyYears=0;
		if(max==0 && this.grownSoyYears==0 )   //this is the type that never grow soybeans
		{ 
			
			//if it is rice, then keep it rice, 
			//if it was corn, check if rice is way more profitable. convert to rice
			if(2*lastYearCornPerHaProfit<lastYearRicePerHaProfit)
			{
				//if rice is super profitable; change 10% corn to rice;
				for (int i=0; i <cornCells.size()*0.1;i++){
					listToChange.add(i);
				}
				for (int i= (int) Math.round(cornCells.size()*0.1);i<cornCells.size();i++){
					listNotChange.add(i);
				}
			}
			
			for(Integer i:listToChange) {
				planningRiceCells.add(cornCells.get(i));
				capital-=costConvertToRicePaddy;
			
			}
			
			for( Integer i: listNotChange){
				planningCornCells.add(cornCells.get(i));
			}
			
			
		    for(LandCell c:riceCells){
		    	planningRiceCells.add(c);
		    }
			
			
		//end of if(	max==0 && this.grownSoyYears==0 )

		}
		
		if(max==0 && this.grownSoyYears>0 ) {
			//reduce soybean growing number
			//need to overwrite based on the regression;
			listToChange.clear();
			listNotChange.clear();
			//go through soy cell list, change to either corn or rice
			for (int j =0; j<this.soyCells.size();j++) 
			 {						
					if(RandomHelper.nextDouble()>0.9){
				    	listToChange.add(j);						
					} else {
					    listNotChange.add(j);	
					}
			 
			 }	
			
			for(Integer i:listToChange)
			{
				if(2*lastYearCornPerHaProfit<lastYearRicePerHaProfit)
				{
					planningRiceCells.add(soyCells.get(i));
				    capital-=costConvertToRicePaddy;
				}
				else
					planningCornCells.add(soyCells.get(i));	
			}
			
			for(Integer i:listNotChange)
			{
				planningSoyCells.add(soyCells.get(i));
			}
			//add last year's corn and rice to planning cell list					
			for(LandCell c: cornCells)
			{
				planningCornCells.add(c);
			}
			for (LandCell c:riceCells)
			{
				planningRiceCells.add(c);
			}
																			
			}
			
		
		if(max==1) {
			listToChange.clear();
			listNotChange.clear();
			//abandon soy
			for(LandCell c:soyCells)
			{
				//convert all soy cells to corn and rice
				if(lastYearCornPerHaProfit*1.8<lastYearRicePerHaProfit){
					planningRiceCells.add(c);
					capital-=costConvertToRicePaddy;
				}
				else 
					planningCornCells.add(c);	
			}
			//old corn and rice cells stay in planning
			for(LandCell c:cornCells){
				planningCornCells.add(c);
			}
			
			for(LandCell c:riceCells){
				planningRiceCells.add(c);
			}
			

		}
		
		if(max==2){
			//continue grow soy, but may change proportion
	//		System.out.println("let's see here "+organicSpace.getTypeID());
	//		System.out.println("soy size="+this.soyCells.size()+" corn size="+this.cornCells.size());
			listToChange.clear();
			listNotChange.clear();
			double random = RandomHelper.nextDoubleFromTo(0.0, 1.0);
			for (int j=0; j<this.soyCells.size()*random;j++) 
			 {	
				listToChange.add(j);						
			 
			 }	
			for (int j=(int)Math.round(soyCells.size()*(1-random));j<soyCells.size();j++)
			{
				listNotChange.add(j);
			}
			
			for(Integer i:listToChange){
				if(2.0*lastYearCornPerHaProfit<lastYearRicePerHaProfit)
				{
					planningRiceCells.add(soyCells.get(i));
				    capital-=costConvertToRicePaddy;
				}
				else
					planningCornCells.add(soyCells.get(i));		
			}
			
			for(Integer i:listNotChange){
				planningSoyCells.add(soyCells.get(i));
			}
			
			for(LandCell c:cornCells){
				planningCornCells.add(c);
			}
			
			for(LandCell c:riceCells){
				planningRiceCells.add(c);
			}
	//		System.out.println("planning="+planningSoyCells.size()+" //corn="+planningCornCells.size());
		}
		
		if(max==3){
			//new grower
			listToChange.clear();
			listNotChange.clear();
			
			double random = RandomHelper.nextDoubleFromTo(0.0, 1.0);
			for (int j=0; j<this.cornCells.size()*random;j++) 
			 {	
				listToChange.add(j);						
			 
			 }				
			for (int j=(int)Math.round(cornCells.size()*(1-random));j<cornCells.size();j++)
			{
				listNotChange.add(j);
			}
			
			for(Integer i:listToChange){
				planningSoyCells.add(cornCells.get(i));
			}
			
			for(Integer i:listNotChange){
				planningCornCells.add(cornCells.get(i));
			}
		    for(LandCell c:soyCells){
		    	planningSoyCells.add(c);
		    }
			
			for(LandCell c:riceCells){
				planningRiceCells.add(c);
			}
			
		
		}
		
		//update land use and environment-nexus:
		//fertilizer, fuel, and water use
		
		this.updateLandUse(organicSpace);
	
	}
	
	public void landUseDecisionBeta(OrganicSpace organicSpace){
		totalFuelInput = 0.0;
		totalFertilizerInput = 0.0;
		totalWaterInput = 0.0;
		
		planningSoyCells.clear();
		planningCornCells.clear();
		planningRiceCells.clear();
		planningOtherCells.clear();
		
//		int landUseNumber;
	//	 OrganicSpace organicSpace = (OrganicSpace) ContextUtils.getContext(this);
//		 System.out.println("receiving land use decision: "+organicSpace.getTypeID());
	/*	 soyPerHaFuelInput = this.getSoyPerHaFuelInput();
		 cornPerHaFuelInput = this.getCornPerHaFuelInput();
		 ricePerHaFuelInput = this.getRicePerHaFuelInput();
		 otherPerHaFuelInput = this.getOtherPerHaFuelInput();
		 fuelUnitCost = this.getFuelUnitCost();
		 
		 double[] cost= new double[4];
		 //cost[0]=soybean
		 //cost[1]=corn
		 //cost[2]=rice
		 //cost[3]=others;
		 cost[0] = soyPerHaFuelInput * fuelUnitCost +   //plowing cost
				 400.0 +  //400 is for seed
				 soyPerHaFertilizerInput * fertilizerUnitCost  //fertilizer + pesticide cost
				 ;
		 
		 cost[1] = cornPerHaFuelInput * fuelUnitCost +
				 785.0 +
				 cornPerHaFertilizerInput * fertilizerUnitCost
				 ;
		 cost [2] = ricePerHaFuelInput * fuelUnitCost +
				 600.0 +
				 ricePerHaFertilizerInput * fertilizerUnitCost
				 ;
		 cost [3] = otherPerHaFuelInput * fuelUnitCost +
				  200.0 +
				  otherPerHaFertilizerInput *fertilizerUnitCost
				  ;
		 
		 double[]  rawIncome = new double [4];
		 double cPrice;
		 double soySubsidy = 2000;
		 double cornSubsidy = 1000;
		 double riceSubsidy = 625;
		 cPrice=this.getCommodityPrice(LandUse.SOY);		    
		 rawIncome[0] = soyPerHaYield * cPrice + soySubsidy;
		 rawIncome[1] = cornPerHaYield * cPrice + cornSubsidy;
		 rawIncome[2] = ricePerHaYield * cPrice + riceSubsidy;
		 rawIncome[3] = otherPerHaYield ;*/
		
		List<Integer> listToChange = new ArrayList<Integer>();
		List<Integer> listNotChange = new ArrayList<Integer>();
		
		double[] profit = new double[4];
	
		profit[0] = lastYearSoyPerHaProfit;
		profit[1] = lastYearCornPerHaProfit;
		profit[2] = lastYearRicePerHaProfit;
		profit[3] = lastYearOtherPerHaProfit;
		; 
		double highestProfit = profit[0];
		int count=0;
		for (int i = 0; i<4; i++){
		//	System.out.println(i+" = "+profit[i]);
			if (highestProfit<profit[i])
				{ highestProfit = profit[i];
		          count = i;
				}
	        }
	//    System.out.println("highest count="+count);
	//    count=0;
		if(count==0)  //highest profit is soybean growing
		{
			for (int i=0; i <cornCells.size()*0.5;i++){
				listToChange.add(i);
			}
			for (int i= (int) Math.round(cornCells.size()*0.5);i<cornCells.size();i++){
				listNotChange.add(i);
			}
			
			  for(LandCell c:riceCells){
			    	planningRiceCells.add(c);
			    }
			  for( Integer i: listNotChange){
					planningCornCells.add(cornCells.get(i));
				}
			  for(Integer i:listToChange)
				   planningSoyCells.add(cornCells.get(i));
			  
			  for(LandCell c:soyCells)
				  planningSoyCells.add(c);
		}
		
		if (count == 1) {  //highest profit is corn 
			
			for (int i=0; i <soyCells.size()*0.8;i++){
				if(soyCells.get(i).getSoyAge()>=1)
				listToChange.add(i);
				else
					listNotChange.add(i);
			}
			for (int i= (int) Math.round(soyCells.size()*0.8);i<soyCells.size();i++){
				listNotChange.add(i);
			}
			
			
			for(LandCell c:riceCells) {
				planningRiceCells.add(c);
			}
			for (Integer i: listNotChange) {
				planningSoyCells.add(soyCells.get(i));
			}
			for (Integer i: listToChange) {
				planningCornCells.add(soyCells.get(i));				
			}
			for(LandCell c: cornCells) {
				planningCornCells.add(c);
			}
			
		}
		
		if (count ==2) {
			if (lastYearSoyPerHaProfit>lastYearCornPerHaProfit) {   //if soy makes more money than corn
				for (int i=0; i <cornCells.size()*0.5;i++){
					listToChange.add(i);          //change to rice
					
				}
				for (int i= (int) Math.round(cornCells.size()*0.5);i<cornCells.size();i++){
					listNotChange.add(i);          //may change to soybean?
				}
				for (Integer i: listToChange) {
					planningRiceCells.add(cornCells.get(i));
					capital-=costConvertToRicePaddy;
				}
				for (Integer i: listNotChange) {
					planningSoyCells.add(cornCells.get(i));
				}
				for (LandCell c:soyCells) {
					planningSoyCells.add(c);
				}
			
			
			} else {                                                  //if corn makes more money than soy
				for (int i=0; i <soyCells.size()*0.5;i++){   
					
					listToChange.add(i);    //change to rice
				}
				for (int i= (int) Math.round(soyCells.size()*0.5);i<soyCells.size();i++){
					listNotChange.add(i);           //may change to corn?
				}
				for (Integer i: listToChange) {
					planningRiceCells.add(soyCells.get(i));		
					capital-=costConvertToRicePaddy;
				}
				for (Integer i: listNotChange) {
					planningCornCells.add(soyCells.get(i));
				}
				
				for (LandCell c:cornCells) {
					planningCornCells.add(c);
				}
			}
			
			for(LandCell c:riceCells) {
				planningRiceCells.add(c);
			}
			
			
			
		}
		
		this.updateLandUse(organicSpace);
		
	
		
	
	}
	
	
	@Override
	public void updateProfit(){
		int tick = (int) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		//	tick has to be called everytime it is used;
		double cPrice=0;
		 double soySubsidy = 2500;
		 double cornSubsidy = 3000;
		 double riceSubsidy = 625;
		 
		
//		lastYearSoyPerHaProfit=0;
//		lastYearCornPerHaProfit=0;
//		lastYearRicePerHaProfit=0;
//		lastYearOtherPerHaProfit=0;
		//current year price	
		profit=0;
		
//		System.out.println("update profit at soybean abstract: "
//		                +tick+": "+ this.getCommodityPrice(LandUse.SOY));
		
		cPrice=this.getCommodityPrice(LandUse.SOY);
		
	//	System.out.println("to check if price is signed  "+cPrice);
		
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
		
//		if (grownSoy && soyCells.size()>0) {
		if(soyProduction > 0 && soyCells.size()>0)	{
//			double soyYield=0;
//			for (int i=0; i < soyCells.size();i++) {
//				yield = soyCells.get(i).getCropYield();
//				soyYield+=yield;
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
					                 -soyPerHaFertilizerInput*fertilizerUnitCost 
					                 - 400.0 //seed cost
					                 + soySubsidy;
		//	System.out.println("soy per ha yield="+soyPerHaYield+" per ha fuel input="+soyPerHaFuelInput+
		//			"soyPerHaFertilizerInput = "+soyPerHaFertilizerInput);
		//	System.out.println("capital = "+capital+" soy profit = "+lastYearSoyPerHaProfit);
			
		}
		
//		System.out.println("soy yield: "
	 //           +this.getCommodityPrice(LandUse.CORN));
		if (cornProduction>0) {
				
				cPrice=this.getCommodityPrice(LandUse.CORN);

				profit+=cornProduction*cPrice;
		//		this.cornSoldToTraderAgent.addCornAmount(yield);
		//		this.cornSoldToTraderAgent.purchaseCommodity(yield*cPrice);
				
			cornPerHaYield=cornProduction/cornCells.size();
			cornPerHaYield = (cornPerHaYield/(cellsize*cellsize))*10000.0;
			lastYearCornPerHaProfit = cornPerHaYield*cPrice-cornPerHaFuelInput*fuelUnitCost
	                -cornPerHaFertilizerInput*fertilizerUnitCost 
	                -785.0 //seed cost
	                + cornSubsidy
	                ;
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
	                -ricePerHaFertilizerInput*fertilizerUnitCost 
	                -600.0  //seed cost
	                + riceSubsidy;
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
	
	public double getTotalWaterInput() {
		return totalWaterInput;
	}

	public void setTotalWaterInput(double totalWaterInput) {
		this.totalWaterInput = totalWaterInput;
	}

	@Override
	public void updateLandUse(OrganicSpace organicSpace) {
		// TODO Auto-generated method stub
		
		for(int j=0;j<planningSoyCells.size();j++){
			//		System.out.println("still grow soys "+organicSpace.getTypeID());
					LandCell c = planningSoyCells.get(j);
					c.setLastLandUse(c.getLandUse());
					c.setLandUse(LandUse.SOY);
					organicSpace.setLandUse(2, c.getXlocation(), c.getYlocation());
					c.setFertilizerInput(LandUse.SOY);
					c.setFuelInput((soyPerHaFuelInput/10000.0)*(cellsize*cellsize));
					c.setWaterRequirement(LandUse.SOY);
					
					capital-= c.getFertilizerInput()*fertilizerUnitCost +
							  c.getFuelInput()*fuelUnitCost;
					 totalFertilizerInput+= c.getFertilizerInput();
					  totalFuelInput+= c.getFuelInput();
					  totalWaterInput+= c.getWaterRequirement();
				}
				
				for(int j=0;j<planningCornCells.size();j++){
//					System.out.println("change to corn");
					LandCell c = planningCornCells.get(j);
					c.setLastLandUse(c.getLandUse());
					c.setLandUse(LandUse.CORN);
					organicSpace.setLandUse(6, c.getXlocation(), c.getYlocation());
					c.setFertilizerInput(LandUse.CORN);
					c.setFuelInput((cornPerHaFuelInput/10000.0)*(cellsize*cellsize));
					c.setWaterRequirement(LandUse.CORN);
					capital-= c.getFertilizerInput()*fertilizerUnitCost +
						      c.getFuelInput()*fuelUnitCost;
					 totalFertilizerInput+= c.getFertilizerInput();
					  totalFuelInput+= c.getFuelInput();
					  totalWaterInput+= c.getWaterRequirement();
					
				}
				
				for(int j=0;j<planningRiceCells.size();j++){
					LandCell c = planningRiceCells.get(j);
					c.setLastLandUse(c.getLandUse());
					c.setLandUse(LandUse.RICE);
					organicSpace.setLandUse(3, c.getXlocation(), c.getYlocation());
					
					c.setFertilizerInput(LandUse.RICE);
					c.setFuelInput((ricePerHaFuelInput/10000.0)*(cellsize*cellsize));
					c.setWaterRequirement(LandUse.RICE);
					capital-= c.getFertilizerInput()*fertilizerUnitCost +
							  c.getFuelInput()*fuelUnitCost;
					 totalFertilizerInput+= c.getFertilizerInput();
					  totalFuelInput+= c.getFuelInput();
					  totalWaterInput+= c.getWaterRequirement();
					
				}
				
				
			 
			  
			  
			  
//			System.out.println("total fertilizer use = "+totalFertilizerInput);	
//			System.out.println("total fuel use = "+totalFuelInput);	
//			System.out.println(tick+" "+"id"+this.getID()+" soy size at decision: "+planningSoyCells.size());
//			System.out.println("corn size at decision: "+planningCornCells.size());
//			System.out.println("rice cells: "+planningRiceCells.size());
//			System.out.println("\\");
			setTotalFertilizerInput(totalFertilizerInput);
			setTotalFuelInput(totalFuelInput);
			setTotalWaterInput(totalWaterInput);
			
	//		System.out.println("farmer id: "+ this.getID()+ " has size "+ this.tenureCells.size()
	//		                 +" soy size "+soyCells.size()+ " corn size " +cornCells.size()
	//		                 + " rice size "+riceCells.size());
	}


}
