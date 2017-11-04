package fiji.plugin.filamentdetector.analyzer;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import fiji.plugin.filamentdetector.analyzer.tipfitter.FilamentTipFitter;

@Plugin(type = Analyzer.class, priority = Priority.HIGH)
public class TipFitterAnalyzer extends AbstractAnalyzer {

	public static String NAME = "Tip Fitter";
	public static String DESCRIPTION = "Use tracked filaments as seeds to fit in "
			+ "1D tip of filaments from both side.";

	private FilamentTipFitter fitter;

	public TipFitterAnalyzer() {
		super();
		setName(NAME);
		setDescription(DESCRIPTION);
	}

	@Override
	public String getAnalyzerInfo() {
		String out = "";
		out += "Name : " + this.name + "\n";
		out += "polynomDegree : " + getFitter().getPolynomDegree() + "\n";
		out += "relativePositionFromEnd : " + getFitter().getRelativePositionFromEnd() + "\n";
		out += "lineFitLength : " + getFitter().getLineFitLength() + "\n";
		out += "channelIndex : " + getFitter().getChannelIndex() + "\n";
		out += "lineWidth : " + getFitter().getLineWidth() + "\n";
		out += "\n";
		return out;
	}

	@Override
	public void analyze() {

		getFitter().setSeeds(filamentWorkflow.getTrackedFilaments());
		getFitter().setImageDisplay(filamentWorkflow.getImageDisplay());

		getFitter().fit();

		this.results = new HashMap<>();
		this.results.put("side1Filaments", getFitter().getSide1Filaments());
		this.results.put("side2Filaments", getFitter().getSide2Filaments());

		this.resultMessage = "Tip Fitting done.\n";
		this.resultMessage += getFitter().getSide1Filaments().size() + getFitter().getSide2Filaments().size();
		this.resultMessage += " filaments have been detected.";
	}

	public FilamentTipFitter getFitter() {
		if (this.fitter == null) {
			this.fitter = new FilamentTipFitter(filamentWorkflow.getContext());
		}
		return this.fitter;
	}

}
