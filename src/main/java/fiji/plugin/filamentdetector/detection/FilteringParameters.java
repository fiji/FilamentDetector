package fiji.plugin.filamentdetector.detection;

public class FilteringParameters {

	private double maxLength = 100;
	private double minLength = 0;
	private double maxSinuosity = Double.POSITIVE_INFINITY;
	private double minSinuosity = 1;

	private boolean disableFiltering = false;

	public double getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(double maxLength) {
		this.maxLength = maxLength;
	}

	public double getMinLength() {
		return minLength;
	}

	public void setMinLength(double minLength) {
		this.minLength = minLength;
	}

	public double getMaxSinuosity() {
		return maxSinuosity;
	}

	public void setMaxSinuosity(double maxSinuosity) {
		this.maxSinuosity = maxSinuosity;
	}

	public double getMinSinuosity() {
		return minSinuosity;
	}

	public void setMinSinuosity(double minSinuosity) {
		this.minSinuosity = minSinuosity;
	}

	public boolean isDisableFiltering() {
		return disableFiltering;
	}

	public void setDisableFiltering(boolean disableFiltering) {
		this.disableFiltering = disableFiltering;
	}

	@Override
	public String toString() {
		String out = "";

		out += "maxLength = " + maxLength + "\n";
		out += "minLength = " + minLength + "\n";
		out += "maxSinuosity = " + maxSinuosity + "\n";
		out += "minSinuosity = " + minSinuosity;

		return out;
	}

}
