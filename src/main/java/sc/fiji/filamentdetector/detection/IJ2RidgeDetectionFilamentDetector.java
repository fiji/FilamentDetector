/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2020 Fiji developers.
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
import org.scijava.ui.UIService;

import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.display.ImageDisplayService;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops.Segment.DetectJunctions;
import net.imagej.ops.Ops.Segment.DetectRidges;
import net.imglib2.RealPoint;
import net.imglib2.roi.geom.real.Polyline;
import net.imglib2.type.numeric.RealType;
import sc.fiji.filamentdetector.ImageUtilService;
import sc.fiji.filamentdetector.event.ImageNotFoundEvent;
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
	private UIService ui;

	@Parameter
	private ImageDisplayService imds;

	@Parameter
	private ImageUtilService ijUtil;

	private double lineWidth = 3.5;
	private double higherThreshold = 20;
	private double lowerThreshold = 7;
	private boolean detectJunctions = false;

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

		long numT = getDataset().dimension(Axes.TIME);
		for (int t = 0; t < numT; t++) {
			this.detectFrame(t, channelIndex);
		}

		this.simplifyFilaments();
	}

	@Override
	public void detectCurrentFrame() {
		detectCurrentFrame(0);
	}

	@Override
	public void detectCurrentFrame(int channelIndex) {
		this.initDetection();

		// The following does not work
		// int currentFrame = getImageDisplay().getIntPosition(Axes.TIME);

		// Need to use IJ1 to get the current frame
		ImagePlus imp = null;
		try {
			imp = convertService.convert(getImageDisplay(), ImagePlus.class);

			int currentFrame = imp.getFrame() - 1;

			this.detectFrame(currentFrame, channelIndex);
			this.simplifyFilaments();

		} catch (NullPointerException e) {
			eventService.publish(new ImageNotFoundEvent());
		}

	}

	@Override
	public void detectFrame(int frame) {
		this.detectFrame(frame, 0);
	}

	@Override
	public void detectFrame(int frame, int channel) {
		Filaments filaments = this.getFilaments();

		if (filaments == null) {
			filaments = new Filaments();
		}

		Dataset dataset = getDataset();
		ImgPlus<? extends RealType<?>> img = dataset.getImgPlus();

		ImgPlus<? extends RealType<?>> slice = ijUtil.cropAlongAxis(img, Axes.TIME, frame);
		slice = ijUtil.cropAlongAxis(slice, Axes.CHANNEL, channel);

		List<Polyline> lines = new ArrayList<>();
		lines = (List<Polyline>) op.run(DetectRidges.class, slice.getImg(), lineWidth, lowerThreshold, higherThreshold,
				(int) lineWidth);

		if (this.detectJunctions) {
			// TODO: create new lines from the detected junctions.
			List<RealPoint> junctions = (List<RealPoint>) op.run(DetectJunctions.class, lines);
			for (RealPoint p : junctions) {
				log.info(p);
			}
		}

		for (Polyline line : lines) {
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

	public boolean isDetectJunctions() {
		return detectJunctions;
	}

	public void setDetectJunctions(boolean detectJunctions) {
		this.detectJunctions = detectJunctions;
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
