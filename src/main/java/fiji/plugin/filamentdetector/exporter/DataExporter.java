package fiji.plugin.filamentdetector.exporter;

import java.io.File;
import java.util.List;

public interface DataExporter<T> {

	public void export(T data, File file);

	public String getName();

	public List<String> getExtension();

	public String getExtensionDescription();

	public String getDescription();

	@Override
	public String toString();
}
