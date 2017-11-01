package fiji.plugin.filamentdetector.tests;

import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.preprocessing.TubenessFilterPreprocessor;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
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

		TubenessFilterPreprocessor proc = new TubenessFilterPreprocessor(context);
		proc.setDoPreprocess(true);
		proc.setSigma(5);

		proc.setInput(dataset);
		proc.preprocess();
		Dataset output = proc.getOutput();

		ij.ui().show(output);

	}
}
