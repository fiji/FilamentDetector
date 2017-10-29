package fiji.plugin.filamentdetector.tests;

import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.preprocessing.GaussianFilterPreprocessor;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.ops.OpService;
import net.imagej.ops.special.computer.UnaryComputerOp;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

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

//		// Gaussian filter
//		GaussianFilterPreprocessor proc = new GaussianFilterPreprocessor(context);
//		proc.setDoPreprocess(true);
//		proc.setGaussianFilterSize(20);
//
//		proc.setInput(dataset);
//		proc.preprocess();
//		Dataset output = proc.getOutput();
//
//		ij.ui().show(output);
		
		Dataset input = dataset.duplicate();

		int[] fixedAxisIndices = new int[] { input.dimensionIndex(Axes.X), input.dimensionIndex(Axes.Y) };

		RandomAccessibleInterval<T> out = (RandomAccessibleInterval<T>) ops.create().img(input.getImgPlus());

		double[] spacing = new double[] { 1, 3 };
		int scale = 1;
		UnaryComputerOp op = (UnaryComputerOp) ops.op("filter.frangiVesselness", out, input.getImgPlus(), spacing, scale);
		ops.slice(out, (RandomAccessibleInterval<T>) input.getImgPlus(), op, fixedAxisIndices);

		CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
		for (int i = 0; i != axes.length; i++) {
			axes[i] = dataset.axis(i);
		}
		Dataset output = ds.create(out);
		output.setAxes(axes);
		
		ij.ui().show(output);

	}
}
