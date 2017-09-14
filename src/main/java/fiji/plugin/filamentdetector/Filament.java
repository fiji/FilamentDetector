package fiji.plugin.filamentdetector;

import java.util.Arrays;
import java.util.stream.IntStream;

import de.biomedical_imaging.ij.steger.Line;

/* A container for Line */
public class Filament {

	private final Line line;
	private final int frame;

	private double length = Double.NaN;
	private double[] lengths;
	private double sinuosity = Double.NaN;

	public Filament(Line line, int frame) {
		this.line = line;
		this.frame = frame;
	}

	public Line getLine() {
		return line;
	}

	public float[] getXCoordinates() {
		return this.line.getXCoordinates();
	}

	public float[] getYCoordinates() {
		return this.line.getYCoordinates();
	}

	public double[] getXCoordinatesAsDouble() {
		return IntStream.range(0, this.getXCoordinates().length).mapToDouble(i -> this.getXCoordinates()[i]).toArray();
	}

	public double[] getYCoordinatesAsDouble() {
		return IntStream.range(0, this.getYCoordinates().length).mapToDouble(i -> this.getYCoordinates()[i]).toArray();
	}

	public int getNumber() {
		return this.line.getNumber();
	}

	public int getFrame() {
		return this.frame;
	}

	public int getID() {
		return this.line.getID();
	}

	public int getSize() {
		return this.getXCoordinates().length;
	}

	public double getLength() {
		if (Double.isNaN(this.length)) {

			float[] x = this.getXCoordinates();
			float[] y = this.getYCoordinates();

			this.lengths = new double[x.length - 1];
			this.length = 0;

			for (int i = 0; i < x.length - 1; i++) {
				this.lengths[i] = Math.sqrt(Math.pow(x[i] - x[i + 1], 2) + Math.pow(y[i] - y[i + 1], 2));
				this.length += this.lengths[i];
			}

		}
		return this.length;
	}

	public double[] getLengths() {
		this.getLength();
		return this.lengths;
	}

	public double getSinuosity() {
		if (Double.isNaN(this.sinuosity)) {

			float[] x = this.getXCoordinates();
			float[] y = this.getYCoordinates();

			double shortLength = Math.sqrt(Math.pow(x[0] - x[x.length - 1], 2) + Math.pow(y[0] - y[x.length - 1], 2));

			this.sinuosity = this.getLength() / shortLength;

		}
		return this.sinuosity;
	}

	public String toString() {
		return "Frame: " + this.getFrame() + " | ID: " + this.getID();
	}

	public String info() {
		String info = "";

		info += this.toString() + "\n";
		info += "N Points: " + this.getSize() + "\n";
		info += "Length: " + String.format("%.2f", this.getLength()) + "\n";
		info += "Sinuosity: " + String.format("%.2f", this.getSinuosity()) + "\n";

		return info;
	}

	/* Simplify the filament by reducing the number of points */
	public Filament simplify(double toleranceDistance) {
		double[] x = this.getXCoordinatesAsDouble();
		double[] y = this.getYCoordinatesAsDouble();

		float[] newX = new float[x.length];
		float[] newY = new float[y.length];

		newX[0] = (float) x[0];
		newY[0] = (float) y[0];

		double totDist = 0;
		double dist;
		int newSize = 1;

		for (int i = 1; i < x.length; i++) {
			dist = Math.sqrt(Math.pow(x[i] - x[i - 1], 2) + Math.pow(y[i] - y[i - 1], 2));

			if (totDist > toleranceDistance) {
				newX[newSize] = (float) x[i];
				newY[newSize] = (float) y[i];
				totDist = 0;
				newSize++;
			} else {
				totDist += dist;
			}
		}

		newX = Arrays.copyOf(newX, newSize);
		newY = Arrays.copyOf(newY, newSize);

		Line line = new Line(newX, newY);
		return new Filament(line, this.getFrame());
	}

}
