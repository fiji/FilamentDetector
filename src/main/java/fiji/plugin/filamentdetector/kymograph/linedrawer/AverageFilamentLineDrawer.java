package fiji.plugin.filamentdetector.kymograph.linedrawer;

import java.util.Comparator;

import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.TrackedFilament;

public class AverageFilamentLineDrawer extends AbstractLineDrawer implements LineDrawer {

	public static String NAME = "Average Filament Line Drawer";
	public static String DESCRIPTION = "Find an \"average filament\" in the tracks while "
			+ "keeping the length similar to the longest one.";

	public AverageFilamentLineDrawer() {
		setName(NAME);
		setDescription(DESCRIPTION);
	}

	@Override
	public double[] draw(TrackedFilament trackedFilament) {
		Filament filament = trackedFilament.stream().max(Comparator.comparing(Filament::getLength)).orElse(null);
		return filament.getTips();
	}
}
