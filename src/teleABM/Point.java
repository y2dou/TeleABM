package teleABM;

public class Point {
	public int x;
	public int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Point plus(Point p) {
		return new Point(x + p.x, y + p.y);
	}
	
	public Point plus(int x, int y) {
		return new Point(this.x + x, this.y + y);
	}
	
	public double getDistanceTo(Point p) {
		double dx = p.x - x;
		double dy = p.y - y;
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	public double getDistanceTo(int x2, int y2) {
		double dx = x2 - x;
		double dy = y2 - y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public Point getTransposed() {
		return new Point(y, x);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}
