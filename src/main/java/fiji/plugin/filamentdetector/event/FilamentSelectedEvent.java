package fiji.plugin.filamentdetector.event;

import org.scijava.event.SciJavaEvent;

import fiji.plugin.filamentdetector.model.Filament;

public class FilamentSelectedEvent extends SciJavaEvent {

	private Filament filament;

	public FilamentSelectedEvent(Filament filament) {
		this.filament = filament;
	}

	public Filament getFilament() {
		return filament;
	}

}
