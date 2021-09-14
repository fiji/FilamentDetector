/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2021 Fiji developers.
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
package sc.fiji.filamentdetector.gui.controller.detection;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import sc.fiji.filamentdetector.detection.FilamentDetector;
import sc.fiji.filamentdetector.detection.IJ2RidgeDetectionFilamentDetector;
import sc.fiji.filamentdetector.gui.event.DetectionParametersUpdatedEvent;
import sc.fiji.filamentdetector.gui.fxwidgets.SliderLabelSynchronizer;
import sc.fiji.filamentdetector.gui.fxwidgets.UpperLowerSynchronizer;

public class IJ2RidgeDetectionFilamentDetectorController extends AbstractFilamentDetectorController {

	private static String FXML_PATH = "/sc/fiji/filamentdetector/gui/view/detection/IJ2RidgeDetectionFilamentDetectorView.fxml";

	@Parameter
	private EventService eventService;

	@FXML
	private Slider lineWidthSlider;

	@FXML
	private TextField lineWidthField;

	@FXML
	private Slider lowerThresholdSlider;

	@FXML
	private TextField lowerThresholdField;

	@FXML
	private Slider upperThresholdSlider;

	@FXML
	private TextField upperThresholdField;

	private UpperLowerSynchronizer thresholdSync;
	private SliderLabelSynchronizer lineWidthSync;

	private IJ2RidgeDetectionFilamentDetector filamentDetector;

	public IJ2RidgeDetectionFilamentDetectorController(Context context, FilamentDetector filamentDetector) {
		context.inject(this);
		setFXMLPath(FXML_PATH);
		this.filamentDetector = (IJ2RidgeDetectionFilamentDetector) filamentDetector;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Fill fields with default values
		thresholdSync = new UpperLowerSynchronizer(lowerThresholdSlider, lowerThresholdField, upperThresholdSlider,
				upperThresholdField);
		thresholdSync.setLowerTooltip("Line points with a response smaller as this threshold are rejected.");
		thresholdSync.setUpperTooltip("Line points with a response larger as this threshold are accepted.");
		thresholdSync.setLowerValue(this.filamentDetector.getLowerThreshold());
		thresholdSync.setUpperValue(this.filamentDetector.getUpperThreshold());

		lineWidthSync = new SliderLabelSynchronizer(lineWidthSlider, lineWidthField);
		lineWidthSync.setTooltip(
				"The line diameter in pixels. It estimates the parameter \"sigma\" (available in the \"Advanced\" tab).");
		lineWidthSync.setValue(this.filamentDetector.getLineWidth());
	}

	@FXML
	public void updateParameters(Event event) {

		if (thresholdSync.isEvent(event)) {
			thresholdSync.update(event);
			this.filamentDetector.setLowerThreshold(thresholdSync.getLowerValue());
			this.filamentDetector.setUpperThreshold(thresholdSync.getUpperValue());
		} else if (lineWidthSync.isEvent(event)) {
			lineWidthSync.update(event);
			this.filamentDetector.setLineWidth(lineWidthSync.getValue());
		}

		eventService.publish(new DetectionParametersUpdatedEvent());
	}

}
