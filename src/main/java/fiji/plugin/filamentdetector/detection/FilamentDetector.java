package fiji.plugin.filamentdetector.detection;

import org.scijava.Named;

import fiji.plugin.filamentdetector.model.Filaments;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;

public interface FilamentDetector extends Named {

	void detect();

	void detect(int channelIndex);

	void detectCurrentFrame();

	void detectCurrentFrame(int channelIndex);

	void detectFrame(int frame);

	Filaments getFilaments();

	void setFilaments(Filaments filaments);

	ImageDisplay getImageDisplay();

	void setImageDisplay(ImageDisplay imageDisplay);

	Dataset getDataset();

	void setDataset(Dataset dataset);

	boolean isDetectOnlyCurrentFrame();

	void setDetectOnlyCurrentFrame(boolean detectOnlyCurrentFrame);

	boolean isSimplifyFilaments();

	void setSimplifyFilaments(boolean simplifyFilaments);

	double getSimplifyToleranceDistance();

	void setSimplifyToleranceDistance(double simplifyToleranceDistance);

	@Override
	String toString();

}