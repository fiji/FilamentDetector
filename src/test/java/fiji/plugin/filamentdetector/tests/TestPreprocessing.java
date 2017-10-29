package fiji.plugin.filamentdetector.tests;

import org.scijava.Context;
import org.scijava.log.LogService;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.ops.OpService;
import net.imagej.ops.convert.RealTypeConverter;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

public class TestPreprocessing {

	public static <T extends RealType<T>> void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		LogService log = ij.log();
		OpService ops = ij.op();
		DatasetService ds = ij.dataset();

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/7,5uM_emccd_lapse1-small-8bit.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		// // Gaussian filter
		// GaussianFilterPreprocessor proc = new GaussianFilterPreprocessor(context);
		// proc.setDoPreprocess(true);
		// proc.setGaussianFilterSize(20);
		//
		// proc.setInput(dataset);
		// proc.preprocess();
		// Dataset output = proc.getOutput();
		//
		// ij.ui().show(output);

		int[] fixedAxisIndices = new int[] { dataset.dimensionIndex(Axes.X), dataset.dimensionIndex(Axes.Y) };

		// Filter parameter
		double[] spacing = new double[] { 1, 1 };
		int scale = 4;

		// Convert to 32 bits
		Img<FloatType> out = (Img<FloatType>) ops.run("convert.float32", dataset.getImgPlus());

		// Apply filter
		Img<FloatType> out2 = (Img<FloatType>) ops.create().img(out);
		UnaryComputerOp op = (UnaryComputerOp) ops.op("filter.frangiVesselness", out2, out, spacing, scale);
		// sigma1, sigma2);
		ops.slice(out2, out, op, fixedAxisIndices);

		// Normalize intensity
		Img<T> out3 = (Img<T>) ops.create().img(dataset.getImgPlus());
		RealTypeConverter op2 = (RealTypeConverter) ops.op("convert.normalizeScale",
				dataset.getImgPlus().firstElement(), out2.firstElement());
		ops.convert().imageType(out3, out2, op2);

		CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
		for (int i = 0; i != axes.length; i++) {
			axes[i] = dataset.axis(i);
		}
		Dataset output = ds.create(out3);
		output.setAxes(axes);

		ij.ui().show(output);

	}
}
