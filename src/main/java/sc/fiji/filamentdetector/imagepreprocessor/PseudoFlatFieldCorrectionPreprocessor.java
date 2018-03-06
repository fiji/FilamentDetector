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
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.Dataset;
import net.imagej.ops.convert.RealTypeConverter;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

@Plugin(type = ImagePreprocessor.class, priority = Priority.HIGH)
public class PseudoFlatFieldCorrectionPreprocessor extends AbstractImagePreprocessor {

	@Parameter
	private ImagePreprocessorService processorService;

	private static boolean DEFAULT_DO_PREPROCESS = false;
	private static double DEFAULT_FLAT_FIELD_CORRECTION_SIZE = 50;

	private double flatFieldCorrectionGaussianFilterSize = DEFAULT_FLAT_FIELD_CORRECTION_SIZE;

	public PseudoFlatFieldCorrectionPreprocessor() {
		super();
		setDoPreprocess(DEFAULT_DO_PREPROCESS);
	}

	@Override
	public <T extends RealType<T>> void preprocess() {
		if (isDoPreprocess()) {
			Dataset dataset = getInput().duplicate();

			// Get Gaussian filtered image and use it as a background
			GaussianFilterPreprocessor processor = processorService.getGaussianFilter();
			processor.setDoPreprocess(true);
			processor.setInput(dataset);
			processor.setGaussianFilterSize(flatFieldCorrectionGaussianFilterSize);
			processor.preprocess();
			Dataset background = processor.getOutput();

			// Convert to 32 bits
			IterableInterval<FloatType> in = (IterableInterval<FloatType>) ops.run("convert.float32",
					dataset.getImgPlus());
			IterableInterval<FloatType> original = (IterableInterval<FloatType>) ops.run("convert.float32",
					dataset.getImgPlus());
			IterableInterval<FloatType> backgroundFloat = (IterableInterval<FloatType>) ops.run("convert.float32",
					background.getImgPlus());

			// Do subtraction
			IterableInterval<FloatType> out2 = ops.create().img(in);
			ops.math().subtract(out2, original, backgroundFloat);

			// Clip intensities
			Img<T> out3 = (Img<T>) ops.create().img(dataset.getImgPlus());
			RealTypeConverter op2 = (RealTypeConverter) ops.op("convert.clip", dataset.getImgPlus().firstElement(),
					out2.firstElement());
			ops.convert().imageType(out3, out2, op2);

			// Normalize intensity
			Img<T> out4 = (Img<T>) ops.create().img(out3);
			RealTypeConverter op3 = (RealTypeConverter) ops.op("convert.normalizeScale", out4.firstElement(),
					out3.firstElement());
			ops.convert().imageType(out4, out3, op3);

			this.output = matchRAIToDataset(out4, dataset);
		} else {
			this.output = getInput();
		}
	}

	public double getFlatFieldCorrectionGaussianFilterSize() {
		return flatFieldCorrectionGaussianFilterSize;
	}

	public void setFlatFieldCorrectionGaussianFilterSize(double flatFieldCorrectionGaussianFilterSize) {
		this.flatFieldCorrectionGaussianFilterSize = flatFieldCorrectionGaussianFilterSize;
	}

}
