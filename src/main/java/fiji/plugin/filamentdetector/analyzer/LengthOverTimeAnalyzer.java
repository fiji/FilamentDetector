package fiji.plugin.filamentdetector.analyzer;

import fiji.plugin.filamentdetector.FilamentWorkflow;

public class LengthOverTimeAnalyzer extends AbstractAnalyzer implements Analyzer {

	public static String NAME = "Filament Growth Curve";
	public static String DESCRIPTION = "Generate the classic filament length versus time curve.";

	private boolean saveResults = true;
	private boolean savePlots = true;

	public LengthOverTimeAnalyzer(FilamentWorkflow filamentWorkflow) {
		super(filamentWorkflow);
		setName(NAME);
		setDescription(DESCRIPTION);
	}

	@Override
	public void analyze() {
		System.out.println("analyze gogogogo");
	}

	public boolean isSaveResults() {
		return saveResults;
	}

	public void setSaveResults(boolean saveResults) {
		this.saveResults = saveResults;
	}

	public boolean isSavePlots() {
		return savePlots;
	}

	public void setSavePlots(boolean savePlots) {
		this.savePlots = savePlots;
	}

	public String getInfo() {
		String out = "";
		out += "Name : " + this.name + "\n";
		out += "Save results : " + this.saveResults + "\n";
		out += "Save plots : " + this.savePlots + "\n";
		out += "\n";
		return out;
	}

}
