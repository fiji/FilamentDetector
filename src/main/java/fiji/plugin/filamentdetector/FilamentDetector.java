package fiji.plugin.filamentdetector;

import org.jfree.util.Log;
import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.detection.DetectionParameters;
import fiji.plugin.filamentdetector.detection.Detector;
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

	private ImageDisplay imageDisplay;
	private Calibrations calibrations;

	private DetectionParameters detectionParameters;

	private Detector detector;
	private Filaments filaments;

	public FilamentDetector(Context context, ImageDisplay imd) {
		context.inject(this);
		this.imageDisplay = imd;
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
		detector.detectCurrentFrame(calibrations.getChannelToUseIndex());
		this.filaments = detector.getFilaments();
	}

	public void detect() {
		detector.detect(calibrations.getChannelToUseIndex());
		this.filaments = detector.getFilaments();
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
		return filaments;
	}

}
