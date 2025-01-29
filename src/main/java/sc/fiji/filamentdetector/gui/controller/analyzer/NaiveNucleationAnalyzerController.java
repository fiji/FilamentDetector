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
package sc.fiji.filamentdetector.gui.controller.analyzer;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import sc.fiji.filamentdetector.analyzer.NaiveNucleationAnalyzer;
import sc.fiji.filamentdetector.gui.fxwidgets.SliderLabelSynchronizer;
import sc.fiji.filamentdetector.overlay.FilamentOverlayService;

public class NaiveNucleationAnalyzerController extends AbstractAnalyzerController implements AnalyzerController {

	private static String FXML_VIEW_FILE = "/sc/fiji/filamentdetector/gui/view/analyzer/NaiveNucleationAnalyzerView.fxml";

	@Parameter
	private StatusService status;

	@Parameter
	private LogService log;

	@Parameter
	private FilamentOverlayService overlay;

	@FXML
	private TextField channelIndexField;

	@FXML
	private Slider intensityThresholdSlider;

	@FXML
	private TextField intensityThresholdField;

	@FXML
	private Slider maxFrameSlider;

	@FXML
	private TextField maxFrameField;

	@FXML
	private Slider lineLengthSlider;

	@FXML
	private TextField lineLengthField;

	@FXML
	private Slider lineThicknessSlider;

	@FXML
	private TextField lineThicknessField;

	@FXML
	private Slider pixelSpacingSlider;

	@FXML
	private TextField pixelSpacingField;

	@FXML
	private TextField resultMessageField;

	@FXML
	private CheckBox colorizedCheckbox;

	private SliderLabelSynchronizer intensityThresholdSync;
	private SliderLabelSynchronizer maxFrameSync;
	private SliderLabelSynchronizer lineLengthSync;
	private SliderLabelSynchronizer lineThicknessSync;
	private SliderLabelSynchronizer pixelSpacingSync;

	private NaiveNucleationAnalyzer analyzer;

	public NaiveNucleationAnalyzerController(Context context, NaiveNucleationAnalyzer analyzer) {
		super(context);
		setFXMLPath(FXML_VIEW_FILE);
		this.analyzer = analyzer;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.analyzer.guessIntensityThresholdFromImage();

		intensityThresholdSync = new SliderLabelSynchronizer(intensityThresholdSlider, intensityThresholdField);
		intensityThresholdSync.setValue(this.analyzer.getIntensityThreshold());

		maxFrameSync = new SliderLabelSynchronizer(maxFrameSlider, maxFrameField);
		maxFrameSync.setValue(this.analyzer.getMaxFrame());

		lineLengthSync = new SliderLabelSynchronizer(lineLengthSlider, lineLengthField);
		lineLengthSync.setValue(this.analyzer.getLineLength());

		lineThicknessSync = new SliderLabelSynchronizer(lineThicknessSlider, lineThicknessField);
		lineThicknessSync.setValue(this.analyzer.getLineThickness());

		pixelSpacingSync = new SliderLabelSynchronizer(pixelSpacingSlider, pixelSpacingField);
		pixelSpacingSync.setValue(this.analyzer.getPixelSpacing());

		channelIndexField.setText(Integer.toString(this.analyzer.getChannelIndex()));

		colorizedCheckbox.setSelected(this.analyzer.isColorizedNucleatedSeeds());

		resultMessageField.setEditable(false);
		resultMessageField.setStyle("-fx-background-color:transparent; -fx-background-insets: 0px;");
	}

	@FXML
	void updateParameters(Event event) {
		if (intensityThresholdSync.isEvent(event)) {
			intensityThresholdSync.update(event);
			this.analyzer.setIntensityThreshold(intensityThresholdSync.getValue());

		} else if (maxFrameSync.isEvent(event)) {
			maxFrameSync.update(event);
			this.analyzer.setMaxFrame((int) maxFrameSync.getValue());

		} else if (lineLengthSync.isEvent(event)) {
			lineLengthSync.update(event);
			this.analyzer.setLineLength(lineLengthSync.getValue());

		} else if (lineThicknessSync.isEvent(event)) {
			lineThicknessSync.update(event);
			this.analyzer.setLineThickness(lineThicknessSync.getValue());

		} else if (pixelSpacingSync.isEvent(event)) {
			pixelSpacingSync.update(event);
			this.analyzer.setPixelSpacing(pixelSpacingSync.getValue());

		} else if (event.getSource().equals(channelIndexField)) {
			this.analyzer.setChannelIndex(Integer.parseInt(channelIndexField.getText()));

		} else if (event.getSource().equals(colorizedCheckbox)) {
			this.analyzer.setColorizedNucleatedSeeds(colorizedCheckbox.isSelected());
		}
	}

	@Override
	public void runPostAnalysisAction() {
		status.showStatus(this.analyzer.getResults().toString());
		resultMessageField.setText(this.analyzer.getResults().toString());
		overlay.refresh();
	}
}
