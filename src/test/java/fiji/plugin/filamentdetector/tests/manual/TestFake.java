package fiji.plugin.filamentdetector.tests.manual;

import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.FilamentWorkflow;
import fiji.plugin.filamentdetector.detection.FilamentDetectorService;
import fiji.plugin.filamentdetector.detection.RidgeDetectionFilamentDetector;
import fiji.plugin.filamentdetector.imagepreprocessor.ImagePreprocessor;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import fiji.plugin.filamentdetector.overlay.ImageDisplayMode;
import fiji.plugin.filamentdetector.tracking.BBoxLAPFilamentsTracker;
import fiji.plugin.filamentdetector.tracking.FilamentTrackerService;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageProcessor;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.axis.Axes;
import net.imagej.axis.CalibratedAxis;
import net.imagej.axis.DefaultLinearAxis;
import net.imagej.display.ImageDisplay;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;

public class TestFake {

	public static void main(final String... args) throws Exception {
		ImageJ ij = net.imagej.Main.launch(args);
		
		Context context = ij.getContext();
		LogService log = ij.log();
		DatasetService ds = ij.dataset();
		FilamentOverlayService overlay = ij.get(FilamentOverlayService.class);
		FilamentDetectorService detectorService = ij.get(FilamentDetectorService.class);
		FilamentTrackerService tarckerService = ij.get(FilamentTrackerService.class);

		Img img = getFakeImage();
		Dataset dataset = ds.create(img);
		CalibratedAxis[] caxis = { new DefaultLinearAxis(Axes.X), new DefaultLinearAxis(Axes.Y),
				new DefaultLinearAxis(Axes.TIME) };
		dataset.setAxes(caxis);

		ij.ui().show(dataset);
		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		FilamentWorkflow wf = new FilamentWorkflow(context, imd);
		wf.initialize();
		wf.getCalibrations().setChannelToUseIndex(1);

		// Enable some preprocessors
		ImagePreprocessor proc = wf.getImagePreprocessor()
				.getPreProcessorByName("Convert8BitPreprocessor");
		proc.setDoPreprocess(true);

		// Preprocess the image
		wf.getImagePreprocessor().preprocess();

		// Get processed data and convert it to ImageDisplay
		// TODO: that step should be easier
		Dataset datasetp = wf.getImagePreprocessor().getPreprocessedDataset();
		ij.ui().show(datasetp);
		ImageDisplay imdp = ij.imageDisplay().getImageDisplays().stream()
				.filter(i -> ((Dataset) i.getActiveView().getData()).equals(datasetp)).findFirst().orElse(null);
		wf.setImageDisplay(imdp);

		overlay.initialize();
		overlay.setImageDisplay(imdp);
		overlay.setViewMode(ImageDisplayMode.COMPOSITE);

		RidgeDetectionFilamentDetector detector = detectorService.getRidgeFilamentDetector();
		wf.setFilamentDetector(detector);
		detector.setImageDisplay(imdp);
		detector.setDataset(datasetp);
		detector.setLineWidth(2.5);

		wf.detect();
		Filaments filaments = wf.getFilaments();

		overlay.add(filaments);

		BBoxLAPFilamentsTracker tracker = tarckerService.getBBoxTracker();
		wf.setFilamentsTracker(tracker);
		wf.track();
		TrackedFilaments trackedFilaments = wf.getTrackedFilaments();

		log.info(trackedFilaments.size());

		overlay.reset();
		overlay.setDrawPlusTips(true);
		overlay.add(trackedFilaments);

		log.info("Done");
	}

	private static <T extends RealType<T>> Img<T> getFakeImage() {

		int width = 200;
		int maxFrame = 20;
		ImagePlus imp = NewImage.createShortImage("Source", width, width, maxFrame, NewImage.FILL_BLACK);
		ImageProcessor ip;

		int nLines = 10;
		for (int frame = 0; frame < maxFrame; frame++) {
			for (int i = 0; i < nLines - 1; i++) {

				int ox1 = (2 * i + 1) * width / nLines / 2;
				int ox2 = (2 * i + 2) * width / nLines / 2;
				int oy1 = (width / nLines) + (frame * 6);
				int oy2 = (2 * width / nLines) + (frame * 6);

				imp.setSlice(frame + 1);
				ip = imp.getProcessor();
				ip.setColor(5000);
				ip.setLineWidth(3);
				ip.drawLine(ox1, oy1, ox2, oy2);
				ip.noise(500);
			}
		}
		return (Img<T>) ImageJFunctions.wrap(imp);
	}
}
