package fiji.plugin.filamentdetector.detection;

import java.awt.Color;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import de.biomedical_imaging.ij.steger.Line;
import de.biomedical_imaging.ij.steger.LineDetector;
import de.biomedical_imaging.ij.steger.Lines;
import fiji.plugin.filamentdetector.event.ImageNotFoundEvent;
import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.overlay.ColorService;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;

public class FilamentsDetector {

	@Parameter
	ConvertService convertService;

	@Parameter
	LogService log;

	@Parameter
	private ColorService colorService;

	@Parameter
	private EventService eventService;

	private ImageDisplay imageDisplay;
	private Dataset dataset;

	private DetectionParameters parameters;

	private LineDetector lineDetector;

	private ImagePlus imp;
	private ImagePlus impData;

	private Filaments filaments;

	public FilamentsDetector(Context context, ImageDisplay imageDisplay, Dataset dataset) {
		new FilamentsDetector(context, imageDisplay, dataset, new DetectionParameters());
	}

	public FilamentsDetector(Context context, ImageDisplay imageDisplay, Dataset dataset, DetectionParameters params) {
		context.inject(this);
		this.imageDisplay = imageDisplay;
		this.dataset = dataset;
		this.parameters = params;
		this.lineDetector = new LineDetector();

		// Convert Dataset to IJ1 ImagePlus and ImageProcessor
		try {
			this.imp = convertService.convert(this.imageDisplay, ImagePlus.class);
			this.impData = convertService.convert(this.dataset, ImagePlus.class);
		} catch (NullPointerException e) {
			eventService.publish(new ImageNotFoundEvent());
		}
	}

	public void detect() {
		detect(0);
	}

	public void detect(int channelIndex) {

		colorService.initialize();
		this.filaments = new Filaments();
		int currentFrame = this.imp.getFrame();
		int currentChannel = this.imp.getChannel();

		this.impData.setC(channelIndex);

		for (int frame = 1; frame < this.impData.getNFrames() + 1; frame++) {
			this.detectFrame(frame);
		}
		this.imp.setT(currentFrame);
		this.imp.setC(currentChannel);

		this.simplify();
	}

	public void detectCurrentFrame() {
		detectCurrentFrame(0);
	}

	public void detectCurrentFrame(int channelIndex) {
		colorService.initialize();

		this.filaments = new Filaments();

		int currentFrame = this.imp.getFrame();
		int currentChannel = this.imp.getChannel();

		this.impData.setC(channelIndex);
		this.detectFrame(currentFrame);

		this.imp.setC(currentChannel);

		this.simplify();
	}

	public void detectFrame(int frame) {

		if (this.filaments == null) {
			this.filaments = new Filaments();
		}

		this.impData.setT(frame);
		ImageProcessor ip = this.impData.getProcessor();

		// Detect lines
		Lines lines = this.lineDetector.detectLines(ip, this.parameters.getSigma(), this.parameters.getUpperThresh(),
				this.parameters.getLowerThresh(), this.parameters.getMinLength(), this.parameters.getMaxLength(),
				this.parameters.isDarkLine(), this.parameters.isDoCorrectPosition(),
				this.parameters.isDoEstimateWidth(), this.parameters.isDoExtendLine(),
				this.parameters.getOverlapOption());

		for (Line line : lines) {
			Filament filament = new Filament(line, frame);

			Color color = colorService.getColor(this.filaments.size() + 1);
			filament.setColor(color);

			this.filaments.add(filament);
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

	private void simplify() {
		if (parameters.isSimplifyFilaments()) {
			filaments = filaments.simplify(parameters.getSimplifyToleranceDistance());

			// Remove filaments with only one point
			filaments = filaments.stream().filter(filament -> filament.getSize() > 1)
					.collect(Collectors.toCollection(Filaments::new));
		}
	}
}
