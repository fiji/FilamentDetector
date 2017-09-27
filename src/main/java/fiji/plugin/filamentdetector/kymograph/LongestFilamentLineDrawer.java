package fiji.plugin.filamentdetector.kymograph;

import java.util.Comparator;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.TrackedFilament;

public class LongestFilamentLineDrawer extends AbstractLineDrawer implements LineDrawer {

	public static String NAME = "Longest Filament Line Drawer";
	public static String DESCRIPTION = "Look for the longest filament in the track and draw the kymograph based on it.";

	public LongestFilamentLineDrawer() {
		setName(NAME);
		setDescription(DESCRIPTION);
	}

	@Override
	public double[] draw(TrackedFilament trackedFilament) {
		Filament filament = trackedFilament.stream().max(Comparator.comparing(Filament::getLength)).orElse(null);
		return filament.getTips();
	}
}
