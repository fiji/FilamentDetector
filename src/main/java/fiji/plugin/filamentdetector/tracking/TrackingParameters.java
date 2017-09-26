package fiji.plugin.filamentdetector.tracking;

public class TrackingParameters {

	private double costThreshold = 0.5;

	public double getCostThreshold() {
		return costThreshold;
	}

	public void setCostThreshold(double costThreshold) {
		this.costThreshold = costThreshold;
	}

	@Override
	public String toString() {
		String out = "";

		out += "Cost Threshold = " + costThreshold;

		return out;
	}

}
