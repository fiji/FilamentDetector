/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2021 Fiji developers.
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
package sc.fiji.filamentdetector.tracking.lap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;

import sc.fiji.filamentdetector.model.Filament;

public class FilamentGraphSegmentSplitter {
	private final List<Filament> segmentStarts;

	private final List<Filament> segmentEnds;

	private final List<List<Filament>> segmentMiddles;

	public FilamentGraphSegmentSplitter(final Graph<Filament, DefaultWeightedEdge> graph,
			final boolean findMiddlePoints) {
		final ConnectivityInspector<Filament, DefaultWeightedEdge> connectivity =
			new ConnectivityInspector<>(graph);
		final List<Set<Filament>> connectedSets = connectivity.connectedSets();
		final Comparator<Filament> framecomparator = Filament.frameComparator;

		segmentStarts = new ArrayList<>(connectedSets.size());
		segmentEnds = new ArrayList<>(connectedSets.size());
		if (findMiddlePoints) {
			segmentMiddles = new ArrayList<>(connectedSets.size());
		} else {
			segmentMiddles = Collections.emptyList();
		}

		for (final Set<Filament> set : connectedSets) {
			if (set.size() < 2) {
				continue;
			}

			final List<Filament> list = new ArrayList<>(set);
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
