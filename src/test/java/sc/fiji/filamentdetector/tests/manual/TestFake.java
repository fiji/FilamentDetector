/*-
* #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2025 Fiji developers.
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
package sc.fiji.filamentdetector.tests.manual;

import org.scijava.Context;
import org.scijava.log.LogService;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageProcessor;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imagej.display.ImageDisplay;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import sc.fiji.filamentdetector.FilamentWorkflow;
import sc.fiji.filamentdetector.detection.FilamentDetectorService;
import sc.fiji.filamentdetector.detection.RidgeDetectionFilamentDetector;
import sc.fiji.filamentdetector.model.Filaments;
import sc.fiji.filamentdetector.model.TrackedFilaments;
import sc.fiji.filamentdetector.overlay.FilamentOverlayService;
import sc.fiji.filamentdetector.overlay.ImageDisplayMode;
import sc.fiji.filamentdetector.tracking.BBoxLAPFilamentsTracker;
import sc.fiji.filamentdetector.tracking.FilamentTrackerService;

public class TestFake {

	public static void main(final String... args) throws Exception {
		ImageJ ij = new ImageJ();
		ij.ui().showUI();

		Context context = ij.getContext();
		LogService log = ij.log();
		DatasetService ds = ij.dataset();
		FilamentOverlayService overlay = ij.get(FilamentOverlayService.class);
		FilamentDetectorService detectorService = ij.get(FilamentDetectorService.class);
		FilamentTrackerService tarckerService = ij.get(FilamentTrackerService.class);

		Img img = getFakeImage();
		Dataset dataset = ds.create(img);
		CalibratedAxis[] caxis = { new DefaultLinearAxis(Axes.X), new DefaultLinearAxis(Axes.Y),
				new DefaultLinearAxis(Axes.TIME) };
		dataset.setAxes(caxis);

		ij.ui().show(dataset);
		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		FilamentWorkflow wf = new FilamentWorkflow(context, imd);
		wf.initialize();
		wf.getCalibrations().setChannelToUseIndex(1);

		// Get processed data and convert it to ImageDisplay
		// TODO: that step should be easier
		ij.ui().show(dataset);
		ImageDisplay imdp = ij.imageDisplay().getImageDisplays().stream()
				.filter(i -> ((Dataset) i.getActiveView().getData()).equals(dataset)).findFirst().orElse(null);
		wf.setImageDisplay(imdp);

		overlay.initialize();
		overlay.setImageDisplay(imdp);
		overlay.setViewMode(ImageDisplayMode.COMPOSITE);

		RidgeDetectionFilamentDetector detector = detectorService.getRidgeFilamentDetector();
		wf.setFilamentDetector(detector);
		detector.setImageDisplay(imdp);
		detector.setDataset(dataset);
		detector.setLineWidth(2.5);

		wf.detect();
		Filaments filaments = wf.getFilaments();

		overlay.add(filaments);

		BBoxLAPFilamentsTracker tracker = tarckerService.getBBoxTracker();
		wf.setFilamentsTracker(tracker);
		wf.track();
		TrackedFilaments trackedFilaments = wf.getTrackedFilaments();

		log.info(trackedFilaments.size());

		overlay.reset();
		overlay.setDrawPlusTips(true);
		overlay.add(trackedFilaments);

		log.info("Done");
	}

	private static <T extends RealType<T>> Img<T> getFakeImage() {

		int width = 200;
		int maxFrame = 20;
		ImagePlus imp = NewImage.createShortImage("Source", width, width, maxFrame, NewImage.FILL_BLACK);
		ImageProcessor ip;

		int nLines = 10;
		for (int frame = 0; frame < maxFrame; frame++) {
			for (int i = 0; i < nLines - 1; i++) {

				int ox1 = (2 * i + 1) * width / nLines / 2;
				int ox2 = (2 * i + 2) * width / nLines / 2;
				int oy1 = (width / nLines) + (frame * 6);
				int oy2 = (2 * width / nLines) + (frame * 6);

				imp.setSlice(frame + 1);
				ip = imp.getProcessor();
				ip.setColor(5000);
				ip.setLineWidth(3);
				ip.drawLine(ox1, oy1, ox2, oy2);
				ip.noise(500);
			}
		}
		return (Img<T>) ImageJFunctions.wrap(imp);
	}
}
