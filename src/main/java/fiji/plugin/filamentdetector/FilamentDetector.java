package fiji.plugin.filamentdetector;

import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.detection.DetectionParameters;
import fiji.plugin.filamentdetector.detection.Detector;
import fiji.plugin.filamentdetector.detection.FilteringParameters;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.model.Filaments;
import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/*
 * This class holds the necessary informations/models necessary during the use of the GUI.
 * Can be also used for scripting.
 */
public class FilamentDetector {

	@Parameter
	private Context context;

	@Parameter
	private ConvertService convert;

	@Parameter
	private LogService log;

	@Parameter
	private GUIStatusService status;

	private ImageDisplay imageDisplay;
	private Calibrations calibrations;

	private DetectionParameters detectionParameters;

	private Detector detector;
	private Filaments filaments;
	private Filaments filteredFilaments;

	public FilamentDetector(Context context, ImageDisplay imd) {
		context.inject(this);
		this.imageDisplay = imd;
		this.filaments = new Filaments();
		this.filteredFilaments = this.filaments;
	}

	public void initialize() throws Exception {
		// Check image is 8-bit
		if (getDataset().getType().getClass() != UnsignedByteType.class) {
			throw new Exception("Please convert the image to 8-bit first.");
		}

		// Get physical pixel sizes (um) and duration between frames (s)
		calibrations = new Calibrations(context, getDataset());
	}

	public void initDetection() {
		detectionParameters = new DetectionParameters();
		detector = new Detector(context, imageDisplay, detectionParameters);
	}

	public void detectCurrentFrame() {
		detectCurrentFrame(true);
	}

	public void detectCurrentFrame(boolean simplifyFilaments) {
		detector.detectCurrentFrame(calibrations.getChannelToUseIndex());
		if (simplifyFilaments) {
			detector.simplify();
		}
		this.filaments = detector.getFilaments();
		this.filteredFilaments = this.filaments;
	}

	public void detect() {
		detect(true);
	}

	public void detect(boolean simplifyFilaments) {
		detector.detect(calibrations.getChannelToUseIndex());
		if (simplifyFilaments) {
			detector.simplify();
		}
		this.filaments = detector.getFilaments();
		this.filteredFilaments = this.filaments;
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

	public void setDetectionParameters(DetectionParameters detectionParameters) {
		this.detectionParameters = detectionParameters;
	}

	public Filaments getFilaments() {
		return filteredFilaments;
	}

	public void filterFilament(FilteringParameters filteringParameters) {
		status.showStatus("Filtering filaments with the following parameters : ");
		status.showStatus(filteringParameters.toString());

		this.filteredFilaments = this.filaments.stream()
				.filter(filament -> filament.getLength() < filteringParameters.getMaxLength())
				.filter(filament -> filament.getLength() > filteringParameters.getMinLength())
				.filter(filament -> filament.getSinuosity() < filteringParameters.getMaxSinuosity())
				.filter(filament -> filament.getSinuosity() > filteringParameters.getMinSinuosity())
				.collect(Collectors.toCollection(Filaments::new));

		status.showStatus(this.filaments.size() - this.filteredFilaments.size() + " / " + this.filaments.size()
				+ " filaments have been removed by the filters.");
	}

}
