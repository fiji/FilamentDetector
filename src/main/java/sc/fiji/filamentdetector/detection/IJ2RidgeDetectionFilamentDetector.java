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
import java.util.ArrayList;
import java.util.List;

import org.scijava.Priority;
import org.scijava.convert.ConvertService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.ops.OpService;
import net.imagej.ops.segment.detectRidges.RidgeDetection;
import net.imglib2.roi.geom.real.DefaultPolyline;
import net.imglib2.type.numeric.RealType;
import sc.fiji.filamentdetector.model.Filament;
import sc.fiji.filamentdetector.model.FilamentFactory;
import sc.fiji.filamentdetector.model.Filaments;
import sc.fiji.filamentdetector.overlay.ColorService;

@Plugin(type = FilamentDetector.class, priority = Priority.HIGH)
public class IJ2RidgeDetectionFilamentDetector extends AbstractFilamentDetector {

	private static String NAME = "IJ2 Ridge Detection";

	@Parameter
	private ConvertService convertService;

	@Parameter
	private LogService log;

	@Parameter
	private OpService op;

	@Parameter
	private ColorService colorService;

	@Parameter
	private EventService eventService;

	@Parameter
	private ImageDisplayService imds;

	private double lineWidth = 3.5;
	private double higherThreshold = 20;
	private double lowerThreshold = 7;

	public IJ2RidgeDetectionFilamentDetector() {
		this.setName(NAME);
	}

	private void initDetection() {
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

		int frame = 1;
		this.detectFrame(frame);

		this.simplifyFilaments();
	}

	@Override
	public void detectCurrentFrame() {
		detectCurrentFrame(0);
	}

	@Override
	public void detectCurrentFrame(int channelIndex) {

		this.initDetection();
		int currentFrame = 0;
		this.detectFrame(currentFrame);
		this.simplifyFilaments();
	}

	@Override
	public void detectFrame(int frame) {

		Filaments filaments = this.getFilaments();

		if (filaments == null) {
			filaments = new Filaments();
		}

		ImageDisplay imd = getImageDisplay();
		Dataset dataset = getDataset();
		ImgPlus<? extends RealType<?>> img = dataset.getImgPlus();

		List<DefaultPolyline> lines = new ArrayList<>();
		lines = (List<DefaultPolyline>) op.run(RidgeDetection.class, img, lineWidth, lowerThreshold, higherThreshold,
				lineWidth);

		for (DefaultPolyline line : lines) {
			Filament filament = FilamentFactory.fromPolyline(line, frame);

			Color color = colorService.getColor(filaments.size() + 1);
			filament.setColor(color);

			filaments.add(filament);
		}

		this.setFilaments(filaments);
	}

	public double getUpperThreshold() {
		return higherThreshold;
	}

	public void setUpperThreshold(double upperThresh) {
		this.higherThreshold = upperThresh;
	}

	public double getLowerThreshold() {
		return lowerThreshold;
	}

	public void setLowerThreshold(double lowerThresh) {
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
