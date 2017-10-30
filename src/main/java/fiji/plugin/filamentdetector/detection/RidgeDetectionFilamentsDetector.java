package fiji.plugin.filamentdetector.detection;

import java.awt.Color;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import de.biomedical_imaging.ij.steger.OverlapOption;
import fiji.plugin.filamentdetector.event.ImageNotFoundEvent;
import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.FilamentFactory;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.overlay.ColorService;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class RidgeDetectionFilamentsDetector extends AbstractFilamentDetector {

	private static String NAME = "Ridge Detection";

	@Parameter
	ConvertService convertService;

	@Parameter
	LogService log;

	@Parameter
	private ColorService colorService;

	@Parameter
	private EventService eventService;

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

	private OverlapOption overlapOption = OverlapOption.NONE;

	private LineDetector lineDetector;

	private ImagePlus imp;
	private ImagePlus impData;

	public RidgeDetectionFilamentsDetector(Context context) {
		context.inject(this);
		this.setName(NAME);
		this.lineDetector = new LineDetector();
	}

	@Override
	public void detect() {
		detect(0);
	}

	@Override
	public void detect(int channelIndex) {

		// Convert Dataset to IJ1 ImagePlus and ImageProcessor
		try {
			this.imp = convertService.convert(getImageDisplay(), ImagePlus.class);
			this.impData = convertService.convert(getDataset(), ImagePlus.class);
		} catch (NullPointerException e) {
			eventService.publish(new ImageNotFoundEvent());
		}

		colorService.initialize();
		setFilaments(new Filaments());
		int currentFrame = this.imp.getFrame();
		int currentChannel = this.imp.getChannel();

		this.impData.setC(channelIndex);

		for (int frame = 1; frame < this.impData.getNFrames() + 1; frame++) {
			this.detectFrame(frame);
		}
		this.imp.setT(currentFrame);
		this.imp.setC(currentChannel);

		this.simplify();
	}

	@Override
	public void detectCurrentFrame() {
		detectCurrentFrame(0);
	}

	@Override
	public void detectCurrentFrame(int channelIndex) {
		colorService.initialize();

		setFilaments(new Filaments());

		int currentFrame = this.imp.getFrame();
		int currentChannel = this.imp.getChannel();

		this.impData.setC(channelIndex);
		this.detectFrame(currentFrame);

		this.imp.setC(currentChannel);

		this.simplify();
	}

	@Override
	public void detectFrame(int frame) {

		Filaments filaments = this.getFilaments();

		if (filaments == null) {
			filaments = new Filaments();
		}

		this.impData.setT(frame);
		ImageProcessor ip = this.impData.getProcessor();

		// Detect lines
		Lines lines = this.lineDetector.detectLines(ip, this.getSigma(), this.getUpperThresh(), this.getLowerThresh(),
				this.getMinLength(), this.getMaxLength(), this.isDarkLine(), this.isDoCorrectPosition(),
				this.isDoEstimateWidth(), this.isDoExtendLine(), this.getOverlapOption());

		for (Line line : lines) {
			Filament filament = FilamentFactory.fromLine(line, frame);

			Color color = colorService.getColor(filaments.size() + 1);
			filament.setColor(color);

			filaments.add(filament);
		}

		this.setFilaments(filaments);

	}

	private void simplify() {
		if (this.isSimplifyFilaments()) {
			Filaments filaments = this.getFilaments();
			filaments = filaments.simplify(this.getSimplifyToleranceDistance());

			// Remove filaments with only one point
			filaments = filaments.stream().filter(filament -> filament.getSize() > 1)
					.collect(Collectors.toCollection(Filaments::new));

			this.setFilaments(filaments);
		}
	}

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
		secondTerm = (-2 * this.lowContrast * (lineWidth / 2.0) / (Math.sqrt(2 * Math.PI) * Math.pow(this.sigma, 3)));
		secondTerm = Math.abs(secondTerm);
		thirdTerm = Math.exp(-(Math.pow(this.lineWidth / 2.0, 2)) / (2 * Math.pow(this.sigma, 2)));
		this.lowerThresh = Math.floor(firstTerm * secondTerm) * thirdTerm;
	}

	@Override
	public String toString() {
		String out = "";

		out += "Detector : " + getName() + "\n";
		out += "Sigma = " + sigma + "\n";
		out += "Lower Threshold = " + lowerThresh + "\n";
		out += "Upper Threshold = " + upperThresh + "\n";

		out += "Line Width = " + lineWidth + "\n";
		out += "High Contrast = " + highContrast + "\n";
		out += "Low Contrast = " + lowContrast + "\n";

		out += "Detect Only Current Frame = " + isDetectOnlyCurrentFrame() + "\n";
		out += "Simplify Filaments = " + isSimplifyFilaments() + "\n";
		out += "Simplify Tolerance Distance = " + getSimplifyToleranceDistance();

		return out;
	}

}
