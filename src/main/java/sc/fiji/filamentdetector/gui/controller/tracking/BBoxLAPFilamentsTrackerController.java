/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2017 Hadrien Mary
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
package sc.fiji.filamentdetector.gui.controller.tracking;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import sc.fiji.filamentdetector.gui.fxwidgets.SliderLabelSynchronizer;
import sc.fiji.filamentdetector.tracking.BBoxLAPFilamentsTracker;
import sc.fiji.filamentdetector.tracking.FilamentsTracker;

public class BBoxLAPFilamentsTrackerController extends AbstractFilamentsTrackerController {

	private static String FXML_PATH = "/sc/fiji/filamentdetector/gui/view/tracking/BBoxLAPFilamentsTrackerView.fxml";

	@FXML
	private Slider costThresholdSlider;

	@FXML
	private TextField costThresholdField;

	@FXML
	private Slider maxFrameGapSlider;

	@FXML
	private TextField maxFrameGapField;

	@FXML
	private CheckBox interpolateFilamentsCheckbox;

	private SliderLabelSynchronizer costThresholdSync;
	private SliderLabelSynchronizer maxFrameGapSync;

	private BBoxLAPFilamentsTracker filamentsTracker;

	public BBoxLAPFilamentsTrackerController(Context context, FilamentsTracker filamentsTracker) {
		context.inject(this);
		setFXMLPath(FXML_PATH);
		this.filamentsTracker = (BBoxLAPFilamentsTracker) filamentsTracker;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// Fill fields with default values
		costThresholdSync = new SliderLabelSynchronizer(costThresholdSlider, costThresholdField);
		costThresholdSync.setTooltip(
				"Discard links between filaments when the IoU of the bounding boxes is below this value (0 to 1).");
		costThresholdSync.setValue(this.filamentsTracker.getCostThreshold());

		maxFrameGapSync = new SliderLabelSynchronizer(maxFrameGapSlider, maxFrameGapField);
		maxFrameGapSync.setValue(this.filamentsTracker.getMaxFrameGap());

		interpolateFilamentsCheckbox.setSelected(this.filamentsTracker.isInterpolateFilaments());
	}

	@FXML
	void updateTrackingParameters(Event event) {

		if (costThresholdSync.isEvent(event)) {
			costThresholdSync.update(event);
			this.filamentsTracker.setCostThreshold(costThresholdSync.getValue());
		} else if (maxFrameGapSync.isEvent(event)) {
			maxFrameGapSync.update(event);
			this.filamentsTracker.setMaxFrameGap(maxFrameGapSync.getValue());
		} else if (event.getSource().equals(interpolateFilamentsCheckbox)) {
			this.filamentsTracker.setInterpolateFilaments(interpolateFilamentsCheckbox.isSelected());
		}
	}

}
