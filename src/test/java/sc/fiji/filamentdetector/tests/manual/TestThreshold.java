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

import org.scijava.Context;
import org.scijava.log.LogService;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imglib2.histogram.Histogram1d;
import net.imglib2.type.numeric.RealType;

public class TestThreshold {

	public static <T extends RealType<T>> void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		LogService log = ij.log();
		OpService op = ij.op();

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/7,5uM_emccd_lapse1-small-8bit-Preprocessed.tif";
		Dataset dataset = ij.dataset().open(fpath);

		Histogram1d<T> in = op.image().histogram((Iterable<T>) dataset.getImgPlus());

		log.info(op.threshold().rosin(in));
		log.info(op.threshold().huang(in));
		log.info(op.threshold().intermodes(in));
		log.info(op.threshold().isoData(in));
		log.info(op.threshold().minimum(in).get(0));
		log.info(op.threshold().li(in));
		log.info(op.threshold().maxEntropy(in));
		log.info(op.threshold().maxLikelihood(in));
		log.info(op.threshold().moments(in));
		log.info(op.threshold().percentile(in));
		log.info(op.threshold().renyiEntropy(in));
		log.info(op.threshold().shanbhag(in));
		log.info(op.threshold().triangle(in));
		log.info(op.threshold().yen(in));

	}

}
