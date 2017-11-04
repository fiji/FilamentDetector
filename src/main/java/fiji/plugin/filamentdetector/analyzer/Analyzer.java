package fiji.plugin.filamentdetector.analyzer;

import org.scijava.Named;
import org.scijava.plugin.RichPlugin;

import fiji.plugin.filamentdetector.FilamentWorkflow;

public interface Analyzer extends Named, RichPlugin {
	void analyze();

	String getDescription();

	void setDescription(String description);

	@Override
	String toString();

	String getAnalyzerInfo();

	String getResultMessage();

	Object getResults();

	void setSaveResults(boolean saveResults);

	boolean isSaveResults();

	FilamentWorkflow getFilamentWorkflow();

	void setFilamentWorkflow(FilamentWorkflow filamentWorkflow);

}
