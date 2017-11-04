package fiji.plugin.filamentdetector.tracking;

import org.scijava.plugin.AbstractRichPlugin;

import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;

public abstract class AbstractFilamentsTracker extends AbstractRichPlugin implements FilamentsTracker {

	private String name;

	private Filaments filaments;
	private TrackedFilaments trackedFilaments;

	@Override
	public Filaments getFilaments() {
		return filaments;
	}

	@Override
	public void setFilaments(Filaments filaments) {
		this.filaments = filaments;
	}

	@Override
	public TrackedFilaments getTrackedFilaments() {
		return trackedFilaments;
	}

	@Override
	public void setTrackedFilaments(TrackedFilaments trackedFilaments) {
		this.trackedFilaments = trackedFilaments;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
