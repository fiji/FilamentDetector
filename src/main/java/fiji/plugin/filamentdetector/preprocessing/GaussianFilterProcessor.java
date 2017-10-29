package fiji.plugin.filamentdetector.preprocessing;

import org.scijava.Context;

import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

public class GaussianFilterProcessor extends AbstractImagePreprocessor {

	private static boolean DEFAULT_DO_PREPROCESS = true;
	private static double DEFAULT_GAUSSIAN_FITLER_SIZE = 1;

	private double gaussianFilterSize = DEFAULT_GAUSSIAN_FITLER_SIZE;

	public GaussianFilterProcessor(Context context) {
		super(context);
		setDoPreprocess(DEFAULT_DO_PREPROCESS);
	}

	@Override
	public <T extends RealType<T>> void preprocess() {
		if (isDoPreprocess()) {
			Dataset dataset = getInput().duplicate();

			int[] fixedAxisIndices = new int[] { dataset.dimensionIndex(Axes.X), dataset.dimensionIndex(Axes.Y) };

			RandomAccessibleInterval<T> out = (RandomAccessibleInterval<T>) ops.create().img(dataset.getImgPlus());

			double[] sigmas = new double[] { this.gaussianFilterSize, this.gaussianFilterSize };
			UnaryComputerOp op = (UnaryComputerOp) ops.op("filter.gauss", dataset.getImgPlus(), sigmas);
			ops.slice(out, (RandomAccessibleInterval<T>) dataset.getImgPlus(), op, fixedAxisIndices);

			this.output = matchRAIToDataset(out, dataset);
		} else {
			this.output = getInput();
		}
	}

	public double getGaussianFilterSize() {
		return gaussianFilterSize;
	}

	public void setGaussianFilterSize(double gaussianFilterSize) {
		this.gaussianFilterSize = gaussianFilterSize;
	}

}
