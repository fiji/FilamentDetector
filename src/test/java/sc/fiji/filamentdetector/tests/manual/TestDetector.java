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
package sc.fiji.filamentdetector.tests.manual;

import io.scif.services.DatasetIOService;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

import org.scijava.Context;
import org.scijava.log.LogService;

import sc.fiji.filamentdetector.FilamentWorkflow;
import sc.fiji.filamentdetector.detection.FilamentDetectorService;
import sc.fiji.filamentdetector.detection.IJ2RidgeDetectionFilamentDetector;
import sc.fiji.filamentdetector.model.Filaments;
import sc.fiji.filamentdetector.overlay.FilamentOverlayService;
import sc.fiji.filamentdetector.overlay.ImageDisplayMode;
import sc.fiji.filamentdetector.tracking.FilamentTrackerService;

public class TestDetector {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		Context context = ij.getContext();

		LogService log = ij.log();
		DatasetIOService dsio = ij.get(DatasetIOService.class);

		FilamentOverlayService overlay = ij.get(FilamentOverlayService.class);
		FilamentDetectorService detectorService = ij.get(FilamentDetectorService.class);
		FilamentTrackerService tarckerService = ij.get(FilamentTrackerService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/fake-only-T.tif";
		// fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/fake-flat.tif";
		fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/fake-only-C.tif";
		Dataset dataset = dsio.open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		FilamentWorkflow wf = new FilamentWorkflow(context, imd);
		wf.initialize();
		wf.getCalibrations().setChannelToUseIndex(1);

		overlay.initialize();
		overlay.setImageDisplay(imd);
		overlay.setViewMode(ImageDisplayMode.COMPOSITE);

		IJ2RidgeDetectionFilamentDetector detector = detectorService.getIJ2RidgeFilamentDetector();
		wf.setFilamentDetector(detector);
		detector.setImageDisplay(imd);
		detector.setDataset(dataset);
		detector.setLineWidth(4);
		detector.setLowerThreshold(7);
		detector.setUpperThreshold(20);

		wf.detect();
		Filaments filaments = wf.getFilaments();

		overlay.add(filaments);

		log.info("Done");
	}

}
