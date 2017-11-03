package fiji.plugin.filamentdetector.tests;

import java.util.List;

import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.GeometryUtils;
import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;
import net.imglib2.RealPoint;

public class TestNucleationDetector {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		FilamentOverlayService overlay = ij.get(FilamentOverlayService.class);
		LogService log = ij.get(LogService.class);
		ConvertService convert = ij.get(ConvertService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/7,5uM_emccd_lapse1-small-8bit.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();
		ImagePlus imp = convert.convert(imd, ImagePlus.class);

		// Parameters
		int spacing = 1;
		int frame = 13;
		int channel = 0;
		int distance = 10;
		
		// Setup visualization
		overlay.setImageDisplay(imd);
		imp.setT(frame);	
		
		// Create filament
		RealPoint start = new RealPoint(23, 38);
		RealPoint end = new RealPoint(28, 47);
		List<RealPoint> line = GeometryUtils.getLinePointsFromSpacing(start, end, spacing);
		Filament seed = new Filament(line, frame);
		
		double seedLength = GeometryUtils.distance(start, end);
		
		RealPoint p1 = GeometryUtils.getPointOnVectorFromDistance(start, end, seedLength + distance);
		RealPoint p2 = GeometryUtils.getPointOnVectorFromDistance(end, start, seedLength + distance);
		
		List<RealPoint> line1 = GeometryUtils.getLinePointsFromSpacing(end, p1, spacing);
		List<RealPoint> line2 = GeometryUtils.getLinePointsFromSpacing(start, p2, spacing);
		
		log.info(p1);
		
		Filament filament1 = new Filament(line1, frame);
		Filament filament2 = new Filament(line2, frame);
		
		overlay.add(filament1);
		overlay.add(filament2);
		
		double[] intensities1 = GeometryUtils.getIntensities(line1, dataset, frame, channel, 0);
		double[] intensities2 = GeometryUtils.getIntensities(line2, dataset, frame, channel, 0);
		
		GeometryUtils.plotPoints(intensities1);
		GeometryUtils.plotPoints(intensities2);
		
		Nd4j.create(6);

	}
}