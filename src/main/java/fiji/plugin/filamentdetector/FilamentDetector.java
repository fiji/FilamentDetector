package fiji.plugin.filamentdetector;

import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.detection.DetectionParameters;
import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/*
 * This class holds the necessary informations/models necessary during the use of the GUI.
 */
public class FilamentDetector {

	@Parameter
	private Context context;

	@Parameter
	private ConvertService convert;

	private ImageDisplay imd;
	private Calibrations calibrations;
	private double channelToUse = 0;

	private DetectionParameters detectionParameters;

	public FilamentDetector(Context context, ImageDisplay imd) {
		context.inject(this);
		this.imd = imd;

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
	}

	public Dataset getDataset() {
		return (Dataset) imd.getActiveView().getData();
	}

	public ImageDisplay getImageDisplay() {
		return imd;
	}

	public ImagePlus getImagePlus() {
		return convert.convert(imd, ImagePlus.class);
	}

	public Calibrations getCalibrations() {
		return calibrations;
	}

	public double getChannelToUse() {
		return channelToUse;
	}

	public void setChannelToUse(double channelToUse) {
		this.channelToUse = channelToUse;
	}

	public DetectionParameters getDetectionParameters() {
		return detectionParameters;
	}

	public void setDetectionParameters(DetectionParameters detectionParameters) {
		this.detectionParameters = detectionParameters;
	}

}
