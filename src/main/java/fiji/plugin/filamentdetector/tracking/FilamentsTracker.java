package fiji.plugin.filamentdetector.tracking;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
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

		// TODO: implement multi-threading

		trackedFilaments = new TrackedFilaments(context);

		// Group filaments by frames in a map
		Map<Integer, List<Filament>> sortedFilaments = filaments.stream()
				.collect(Collectors.groupingBy(Filament::getFrame));

		// Get the sorted list of frames that contains filaments
		List<Integer> frames = sortedFilaments.keySet().stream().sorted().collect(Collectors.toList());

		// Prepare cost function
		CostFunction<Filament, Filament> costFunction = new BoundingBoxOverlapCostFunction();

		// Initialize variables

		double alternativeCostFactor = 1.01;
		double percentile = 1;

		JaqamanLinkingCostMatrixCreator<Filament, Filament> creator;
		JaqamanLinker<Filament, Filament> linker;

		List<Filament> sources;
		List<Filament> targets;
		// int deltaFrames = 0;
		int frameSource;
		int frameTarget;

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
				double cost = costs.get(source);
				Filament target = assignment.get(source);
				trackedFilaments.addLink(source, target);
			}

		}
	}

	public TrackedFilaments getTrackedFilaments() {
		return trackedFilaments;
	}

	public TrackingParameters getTrackingParameters() {
		return trackingParameters;
	}

}
