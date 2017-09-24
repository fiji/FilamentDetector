package fiji.plugin.filamentdetector.event;

import org.scijava.event.SciJavaEvent;

import fiji.plugin.filamentdetector.detection.FilteringParameters;

public class FilterFilamentEvent extends SciJavaEvent {

	private FilteringParameters filteringParameters;

	public FilterFilamentEvent(FilteringParameters filteringParameters) {
		this.filteringParameters = filteringParameters;
	}

	public FilteringParameters getFilteringParameters() {
		return filteringParameters;
	}

}
