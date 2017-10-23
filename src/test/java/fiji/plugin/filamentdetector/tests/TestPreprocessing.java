package fiji.plugin.filamentdetector.tests;

import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.axis.CalibratedAxis;
import net.imagej.display.ImageDisplay;
import net.imagej.ops.OpService;
import net.imagej.ops.convert.RealTypeConverter;
import net.imglib2.IterableInterval;
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

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		ImagePreprocessor proc = new ImagePreprocessor(context, imd);
		proc.setGaussianFilterSize(50);
		Dataset background = proc.applyGaussianFilter(dataset);
		ij.ui().show(background);

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

		CalibratedAxis[] axes = new CalibratedAxis[dataset.numDimensions()];
		for (int i = 0; i != axes.length; i++) {
			axes[i] = dataset.axis(i);
		}
		Dataset output = ds.create(out3);
		output.setAxes(axes);

		ij.ui().show(output);

	}
}
