package fiji.plugin.filamentdetector.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.FilamentWorkflow;
import fiji.plugin.filamentdetector.detection.FilamentDetectorService;
import fiji.plugin.filamentdetector.imagepreprocessor.ImagePreprocessor;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
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

public class TestFilamentWorkflow {

	@Test
	public void testFilamentWorkflow() {
		ImageJ ij = new ImageJ();

		Context context = ij.getContext();
		LogService log = ij.log();
		DatasetService ds = ij.dataset();
		FilamentDetectorService detectorService = ij.get(FilamentDetectorService.class);
		FilamentTrackerService tarckerService = ij.get(FilamentTrackerService.class);

		int width = 100;
		int maxFrame = 10;
		int nLines = 5;

		Img img = getFakeImage(width, maxFrame, nLines);
		Dataset dataset = ds.create(img);
		CalibratedAxis[] caxis = { new DefaultLinearAxis(Axes.X), new DefaultLinearAxis(Axes.Y),
				new DefaultLinearAxis(Axes.TIME) };
		dataset.setAxes(caxis);

		ImageDisplay imd = (ImageDisplay) ij.display().createDisplayQuietly(dataset);

		FilamentWorkflow wf = new FilamentWorkflow(context, imd);
		wf.initialize();
		wf.getCalibrations().setChannelToUseIndex(1);

		// Enable some preprocessors
		ImagePreprocessor proc = wf.getImagePreprocessor().getPreProcessorByName("Convert8BitPreprocessor");
		proc.setDoPreprocess(true);

		// Preprocess the image
		wf.getImagePreprocessor().preprocess();

		// Get processed data and convert it to ImageDisplay
		// TODO: that step should be easier
		Dataset datasetp = wf.getImagePreprocessor().getPreprocessedDataset();
		ImageDisplay imdp = (ImageDisplay) ij.display().createDisplayQuietly(datasetp);
		wf.setImageDisplay(imdp);

		wf.setFilamentDetector(detectorService.getRidgeFilamentDetector());
		wf.getFilamentDetector().setImageDisplay(imdp);
		wf.getFilamentDetector().setDataset(datasetp);
		wf.detect();
		Filaments filaments = wf.getFilaments();

		wf.setFilamentsTracker(tarckerService.getBBoxTracker());
		wf.track();
		TrackedFilaments trackedFilaments = wf.getTrackedFilaments();

		log.info(filaments.size());
		log.info(trackedFilaments.size());
		assertEquals(filaments.size(), nLines * maxFrame);
		assertEquals(trackedFilaments.size(), nLines);

	}

	private static <T extends RealType<T>> Img<T> getFakeImage(int width, int maxFrame, int nLines) {

		ImagePlus imp = NewImage.createShortImage("Source", width, width, maxFrame, NewImage.FILL_BLACK);
		ImageProcessor ip;

		for (int i = 0; i < nLines; i++) {
			for (int frame = 0; frame < maxFrame; frame++) {
				int ox1 = (2 * i + 1) * width / nLines / 2;
				int ox2 = (2 * i + 2) * width / nLines / 2;
				int oy1 = (width / nLines) + (frame * 6);
				int oy2 = (2 * width / nLines) + (frame * 6);

				imp.setSlice(frame + 1);
				ip = imp.getProcessor();
				ip.setColor(5000);
				ip.setLineWidth(3);
				ip.drawLine(ox1, oy1, ox2, oy2);
			}
		}
		return (Img<T>) ImageJFunctions.wrap(imp);
	}

}
