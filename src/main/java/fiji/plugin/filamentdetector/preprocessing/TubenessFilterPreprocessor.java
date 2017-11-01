package fiji.plugin.filamentdetector.preprocessing;

import org.scijava.Context;

import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;

public class TubenessFilterPreprocessor extends AbstractImagePreprocessor {

	private static boolean DEFAULT_DO_PREPROCESS = false;
	private static double DEFAULT_SIGMA = 5;

	private double sigma = DEFAULT_SIGMA;
	private double[] calibrations = new double[] { 1, 1 };

	public TubenessFilterPreprocessor(Context context) {
		super(context);
		setDoPreprocess(DEFAULT_DO_PREPROCESS);
	}

	@Override
	public <T extends RealType<T>> void preprocess() {
		if (isDoPreprocess()) {
			Dataset dataset = getInput().duplicate();

			int[] fixedAxisIndices = new int[] { dataset.dimensionIndex(Axes.X), dataset.dimensionIndex(Axes.Y) };

			// Apply filter
			Img<T> out = (Img<T>) ops.create().img(dataset.getImgPlus().getImg());
			UnaryComputerOp op = (UnaryComputerOp) ops.op("filter.tubeness", out, dataset.getImgPlus(), sigma,
					calibrations);

			ops.slice(out, dataset.getImgPlus(), op, fixedAxisIndices);

			this.output = matchRAIToDataset(out, dataset);
		} else {
			this.output = getInput();
		}
	}

	public double getSigma() {
		return sigma;
	}

	public void setSigma(double sigma) {
		this.sigma = sigma;
	}

	public double[] getCalibrations() {
		return calibrations;
	}

	public void setCalibrations(double[] calibrations) {
		this.calibrations = calibrations;
	}

}
