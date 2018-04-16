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

import java.util.Arrays;
import java.util.List;

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

public class TestIntensityProfile2 {

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

		// RealPoint start = new RealPoint(122, 80);
		// RealPoint end = new RealPoint(154, 67);

		RealPoint start = new RealPoint(158, 7);
		RealPoint end = new RealPoint(193, 29);

		List<RealPoint> points = Arrays.asList(start, end);
		int frame = 13;
		int channel = 0;

		overlay.setImageDisplay(imd);
		imp.setT(frame);

		List<RealPoint> line = GeometryUtils.getLinePointsFromSpacing(start, end, 1);
		log.info(line);
		Filament filament = new Filament(line, frame);
		overlay.add(filament);

/*		INDArray intensities = GeometryUtils.getIntensities(line, dataset, frame, channel, 0);
		GeometryUtils.plotPoints(intensities);*/

	}
}
