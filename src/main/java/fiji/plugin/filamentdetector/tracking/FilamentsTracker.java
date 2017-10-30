package fiji.plugin.filamentdetector.tracking;

import org.scijava.Named;

import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;

public interface FilamentsTracker extends Named {

	void setFilaments(Filaments filaments);

	Filaments getFilaments();

	void track();

	TrackedFilaments getTrackedFilaments();

	void setTrackedFilaments(TrackedFilaments trackedFilaments);

	String toString();

}