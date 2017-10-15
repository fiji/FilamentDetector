package fiji.plugin.filamentdetector.analyzer;

public interface Analyzer {
	void analyze();

	String getName();

	void setName(String name);

	String getDescription();

	void setDescription(String description);

	String toString();

	String getInfo();
}
