package teleABM;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class MyLandCell {

private LandCell cell;
	
	/**
	 * Current land use as intended by the cell's owner.
	 */
	private LandUse landUse;
	
	/**
	 * Immediately previous land use.
	 */
	private LandUse lastLandUse;	// belief (contrasted with truth in LandCell)
	
	private HashMap<LandUse, Double> neighbourLandUses = new HashMap<LandUse, Double>();
	
	List<LandCell> tenureCells = new LinkedList<LandCell>();
	
	public MyLandCell(LandCell c, SoybeanAgent h) {
		
		this.cell = c;
		
	}
	
	public LandUse getLandUse() {
		return this.landUse;
	}
	
	public LandCell getCell() {
		return cell;
	}
	
	public void setLandUse(LandUse landUse) {
		this.landUse = landUse;
	}
	
	
}
