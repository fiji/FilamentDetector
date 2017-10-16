package fiji.plugin.filamentdetector.analyzer;

import fiji.plugin.filamentdetector.FilamentWorkflow;

public abstract class AbstractAnalyzer implements Analyzer {

	protected String name;
	protected String description;
	protected FilamentWorkflow filamentWorkflow;

	public AbstractAnalyzer(FilamentWorkflow filamentWorkflow) {
		this.filamentWorkflow = filamentWorkflow;
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

	public FilamentWorkflow getFilamentWorkflow() {
		return filamentWorkflow;
	}

}
