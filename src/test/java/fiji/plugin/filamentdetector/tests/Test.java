package fiji.plugin.filamentdetector.tests;

import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.Calibrations;
import fiji.plugin.filamentdetector.detection.DetectionParameters;
import fiji.plugin.filamentdetector.detection.FilamentsDetector;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import fiji.plugin.filamentdetector.tracking.FilamentsTracker;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class Test {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		LogService log = ij.log();
		FilamentOverlayService overlayService = ij.get(FilamentOverlayService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/seeds.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		// Check image exist on disk
		if (dataset.getSource() == "") {
			log.error("Please save the image on disk before processing it.");
			return;
		}

		// Check image is 8-bit
		if (dataset.getType().getClass() != UnsignedByteType.class) {
			log.error("Please convert the image to 8-bit first.");
			return;
		}

		// Get physical pixel sizes (um) and duration between frames (s)
		Calibrations cals = new Calibrations(ij.context(), dataset);
		log.info("Pixel size is: " + cals.getDx());
		log.info("dt is: " + cals.getDt());

		// Setup parameters
		DetectionParameters params = new DetectionParameters();
		params.setSigma(2.5);

		// Detect filaments
		FilamentsDetector detector = new FilamentsDetector(ij.context(), imd, dataset, params);
		detector.detect();
		Filaments filaments = detector.getFilaments();

		// Simplify filaments by reducing the number of points
		double toleranceDistance = 10;
		filaments = filaments.simplify(toleranceDistance);

		// Filter lines by length and sinuosity
		double maxLength = 500;
		double minLength = 5;
		double maxSinuosity = 1.2;
		double minSinuosity = 0;

		Filaments filteredFilaments = filaments.stream().filter(filament -> filament.getLength() < maxLength)
				.filter(filament -> filament.getLength() > minLength)
				.filter(filament -> filament.getSinuosity() < maxSinuosity)
				.filter(filament -> filament.getSinuosity() > minSinuosity)
				.collect(Collectors.toCollection(Filaments::new));

		// log.info(filteredFilaments.info());

		log.info("Size before filters : " + filaments.size());
		log.info("Size after filters : " + filteredFilaments.size());

		// Show lines as ROIs
		// List<Roi> rois = ROIFactory.createROIs(filteredFilaments);
		// ROIFactory.displayInROIManager(rois);

		// Track filaments over time
		FilamentsTracker tracker = new FilamentsTracker(ij.context());
		tracker.setFilaments(filteredFilaments);
		tracker.track();
		TrackedFilaments trackedFilaments = tracker.getTrackedFilaments();

		log.info(trackedFilaments.size());

		overlayService.setImageDisplay(imd);
		overlayService.setColorAlpha(170);
		overlayService.add(trackedFilaments);

		// Build GUI to do live fine parameters tuning
		// Filter TrackedFilament
		// Export data
		// Build kymographs automatically

	}
}
