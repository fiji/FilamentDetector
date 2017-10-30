package fiji.plugin.filamentdetector.detection;

import fiji.plugin.filamentdetector.model.Filaments;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;

public abstract class AbstractFilamentDetector implements FilamentDetector {

	private String name;

	private boolean simplifyFilaments = true;
	private double simplifyToleranceDistance = 5;
	private boolean detectOnlyCurrentFrame = false;

	private ImageDisplay imageDisplay;
	private Dataset dataset;

	private Filaments filaments;

	@Override
	public void setDetectOnlyCurrentFrame(boolean detectOnlyCurrentFrame) {
		this.detectOnlyCurrentFrame = detectOnlyCurrentFrame;
	}

	@Override
	public boolean isSimplifyFilaments() {
		return simplifyFilaments;
	}

	@Override
	public void setSimplifyFilaments(boolean simplifyFilaments) {
		this.simplifyFilaments = simplifyFilaments;
	}

	@Override
	public double getSimplifyToleranceDistance() {
		return simplifyToleranceDistance;
	}

	@Override
	public void setSimplifyToleranceDistance(double simplifyToleranceDistance) {
		this.simplifyToleranceDistance = simplifyToleranceDistance;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	@Override
	public void setFilaments(Filaments filaments) {
		this.filaments = filaments;
	}

	@Override
	public Filaments getFilaments() {
		return this.filaments;
	}

	@Override
	public ImageDisplay getImageDisplay() {
		return imageDisplay;
	}

	@Override
	public void setImageDisplay(ImageDisplay imageDisplay) {
		this.imageDisplay = imageDisplay;
	}

	@Override
	public Dataset getDataset() {
		return dataset;
	}

	public boolean isDetectOnlyCurrentFrame() {
		return detectOnlyCurrentFrame;
	}

}
