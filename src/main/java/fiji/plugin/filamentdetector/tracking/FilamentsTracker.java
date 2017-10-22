package fiji.plugin.filamentdetector.tracking;

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
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilament;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import fiji.plugin.filamentdetector.tracking.lap.BoundingBoxOverlapCostFunction;
import fiji.plugin.filamentdetector.tracking.lap.JaqamanFilamentSegmentCostMatrixCreator;
import fiji.plugin.trackmate.tracking.sparselap.costfunction.CostFunction;
import fiji.plugin.trackmate.tracking.sparselap.costmatrix.JaqamanLinkingCostMatrixCreator;
import fiji.plugin.trackmate.tracking.sparselap.linker.JaqamanLinker;

public class FilamentsTracker {

	@Parameter
	private LogService log;

	@Parameter
	private Context context;

	private Filaments filaments;
	private TrackedFilaments trackedFilaments;

	private TrackingParameters trackingParameters;

	public FilamentsTracker(Context context, Filaments filaments) {
		new FilamentsTracker(context, filaments, new TrackingParameters());
	}

	public FilamentsTracker(Context context, Filaments filaments, TrackingParameters trackingParameters) {
		context.inject(this);
		this.setFilaments(filaments);
		this.trackingParameters = trackingParameters;
	}

	public Filaments getFilaments() {
		return filaments;
	}

	public void setFilaments(Filaments filaments) {
		this.filaments = filaments;
	}

	public void track() {

		// TODO: implement multithreading

		SimpleWeightedGraph<Filament, DefaultWeightedEdge> graph = trackFrameToFrame();
		graph = trackGapMergeSplit(graph);

		trackedFilaments = new TrackedFilaments(context);
		trackedFilaments.buildTracks(graph);

		if (this.trackingParameters.isInterpolateFilaments()) {
			interpolateFilaments();
		}

	}

	/*
	 * 1. Frame to frame linking.
	 */
	private SimpleWeightedGraph<Filament, DefaultWeightedEdge> trackFrameToFrame() {

		// Group filaments by frames in a map
		Map<Integer, List<Filament>> sortedFilaments = filaments.stream()
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
					trackingParameters.getCostThreshold(), alternativeCostFactor, percentile);
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
		settings.put(KEY_GAP_CLOSING_MAX_FRAME_GAP, (int) this.getTrackingParameters().getMaxFrameGap());

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

	public TrackedFilaments getTrackedFilaments() {
		return trackedFilaments;
	}

	public TrackingParameters getTrackingParameters() {
		return trackingParameters;
	}

	public void interpolateFilaments() {
		double maxFrames;
		Filament filament;
		Filament filamentBefore;
		Filament filamentAfter;

		for (TrackedFilament trackedFilament : trackedFilaments) {
			maxFrames = trackedFilament.get(trackedFilament.size() - 1).getFrame();

			for (int frame = 0; frame < maxFrames; frame++) {
				filament = trackedFilament.getFilamentByFrame(frame);
				filamentBefore = trackedFilament.getFilamentByFrame(frame - 1);
				if (filament == null && filamentBefore != null) {
					System.out.println("********");
					System.out.println(filament);
					System.out.println(filamentBefore);
				}
			}

		}
	}

}
