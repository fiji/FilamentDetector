package fiji.plugin.filamentdetector.analyzer.tipfitter;

import java.util.HashMap;
import java.util.Map;

import fiji.plugin.filamentdetector.FilamentWorkflow;
import fiji.plugin.filamentdetector.analyzer.AbstractAnalyzer;
import fiji.plugin.filamentdetector.analyzer.Analyzer;

public class TipFitterAnalyzer extends AbstractAnalyzer implements Analyzer {

	public static String NAME = "Tip Fitter";
	public static String DESCRIPTION = "Use tracked filaments as seeds to fit in 1D tip of filaments from both side.";

	private String resultMessage;
	private Map<String, Object> results;

	private FilamentTipFitter fitter;

	public TipFitterAnalyzer(FilamentWorkflow filamentWorkflow) {
		super(filamentWorkflow);
		setName(NAME);
		setDescription(DESCRIPTION);

		this.fitter = new FilamentTipFitter(filamentWorkflow.getContext());
	}

	public String getInfo() {
		String out = "";
		out += "Name : " + this.name + "\n";
		out += "polynomDegree : " + this.fitter.getPolynomDegree() + "\n";
		out += "relativePositionFromEnd : " + this.fitter.getRelativePositionFromEnd() + "\n";
		out += "lineFitLength : " + this.fitter.getLineFitLength() + "\n";
		out += "channelIndex : " + this.fitter.getChannelIndex() + "\n";
		out += "lineWidth : " + this.fitter.getLineWidth() + "\n";
		out += "\n";
		return out;
	}

	@Override
	public void analyze() {

		this.fitter.setSeeds(filamentWorkflow.getTrackedFilaments());
		this.fitter.setImageDisplay(filamentWorkflow.getImageDisplay());

		this.fitter.fit();

		this.results = new HashMap<>();
		this.results.put("side1Filaments", this.fitter.getSide1Filaments());
		this.results.put("side2Filaments", this.fitter.getSide2Filaments());

		this.resultMessage = "Tip Fitting done.\n";
		this.resultMessage += this.fitter.getSide1Filaments().size() + this.fitter.getSide2Filaments().size();
		this.resultMessage += " filaments have been detected.";
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public Map<String, Object> getResults() {
		return results;
	}

	public FilamentTipFitter getFitter() {
		return fitter;
	}

}
