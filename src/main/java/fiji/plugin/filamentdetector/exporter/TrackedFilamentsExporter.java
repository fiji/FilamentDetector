package fiji.plugin.filamentdetector.exporter;

import org.scijava.Context;

public abstract class TrackedFilamentsExporter<TrackedFilaments> extends AbstractDataExporter<TrackedFilaments> {

	public TrackedFilamentsExporter(Context context) {
		super(context);
	}

}
