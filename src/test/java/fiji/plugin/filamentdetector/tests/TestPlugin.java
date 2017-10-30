package fiji.plugin.filamentdetector.tests;

import org.scijava.Context;

import fiji.plugin.filamentdetector.FilamentDetectorPlugin;
import net.imagej.Dataset;
import net.imagej.ImageJ;

public class TestPlugin {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/7,5uM_emccd_lapse1-small-8bit.tif";
		// fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/test-tracking.tif";
		// fpath =
		// "/home/hadim/.doc/Code/Postdoc/ij/testdata/12,5uM_emccd_lapse1-8bit-small.tif";
		// fpath =
		// "/home/hadim/.doc/Code/Postdoc/ij/testdata/10uM_emccd_lapse2-8bit.tif";
		Dataset data = ij.dataset().open(fpath);
		ij.ui().show(data);

		ij.command().run(FilamentDetectorPlugin.class, true);

	}
}
