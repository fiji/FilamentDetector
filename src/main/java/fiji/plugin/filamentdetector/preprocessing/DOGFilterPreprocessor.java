package fiji.plugin.filamentdetector.preprocessing;

import org.scijava.Context;

import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.ops.convert.RealTypeConverter;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

public class DOGFilterPreprocessor extends AbstractImagePreprocessor {

	private static boolean DEFAULT_DO_PREPROCESS = false;
	private static double DEFAULT_DOG_SIGMA1 = 6;
	private static double DEFAULT_DOG_SIGMA2 = 2;

	private double sigma1 = DEFAULT_DOG_SIGMA1;
	private double sigma2 = DEFAULT_DOG_SIGMA2;

	public DOGFilterPreprocessor(Context context) {
		super(context);
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
			Img<FloatType> out2 = (Img<FloatType>) ops.create().img(out);
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
