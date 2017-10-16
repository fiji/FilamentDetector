package fiji.plugin.filamentdetector.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.overlay.ColorService;

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

}
