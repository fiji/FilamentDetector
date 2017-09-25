package fiji.plugin.filamentdetector.exporter;

import org.scijava.Context;

public abstract class AbstractDataExporter<T> implements DataExporter<T> {

	public AbstractDataExporter(Context context) {
		context.inject(this);
	}
}
