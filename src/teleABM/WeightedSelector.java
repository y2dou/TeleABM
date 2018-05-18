/**
 * 
 */
package teleABM;

import java.util.ArrayList;
import java.util.ListIterator;

import repast.simphony.random.RandomHelper;
import cern.jet.random.engine.RandomEngine;

/**
 * @author DOU Yue
 * This is for non-normal distribution
 */
public class WeightedSelector<T> {

	private ArrayList<Double> cumulativeProbabilities = new ArrayList<Double>();
	private ArrayList<T> objects = new ArrayList<T>();
	
	private double totalProbability = 0;
	
	private int numRemoved = 0;
	
	private RandomEngine generator;
	
	public WeightedSelector(String name) {
	
//		generator = RandomHelper.getGenerator(name);
		
		generator = RandomHelper.getGenerator();
		//even without name it works fine, have tested. 
	
	}
	
	public void add(T option, double relativeProbability) {
		totalProbability += relativeProbability;
		
		objects.add(option);
		cumulativeProbabilities.add(totalProbability);
	}
	/**
	 * <p>
	 * Remove an item from the weighted selector. This is expected to be an infrequent operation,
	 * so its optimality is sacrificed to improve the performance of the addition and sampling operations.
	 * 
	 * <p>
	 * This method nullifies the option and sets its selection probability to zero.
	 * 
	 * <p>
	 * This operation is not appropriate if many calls to the <code>remove</code> operation are performed, as it
	 * will result in many empty entries.
	 * 
	 * @param option The option to be removed.
	 */
	public void remove(T option) {
		int index = objects.indexOf(option);
		
		if (index == -1)
			return;
		
		objects.set(index, null);
		
		double relativeProbability;
		try {
			relativeProbability = cumulativeProbabilities.get(index) - cumulativeProbabilities.get(index - 1);
			cumulativeProbabilities.set(index, cumulativeProbabilities.get(index - 1));
		} catch (Exception e) {
			relativeProbability = cumulativeProbabilities.get(index);
			cumulativeProbabilities.set(index, 0d);
		}
		
		for (int i = index + 1; i < size(); i++) {
			cumulativeProbabilities.set(i, cumulativeProbabilities.get(i) - relativeProbability);
		}
		
		totalProbability -= relativeProbability;
		numRemoved++;
	}
	
	public void reset() {
		totalProbability = 0;
		numRemoved = 0;
		
		objects.clear();
		cumulativeProbabilities.clear();
	}
	
	/**
	 * Samples an option based on weighted probabilities.
	 * 
	 * @see #sampleIndex()
	 * @return The sampled option.
	 */
	public T sample() {
		return objects.get(sampleIndex()); 
	}
	
	/**
	 * Samples an option (index) based on weighted probabilities.
	 * 
	 * <p>
	 * This operation has a worst-case performance of O(n) and an average performance of O(n/2) assuming the 
	 * weights are the same. Runtime can be improved by rearranging the elements in decreasing order of weight.
	 * 
	 * @return The index of the sampled option.
	 */
	public int sampleIndex() {
		double sampled = generator.nextDouble() * totalProbability;
		
		ListIterator<Double> iter = cumulativeProbabilities.listIterator();
		while (iter.hasNext()) {
			Double d = iter.next();
			if (sampled < d)
				return iter.previousIndex();
		}
		return objects.size() - 1; 
	}
	
	public int size() {
		return objects.size() - numRemoved;
	}
	
	/**
	 * Return the cumulative probability. For a typical probability distribution, this would return 1, but this function 
	 * returns the sum of the elements' relative probabilities.
	 * 
	 * <p>This function will not be used in most cases.</p>
	 * 
	 * @return cumulative probability
	 */
	public double getCumulativeProbability() {
		return totalProbability;
	}
	
	/**
	 * Test the weighted selector. This creates a weighted selector with known probabilities and 
	 * outputs the results.
	 * 
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
		int samples = 100000;
		
		if (args.length > 0) {
			try {
				samples = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {}
		}
		
		RandomHelper.registerGenerator("test", (int) System.currentTimeMillis());
		WeightedSelector<String> selector = new WeightedSelector<String>("test");
		selector.add("1/100", 1);
		selector.add("55/155", 55); // to be removed
		selector.add("5/100", 5);
		selector.add("15/100", 15); //21
		selector.add("19/100", 19); //40
		selector.add("40/100", 40); //80
		selector.remove("55/155");  // removing one index
		selector.add("20/100", 20);
		
		int[] counts = new int[selector.size()];
		
		for (int i = 0; i < samples; i++) {
			counts[selector.sampleIndex()]++;
		}
		
		for (int i = 0; i < selector.size(); i++) {
			System.out.print(selector.objects.get(i));
			System.out.print(": ");
			System.out.print(counts[i] / (double) samples);
			
			System.out.print(" / ");
			System.out.println(selector.cumulativeProbabilities.get(i));
		}
		System.out.print("Total: ");
		System.out.println(selector.totalProbability);
	}
	
}
