package fiji.plugin.filamentdetector.tracking;

public class TrackingParameters {

	private double costThreshold = 1.1;
	private double maxFrameGap = 15;
	private boolean interpolateFilaments = true;

	public double getCostThreshold() {
		return costThreshold;
	}

	public void setCostThreshold(double costThreshold) {
		this.costThreshold = costThreshold;
	}

	public double getMaxFrameGap() {
		return maxFrameGap;
	}

	public void setMaxFrameGap(double maxFrameGap) {
		this.maxFrameGap = maxFrameGap;
	}

	public boolean isInterpolateFilaments() {
		return interpolateFilaments;
	}

	public void setInterpolateFilaments(boolean interpolateFilaments) {
		this.interpolateFilaments = interpolateFilaments;
	}

	@Override
	public String toString() {
		String out = "";
		out += "Cost Threshold = " + costThreshold + "\n";
		out += "Maximum Frame Gap = " + maxFrameGap + "\n";
		out += "Interpolate filaments = " + interpolateFilaments + "\n";
		return out;
	}

}
