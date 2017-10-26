package fiji.plugin.filamentdetector.tests;

import java.util.Arrays;
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

public class TestIntensityProfile2 {

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

		// RealPoint start = new RealPoint(122, 80);
		// RealPoint end = new RealPoint(154, 67);

		RealPoint start = new RealPoint(158, 7);
		RealPoint end = new RealPoint(193, 29);

		List<RealPoint> points = Arrays.asList(start, end);
		int frame = 13;
		int channel = 0;

		overlay.setImageDisplay(imd);
		imp.setT(frame);

		List<RealPoint> line = GeometryUtils.getLinePointsFromSpacing(start, end, 1);
		log.info(line);
		Filament filament = new Filament(line, frame);
		overlay.add(filament);

		double[] intensities = GeometryUtils.getIntensities(line, dataset, frame, channel, 0);
		GeometryUtils.plotPoints(intensities);

	}
}
