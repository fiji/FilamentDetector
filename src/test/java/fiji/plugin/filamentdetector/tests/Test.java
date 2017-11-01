package fiji.plugin.filamentdetector.tests;

import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.FilamentWorkflow;
import fiji.plugin.filamentdetector.detection.RidgeDetectionFilamentsDetector;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import fiji.plugin.filamentdetector.overlay.ImageDisplayMode;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import fiji.plugin.filamentdetector.tracking.BBoxLAPFilamentsTracker;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

public class Test {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		LogService log = ij.log();
		FilamentOverlayService overlay = ij.get(FilamentOverlayService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/fake1.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		FilamentWorkflow wf = new FilamentWorkflow(context, imd);
		wf.initialize();
		wf.getCalibrations().setChannelToUseIndex(2);

		// Enable some preprocessors
		ImagePreprocessor proc = wf.getImagePreprocessor()
				.getPreProcessorByName("PseudoFlatFieldCorrectionPreprocessor");
		proc.setDoPreprocess(true);
		proc = wf.getImagePreprocessor().getPreProcessorByName("DOGFilterPreprocessor");
		proc.setDoPreprocess(true);

		// Preprocess the image
		wf.getImagePreprocessor().preprocess();

		// Get processed data and convert it to ImageDisplay
		// TODO: that step should be easier
		Dataset datasetp = wf.getImagePreprocessor().getPreprocessedDataset();
		ij.ui().show(datasetp);
		ImageDisplay imdp = ij.imageDisplay().getImageDisplays().stream()
				.filter(i -> ((Dataset) i.getActiveView().getData()).equals(datasetp)).findFirst().orElse(null);
		wf.setImageDisplay(imdp);

		overlay.initialize();
		overlay.setImageDisplay(imdp);
		overlay.setViewMode(ImageDisplayMode.COMPOSITE);

		RidgeDetectionFilamentsDetector detector = new RidgeDetectionFilamentsDetector(context);
		wf.setFilamentDetector(detector);
		detector.setImageDisplay(imdp);
		detector.setDataset(datasetp);
		detector.setLineWidth(2.5);

		wf.detect();
		Filaments filaments = wf.getFilaments();

		overlay.add(filaments);

		BBoxLAPFilamentsTracker tracker = new BBoxLAPFilamentsTracker(context);
		wf.setFilamentsTracker(tracker);
		wf.track();
		TrackedFilaments trackedFilaments = wf.getTrackedFilaments();

		overlay.reset();
		overlay.setDrawPlusTips(true);
		overlay.add(trackedFilaments);
		
		log.info("Done");
	}
}
