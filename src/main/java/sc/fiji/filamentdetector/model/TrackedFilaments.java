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
package sc.fiji.filamentdetector.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

import sc.fiji.filamentdetector.overlay.ColorService;

public class TrackedFilaments extends ArrayList<TrackedFilament> {

	private static final long serialVersionUID = 1L;
	private static int colorCounter = 1;

	@Parameter
	ColorService colorService;

	public TrackedFilaments(Context context) {
		context.inject(this);
	}

	public TrackedFilaments() {
	}

	/*
	 * Add a new link. If source does not exist in any of the last elements, then
	 * create a new TrackedFilament.
	 */
	public TrackedFilament addLink(Filament source, Filament target) {

		if (this.size() == 0) {
			colorService.initialize();
		}

		// Check if we need to create a new TrackedFilament
		TrackedFilament currentTrackedFilament = null;
		for (TrackedFilament trackedFilament : this) {
			if (trackedFilament.lastFilamentIs(source)) {
				target.setColor(trackedFilament.getColor());
				trackedFilament.add(target);
				return trackedFilament;
			}
		}

		currentTrackedFilament = new TrackedFilament();
		currentTrackedFilament.add(source);
		currentTrackedFilament.add(target);
		currentTrackedFilament.setColor(this.nextColor());

		this.add(currentTrackedFilament);
		return currentTrackedFilament;

	}

	public List<Integer> getIDs() {
		return this.stream().map(x -> x.getId()).collect(Collectors.toList());
	}

	private Color nextColor() {

		if (colorCounter == colorService.getLength()) {
			colorCounter = 1;
		}

		Color color = colorService.getColor(colorCounter);
		colorCounter++;
		return color;
	}

	public void buildTracks(SimpleWeightedGraph<Filament, DefaultWeightedEdge> graph) {

		this.clear();
		colorService.initialize();

		TrackedFilament currentTrackedFilament = null;

		// Reconstruct all the possible tracks from the graph object
		// This method do not take into account merging and splitting (only gap closing)

		for (Filament filament1 : graph.vertexSet()) {
			for (Filament filament2 : graph.vertexSet()) {
				if (graph.getEdge(filament1, filament2) != null) {

					currentTrackedFilament = this.getTrackedFilament(filament1);
					if (currentTrackedFilament == null) {
						currentTrackedFilament = this.getTrackedFilament(filament2);
					}

					if (currentTrackedFilament == null) {
						currentTrackedFilament = new TrackedFilament();
						currentTrackedFilament.add(filament1);
						currentTrackedFilament.add(filament2);
						currentTrackedFilament.setColor(this.nextColor());

						this.add(currentTrackedFilament);
					} else {
						if (currentTrackedFilament.contains(filament1) && !currentTrackedFilament.contains(filament2)) {
							currentTrackedFilament.add(filament2);
							filament2.setColor(currentTrackedFilament.getColor());
						} else if (currentTrackedFilament.contains(filament2)
								&& !currentTrackedFilament.contains(filament1)) {
							currentTrackedFilament.add(filament1);
							filament1.setColor(currentTrackedFilament.getColor());
						}
					}
					currentTrackedFilament = currentTrackedFilament.stream()
							.sorted((f1, f2) -> Double.compare(f1.getFrame(), f2.getFrame()))
							.collect(Collectors.toCollection(TrackedFilament::new));
				}
			}
		}
	}

	public boolean contains(Filament filament) {
		for (TrackedFilament trackedFilament : this) {
			if (trackedFilament.contains(filament)) {
				return true;
			}
		}
		return false;
	}

	public TrackedFilament getTrackedFilament(Filament filament) {
		for (TrackedFilament trackedFilament : this) {
			if (trackedFilament.contains(filament)) {
				return trackedFilament;
			}
		}
		return null;
	}

}
