package fiji.plugin.filamentdetector;

import net.imagej.Dataset;
import net.imagej.axis.Axes;

/* An easy way to get Dataset calibrations */
public class Calibrations {

	private Dataset data;

	private double dx;
	private double dy;
	private double dz;
	private double dt;

	public Calibrations(Dataset data) {

		this.data = data;

		// Get dimensions indexes
		int xIndex = this.data.dimensionIndex(Axes.X);
		int yIndex = this.data.dimensionIndex(Axes.Y);
		int zIndex = this.data.dimensionIndex(Axes.Z);
		int timeIndex = this.data.dimensionIndex(Axes.TIME);
		int channelIndex = this.data.dimensionIndex(Axes.X);

		// Set image calibrations of dimensions
		this.dx = xIndex != -1 ? this.data.axis(xIndex).calibratedValue(1) : 1;
		this.dy = yIndex != -1 ? this.data.axis(yIndex).calibratedValue(1) : 1;
		this.dz = zIndex != -1 ? this.data.axis(zIndex).calibratedValue(1) : 1;
		this.dt = timeIndex != -1 ? this.data.axis(timeIndex).calibratedValue(1) : 1;
	}

	public Dataset getData() {
		return data;
	}

	public double getDx() {
		return dx;
	}

	public void setDx(double dx) {
		this.dx = dx;
	}

	public void setDy(double dy) {
		this.dy = dy;
	}

	public void setDz(double dz) {
		this.dz = dz;
	}

	public void setDt(double dt) {
		this.dt = dt;
	}

	public double getDy() {
		return dy;
	}

	public double getDz() {
		return dz;
	}

	public double getDt() {
		return dt;
	}

}
