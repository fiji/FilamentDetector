package fiji.plugin.filamentdetector;

import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import fiji.plugin.filamentdetector.detection.FilamentDetector;
import fiji.plugin.filamentdetector.detection.FilteringParameters;
import fiji.plugin.filamentdetector.event.ImageNotFoundEvent;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessors;
import fiji.plugin.filamentdetector.tracking.FilamentsTracker;
import fiji.plugin.filamentdetector.tracking.FilteringTrackedFilamentsParameters;
import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;

/*
 * This class holds the necessary informations/models necessary during
 *  the use of the GUI. Can be also used for scripting.
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

	final private ImageDisplay sourceImage;
	private ImageDisplay imageDisplay;
	private Calibrations calibrations;

	private ImagePreprocessors imagePreprocessors;

	private FilamentDetector filamentsDetector;
	private FilamentsTracker filamentsTracker;

	private Filaments filaments;
	private Filaments filteredFilaments;

	private TrackedFilaments trackedFilaments;
	private TrackedFilaments filteredTrackedFilaments;

	public FilamentWorkflow(Context context, ImageDisplay imd) {
		context.inject(this);
		this.imageDisplay = imd;
		this.sourceImage = imd;

		this.imagePreprocessors = new ImagePreprocessors(context, this.sourceImage);

		this.filaments = new Filaments();
		this.filteredFilaments = this.filaments;

		this.trackedFilaments = new TrackedFilaments(context);
		this.filteredTrackedFilaments = this.trackedFilaments;
	}

	public void initialize() throws Exception {
		// Get physical pixel sizes (um) and duration between frames (s)
		calibrations = new Calibrations(context, getDataset(), getImagePlus());
	}

	public void setFilamentDetector(FilamentDetector filamentDetector) {
		this.filamentsDetector = filamentDetector;
	}

	public void setFilamentsTracker(FilamentsTracker filamentsTracker) {
		this.filamentsTracker = filamentsTracker;
	}

	private void initDetection() {
		Dataset data;
		if (this.imagePreprocessors.isHasBeenPreprocessed()) {
			data = this.imagePreprocessors.getPreprocessedDataset();
		} else {
			data = getDataset();
		}
		this.filamentsDetector.setDataset(data);
		this.filamentsDetector.setImageDisplay(imageDisplay);
	}

	public void detectCurrentFrame() {
		this.initDetection();
		this.filamentsDetector.detectCurrentFrame(calibrations.getChannelToUseIndex());
		this.filaments = filamentsDetector.getFilaments();
		this.filteredFilaments = this.filaments;
	}

	public void detect() {
		this.initDetection();
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

	public void setImageDisplay(ImageDisplay imageDisplay) {
		this.imageDisplay = imageDisplay;
	}

	public ImageDisplay getImageDisplay() {
		return imageDisplay;
	}

	public ImageDisplay getSourceImage() {
		return sourceImage;
	}

	public ImagePlus getImagePlus() {
		return convert.convert(imageDisplay, ImagePlus.class);
	}

	public Calibrations getCalibrations() {
		return calibrations;
	}

	public FilamentDetector getFilamentDetector() {
		return filamentsDetector;
	}

	public FilamentsTracker getFilamentsTracker() {
		return filamentsTracker;
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

			double[] bbox = new double[] { 0, getCalibrations().getSizeX(), 0, getCalibrations().getSizeY() };
			bbox[0] += filteringParameters.getBorderLimit();
			bbox[1] -= filteringParameters.getBorderLimit();
			bbox[2] += filteringParameters.getBorderLimit();
			bbox[3] -= filteringParameters.getBorderLimit();

			this.filteredTrackedFilaments = this.trackedFilaments.stream()
					.filter(trackedFilament -> trackedFilament.size() <= filteringParameters.getMaxSize())
					.filter(trackedFilament -> trackedFilament.size() >= filteringParameters.getMinSize())
					.filter(trackedFilament -> trackedFilament.insideBbox(bbox))
					.collect(Collectors.toCollection(() -> new TrackedFilaments(context)));
		} else {
			this.filteredTrackedFilaments = this.trackedFilaments;
		}
	}

	public ImagePreprocessors getImagePreprocessor() {
		return imagePreprocessors;
	}

	public Context getContext() {
		return this.context;
	}

}
