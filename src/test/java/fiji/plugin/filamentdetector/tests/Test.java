package fiji.plugin.filamentdetector.tests;

import org.scijava.Context;

import fiji.plugin.filamentdetector.FilamentDetectorPlugin;
import net.imagej.Dataset;
import net.imagej.ImageJ;

public class Test {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		// String name = "filaments_single_time.ome.tif";
		String name = "filaments.ome.tif";
		String fpath = FilamentDetectorPlugin.class.getResource("/fiji/plugin/filamentdetector/tests/testdata/" + name)
				.getPath();
		
		//fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/seeds.tif";
		Dataset data = ij.dataset().open(fpath);
		ij.ui().show(data);

		ij.command().run(FilamentDetectorPlugin.class, true);

	}
}
