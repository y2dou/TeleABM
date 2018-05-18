package teleABM;

public class XYGenerator {
	private int x;
	private int y;
	
	private int radius;
	private int radiusLimit = Integer.MAX_VALUE;
	
	public XYGenerator() {
		x = 0;
		y = 0;
		radius = 0;
	}
	
	public XYGenerator(int radiusLimit) {
		this();
		this.radiusLimit = radiusLimit;
	}
	
	public boolean hasNext() {
		return radius <= radiusLimit;
	}
	
	public Point next() {
		Point point = new Point(x, y);
		
		// generate next point
		if (x == radius && y == radius) {
			radius++;
			
			x = -radius;
			y = -radius;
		} else {
			if (x == radius) {
				x = -radius;
				y++;
			} else if (y == -radius || y == radius) {
				x++;
			} else {
				x = radius;
			}
		}
		
		return point;
	}
	
	public static void main(String[] args) {
		XYGenerator g = new XYGenerator();
		for (int i = 0; i < 100; i++) {
			System.out.println(g.next());
		}
	}
}
