package fiji.plugin.filamentdetector.detection;

import de.biomedical_imaging.ij.steger.OverlapOption;

public class DetectionParameters {

	private double sigma = 1.51;
	private double upperThresh = 7.99;
	private double lowerThresh = 3.06;

	private double lineWidth = 3.5;
	private double highContrast = 230;
	private double lowContrast = 87;

	private double minLength = 0;
	private double maxLength = 0;
	private boolean isDarkLine = false;
	private boolean doCorrectPosition = true;
	private boolean doEstimateWidth = true;
	private boolean doExtendLine = true;
	private boolean simplifyFilaments = true;
	private double simplifyToleranceDistance = 5;

	private OverlapOption overlapOption = OverlapOption.NONE;

	private boolean detectOnlyCurrentFrame = false;

	public double getSigma() {
		return sigma;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}

	public double getUpperThresh() {
		return upperThresh;
	}

	public void setUpperThresh(double upperThresh) {
		this.upperThresh = upperThresh;
	}

	public double getLowerThresh() {
		return lowerThresh;
	}

	public void setLowerThresh(double lowerThresh) {
		this.lowerThresh = lowerThresh;
	}

	public double getMinLength() {
		return minLength;
	}

	public void setMinLength(double minLength) {
		this.minLength = minLength;
	}

	public double getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(double maxLength) {
		this.maxLength = maxLength;
	}

	public boolean isDarkLine() {
		return isDarkLine;
	}

	public void setDarkLine(boolean isDarkLine) {
		this.isDarkLine = isDarkLine;
	}

	public boolean isDoCorrectPosition() {
		return doCorrectPosition;
	}

	public void setDoCorrectPosition(boolean doCorrectPosition) {
		this.doCorrectPosition = doCorrectPosition;
	}

	public boolean isDoEstimateWidth() {
		return doEstimateWidth;
	}

	public void setDoEstimateWidth(boolean doEstimateWidth) {
		this.doEstimateWidth = doEstimateWidth;
	}

	public boolean isDoExtendLine() {
		return doExtendLine;
	}

	public void setDoExtendLine(boolean doExtendLine) {
		this.doExtendLine = doExtendLine;
	}

	public OverlapOption getOverlapOption() {
		return overlapOption;
	}

	public void setOverlapOption(OverlapOption overlapOption) {
		this.overlapOption = overlapOption;
	}

	public boolean isDetectOnlyCurrentFrame() {
		return detectOnlyCurrentFrame;
	}

	public void setDetectOnlyCurrentFrame(boolean detectOnlyCurrentFrame) {
		this.detectOnlyCurrentFrame = detectOnlyCurrentFrame;
	}

	public boolean isSimplifyFilaments() {
		return simplifyFilaments;
	}

	public void setSimplifyFilaments(boolean simplifyFilaments) {
		this.simplifyFilaments = simplifyFilaments;
	}

	public double getSimplifyToleranceDistance() {
		return simplifyToleranceDistance;
	}

	public void setSimplifyToleranceDistance(double simplifyToleranceDistance) {
		this.simplifyToleranceDistance = simplifyToleranceDistance;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
		computerParameters();
	}

	public double getHighContrast() {
		return highContrast;
	}

	public void setHighContrast(double highContrast) {
		this.highContrast = highContrast;
		computerParameters();
	}

	public double getLowContrast() {
		return lowContrast;
	}

	public void setLowContrast(double lowContrast) {
		this.lowContrast = lowContrast;
		computerParameters();
	}

	private void computerParameters() {
		// Compute sigma
		this.sigma = (this.lineWidth / (2 * Math.sqrt(3))) + 0.5;

		// Compute upper threshold
		double firstTerm = 0.17;
		double secondTerm = (-2 * this.highContrast * (lineWidth / 2.0)
				/ (Math.sqrt(2 * Math.PI) * Math.pow(this.sigma, 3)));
		secondTerm = Math.abs(secondTerm);
		double thirdTerm = Math.exp(-(Math.pow(this.lineWidth / 2.0, 2)) / (2 * Math.pow(this.sigma, 2)));
		this.upperThresh = Math.floor(firstTerm * secondTerm) * thirdTerm;

		// Compute lower threshold
		firstTerm = 0.17;
		secondTerm = (-2 * this.lowContrast * (lineWidth / 2.0)
				/ (Math.sqrt(2 * Math.PI) * Math.pow(this.sigma, 3)));
		secondTerm = Math.abs(secondTerm);
		thirdTerm = Math.exp(-(Math.pow(this.lineWidth / 2.0, 2)) / (2 * Math.pow(this.sigma, 2)));
		this.lowerThresh = Math.floor(firstTerm * secondTerm) * thirdTerm;
	}

	@Override
	public String toString() {
		String out = "";

		out += "Sigma = " + sigma + "\n";
		out += "Lower Threshold = " + lowerThresh + "\n";
		out += "Upper Threshold = " + upperThresh + "\n";

		out += "Line Width = " + lineWidth + "\n";
		out += "High Contrast = " + highContrast + "\n";
		out += "Low Contrast = " + lowContrast + "\n";

		out += "Detect Only Current Frame = " + detectOnlyCurrentFrame + "\n";
		out += "Simplify Filaments = " + simplifyFilaments + "\n";
		out += "Simplify Tolerance Distance = " + simplifyToleranceDistance;

		return out;
	}

}
