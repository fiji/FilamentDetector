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

import org.scijava.Priority;
import org.scijava.convert.ConvertService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import net.imagej.display.ImageDisplayService;
import sc.fiji.filamentdetector.event.ImageNotFoundEvent;
import sc.fiji.filamentdetector.model.Filaments;
import sc.fiji.filamentdetector.overlay.ColorService;

@Plugin(type = FilamentDetector.class, priority = Priority.HIGH)
public class IJ2RidgeDetectionFilamentDetector extends AbstractFilamentDetector {

	private static String NAME = "IJ2 Ridge Detection";

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

	private double lineWidth = 3.5;
	private double higherThreshold = 20;
	private double lowerThreshold = 7;

	private ImagePlus imp;
	private ImagePlus impData;

	public IJ2RidgeDetectionFilamentDetector() {
		this.setName(NAME);
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

		this.impData.setC(channelIndex);

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

		this.impData.setC(channelIndex);
		this.detectFrame(currentFrame);
		this.imp.setC(currentChannel);
		this.simplifyFilaments();
	}

	@Override
	public void detectFrame(int frame) {

		Filaments filaments = this.getFilaments();

		if (filaments == null) {
			filaments = new Filaments();
		}

		this.impData.setT(frame);
		ImageProcessor ip = this.impData.getProcessor();

		this.setFilaments(filaments);
	}

	public double getUpperThreshold() {
		return higherThreshold;
	}

	public void setUpperThresh(double upperThresh) {
		this.higherThreshold = upperThresh;
	}

	public double getLowerThreshold() {
		return lowerThreshold;
	}

	public void setLowerThresh(double lowerThresh) {
		this.lowerThreshold = lowerThresh;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}

	@Override
	public String toString() {
		String out = "";

		out += "Detector : " + getName() + "\n";
		out += "Line Width = " + lineWidth + "\n";
		out += "Lower Threshold = " + lowerThreshold + "\n";
		out += "Upper Threshold = " + higherThreshold + "\n";

		out += "Detect Only Current Frame = " + isDetectOnlyCurrentFrame() + "\n";
		out += "Simplify Filaments = " + isSimplifyFilaments() + "\n";
		out += "Simplify Tolerance Distance = " + getSimplifyToleranceDistance();

		return out;
	}

}
