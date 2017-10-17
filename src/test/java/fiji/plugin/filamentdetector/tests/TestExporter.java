package fiji.plugin.filamentdetector.tests;

import java.io.File;

import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.Calibrations;
import fiji.plugin.filamentdetector.detection.DetectionParameters;
import fiji.plugin.filamentdetector.detection.FilamentsDetector;
import fiji.plugin.filamentdetector.exporter.CSVFilamentExporter;
import fiji.plugin.filamentdetector.exporter.CSVTrackedFilamentExporter;
import fiji.plugin.filamentdetector.exporter.DataExporter;
import fiji.plugin.filamentdetector.exporter.IJ1RoiFilamentExporter;
import fiji.plugin.filamentdetector.exporter.JSONFilamentExporter;
import fiji.plugin.filamentdetector.exporter.JSONTrackedFilamentExporter;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import fiji.plugin.filamentdetector.tracking.FilamentsTracker;
import fiji.plugin.filamentdetector.tracking.TrackingParameters;
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

		Calibrations calibrations = new Calibrations(ij.context(), dataset);

		// Setup parameters
		DetectionParameters params = new DetectionParameters();
		params.setSigma(2.5);

		TrackingParameters trackingParams = new TrackingParameters();
		trackingParams.setCostThreshold(0.5);

		// Detect filaments
		FilamentsDetector detector = new FilamentsDetector(ij.context(), imd, dataset, params);
		detector.detect();
		Filaments filaments = detector.getFilaments();

		log.info("Filaments Detected : " + filaments.size());

		FilamentsTracker tracker = new FilamentsTracker(ij.context(), filaments, trackingParams);
		tracker.track();
		TrackedFilaments trackedFilaments = tracker.getTrackedFilaments();

		log.info("Tracked Filaments Detected : " + trackedFilaments.size());

		DataExporter<Filaments> exporter;
		exporter = new JSONFilamentExporter(context, calibrations);
		exporter = new IJ1RoiFilamentExporter(context);
		exporter = new CSVFilamentExporter(context, calibrations);

		File file = new File("/home/hadim/test" + exporter.getExtension().substring(1));
		// exporter.export(filaments, file);

		DataExporter<TrackedFilaments> trackedExporter;
		trackedExporter = new CSVTrackedFilamentExporter(context, calibrations);
		trackedExporter = new JSONTrackedFilamentExporter(context, calibrations);

		file = new File("/home/hadim/test" + trackedExporter.getExtension().substring(1));
		trackedExporter.export(trackedFilaments, file);
	}
}
