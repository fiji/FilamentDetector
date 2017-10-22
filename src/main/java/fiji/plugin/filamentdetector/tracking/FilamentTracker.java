package fiji.plugin.filamentdetector.tracking;

import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;

public interface FilamentTracker {

	Filaments getFilaments();

	void setFilaments(Filaments filaments);

	void track();

	TrackedFilaments getTrackedFilaments();

	TrackingParameters getTrackingParameters();

}