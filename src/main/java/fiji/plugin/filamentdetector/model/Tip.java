package fiji.plugin.filamentdetector.model;

import java.util.Arrays;

import com.google.common.primitives.Doubles;

/* 
 * Represent a tip of a TrackedFilament over time.
 * 
 */
public class Tip {

	private double[] x;
	private double[] y;
	private int[] frames;

	private double averageX = Double.NaN;
	private double averageY = Double.NaN;

	private double stdX = Double.NaN;
	private double stdY = Double.NaN;

	public Tip(double[] x, double[] y, int[] frames) {
		this.x = x;
		this.y = y;
		this.frames = frames;
	}

	public double[] getX() {
		return x;
	}

	public double[] getY() {
		return y;
	}

	public int[] getFrames() {
		return frames;
	}

	public double getAverageX() {
		if (Double.isNaN(averageX)) {
			averageX = Arrays.stream(x).summaryStatistics().getAverage();
		}
		return averageX;
	}

	public double getAverageY() {
		if (Double.isNaN(averageY)) {
			averageY = Arrays.stream(y).summaryStatistics().getAverage();
		}
		return averageY;
	}

	public double getStdX() {
		if (Double.isNaN(stdX)) {
			double rawSum = Doubles.asList(x).stream()
					.mapToDouble((xx) -> Math.pow(xx.doubleValue() - getAverageX(), 2.0)).sum();
			stdX = Math.sqrt(rawSum / (x.length - 1));
		}
		return stdX;
	}

	public double getStdY() {
		if (Double.isNaN(stdY)) {
			double rawSum = Doubles.asList(y).stream()
					.mapToDouble((yy) -> Math.pow(yy.doubleValue() - getAverageY(), 2.0)).sum();
			stdY = Math.sqrt(rawSum / (y.length - 1));
		}
		return stdY;
	}

	public double getDispX() {
		return x[x.length - 1] - x[0];
	}

	public double getDispY() {
		return y[y.length - 1] - y[0];
	}

	@Override
	public String toString() {
		String out = "";
		out += "Average x : " + getAverageX() + " | ";
		out += "Average y : " + getAverageY() + "\n";
		return out;
	}
}
