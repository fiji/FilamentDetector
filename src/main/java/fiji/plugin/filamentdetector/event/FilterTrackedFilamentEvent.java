package fiji.plugin.filamentdetector.event;

import org.scijava.event.SciJavaEvent;

import fiji.plugin.filamentdetector.tracking.FilteringTrackedFilamentsParameters;

public class FilterTrackedFilamentEvent extends SciJavaEvent {

	private FilteringTrackedFilamentsParameters filteringParameters;

	public FilterTrackedFilamentEvent(FilteringTrackedFilamentsParameters filteringParameters) {
		this.filteringParameters = filteringParameters;
	}

	public FilteringTrackedFilamentsParameters getFilteringTrackedFilamentsParameters() {
		return filteringParameters;
	}

}
