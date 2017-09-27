package fiji.plugin.filamentdetector.tests;

import java.util.List;

import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.detection.DetectionParameters;
import fiji.plugin.filamentdetector.detection.FilamentsDetector;
import fiji.plugin.filamentdetector.kymograph.KymographGenerator;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import fiji.plugin.filamentdetector.tracking.FilamentsTracker;
import fiji.plugin.filamentdetector.tracking.TrackingParameters;
import ij.plugin.frame.RoiManager;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

public class TestKymograph {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		final Context context = ij.getContext();

		LogService log = ij.log();

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/seeds.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imageDisplay = ij.imageDisplay().getActiveImageDisplay();

		// Detect filaments
		DetectionParameters params = new DetectionParameters();
		FilamentsDetector detector = new FilamentsDetector(ij.context(), imageDisplay, params);
		detector.detect();
		Filaments filaments = detector.getFilaments();

		log.info("Filaments : " + filaments.size());

		// Track filaments over time
		TrackingParameters trackingParameters = new TrackingParameters();
		FilamentsTracker tracker = new FilamentsTracker(ij.context(), filaments, trackingParameters);
		tracker.track();
		TrackedFilaments trackedFilaments = tracker.getTrackedFilaments();

		log.info("Tracked filaments : " + trackedFilaments.size());

		// Generate kymographs
		KymographGenerator kymographGenerator = new KymographGenerator(context);
		kymographGenerator.setImageDisplay(imageDisplay);
		kymographGenerator.setTrackedFilaments(trackedFilaments);

		kymographGenerator.getKymographParameters().setShowKymographs(false);
		kymographGenerator.getKymographParameters().setSaveKymographLines(true);
		kymographGenerator.getKymographParameters().setSaveKymographs(false);

		kymographGenerator.build();

		List<Dataset> kymographs = kymographGenerator.getKymographs();
		log.info(kymographs);

		RoiManager rm = RoiManager.getRoiManager();
		rm.runCommand("Show All");

	}
}
