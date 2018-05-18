/**
 * 
 */
package teleABM;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author geododo
 *
 */
public class MarketPrices implements Message, Iterable<Entry<LandUse, Double>> {
	private Map<LandUse, Double> prices = new HashMap<LandUse, Double>();
	
	public MarketPrices() {
		
	}
	
	public Double getPrice(LandUse good) {
		return prices.get(good);
	}
	
	public Iterator<Entry<LandUse, Double>> iterator() {
		return prices.entrySet().iterator();
	}
	
	public void setPrice(LandUse good, Double price) {
		prices.put(good, price);
	}

	@Override
	public MessageType getMessageType() {
		return MessageType.MARKET_PRICES;
	}

}

