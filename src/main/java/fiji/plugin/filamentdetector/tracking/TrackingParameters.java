package fiji.plugin.filamentdetector.tracking;

public class TrackingParameters {

	private double costThreshold = 1.1;
	private double maxFrameGap = 15;

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

	@Override
	public String toString() {
		String out = "";
		out += "Cost Threshold = " + costThreshold;
		out += "Maximum Frame Gap = " + maxFrameGap;
		return out;
	}

}
