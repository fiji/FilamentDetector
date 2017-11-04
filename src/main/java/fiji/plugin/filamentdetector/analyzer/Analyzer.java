package fiji.plugin.filamentdetector.analyzer;

import fiji.plugin.filamentdetector.FilamentWorkflow;

public interface Analyzer {
	void analyze();

	String getName();

	void setName(String name);

	String getDescription();

	void setDescription(String description);

	String toString();

	String getInfo();

	String getResultMessage();

	Object getResults();

	void setSaveResults(boolean saveResults);

	boolean isSaveResults();

	FilamentWorkflow getFilamentWorkflow();

}
