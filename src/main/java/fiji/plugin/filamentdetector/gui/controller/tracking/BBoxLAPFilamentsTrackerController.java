package fiji.plugin.filamentdetector.gui.controller.tracking;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import fiji.plugin.filamentdetector.gui.fxwidgets.SliderLabelSynchronizer;
import fiji.plugin.filamentdetector.tracking.BBoxLAPFilamentsTracker;
import fiji.plugin.filamentdetector.tracking.FilamentsTracker;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class BBoxLAPFilamentsTrackerController extends AbstractFilamentsTrackerController {

	private static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/tracking/BBoxLAPFilamentsTrackerView.fxml";

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
