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
package sc.fiji.filamentdetector;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

import net.imagej.ImageJService;
import net.imagej.ImgPlus;
import net.imagej.axis.AxisType;
import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

@Plugin(type = Service.class)
public class ImageUtilService extends AbstractService implements ImageJService {

	@Parameter
	private LogService log;

	@Parameter
	private OpService op;

	public ImgPlus<? extends RealType<?>> cropAlongAxis(ImgPlus<? extends RealType<?>> img, AxisType axis, long index) {

		if (img.dimensionIndex(axis) == -1) {
			return img;
		}

		int nDim = img.numDimensions();
		long[] intervalsArray = new long[nDim * 2];

		for (int i = 0; i < nDim; i++) {

			if (img.axis(i).type().equals(axis)) {
				intervalsArray[i] = index;
				intervalsArray[i + nDim] = index;
			} else {
				intervalsArray[i] = 0;
				intervalsArray[i + nDim] = img.dimension(i) - 1;
			}
		}

		FinalInterval interval = Intervals.createMinMax(intervalsArray);
		return (ImgPlus<? extends RealType<?>>) op.transform().crop(img, interval);
	}

}
