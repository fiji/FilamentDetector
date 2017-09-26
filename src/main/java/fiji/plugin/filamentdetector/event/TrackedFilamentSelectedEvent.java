package fiji.plugin.filamentdetector.event;

import org.scijava.event.SciJavaEvent;

import fiji.plugin.filamentdetector.model.TrackedFilament;

public class TrackedFilamentSelectedEvent extends SciJavaEvent {

	private TrackedFilament trackedFilament;

	public TrackedFilamentSelectedEvent(TrackedFilament trackedFilament) {
		this.trackedFilament = trackedFilament;
	}

	public TrackedFilament getTrackedFilament() {
		return trackedFilament;
	}

}
