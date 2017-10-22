package fiji.plugin.filamentdetector.tracking.lap;

import fiji.plugin.filamentdetector.tracking.TrackingParameters;

public class LAPTrackingParameters implements TrackingParameters {

	private double costThreshold = 2;

	public double getCostThreshold() {
		return costThreshold;
	}

	public void setCostThreshold(double costThreshold) {
		this.costThreshold = costThreshold;
	}

	/* (non-Javadoc)
	 * @see fiji.plugin.filamentdetector.tracking.lap.TrackingParameters#toString()
	 */
	@Override
	public String toString() {
		String out = "";

		out += "Cost Threshold = " + costThreshold;

		return out;
	}

}
