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
package sc.fiji.filamentdetector.imagepreprocessor;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import net.imagej.Dataset;
import net.imagej.axis.CalibratedAxis;
import net.imagej.ops.convert.RealTypeConverter;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

@Plugin(type = ImagePreprocessor.class, priority = Priority.HIGH)
public class Convert8BitPreprocessor extends AbstractImagePreprocessor {

	private static boolean DEFAULT_DO_PREPROCESS = true;

	public Convert8BitPreprocessor() {
		super();
		setDoPreprocess(DEFAULT_DO_PREPROCESS);
	}

	@Override
	public <T extends RealType<T>> void preprocess() {
		if (isDoPreprocess() && getInput().getType().getClass() != UnsignedByteType.class) {
			Dataset dataset = getInput().duplicate();

			Img<UnsignedByteType> out = ops.create().img(dataset, new UnsignedByteType());

			RealTypeConverter op = (RealTypeConverter) ops.op("convert.normalizeScale",
					dataset.getImgPlus().firstElement(), out.firstElement());
			ops.convert().imageType(out, (IterableInterval<T>) dataset.getImgPlus(), op);

			CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
			for (int i = 0; i != axes.length; i++) {
				axes[i] = dataset.axis(i);
			}
			this.output = matchRAIToDataset(out, dataset);
		} else {
			this.output = getInput();
		}
	}

}
