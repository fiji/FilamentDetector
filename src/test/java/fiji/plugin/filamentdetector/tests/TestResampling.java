package fiji.plugin.filamentdetector.tests;

import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.Selection;
import ij.plugin.frame.RoiManager;
import ij.process.FloatPolygon;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

public class TestResampling {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		LogService log = ij.log();
		FilamentOverlayService overlayService = ij.get(FilamentOverlayService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/seeds.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		RoiManager rm = RoiManager.getRoiManager();

		float[] xCoords = new float[] { 125, 177, 128, 40, 159 };
		float[] yCoords = new float[] { 287, 238, 199, 139, 107 };
		Roi line = makePolygon(xCoords, yCoords);

		rm.addRoi(line);

		// Resample coordinates so they are all equally spaced.
		double distSpaced = 1;

		rm.runCommand("Show All");
	}

	private static Roi makePolygon(float[] x, float[] y) {
		FloatPolygon polygon = new FloatPolygon(x, y);
		return new PolygonRoi(polygon, Roi.FREELINE);
	}

	private static float getDistance(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}
}
