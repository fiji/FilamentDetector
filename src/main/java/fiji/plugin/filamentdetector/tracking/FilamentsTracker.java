package fiji.plugin.filamentdetector.tracking;

import fiji.plugin.filamentdetector.Filament;
import fiji.plugin.filamentdetector.Filaments;
import fiji.plugin.filamentdetector.TrackedFilament;
import fiji.plugin.filamentdetector.TrackedFilaments;

public class FilamentsTracker {

	private Filaments filaments;
	private TrackedFilaments trackedFilaments;

	public FilamentsTracker(Filaments filaments) {
		this.setFilaments(filaments);
	}

	public Filaments getFilaments() {
		return filaments;
	}

	public void setFilaments(Filaments filaments) {
		this.filaments = filaments;
	}

	public void track() {
		trackedFilaments = new TrackedFilaments();

		TrackedFilament trackedFilament = new TrackedFilament();

		for (Filament filament : filaments) {
			trackedFilament.add(filament);
		}

		trackedFilaments.add(trackedFilament);
	}

	public TrackedFilaments getTrackedFilaments() {
		return trackedFilaments;
	}

}
