package fiji.plugin.filamentdetector.preprocessing;

import org.scijava.Context;

import net.imagej.Dataset;
import net.imagej.axis.Axes;
import net.imagej.ops.convert.RealTypeConverter;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

public class FrangiFilterPreprocessor extends AbstractImagePreprocessor {

	private static boolean DEFAULT_DO_PREPROCESS = false;
	private static double DEFAULT_SPACING = 1;
	private static int DEFAULT_SCALE = 2;

	private double spacing = DEFAULT_SPACING;
	private int scale = DEFAULT_SCALE;

	public FrangiFilterPreprocessor(Context context) {
		super(context);
		setDoPreprocess(DEFAULT_DO_PREPROCESS);
	}

	@Override
	public <T extends RealType<T>> void preprocess() {
		if (isDoPreprocess()) {
			Dataset dataset = getInput().duplicate();

			int[] fixedAxisIndices = new int[] { dataset.dimensionIndex(Axes.X), dataset.dimensionIndex(Axes.Y) };

			// Filter parameter
			double[] spacingArray = new double[] { spacing, spacing };

			// Convert to 32 bits
			Img<FloatType> out = (Img<FloatType>) ops.run("convert.float32", dataset.getImgPlus());

			// Apply filter
			Img<FloatType> out2 = ops.create().img(out);
			UnaryComputerOp op = (UnaryComputerOp) ops.op("filter.frangiVesselness", out2, out, spacingArray, scale);
			// sigma1, sigma2);
			ops.slice(out2, out, op, fixedAxisIndices);

			// Normalize intensity
			Img<T> out3 = (Img<T>) ops.create().img(dataset.getImgPlus());
			RealTypeConverter op2 = (RealTypeConverter) ops.op("convert.normalizeScale",
					dataset.getImgPlus().firstElement(), out2.firstElement());
			ops.convert().imageType(out3, out2, op2);

			this.output = matchRAIToDataset(out3, dataset);
		} else {
			this.output = getInput();
		}
	}

	public double getSpacing() {
		return spacing;
	}

	public void setSpacing(double spacing) {
		this.spacing = spacing;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

}
