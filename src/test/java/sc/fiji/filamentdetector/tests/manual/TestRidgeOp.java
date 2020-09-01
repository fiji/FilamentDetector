/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2020 Fiji developers.
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

import java.util.List;

import org.scijava.Context;
import org.scijava.log.LogService;

import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops.Segment.DetectRidges;
import net.imglib2.roi.geom.real.Polyline;
import net.imglib2.type.numeric.RealType;
import sc.fiji.filamentdetector.ImageUtilService;

public class TestRidgeOp {

	public static <T extends RealType<T>> void main(final String... args) throws Exception {
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();
		Context context = ij.getContext();

		LogService log = ij.log();
		OpService op = ij.op();
		ImageUtilService ijUtil = context.getService(ImageUtilService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/7,5uM_emccd_lapse1-small-8bit-Preprocessed.tif";
		Dataset dataset = (Dataset) ij.io().open(fpath);

		ImgPlus<? extends RealType<?>> slice = ijUtil.cropAlongAxis(dataset.getImgPlus(), Axes.TIME, 1);
		slice = ijUtil.cropAlongAxis(slice, Axes.CHANNEL, 0);
		ij.ui().show(slice);

		log.info(slice.numDimensions());

		List<Polyline> lines = (List<Polyline>) op.run(DetectRidges.class, slice, 4.0, 0.0, 100.0, (int) 4);

		log.info(lines);

	}

}
