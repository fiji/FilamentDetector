package fiji.plugin.filamentdetector.gui.controller.detection;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import fiji.plugin.filamentdetector.detection.FilamentDetector;
import fiji.plugin.filamentdetector.detection.RidgeDetectionFilamentsDetector;
import fiji.plugin.filamentdetector.gui.controller.helper.SliderLabelSynchronizer;
import fiji.plugin.filamentdetector.gui.controller.helper.UpperLowerSynchronizer;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class RidgeDetectionFilamentDetectorController extends AbstractFilamentDetectorController {

	private static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/detection/RidgeDetectionFilamentDetectorView.fxml";

	@FXML
	private Slider lineWidthSlider;

	@FXML
	private TextField lineWidthField;

	@FXML
	private Slider highContrastSlider;

	@FXML
	private TextField highContrastField;

	@FXML
	private Slider lowContrastSlider;

	@FXML
	private TextField lowContrastField;

	@FXML
	private Slider sigmaSlider;

	@FXML
	private TextField sigmaField;

	@FXML
	private Slider lowerThresholdSlider;

	@FXML
	private TextField lowerThresholdField;

	@FXML
	private Slider upperThresholdSlider;

	@FXML
	private TextField upperThresholdField;

	private SliderLabelSynchronizer sigmaSync;
	private UpperLowerSynchronizer thresholdSync;
	private SliderLabelSynchronizer lineWidthSync;
	private UpperLowerSynchronizer contrastSync;

	private RidgeDetectionFilamentsDetector filamentDetector;

	public RidgeDetectionFilamentDetectorController(Context context, FilamentDetector filamentDetector) {
		context.inject(this);
		setFXMLPath(FXML_PATH);
		this.filamentDetector = (RidgeDetectionFilamentsDetector) filamentDetector;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Fill fields with default values
		sigmaSync = new SliderLabelSynchronizer(sigmaSlider, sigmaField);
		sigmaSync.setTooltip("Determines the sigma for the derivatives. It depends on the line width.");
		sigmaSync.setValue(this.filamentDetector.getSigma());

		thresholdSync = new UpperLowerSynchronizer(lowerThresholdSlider, lowerThresholdField, upperThresholdSlider,
				upperThresholdField);
		thresholdSync.setLowerTooltip("Line points with a response smaller as this threshold are rejected.");
		thresholdSync.setUpperTooltip("Line points with a response larger as this threshold are accepted.");
		thresholdSync.setLowerValue(this.filamentDetector.getLowerThresh());
		thresholdSync.setUpperValue(this.filamentDetector.getUpperThresh());

		lineWidthSync = new SliderLabelSynchronizer(lineWidthSlider, lineWidthField);
		lineWidthSync.setTooltip(
				"The line diameter in pixels. It estimates the parameter \"sigma\" (available in the \"Advanced\" tab).");
		lineWidthSync.setValue(this.filamentDetector.getLineWidth());

		contrastSync = new UpperLowerSynchronizer(lowContrastSlider, lowContrastField, highContrastSlider,
				highContrastField);
		contrastSync.setLowerTooltip(
				"Lowest grayscale value of the line. It estimates the parameter \"Upper Threshold\" (available in the \\\"Advanced\\\" tab).");
		contrastSync.setUpperTooltip(
				"Highest grayscale value of the line. It estimates the parameter \"Lower Threshold\" (available in the \\\"Advanced\\\" tab).");
		contrastSync.setLowerValue(this.filamentDetector.getLowContrast());
		contrastSync.setUpperValue(this.filamentDetector.getHighContrast());

	}

	@FXML
	public void updateParameters(Event event) {

		if (sigmaSync.isEvent(event)) {
			sigmaSync.update(event);
			this.filamentDetector.setSigma(sigmaSync.getValue());

		} else if (thresholdSync.isEvent(event)) {
			thresholdSync.update(event);
			this.filamentDetector.setLowerThresh(thresholdSync.getLowerValue());
			this.filamentDetector.setUpperThresh(thresholdSync.getUpperValue());
		}

		if (lineWidthSync.isEvent(event)) {
			lineWidthSync.update(event);
			this.filamentDetector.setLineWidth(lineWidthSync.getValue());
			sigmaSync.setValue(this.filamentDetector.getSigma());

		} else if (contrastSync.isEvent(event)) {
			contrastSync.update(event);
			this.filamentDetector.setLowContrast(contrastSync.getLowerValue());
			this.filamentDetector.setHighContrast(contrastSync.getUpperValue());
			thresholdSync.setLowerValue(this.filamentDetector.getLowerThresh());
			thresholdSync.setUpperValue(this.filamentDetector.getUpperThresh());
		}

	}

}
