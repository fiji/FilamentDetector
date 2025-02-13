/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2025 Fiji developers.
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
package sc.fiji.filamentdetector.tracking;

import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_ALLOW_GAP_CLOSING;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_ALLOW_TRACK_MERGING;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_ALLOW_TRACK_SPLITTING;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_ALTERNATIVE_LINKING_COST_FACTOR;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_CUTOFF_PERCENTILE;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_GAP_CLOSING_FEATURE_PENALTIES;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_GAP_CLOSING_MAX_DISTANCE;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_GAP_CLOSING_MAX_FRAME_GAP;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_MERGING_FEATURE_PENALTIES;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_MERGING_MAX_DISTANCE;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_SPLITTING_FEATURE_PENALTIES;
import static fiji.plugin.trackmate.tracking.TrackerKeys.KEY_SPLITTING_MAX_DISTANCE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.tracking.jaqaman.JaqamanLinker;
import fiji.plugin.trackmate.tracking.jaqaman.costfunction.CostFunction;
import fiji.plugin.trackmate.tracking.jaqaman.costmatrix.JaqamanLinkingCostMatrixCreator;
import sc.fiji.filamentdetector.model.Filament;
import sc.fiji.filamentdetector.model.TrackedFilaments;
import sc.fiji.filamentdetector.tracking.lap.BoundingBoxOverlapCostFunction;
import sc.fiji.filamentdetector.tracking.lap.JaqamanFilamentSegmentCostMatrixCreator;

@Plugin(type = FilamentsTracker.class, priority = Priority.HIGH)
public class BBoxLAPFilamentsTracker extends AbstractFilamentsTracker {

	private static String NAME = "BoundingBox LAP Tracker";

	@Parameter
	private LogService log;

	@Parameter
	private Context context;

	private double costThreshold = 1.1;
	private double maxFrameGap = 15;
	private boolean interpolateFilaments = true;

	public BBoxLAPFilamentsTracker() {
		super();
		setName(NAME);
	}

	@Override
	public void track() {

		// TODO: implement multithreading

		SimpleWeightedGraph<Filament, DefaultWeightedEdge> graph = trackFrameToFrame();
		graph = trackGapMergeSplit(graph);

		setTrackedFilaments(new TrackedFilaments(context));
		getTrackedFilaments().buildTracks(graph);

		if (this.isInterpolateFilaments()) {
			interpolateFilaments();
		}

	}

	/*
	 * 1. Frame to frame linking.
	 */
	private SimpleWeightedGraph<Filament, DefaultWeightedEdge> trackFrameToFrame() {

		// Group filaments by frames in a map
		Map<Integer, List<Filament>> sortedFilaments = getFilaments().stream()
				.collect(Collectors.groupingBy(Filament::getFrame));

		// Get the sorted list of frames that contains filaments
		List<Integer> frames = sortedFilaments.keySet().stream().sorted().collect(Collectors.toList());

		// Prepare cost function
		CostFunction<Filament, Filament> costFunction = new BoundingBoxOverlapCostFunction();

		// Initialize variables

		double alternativeCostFactor = 1.1;
		double percentile = 1;

		JaqamanLinkingCostMatrixCreator<Filament, Filament> creator;
		JaqamanLinker<Filament, Filament> linker;

		List<Filament> sources;
		List<Filament> targets;
		// int deltaFrames = 0;
		int frameSource;
		int frameTarget;

		SimpleWeightedGraph<Filament, DefaultWeightedEdge> graph;
		graph = new SimpleWeightedGraph<Filament, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		DefaultWeightedEdge edge;
		double cost;

		// Iterate over all the frames two by two
		for (int i = 0; i < frames.size() - 1; i++) {

			frameSource = frames.get(i);
			frameTarget = frames.get(i + 1);

			sources = sortedFilaments.get(frameSource);
			targets = sortedFilaments.get(frameTarget);
			// deltaFrames = frameTarget - frameSource;

			// Build the matrix
			creator = new JaqamanLinkingCostMatrixCreator<Filament, Filament>(sources, targets, costFunction,
					this.getCostThreshold(), alternativeCostFactor, percentile);
			linker = new JaqamanLinker<Filament, Filament>(creator);

			// Solve it
			if (!linker.checkInput() || !linker.process()) {
				log.error("At frame " + frameSource + " to " + frameTarget + ": " + linker.getErrorMessage());
			}

			// Process results
			Map<Filament, Double> costs = linker.getAssignmentCosts();
			Map<Filament, Filament> assignment = linker.getResult();

			for (final Filament source : assignment.keySet()) {
				cost = costs.get(source);
				Filament target = assignment.get(source);
				// trackedFilaments.addLink(source, target);

				graph.addVertex(source);
				graph.addVertex(target);
				edge = graph.addEdge(source, target);
				graph.setEdgeWeight(edge, cost);
			}

		}
		return graph;
	}

	/*
	 * 2. Gap-closing, merging and splitting.
	 */
	private SimpleWeightedGraph<Filament, DefaultWeightedEdge> trackGapMergeSplit(
			SimpleWeightedGraph<Filament, DefaultWeightedEdge> graph) {

		final Map<String, Object> settings = new HashMap<String, Object>();

		settings.put(KEY_ALLOW_GAP_CLOSING, true);
		settings.put(KEY_GAP_CLOSING_FEATURE_PENALTIES, new HashMap<String, Double>());
		settings.put(KEY_GAP_CLOSING_MAX_DISTANCE, 1000.0);
		settings.put(KEY_GAP_CLOSING_MAX_FRAME_GAP, (int) this.getMaxFrameGap());

		settings.put(KEY_ALLOW_TRACK_SPLITTING, false);
		settings.put(KEY_SPLITTING_FEATURE_PENALTIES, new HashMap<String, Double>());
		settings.put(KEY_SPLITTING_MAX_DISTANCE, 15.0);

		settings.put(KEY_ALLOW_TRACK_MERGING, false);
		settings.put(KEY_MERGING_FEATURE_PENALTIES, new HashMap<String, Double>());
		settings.put(KEY_MERGING_MAX_DISTANCE, 15.0);

		settings.put(KEY_ALTERNATIVE_LINKING_COST_FACTOR, 2000.0);
		settings.put(KEY_CUTOFF_PERCENTILE, 1.0);

		final JaqamanFilamentSegmentCostMatrixCreator costMatrixCreator = new JaqamanFilamentSegmentCostMatrixCreator(
				graph, settings);
		final JaqamanLinker<Filament, Filament> linker = new JaqamanLinker<Filament, Filament>(costMatrixCreator);

		if (!linker.checkInput() || !linker.process()) {
			log.error(linker.getErrorMessage());
		}

		final Map<Filament, Filament> assignment = linker.getResult();
		final Map<Filament, Double> costs = linker.getAssignmentCosts();

		for (final Filament source : assignment.keySet()) {
			final Filament target = assignment.get(source);
			final DefaultWeightedEdge edge = graph.addEdge(source, target);

			final double cost = costs.get(source);
			graph.setEdgeWeight(edge, cost);
		}

		return graph;
	}

	public void interpolateFilaments() {
		// double maxFrames;
		// Filament filament;
		// Filament filamentBefore;
		// Filament filamentAfter;
		// int nFrames;
		// float[] x;
		// float[] y;
		// int divider;
		// int indexAfter;
		//
		// for (TrackedFilament trackedFilament : trackedFilaments) {
		// maxFrames = trackedFilament.get(trackedFilament.size() - 1).getFrame();
		//
		// for (int frame = 0; frame < maxFrames; frame++) {
		//
		// filament = trackedFilament.getFilamentByFrame(frame);
		// if (filament == null) {
		// filamentBefore = trackedFilament.getFilamentByFrame(frame - 1);
		//
		// if (filamentBefore != null) {
		//
		// indexAfter = trackedFilament.indexOf(filamentBefore) + 1;
		// if (indexAfter >= trackedFilament.size()) {
		// filamentAfter = trackedFilament.get(indexAfter);
		// if (filamentAfter != null) {
		// nFrames = filamentAfter.getFrame() - filamentBefore.getFrame() - 1;
		// System.out.println(nFrames);
		//
		// x = new float[2];
		// y = new float[2];
		//
		// divider = nFrames - 1 + 2;
		//
		// x[0] = (float) (filamentBefore.getTips()[0] + filamentAfter.getTips()[0]) /
		// divider;
		// y[0] = (float) (filamentBefore.getTips()[1] + filamentAfter.getTips()[1]) /
		// divider;
		// x[1] = (float) (filamentBefore.getTips()[2] + filamentAfter.getTips()[2]) /
		// divider;
		// y[1] = (float) (filamentBefore.getTips()[3] + filamentAfter.getTips()[3]) /
		// divider;
		//
		// filament = new Filament(x, y, frame);
		// trackedFilament.add(filament);
		// filament.setColor(trackedFilament.getColor());
		//
		// trackedFilament = trackedFilament.stream()
		// .sorted((f1, f2) -> Double.compare(f1.getFrame(), f2.getFrame()))
		// .collect(Collectors.toCollection(TrackedFilament::new));
		// }
		// }
		// }
		//
		// }
		// }
		//
		// }
	}

	public double getCostThreshold() {
		return costThreshold;
	}

	public void setCostThreshold(double costThreshold) {
		this.costThreshold = costThreshold;
	}

	public double getMaxFrameGap() {
		return maxFrameGap;
	}

	public void setMaxFrameGap(double maxFrameGap) {
		this.maxFrameGap = maxFrameGap;
	}

	public boolean isInterpolateFilaments() {
		return interpolateFilaments;
	}

	public void setInterpolateFilaments(boolean interpolateFilaments) {
		this.interpolateFilaments = interpolateFilaments;
	}

	@Override
	public String toString() {
		String out = "";
		out += "Tracker Name : " + getName() + "\n";
		out += "Cost Threshold = " + costThreshold + "\n";
		out += "Maximum Frame Gap = " + maxFrameGap + "\n";
		out += "Interpolate filaments = " + interpolateFilaments + "\n";
		return out;
	}

}
