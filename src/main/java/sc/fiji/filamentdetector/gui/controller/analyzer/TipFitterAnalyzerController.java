/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2020 Fiji developers.
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
package sc.fiji.filamentdetector.gui.controller.analyzer;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import sc.fiji.filamentdetector.analyzer.TipFitterAnalyzer;
import sc.fiji.filamentdetector.gui.GUIStatusService;
import sc.fiji.filamentdetector.gui.fxwidgets.SliderLabelSynchronizer;
import sc.fiji.filamentdetector.model.TrackedFilament;
import sc.fiji.filamentdetector.model.TrackedFilaments;
import sc.fiji.filamentdetector.overlay.FilamentOverlayService;

public class TipFitterAnalyzerController extends AbstractAnalyzerController implements AnalyzerController {

	private static String FXML_VIEW_FILE = "/sc/fiji/filamentdetector/gui/view/analyzer/TipFitterAnalyzerView.fxml";

	@Parameter
	private GUIStatusService status;

	@Parameter
	private LogService log;

	@Parameter
	private FilamentOverlayService overlay;

	@FXML
	private TextField channelIndexField;

	@FXML
	private Slider lineWidthSlider;

	@FXML
	private TextField lineWidthField;

	@FXML
	private Slider lineFitLengthSlider;

	@FXML
	private TextField lineFitLengthField;

	@FXML
	private Slider polynomDegreeSlider;

	@FXML
	private TextField polynomDegreeField;

	@FXML
	private Slider relativeDistSlider;

	@FXML
	private TextField relativeDistField;

	@FXML
	private CheckBox overlayFilamentsCheckbox;

	private SliderLabelSynchronizer lineWidthSync;
	private SliderLabelSynchronizer lineFitLengthSync;
	private SliderLabelSynchronizer polynomDegreeSync;
	private SliderLabelSynchronizer relativeDistSync;

	private TipFitterAnalyzer analyzer;

	public TipFitterAnalyzerController(Context context, TipFitterAnalyzer analyzer) {
		super(context);
		setFXMLPath(FXML_VIEW_FILE);
		this.analyzer = analyzer;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		lineWidthSync = new SliderLabelSynchronizer(lineWidthSlider, lineWidthField);
		lineWidthSync.setValue(this.analyzer.getFitter().getLineWidth());

		lineFitLengthSync = new SliderLabelSynchronizer(lineFitLengthSlider, lineFitLengthField);
		lineFitLengthSync.setValue(this.analyzer.getFitter().getLineFitLength());

		polynomDegreeSync = new SliderLabelSynchronizer(polynomDegreeSlider, polynomDegreeField);
		polynomDegreeSync.setValue(this.analyzer.getFitter().getPolynomDegree());

		relativeDistSync = new SliderLabelSynchronizer(relativeDistSlider, relativeDistField);
		relativeDistSync.setValue(this.analyzer.getFitter().getRelativePositionFromEnd());

		channelIndexField.setText(Integer.toString(this.analyzer.getFitter().getChannelIndex()));
	}

	@FXML
	void updateParameters(ActionEvent event) {

		if (lineWidthSync.isEvent(event)) {
			lineWidthSync.update(event);
			this.analyzer.getFitter().setLineWidth(lineWidthSync.getValue());

		} else if (lineFitLengthSync.isEvent(event)) {
			lineFitLengthSync.update(event);
			this.analyzer.getFitter().setLineFitLength(lineFitLengthSync.getValue());

		} else if (polynomDegreeSync.isEvent(event)) {
			polynomDegreeSync.update(event);
			this.analyzer.getFitter().setPolynomDegree((int) polynomDegreeSync.getValue());

		} else if (relativeDistSync.isEvent(event)) {
			relativeDistSync.update(event);
			this.analyzer.getFitter().setRelativePositionFromEnd(relativeDistSync.getValue());

		}
	}

	@Override
	public void runPostAnalysisAction() {
		Map<TrackedFilament, TrackedFilament> side1 = (Map<TrackedFilament, TrackedFilament>) this.analyzer.getResults()
				.get("side1Filaments");
		Map<TrackedFilament, TrackedFilament> side2 = (Map<TrackedFilament, TrackedFilament>) this.analyzer.getResults()
				.get("side2Filaments");

		TrackedFilaments side1AsList = side1.values().stream().collect(Collectors.toCollection(TrackedFilaments::new));
		TrackedFilaments side2AsList = side2.values().stream().collect(Collectors.toCollection(TrackedFilaments::new));

		overlay.reset();
		overlay.add(side1AsList);
		overlay.add(side2AsList);
	}
}
