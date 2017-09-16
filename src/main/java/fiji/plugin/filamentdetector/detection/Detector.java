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
import net.imagej.Dataset;

public class Detector {

	@Parameter
	ConvertService convertService;

	@Parameter
	LogService log;

	private Dataset image;
	private DetectionParameters params;

	private LineDetector lineDetector;

	private ImagePlus imp;

	private Filaments filaments;

	public Detector(Context context, Dataset image) {
		new Detector(context, image, new DetectionParameters());
	}

	public Detector(Context context, Dataset image, DetectionParameters params) {
		context.inject(this);
		this.image = image;
		this.params = params;
		this.lineDetector = new LineDetector();

		// Convert Dataset to IJ1 ImagePlus and ImageProcessor
		this.imp = convertService.convert(this.image, ImagePlus.class);
	}

	public void detect() {

		this.filaments = new Filaments();
		int currentFrame = this.imp.getFrame();

		for (int frame = 1; frame < this.imp.getNFrames() + 1; frame++) {
			this.detectFrame(frame);
		}

		this.imp.setT(currentFrame);

	}

	public void detectFrame(int frame) {

		if (this.filaments == null) {
			this.filaments = new Filaments();
		}

		this.imp.setT(frame);
		ImageProcessor ip = this.imp.getProcessor();

		// Detect lines
		Lines lines = this.lineDetector.detectLines(ip, this.params.getSigma(), this.params.getUpperThresh(),
				this.params.getLowerThresh(), this.params.getMinLength(), this.params.getMaxLength(),
				this.params.isDarkLine(), this.params.isDoCorrectPosition(), this.params.isDoEstimateWidth(),
				this.params.isDoExtendLine(), this.params.getOverlapOption());

		for (Line line : lines) {
			this.filaments.add(new Filament(line, frame));
		}

	}

	public Filaments getFilaments() {
		return this.filaments;
	}
}
