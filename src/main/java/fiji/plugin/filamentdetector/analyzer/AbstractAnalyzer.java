package fiji.plugin.filamentdetector.analyzer;

import java.util.HashMap;
import java.util.Map;

import org.scijava.Context;

import fiji.plugin.filamentdetector.FilamentWorkflow;

public abstract class AbstractAnalyzer implements Analyzer {

	protected String name;
	protected String description;
	protected FilamentWorkflow filamentWorkflow;

	protected boolean saveResults = false;
	protected String resultMessage;
	protected Map<String, Object> results;

	public AbstractAnalyzer(Context context, FilamentWorkflow filamentWorkflow) {
		context.inject(this);
		this.filamentWorkflow = filamentWorkflow;
		this.results = new HashMap<>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public FilamentWorkflow getFilamentWorkflow() {
		return filamentWorkflow;
	}

	@Override
	public String getResultMessage() {
		return resultMessage;
	}

	@Override
	public boolean isSaveResults() {
		return saveResults;
	}

	@Override
	public void setSaveResults(boolean saveResults) {
		this.saveResults = saveResults;
	}

	@Override
	public Map<String, Object> getResults() {
		return this.results;
	}

}
