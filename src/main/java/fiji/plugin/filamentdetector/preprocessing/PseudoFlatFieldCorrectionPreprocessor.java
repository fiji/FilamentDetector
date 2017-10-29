package fiji.plugin.filamentdetector.preprocessing;

import org.scijava.Context;

import net.imagej.Dataset;
import net.imagej.axis.CalibratedAxis;
import net.imagej.ops.convert.RealTypeConverter;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

public class PseudoFlatFieldCorrectionPreprocessor extends AbstractImagePreprocessor {

	private static boolean DEFAULT_DO_PREPROCESS = false;
	private static double DEFAULT_FLAT_FIELD_CORRECTION_SIZE = 50;

	private double flatFieldCorrectionGaussianFilterSize = DEFAULT_FLAT_FIELD_CORRECTION_SIZE;

	public PseudoFlatFieldCorrectionPreprocessor(Context context) {
		super(context);
		setDoPreprocess(DEFAULT_DO_PREPROCESS);
	}

	@Override
	public <T extends RealType<T>> void preprocess() {
		if (isDoPreprocess()) {
			Dataset dataset = getInput().duplicate();

			// Get Gaussian filtered image and use it as a background
			GaussianFilterPreprocessor processor = new GaussianFilterPreprocessor(context);
			processor.setDoPreprocess(true);
			processor.setInput(dataset);
			processor.setGaussianFilterSize(flatFieldCorrectionGaussianFilterSize);
			processor.preprocess();
			Dataset background = processor.getOutput();

			// Convert to 32 bits
			IterableInterval<FloatType> out = (IterableInterval<FloatType>) ops.run("convert.float32",
					dataset.getImgPlus());
			IterableInterval<FloatType> original = (IterableInterval<FloatType>) ops.run("convert.float32",
					dataset.getImgPlus());
			IterableInterval<FloatType> backgroundFloat = (IterableInterval<FloatType>) ops.run("convert.float32",
					background.getImgPlus());

			// Do subtraction
			IterableInterval<FloatType> out2 = (IterableInterval<FloatType>) ops.create().img(out);
			ops.math().subtract(out2, original, backgroundFloat);

			// Clip intensities
			Img<T> out3 = (Img<T>) ops.create().img(dataset.getImgPlus());
			RealTypeConverter op2 = (RealTypeConverter) ops.op("convert.clip", dataset.getImgPlus().firstElement(),
					out2.firstElement());
			ops.convert().imageType(out3, out2, op2);

			// Normalize intensity
			Img<T> out4 = (Img<T>) ops.create().img(out3);
			RealTypeConverter scaleOp = (RealTypeConverter) ops.op("convert.normalizeScale", out4.firstElement(),
					out3.firstElement());
			ops.convert().imageType(out4, out3, scaleOp);

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
