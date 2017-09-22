package fiji.plugin.filamentdetector.detection;

import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.Filaments;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import net.imagej.display.ImageDisplay;

public class Detector {

	@Parameter
	ConvertService convertService;

	@Parameter
	LogService log;

	private ImageDisplay imageDisplay;
	private DetectionParameters parameters;

	private LineDetector lineDetector;

	private ImagePlus imp;

	private Filaments filaments;

	public Detector(Context context, ImageDisplay imageDisplay) {
		new Detector(context, imageDisplay, new DetectionParameters());
	}

	public Detector(Context context, ImageDisplay imageDisplay, DetectionParameters params) {
		context.inject(this);
		this.imageDisplay = imageDisplay;
		this.parameters = params;
		this.lineDetector = new LineDetector();

		// Convert Dataset to IJ1 ImagePlus and ImageProcessor
		this.imp = convertService.convert(this.imageDisplay, ImagePlus.class);
	}

	public void detect() {

		this.filaments = new Filaments();
		int currentFrame = this.imp.getFrame();

		for (int frame = 1; frame < this.imp.getNFrames() + 1; frame++) {
			this.detectFrame(frame);
		}

		this.imp.setT(currentFrame);

	}

	public void detectCurrentFrame() {
		this.filaments = new Filaments();
		int currentFrame = this.imp.getFrame();
		this.detectFrame(currentFrame);
	}

	public void detectFrame(int frame) {

		if (this.filaments == null) {
			this.filaments = new Filaments();
		}

		this.imp.setT(frame);
		ImageProcessor ip = this.imp.getProcessor();

		// Detect lines
		Lines lines = this.lineDetector.detectLines(ip, this.parameters.getSigma(), this.parameters.getUpperThresh(),
				this.parameters.getLowerThresh(), this.parameters.getMinLength(), this.parameters.getMaxLength(),
				this.parameters.isDarkLine(), this.parameters.isDoCorrectPosition(),
				this.parameters.isDoEstimateWidth(), this.parameters.isDoExtendLine(),
				this.parameters.getOverlapOption());
		
		for (Line line : lines) {
			this.filaments.add(new Filament(line, frame));
		}

	}

	public Filaments getFilaments() {
		return this.filaments;
	}

	public DetectionParameters getParameters() {
		return parameters;
	}

	public void setParameters(DetectionParameters parameters) {
		this.parameters = parameters;
	}
}
