package fiji.plugin.filamentdetector;

import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.detection.DetectionParameters;
import fiji.plugin.filamentdetector.detection.FilamentsDetector;
import fiji.plugin.filamentdetector.detection.FilteringParameters;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import fiji.plugin.filamentdetector.tracking.FilamentsTracker;
import fiji.plugin.filamentdetector.tracking.FilteringTrackedFilamentsParameters;
import fiji.plugin.filamentdetector.tracking.TrackingParameters;
import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/*
 * This class holds the necessary informations/models necessary during the use of the GUI.
 * Can be also used for scripting.
 */
public class FilamentWorkflow {

	@Parameter
	private Context context;

	@Parameter
	private ConvertService convert;

	@Parameter
	private LogService log;

	private ImageDisplay imageDisplay;
	private Calibrations calibrations;

	private DetectionParameters detectionParameters;
	private TrackingParameters trackingParameters;

	private FilamentsDetector filamentsDetector;
	private FilamentsTracker filamentsTracker;

	private Filaments filaments;
	private Filaments filteredFilaments;

	private TrackedFilaments trackedFilaments;
	private TrackedFilaments filteredTrackedFilaments;

	public FilamentWorkflow(Context context, ImageDisplay imd) {
		context.inject(this);
		this.imageDisplay = imd;

		this.filaments = new Filaments();
		this.filteredFilaments = this.filaments;

		this.trackedFilaments = new TrackedFilaments(context);
		this.filteredTrackedFilaments = this.trackedFilaments;
	}

	public void initialize() throws Exception {
		// Check image is 8-bit
		if (getDataset().getType().getClass() != UnsignedByteType.class) {
			throw new Exception("Please convert the image to 8-bit first.");
		}

		// Get physical pixel sizes (um) and duration between frames (s)
		calibrations = new Calibrations(context, getDataset(), getImagePlus());
	}

	public void initDetection() {
		detectionParameters = new DetectionParameters();
		filamentsDetector = new FilamentsDetector(context, imageDisplay, detectionParameters);
	}

	public void initTracking() {
		trackingParameters = new TrackingParameters();
		filamentsTracker = new FilamentsTracker(context, getFilaments(), trackingParameters);
	}

	public void detectCurrentFrame() {
		this.filamentsDetector.detectCurrentFrame(calibrations.getChannelToUseIndex());
		this.filaments = filamentsDetector.getFilaments();
		this.filteredFilaments = this.filaments;
	}

	public void detect() {
		this.filamentsDetector.detect(calibrations.getChannelToUseIndex());
		this.filaments = filamentsDetector.getFilaments();
		this.filteredFilaments = this.filaments;
	}

	public void track() {
		this.filamentsTracker.setFilaments(getFilaments());
		this.filamentsTracker.track();
		this.trackedFilaments = filamentsTracker.getTrackedFilaments();
		this.filteredTrackedFilaments = this.trackedFilaments;
	}

	public Dataset getDataset() {
		return (Dataset) imageDisplay.getActiveView().getData();
	}

	public ImageDisplay getImageDisplay() {
		return imageDisplay;
	}

	public ImagePlus getImagePlus() {
		return convert.convert(imageDisplay, ImagePlus.class);
	}

	public Calibrations getCalibrations() {
		return calibrations;
	}

	public DetectionParameters getDetectionParameters() {
		return detectionParameters;
	}

	public TrackingParameters getTrackingParameters() {
		return trackingParameters;
	}

	public Filaments getFilaments() {
		return filteredFilaments;
	}

	public TrackedFilaments getTrackedFilaments() {
		return filteredTrackedFilaments;
	}

	public void filterFilament(FilteringParameters filteringParameters) {
		if (!filteringParameters.isDisableFiltering()) {
			this.filteredFilaments = this.filaments.stream()
					.filter(filament -> filament.getLength() <= filteringParameters.getMaxLength())
					.filter(filament -> filament.getLength() >= filteringParameters.getMinLength())
					.filter(filament -> filament.getSinuosity() <= filteringParameters.getMaxSinuosity())
					.filter(filament -> filament.getSinuosity() >= filteringParameters.getMinSinuosity())
					.collect(Collectors.toCollection(Filaments::new));
		} else {
			this.filteredFilaments = this.filaments;
		}
	}

	public void filterTrackedFilament(FilteringTrackedFilamentsParameters filteringParameters) {
		if (!filteringParameters.isDisableFiltering()) {
			this.filteredTrackedFilaments = this.trackedFilaments.stream()
					.filter(trackedFilament -> trackedFilament.size() <= filteringParameters.getMaxSize())
					.filter(trackedFilament -> trackedFilament.size() >= filteringParameters.getMinSize())
					.collect(Collectors.toCollection(() -> new TrackedFilaments(context)));
		} else {
			this.filteredTrackedFilaments = this.trackedFilaments;
		}
	}

}
