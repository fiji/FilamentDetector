package fiji.plugin.filamentdetector.tests;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.solvers.LaguerreSolver;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.scijava.Context;
import org.scijava.log.LogService;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

public class TestIntensityProfile {

	public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		FilamentOverlayService overlay = ij.get(FilamentOverlayService.class);
		LogService log = ij.get(LogService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/7,5uM_emccd_lapse1-small-8bit.tif";
		Dataset dataset = ij.dataset().open(fpath);
		ij.ui().show(dataset);

		ImageDisplay imd = ij.imageDisplay().getActiveImageDisplay();

		double[] start = new double[] { 122, 80 };
		double[] end = new double[] { 154, 67 };
		// double[] start = new double[] { 22, 36 };
		// double[] end = new double[] { 13, 21 };

		double[] x = new double[] { start[0], end[0] };
		double[] y = new double[] { start[1], end[1] };
		Filament filament = new Filament(x, y, 13);

		overlay.setImageDisplay(imd);
		overlay.add(filament);

		/*----------------------------*/

		// Parameters
		int polynomDegree = 4;
		double relativePositionFromEnd = 0.4;

		// Get intensities
		double[] intensities = filament.getIntensitiesAsArray(context, imd, 0, 4);
		double[] positions = IntStream.range(0, intensities.length).mapToDouble(i -> i).toArray();

		// Build points
		final WeightedObservedPoints points = new WeightedObservedPoints();
		for (int i = 0; i < intensities.length; i++) {
			points.add(i, intensities[i]);
		}

		// Fit to polynomial
		PolynomialCurveFitter fitter = PolynomialCurveFitter.create(polynomDegree);
		double[] coeff = fitter.fit(points.toList());

		// Find roots of the first derivative
		PolynomialFunction func = new PolynomialFunction(coeff);
		LaguerreSolver laguerreSolver = new LaguerreSolver();
		Complex[] roots = laguerreSolver.solveAllComplex(func.polynomialDerivative().getCoefficients(), 0);

		// Find the x values of the roots
		List<Double> xRoots = Arrays.stream(roots).map(v -> v.getReal()).filter(v -> v < intensities.length)
				.filter(v -> v > 0).collect(Collectors.toList());

		// Find the two consecutive roots with the greatest difference
		double[] diff = IntStream.range(0, xRoots.size() - 1).mapToDouble(i -> xRoots.get(i + 1) - xRoots.get(i))
				.toArray();
		int maxIndex = IntStream.range(0, diff.length).reduce((i, j) -> diff[i] < diff[j] ? j : i).getAsInt();

		// Get the start and end position of the half Gaussian estimating the tip
		double startPosition = xRoots.get(maxIndex);
		double endPosition = xRoots.get(maxIndex + 1);

		// Calculate the position of the tip on the 1D line space
		double tipPosition1D = endPosition - ((endPosition - startPosition) / (1 / relativePositionFromEnd));
		log.info(tipPosition1D);

		// Get the position of the tip in the image 2D space
		double distRatio = tipPosition1D / Math.sqrt(Math.pow(start[0] - end[0], 2) + Math.pow(start[1] - end[1], 2));
		double tipX = (1 - distRatio) * start[0] + distRatio * end[0];
		double tipY = (1 - distRatio) * start[1] + distRatio * end[1];

		log.info(tipX);
		log.info(tipY);
	}
}
