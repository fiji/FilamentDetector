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

import java.awt.Color;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;

import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;
import net.imglib2.RealPoint;
import sc.fiji.filamentdetector.GeometryUtils;
import sc.fiji.filamentdetector.model.Filament;
import sc.fiji.filamentdetector.overlay.FilamentOverlayService;

public class TestNucleationDetector {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		FilamentOverlayService overlay = ij.get(FilamentOverlayService.class);
		LogService log = ij.get(LogService.class);
		ConvertService convert = ij.get(ConvertService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/7,5uM_emccd_lapse1-small-8bit.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();
		ImagePlus imp = convert.convert(imd, ImagePlus.class);

		// Parameters
		int pixelSpacing = 1;
		int threshold = 100;
		int maxFrame = 15;
		int thickness = 2;

		int frame = 13;
		int channel = 0;
		int distance = 10;

		// Setup visualization
		overlay.setImageDisplay(imd);
		overlay.setFilamentWidth(0);
		imp.setT(frame);

		// Create filament
		RealPoint start = new RealPoint(23, 38);
		RealPoint end = new RealPoint(28, 47);
		// RealPoint start = new RealPoint(52, 26);
		// RealPoint end = new RealPoint(64, 43);
		List<RealPoint> seedLine = GeometryUtils.getLinePointsFromSpacing(start, end, pixelSpacing);
		Filament seed = new Filament(seedLine, frame);
		seed.setColor(Color.RED);

		double seedLength = GeometryUtils.distance(start, end);

		RealPoint p1 = GeometryUtils.getPointOnVectorFromDistance(start, end, seedLength + distance);
		RealPoint p2 = GeometryUtils.getPointOnVectorFromDistance(end, start, seedLength + distance);

		List<RealPoint> line1 = GeometryUtils.getLinePointsFromSpacing(end, p1, pixelSpacing);
		List<RealPoint> line2 = GeometryUtils.getLinePointsFromSpacing(start, p2, pixelSpacing);

		overlay.add(new Filament(line1, frame));
		overlay.add(new Filament(line2, frame));

		INDArray intensities = GeometryUtils.getIntensities(line1, dataset, frame, channel, 0, thickness, pixelSpacing);
		log.info(intensities);
	}
}
