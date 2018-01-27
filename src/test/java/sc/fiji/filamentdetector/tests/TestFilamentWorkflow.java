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
package sc.fiji.filamentdetector.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
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
import sc.fiji.filamentdetector.imagepreprocessor.ImagePreprocessor;
import sc.fiji.filamentdetector.model.Filaments;
import sc.fiji.filamentdetector.model.TrackedFilaments;
import sc.fiji.filamentdetector.tracking.FilamentTrackerService;

public class TestFilamentWorkflow {

	@Test
	public void testFilamentWorkflow() {
		ImageJ ij = new ImageJ();

		Context context = ij.getContext();
		LogService log = ij.log();
		DatasetService ds = ij.dataset();
		FilamentDetectorService detectorService = ij.get(FilamentDetectorService.class);
		FilamentTrackerService tarckerService = ij.get(FilamentTrackerService.class);

		int width = 100;
		int maxFrame = 10;
		int nLines = 5;

		Img img = getFakeImage(width, maxFrame, nLines);
		Dataset dataset = ds.create(img);
		CalibratedAxis[] caxis = { new DefaultLinearAxis(Axes.X), new DefaultLinearAxis(Axes.Y),
				new DefaultLinearAxis(Axes.TIME) };
		dataset.setAxes(caxis);

		ImageDisplay imd = (ImageDisplay) ij.display().createDisplayQuietly(dataset);

		FilamentWorkflow wf = new FilamentWorkflow(context, imd);
		wf.initialize();
		wf.getCalibrations().setChannelToUseIndex(1);

		// Enable some preprocessors
		ImagePreprocessor proc = wf.getImagePreprocessor().getPreProcessorByName("Convert8BitPreprocessor");
		proc.setDoPreprocess(true);

		// Preprocess the image
		wf.getImagePreprocessor().preprocess();

		// Get processed data and convert it to ImageDisplay
		// TODO: that step should be easier
		Dataset datasetp = wf.getImagePreprocessor().getPreprocessedDataset();
		ImageDisplay imdp = (ImageDisplay) ij.display().createDisplayQuietly(datasetp);
		wf.setImageDisplay(imdp);

		wf.setFilamentDetector(detectorService.getRidgeFilamentDetector());
		wf.getFilamentDetector().setImageDisplay(imdp);
		wf.getFilamentDetector().setDataset(datasetp);
		wf.detect();
		Filaments filaments = wf.getFilaments();

		wf.setFilamentsTracker(tarckerService.getBBoxTracker());
		wf.track();
		TrackedFilaments trackedFilaments = wf.getTrackedFilaments();

		log.info(filaments.size());
		log.info(trackedFilaments.size());
		assertEquals(filaments.size(), nLines * maxFrame);
		assertEquals(trackedFilaments.size(), nLines);

	}

	private static <T extends RealType<T>> Img<T> getFakeImage(int width, int maxFrame, int nLines) {

		ImagePlus imp = NewImage.createShortImage("Source", width, width, maxFrame, NewImage.FILL_BLACK);
		ImageProcessor ip;

		for (int i = 0; i < nLines; i++) {
			for (int frame = 0; frame < maxFrame; frame++) {
				int ox1 = (2 * i + 1) * width / nLines / 2;
				int ox2 = (2 * i + 2) * width / nLines / 2;
				int oy1 = (width / nLines) + (frame * 6);
				int oy2 = (2 * width / nLines) + (frame * 6);

				imp.setSlice(frame + 1);
				ip = imp.getProcessor();
				ip.setColor(5000);
				ip.setLineWidth(3);
				ip.drawLine(ox1, oy1, ox2, oy2);
			}
		}
		return (Img<T>) ImageJFunctions.wrap(imp);
	}

}
