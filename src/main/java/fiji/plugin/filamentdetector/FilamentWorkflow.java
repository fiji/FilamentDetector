package fiji.plugin.filamentdetector;

import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import fiji.plugin.filamentdetector.detection.DetectionParameters;
import fiji.plugin.filamentdetector.detection.FilamentsDetector;
import fiji.plugin.filamentdetector.detection.FilteringParameters;
import fiji.plugin.filamentdetector.event.ImageNotFoundEvent;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import fiji.plugin.filamentdetector.tracking.FilamentTracker;
import fiji.plugin.filamentdetector.tracking.FilteringTrackedFilamentsParameters;
import fiji.plugin.filamentdetector.tracking.TrackingParameters;
import fiji.plugin.filamentdetector.tracking.lap.LAPFilamentTracker;
import fiji.plugin.filamentdetector.tracking.lap.LAPTrackingParameters;
import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;

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

	@Parameter
	private UIService ui;

	@Parameter
	private EventService eventService;

	private ImageDisplay imageDisplay;
	private Calibrations calibrations;

	private ImagePreprocessor imagePreprocessor;

	private DetectionParameters detectionParameters;
	private LAPTrackingParameters trackingParameters;

	private FilamentsDetector filamentsDetector;
	private FilamentTracker filamentsTracker;

	private Filaments filaments;
	private Filaments filteredFilaments;

	private TrackedFilaments trackedFilaments;
	private TrackedFilaments filteredTrackedFilaments;

	public FilamentWorkflow(Context context, ImageDisplay imd) {
		context.inject(this);
		this.imageDisplay = imd;

		this.imagePreprocessor = new ImagePreprocessor(context, imd);

		this.filaments = new Filaments();
		this.filteredFilaments = this.filaments;

		this.trackedFilaments = new TrackedFilaments(context);
		this.filteredTrackedFilaments = this.trackedFilaments;
	}

	public void initialize() throws Exception {
		// Get physical pixel sizes (um) and duration between frames (s)
		calibrations = new Calibrations(context, getDataset(), getImagePlus());
	}

	public void initDetection() {
		detectionParameters = new DetectionParameters();
		Dataset data;
		if (imagePreprocessor.isHasBeenPreprocessed()) {
			data = imagePreprocessor.getPreprocessedImage();
		} else {
			data = getDataset();
		}
		filamentsDetector = new FilamentsDetector(context, imageDisplay, data, detectionParameters);
	}

	public void initTracking() {
		trackingParameters = new LAPTrackingParameters();
		filamentsTracker = new LAPFilamentTracker(context, getFilaments(), trackingParameters);
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
		try {
			Dataset dataset = (Dataset) imageDisplay.getActiveView().getData();
			return dataset;
		} catch (NullPointerException e) {
			eventService.publish(new ImageNotFoundEvent());
			return null;
		}
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

	public LAPTrackingParameters getTrackingParameters() {
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

	public ImagePreprocessor getImagePreprocessor() {
		return imagePreprocessor;
	}

}
