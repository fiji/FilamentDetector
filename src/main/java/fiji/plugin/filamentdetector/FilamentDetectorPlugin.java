package fiji.plugin.filamentdetector;

import java.util.stream.Collectors;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import fiji.plugin.filamentdetector.overlay.ColorService;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import fiji.plugin.filamentdetector.tracking.FilamentsTracker;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;
import net.imglib2.type.numeric.integer.UnsignedByteType;

@Plugin(type = Command.class, menuPath = "Plugins>Tracking>FilamentDetector")
public class FilamentDetectorPlugin implements Command {

	@Parameter
	private ImageJ ij;

	@Parameter
	private LogService log;

	@Parameter
	private ConvertService convert;

	@Parameter
	private FilamentOverlayService overlayService;

	@Parameter
	private DatasetService dservice;

	@Parameter
	private ColorService colorService;

	@Parameter(type = ItemIO.INPUT)
	private ImageDisplay imd;

	private Dataset dataset;

	public static final String PLUGIN_NAME = "FilamentDetector";
	public static final String VERSION = version();

	private static String version() {
		String version = null;
		final Package pack = FilamentDetectorPlugin.class.getPackage();
		if (pack != null) {
			version = pack.getImplementationVersion();
		}
		return version == null ? "DEVELOPMENT" : version;
	}

	@Override
	public void run() {

		log.info("Running " + PLUGIN_NAME + " version " + VERSION);

		// Get Dataset
		dataset = dservice.getDatasets(imd).get(0);

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
		Calibrations cals = new Calibrations(dataset);
		log.info("Pixel size is: " + cals.getDx());
		log.info("dt is: " + cals.getDt());

		// Setup parameters
		DetectionParameters params = new DetectionParameters();
		params.setSigma(2.5);

		// Detect filaments
		Detector detector = new Detector(ij.context(), dataset, params);
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
		FilamentsTracker tracker = new FilamentsTracker(ij.context(), filteredFilaments);
		tracker.track();
		TrackedFilaments trackedFilaments = tracker.getTrackedFilaments();

		log.info(trackedFilaments.size());

		overlayService.setImageDisplay(imd);
		overlayService.setColorALpha(170);
		overlayService.add(trackedFilaments);

		// Build GUI to do live fine parameters tuning
		// Filter TrackedFilament
		// Export data
		// Build kymographs automatically
	}

}
