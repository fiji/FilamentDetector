package fiji.plugin.filamentdetector.exporter;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

public abstract class FilamentsExporter<Filaments> extends AbstractDataExporter<Filaments> {

	public FilamentsExporter(Context context) {
		super(context);
	}

}
