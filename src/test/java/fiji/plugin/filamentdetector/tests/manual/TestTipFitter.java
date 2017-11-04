package fiji.plugin.filamentdetector.tests.manual;

import java.awt.Color;

import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.analyzer.tipfitter.FilamentTipFitter;
import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

public class TestTipFitter {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		FilamentOverlayService overlay = ij.get(FilamentOverlayService.class);
		LogService log = ij.get(LogService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/7,5uM_emccd_lapse1-small-8bit.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		double[] x = new double[] { 28, 22 };
		double[] y = new double[] { 48, 37 };
		Filament singleSeed = new Filament(x, y, 13);

		overlay.setImageDisplay(imd);
		overlay.add(singleSeed);

		TrackedFilaments seeds = new TrackedFilaments();
		TrackedFilament seed = new TrackedFilament();
		seed.add(singleSeed);
		seed.setColor(Color.red);
		seeds.add(seed);

		FilamentTipFitter tipFitter = new FilamentTipFitter(context);
		tipFitter.setImageDisplay(imd);
		tipFitter.setSeeds(seeds);
		tipFitter.fit();

		overlay.reset();
		// overlay.add(tipFitter.getFilaments());
	}
}
