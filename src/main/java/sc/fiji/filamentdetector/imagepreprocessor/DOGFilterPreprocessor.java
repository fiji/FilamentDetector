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
import net.imagej.axis.Axes;
import net.imagej.ops.convert.RealTypeConverter;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

@Plugin(type = ImagePreprocessor.class, priority = Priority.HIGH)
public class DOGFilterPreprocessor extends AbstractImagePreprocessor {

	private static boolean DEFAULT_DO_PREPROCESS = false;
	private static double DEFAULT_DOG_SIGMA1 = 6;
	private static double DEFAULT_DOG_SIGMA2 = 2;

	private double sigma1 = DEFAULT_DOG_SIGMA1;
	private double sigma2 = DEFAULT_DOG_SIGMA2;

	public DOGFilterPreprocessor() {
		setDoPreprocess(DEFAULT_DO_PREPROCESS);
	}

	@Override
	public <T extends RealType<T>> void preprocess() {
		if (isDoPreprocess()) {
			Dataset dataset = getInput().duplicate();

			int[] fixedAxisIndices = new int[] { dataset.dimensionIndex(Axes.X), dataset.dimensionIndex(Axes.Y) };

			// Convert to 32 bits
			Img<FloatType> out = (Img<FloatType>) ops.run("convert.float32", dataset.getImgPlus());

			// Apply filter
			Img<FloatType> out2 = ops.create().img(out);
			UnaryComputerOp op = (UnaryComputerOp) ops.op("filter.dog", out, sigma1, sigma2);
			ops.slice(out2, out, op, fixedAxisIndices);

			// Clip intensities
			Img<T> out3 = (Img<T>) ops.create().img(dataset.getImgPlus());
			RealTypeConverter op2 = (RealTypeConverter) ops.op("convert.clip", dataset.getImgPlus().firstElement(),
					out2.firstElement());
			ops.convert().imageType(out3, out2, op2);

			this.output = matchRAIToDataset(out3, dataset);
		} else {
			this.output = getInput();
		}
	}

	public double getSigma1() {
		return sigma1;
	}

	public void setSigma1(double sigma1) {
		this.sigma1 = sigma1;
	}

	public double getSigma2() {
		return sigma2;
	}

	public void setSigma2(double sigma2) {
		this.sigma2 = sigma2;
	}

}
