/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2017 Hadrien Mary
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package fiji.plugin.filamentdetector.analyzer.tipfitter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import fiji.plugin.filamentdetector.GeometryUtils;
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
	private Map<TrackedFilament, TrackedFilament> side1Filaments;
	private Map<TrackedFilament, TrackedFilament> side2Filaments;
	private ImageDisplay imageDisplay;

	private int polynomDegree = 5;
	private double relativePositionFromEnd = 0.5;
	private double lineFitLength = 20;
	private int channelIndex = 0;
	private double lineWidth = 4;

	public FilamentTipFitter(Context context) {
		context.inject(this);
	}

	public void fit() {
		this.side1Filaments = new HashMap<>();
		this.side2Filaments = new HashMap<>();

		TrackedFilament side1Filament;
		TrackedFilament side2Filament;
		Filament filament;

		for (TrackedFilament seed : this.seeds) {
			side1Filament = new TrackedFilament();
			side2Filament = new TrackedFilament();

			for (Filament singleSeed : seed) {
				// Fit the side 1
				filament = this.fitSeed(singleSeed, true);
				if (filament != null) {
					side1Filament.add(filament);
				}

				// Fit the side 2
				filament = this.fitSeed(singleSeed, false);
				if (filament != null) {
					side2Filament.add(filament);
				}
			}

			if (side1Filament.size() > 0) {
				side1Filament.setColor(seed.getColor());
				this.side1Filaments.put(seed, side1Filament);
			}
			if (side2Filament.size() > 0) {
				side2Filament.setColor(seed.getColor());
				this.side2Filaments.put(seed, side2Filament);
			}
		}
	}

	private Filament fitSeed(Filament filament, boolean fitFromStart) {

		double[] seedTip;
		double[] otherTip;
		if (fitFromStart) {
			seedTip = new double[] { filament.getTips()[0], filament.getTips()[1] };
			otherTip = new double[] { filament.getTips()[2], filament.getTips()[3] };
		} else {
			seedTip = new double[] { filament.getTips()[2], filament.getTips()[3] };
			otherTip = new double[] { filament.getTips()[0], filament.getTips()[1] };
		}

		double[] fitEnd = GeometryUtils.getPointOnVectorFromDistance(seedTip, otherTip, -lineFitLength);

		double[] x = new double[] { seedTip[0], fitEnd[0] };
		double[] y = new double[] { seedTip[1], fitEnd[1] };
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

		if (diff.length == 0) {
			return null;
		}

		int maxIndex = IntStream.range(0, diff.length).reduce((i, j) -> diff[i] < diff[j] ? j : i).getAsInt();

		// Get the start and end position of the half Gaussian estimating the tip
		double startPosition = xRoots.get(maxIndex);
		double endPosition = xRoots.get(maxIndex + 1);

		// Calculate the position of the tip on the 1D line space
		double tipPosition1D = endPosition - ((endPosition - startPosition) / (1 / relativePositionFromEnd));

		// Get the position of the tip in the image 2D space
		double[] tipPosition = GeometryUtils.getPointOnVectorFromDistance(fitLine.getStartTipAsArray(),
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

		x = new double[] { seedTip[0], tipPosition[0] };
		y = new double[] { seedTip[1], tipPosition[1] };
		Filament trueFilament = new Filament(x, y, filament.getFrame());
		trueFilament.setColor(filament.getColor());

		return trueFilament;
	}

	public TrackedFilaments getSeeds() {
		return seeds;
	}

	public Map<TrackedFilament, TrackedFilament> getSide1Filaments() {
		return side1Filaments;
	}

	public Map<TrackedFilament, TrackedFilament> getSide2Filaments() {
		return side2Filaments;
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

	public int getChannelIndex() {
		return channelIndex;
	}

	public void setChannelIndex(int channelIndex) {
		this.channelIndex = channelIndex;
	}

	public double getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(double lineWidth) {
		this.lineWidth = lineWidth;
	}

	public void setSeeds(TrackedFilaments seeds) {
		this.seeds = seeds;
	}

	public void setImageDisplay(ImageDisplay imageDisplay) {
		this.imageDisplay = imageDisplay;
	}

}
