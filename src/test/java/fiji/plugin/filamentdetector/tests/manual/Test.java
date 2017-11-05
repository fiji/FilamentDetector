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
package fiji.plugin.filamentdetector.tests.manual;

import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.FilamentWorkflow;
import fiji.plugin.filamentdetector.detection.FilamentDetectorService;
import fiji.plugin.filamentdetector.detection.RidgeDetectionFilamentDetector;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import fiji.plugin.filamentdetector.overlay.ImageDisplayMode;
import fiji.plugin.filamentdetector.tracking.BBoxLAPFilamentsTracker;
import fiji.plugin.filamentdetector.tracking.FilamentTrackerService;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

public class Test {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		LogService log = ij.log();
		FilamentOverlayService overlay = ij.get(FilamentOverlayService.class);
		FilamentDetectorService detectorService = ij.get(FilamentDetectorService.class);
		FilamentTrackerService tarckerService = ij.get(FilamentTrackerService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/fake1.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		FilamentWorkflow wf = new FilamentWorkflow(context, imd);
		wf.initialize();
		wf.getCalibrations().setChannelToUseIndex(2);

		// Enable some preprocessors
		// ImagePreprocessor proc = wf.getImagePreprocessor()
		// .getPreProcessorByName("PseudoFlatFieldCorrectionPreprocessor");
		// proc.setDoPreprocess(true);
		// proc =
		// wf.getImagePreprocessor().getPreProcessorByName("DOGFilterPreprocessor");
		// proc.setDoPreprocess(true);
		//
		// // Preprocess the image
		// wf.getImagePreprocessor().preprocess();

		// Get processed data and convert it to ImageDisplay
		// TODO: that step should be easier
		Dataset datasetp = wf.getImagePreprocessor().getPreprocessedDataset();
		ij.ui().show(datasetp);
		ImageDisplay imdp = ij.imageDisplay().getImageDisplays().stream()
				.filter(i -> ((Dataset) i.getActiveView().getData()).equals(datasetp)).findFirst().orElse(null);
		imdp = imd;
		wf.setImageDisplay(imdp);

		overlay.initialize();
		overlay.setImageDisplay(imdp);
		overlay.setViewMode(ImageDisplayMode.COMPOSITE);

		RidgeDetectionFilamentDetector detector = detectorService.getRidgeFilamentDetector();
		wf.setFilamentDetector(detector);
		detector.setImageDisplay(imdp);
		detector.setDataset(datasetp);
		detector.setLineWidth(2.5);

		wf.detect();
		Filaments filaments = wf.getFilaments();

		overlay.add(filaments);

		BBoxLAPFilamentsTracker tracker = tarckerService.getBBoxTracker();
		wf.setFilamentsTracker(tracker);
		wf.track();
		TrackedFilaments trackedFilaments = wf.getTrackedFilaments();

		overlay.reset();
		overlay.setDrawPlusTips(true);
		overlay.add(trackedFilaments);

		log.info("Done");
	}

}
