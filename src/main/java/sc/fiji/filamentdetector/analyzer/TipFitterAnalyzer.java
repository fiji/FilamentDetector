/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2025 Fiji developers.
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package sc.fiji.filamentdetector.analyzer;

import java.util.HashMap;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import sc.fiji.filamentdetector.analyzer.tipfitter.FilamentTipFitter;

@Plugin(type = Analyzer.class, priority = Priority.HIGH)
public class TipFitterAnalyzer extends AbstractAnalyzer {

	public static String NAME = "Tip Fitter";
	public static String DESCRIPTION = "Use tracked filaments as seeds to fit in "
			+ "1D tip of filaments from both side." + " (this analyzer is in development)";

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
