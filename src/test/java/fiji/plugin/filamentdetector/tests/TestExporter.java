package fiji.plugin.filamentdetector.tests;

import java.io.File;

import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.detection.DetectionParameters;
import fiji.plugin.filamentdetector.detection.FilamentsDetector;
import fiji.plugin.filamentdetector.exporter.DataExporter;
import fiji.plugin.filamentdetector.exporter.JSONFilamentExporter;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

public class TestExporter {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		LogService log = ij.log();
		FilamentOverlayService overlayService = ij.get(FilamentOverlayService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/seeds.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		// Setup parameters
		DetectionParameters params = new DetectionParameters();
		params.setSigma(2.5);

		// Detect filaments
		FilamentsDetector detector = new FilamentsDetector(ij.context(), imd, params);
		detector.detect();
		Filaments filaments = detector.getFilaments();

		log.info("Filaments Detected : " + filaments.size());

		DataExporter<Filaments> exporter = new JSONFilamentExporter(context);
		// exporter = new IJ1RoiFilamentExporter(context);
		File file = new File("/home/hadim/test.json");
		exporter.export(filaments, file);
	}
}
