package fiji.plugin.filamentdetector.exporter;

import org.scijava.Context;

public abstract class FilamentsExporter<Filaments> extends AbstractDataExporter<Filaments> {

	public FilamentsExporter(Context context) {
		super(context);
	}

}
