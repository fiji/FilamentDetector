package fiji.plugin.filamentdetector.detection;

public class FilteringParameters {

	private double maxLength = 100;
	private double minLength = 0;
	private double maxSinuosity = 2;
	private double minSinuosity = 1;

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

	public String toString() {
		String out = "";

		out += "maxLength = " + maxLength + "\n";
		out += "minLength = " + minLength + "\n";
		out += "maxSinuosity = " + maxSinuosity + "\n";
		out += "minSinuosity = " + minSinuosity;

		return out;
	}

}
