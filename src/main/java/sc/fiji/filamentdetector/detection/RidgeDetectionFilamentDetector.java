/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2017 Hadrien Mary
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package sc.fiji.filamentdetector.detection;

import java.awt.Color;

import org.scijava.Priority;
import org.scijava.convert.ConvertService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import de.biomedical_imaging.ij.steger.OverlapOption;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import net.imagej.display.ImageDisplayService;
import sc.fiji.filamentdetector.event.ImageNotFoundEvent;
import sc.fiji.filamentdetector.model.Filament;
import sc.fiji.filamentdetector.model.FilamentFactory;
import sc.fiji.filamentdetector.model.Filaments;
import sc.fiji.filamentdetector.overlay.ColorService;

@Plugin(type = FilamentDetector.class, priority = Priority.HIGH)
public class RidgeDetectionFilamentDetector extends AbstractFilamentDetector {

	private static String NAME = "Ridge Detection";

	@Parameter
	ConvertService convertService;

	@Parameter
	LogService log;

	@Parameter
	private ColorService colorService;

	@Parameter
	private EventService eventService;

	@Parameter
	private ImageDisplayService imds;

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

	public RidgeDetectionFilamentDetector() {
		this.setName(NAME);
		this.lineDetector = new LineDetector();
	}

	private void initDetection() {
		// Convert Dataset to IJ1 ImagePlus and ImageProcessor
		try {
			if (this.imp == null) {
				this.imp = convertService.convert(getImageDisplay(), ImagePlus.class);
			}

			if (this.impData == null) {
				this.impData = convertService.convert(getDataset(), ImagePlus.class);
			}
		} catch (NullPointerException e) {
			eventService.publish(new ImageNotFoundEvent());
		}

		colorService.initialize();
		setFilaments(new Filaments());
	}

	@Override
	public void detect() {
		detect(0);
	}

	@Override
	public void detect(int channelIndex) {

		this.initDetection();
		int currentFrame = this.imp.getFrame();
		int currentChannel = this.imp.getChannel();

		this.impData.setC(channelIndex + 1);

		for (int frame = 1; frame < this.impData.getNFrames() + 1; frame++) {
			this.detectFrame(frame);
		}
		this.imp.setT(currentFrame);
		this.imp.setC(currentChannel);
		this.simplifyFilaments();
	}

	@Override
	public void detectCurrentFrame() {
		detectCurrentFrame(0);
	}

	@Override
	public void detectCurrentFrame(int channelIndex) {

		this.initDetection();
		int currentFrame = this.imp.getFrame();
		int currentChannel = this.imp.getChannel();

		this.impData.setC(channelIndex + 1);
		this.detectFrame(currentFrame);
		this.imp.setC(currentChannel);
		this.simplifyFilaments();
	}

	@Override
	public void detectFrame(int frame, int channel) {
		this.detectFrame(frame);
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
