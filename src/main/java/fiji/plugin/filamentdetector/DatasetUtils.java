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
package fiji.plugin.filamentdetector;

import org.scijava.Context;
import org.scijava.log.LogService;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

public class DatasetUtils {

	// TODO: use IJ Ops to do that
	static public ImageDisplay convertTo8Bit(ImageDisplay imageDisplay, ImageJ ij) {
		/*
		 * ImageDisplay out = null; Dataset dataset = (Dataset)
		 * imageDisplay.getActiveView().getData();
		 * 
		 * Img<DoubleType> clipped = ij.op().create().img(dataset); Op clip_op =
		 * ij.op().op("convert.clip", dataset.getImgPlus().firstElement(),
		 * dataset.firstElement()); ij.op().op("convert.imageType", clipped, dataset,
		 * clip_op);
		 * 
		 * Dataset converted = ij.dataset().create(ij.op().create().imgPlus(clipped));
		 * ij.ui().show(converted);
		 * 
		 * return ij.imageDisplay().getActiveImageDisplay();
		 */
		return null;
	}

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		LogService log = ij.log();

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/test-16bit.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		ImageDisplay converted = DatasetUtils.convertTo8Bit(imd, ij);
	}

}
