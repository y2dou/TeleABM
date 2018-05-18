/**
 * 
 */
package teleABM;

/**
 * @author geododo
 *
 */
public class Range<T extends Number> {
	private T lower;
	private T upper;
	
	public Range(T lower, T upper) {
		this.lower = lower;
		this.upper = upper;
	}
	
	public T getLower() {
		return lower;
	}
	
	public T getUpper() {
		return upper;
	}
	
	public void setLower(T lower) {
		this.lower = lower;
	}
	
	public void setUpper(T upper) {
		this.upper = upper;
	}
}
