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

import org.scijava.Context;
import org.scijava.log.LogService;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;
import sc.fiji.filamentdetector.analyzer.tipfitter.FilamentTipFitter;
import sc.fiji.filamentdetector.model.Filament;
import sc.fiji.filamentdetector.model.TrackedFilament;
import sc.fiji.filamentdetector.model.TrackedFilaments;
import sc.fiji.filamentdetector.overlay.FilamentOverlayService;

public class TestTipFitter {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		FilamentOverlayService overlay = ij.get(FilamentOverlayService.class);
		LogService log = ij.get(LogService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/7,5uM_emccd_lapse1-small-8bit.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		double[] x = new double[] { 28, 22 };
		double[] y = new double[] { 48, 37 };
		Filament singleSeed = new Filament(x, y, 13);

		overlay.setImageDisplay(imd);
		overlay.add(singleSeed);

		TrackedFilaments seeds = new TrackedFilaments();
		TrackedFilament seed = new TrackedFilament();
		seed.add(singleSeed);
		seed.setColor(Color.red);
		seeds.add(seed);

		FilamentTipFitter tipFitter = new FilamentTipFitter(context);
		tipFitter.setImageDisplay(imd);
		tipFitter.setSeeds(seeds);
		tipFitter.fit();

		overlay.reset();
		// overlay.add(tipFitter.getFilaments());
	}
}
