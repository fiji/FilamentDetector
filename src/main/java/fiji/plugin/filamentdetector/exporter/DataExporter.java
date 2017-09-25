package fiji.plugin.filamentdetector.exporter;

import java.io.File;
import java.util.List;

public interface DataExporter<T> {

	public void export(T data, File file);

	public String getName();

	public String getExtension();

	public List<String> getExtensionFilters();

	public String getExtensionDescription();

	public String getDescription();

	@Override
	public String toString();
}
