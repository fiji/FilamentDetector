package fiji.plugin.filamentdetector.analyzer.tipfitter;

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
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import ij.gui.Plot;
import net.imagej.display.ImageDisplay;

public class FilamentTipFitter {

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	private TrackedFilaments seeds;
	private TrackedFilaments trackedFilaments;
	private ImageDisplay imageDisplay;

	private int polynomDegree = 5;
	private double relativePositionFromEnd = 0.5;
	private double lineFitLength = 20;
	private double channelIndex = 0;
	private double lineWidth = 4;

	public FilamentTipFitter(Context context, TrackedFilaments seeds, ImageDisplay imageDisplay) {
		context.inject(this);
		this.seeds = seeds;
		this.imageDisplay = imageDisplay;
	}

	public void fit() {
		this.trackedFilaments = new TrackedFilaments();
		TrackedFilament trackedFilament;
		Filament filament;

		for (TrackedFilament seed : this.seeds) {
			trackedFilament = new TrackedFilament();

			for (Filament singleSeed : seed) {
				// Fit from one side
				filament = this.fitSeed(singleSeed, true);
				trackedFilament.add(filament);

				// Fit the other side
				filament = this.fitSeed(singleSeed, false);
				trackedFilament.add(filament);
			}

			trackedFilaments.add(trackedFilament);
			trackedFilament.setColor(seed.getColor());
		}
	}

	private Filament fitSeed(Filament filament, boolean fitFromStart) {

		float[] seedTip;
		float[] otherTip;
		if (fitFromStart) {
			seedTip = new float[] { (float) filament.getTips()[0], (float) filament.getTips()[1] };
			otherTip = new float[] { (float) filament.getTips()[2], (float) filament.getTips()[3] };
		} else {
			seedTip = new float[] { (float) filament.getTips()[2], (float) filament.getTips()[3] };
			otherTip = new float[] { (float) filament.getTips()[0], (float) filament.getTips()[1] };
		}

		float[] fitEnd = FilamentTipFitter.getPointOnVectorFromDistance(seedTip, otherTip, -lineFitLength);

		float[] x = new float[] { seedTip[0], fitEnd[0] };
		float[] y = new float[] { seedTip[1], fitEnd[1] };
		Filament fitLine = new Filament(x, y, filament.getFrame());

		// Get intensities
		double[] intensities = fitLine.getIntensitiesAsArray(context, this.imageDisplay, this.channelIndex,
				this.lineWidth);
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

		// Get the position of the tip in the image 2D space
		double[] tipPosition = FilamentTipFitter.getPointOnVectorFromDistance(fitLine.getStartTipAsArray(),
				fitLine.getEndTipAsArray(), tipPosition1D);

		// Only for debugging
		boolean debug = false;
		if (debug) {
			Plot plot = new Plot("", "", "", positions, Arrays.stream(positions).map(i -> func.value(i)).toArray());
			plot.addPoints(positions, intensities, 1);
			plot.show();

			Plot plot2 = new Plot("", "", "", positions,
					Arrays.stream(positions).map(i -> func.polynomialDerivative().value(i)).toArray());
			plot2.show();
		}

		x = new float[] { seedTip[0], (float) tipPosition[0] };
		y = new float[] { seedTip[1], (float) tipPosition[1] };
		Filament trueFilament = new Filament(x, y, filament.getFrame());
		trueFilament.setColor(filament.getColor());

		return trueFilament;

	}

	public TrackedFilaments getSeeds() {
		return seeds;
	}

	public TrackedFilaments getFilaments() {
		return trackedFilaments;
	}

	public int getPolynomDegree() {
		return polynomDegree;
	}

	public void setPolynomDegree(int polynomDegree) {
		this.polynomDegree = polynomDegree;
	}

	public double getRelativePositionFromEnd() {
		return relativePositionFromEnd;
	}

	public void setRelativePositionFromEnd(double relativePositionFromEnd) {
		this.relativePositionFromEnd = relativePositionFromEnd;
	}

	public double getLineFitLength() {
		return lineFitLength;
	}

	public void setLineFitLength(double lineFitLength) {
		this.lineFitLength = lineFitLength;
	}

	public ImageDisplay getImageDisplay() {
		return imageDisplay;
	}

	public double getChannelIndex() {
		return channelIndex;
	}

	public void setChannelIndex(double channelIndex) {
		this.channelIndex = channelIndex;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}

	static private float[] getPointOnVectorFromDistance(float[] start, float[] end, double distance) {
		float distRatio = (float) (distance
				/ Math.sqrt(Math.pow(start[0] - end[0], 2) + Math.pow(start[1] - end[1], 2)));
		float x = (float) ((1 - distRatio) * start[0] + distRatio * end[0]);
		float y = (float) ((1 - distRatio) * start[1] + distRatio * end[1]);
		return new float[] { x, y };
	}

	static private double[] getPointOnVectorFromDistance(double[] start, double[] end, double distance) {
		double distRatio = (double) (distance
				/ Math.sqrt(Math.pow(start[0] - end[0], 2) + Math.pow(start[1] - end[1], 2)));
		double x = (double) ((1 - distRatio) * start[0] + distRatio * end[0]);
		double y = (double) ((1 - distRatio) * start[1] + distRatio * end[1]);
		return new double[] { x, y };
	}

}
