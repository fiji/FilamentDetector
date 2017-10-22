package fiji.plugin.filamentdetector.tracking.lap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;

import fiji.plugin.filamentdetector.model.Filament;

public class FilamentGraphSegmentSplitter {
	private final List<Filament> segmentStarts;

	private final List<Filament> segmentEnds;

	private final List<List<Filament>> segmentMiddles;

	public FilamentGraphSegmentSplitter(final UndirectedGraph<Filament, DefaultWeightedEdge> graph,
			final boolean findMiddlePoints) {
		final ConnectivityInspector<Filament, DefaultWeightedEdge> connectivity = new ConnectivityInspector<Filament, DefaultWeightedEdge>(
				graph);
		final List<Set<Filament>> connectedSets = connectivity.connectedSets();
		final Comparator<Filament> framecomparator = Filament.frameComparator;

		segmentStarts = new ArrayList<Filament>(connectedSets.size());
		segmentEnds = new ArrayList<Filament>(connectedSets.size());
		if (findMiddlePoints) {
			segmentMiddles = new ArrayList<List<Filament>>(connectedSets.size());
		} else {
			segmentMiddles = Collections.emptyList();
		}

		for (final Set<Filament> set : connectedSets) {
			if (set.size() < 2) {
				continue;
			}

			final List<Filament> list = new ArrayList<Filament>(set);
			Collections.sort(list, framecomparator);

			segmentEnds.add(list.remove(list.size() - 1));
			segmentStarts.add(list.remove(0));
			if (findMiddlePoints) {
				segmentMiddles.add(list);
			}
		}
	}

	public List<Filament> getSegmentEnds() {
		return segmentEnds;
	}

	public List<List<Filament>> getSegmentMiddles() {
		return segmentMiddles;
	}

	public List<Filament> getSegmentStarts() {
		return segmentStarts;
	}

}