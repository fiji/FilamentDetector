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

import org.scijava.log.LogService;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imglib2.type.numeric.RealType;
import sc.fiji.filamentdetector.imagepreprocessor.DOGFilterPreprocessor;
import sc.fiji.filamentdetector.imagepreprocessor.ImagePreprocessorService;
import sc.fiji.filamentdetector.imagepreprocessor.TubenessFilterPreprocessor;

public class TestPreprocessing {

	public static <T extends RealType<T>> void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);

		LogService log = ij.log();
		ImagePreprocessorService procService = ij.get(ImagePreprocessorService.class);
		DatasetIOService dsio = ij.get(DatasetIOService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/7,5uM_emccd_lapse1-small-8bit.tif";
		fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/7,5uM_emccd_lapse1.tif";
		Dataset dataset = dsio.open(fpath);
		ij.ui().show(dataset);

/*		TubenessFilterPreprocessor proc = procService.getTubenessFilter();
		proc.setDoPreprocess(true);
		proc.setSigma(5);*/

		DOGFilterPreprocessor proc = procService.getDOGFilter();
		proc.setDoPreprocess(true);
		proc.setSigma1(6);
		proc.setSigma2(2);

		proc.setInput(dataset);
		proc.preprocess();
		Dataset output = proc.getOutput();

		ij.ui().show(output);

		log.info("Done");
	}
}
