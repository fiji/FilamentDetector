
package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.FilamentDetector;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

public class DetectFilamentController extends Controller implements Initializable {

	@Parameter
	private GUIStatusService status;

	@Parameter
	private LogService log;

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

	@FXML
	private Button detectButton;

	@FXML
	private CheckBox detectCurrentFrameButton;

	@FXML
	private CheckBox liveDetectionButton;

	private FilamentDetector filamentDetector;

	public DetectFilamentController(Context context, FilamentDetector filamentDetector) {
		context.inject(this);
		this.filamentDetector = filamentDetector;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		this.filamentDetector.initDetection();

		sigmaField.setText(Double.toString(filamentDetector.getDetectionParameters().getSigma()));
		sigmaSlider.setValue(filamentDetector.getDetectionParameters().getSigma());

		lowerThresholdField.setText(Double.toString(filamentDetector.getDetectionParameters().getLowerThresh()));
		lowerThresholdSlider.setValue(filamentDetector.getDetectionParameters().getLowerThresh());

		upperThresholdField.setText(Double.toString(filamentDetector.getDetectionParameters().getUpperThresh()));
		upperThresholdSlider.setValue(filamentDetector.getDetectionParameters().getUpperThresh());

		this.setToolTips();
	}

	@FXML
	void updateDetectionParameters(Event event) {

		DecimalFormat f = new DecimalFormat("##.00");

		if (event.getSource().equals(sigmaSlider)) {
			double newSigmaValue;
			try {
				newSigmaValue = f.parse(f.format(sigmaSlider.getValue())).doubleValue();
				sigmaField.setText(Double.toString(newSigmaValue));
				filamentDetector.getDetectionParameters().setSigma(newSigmaValue);
			} catch (ParseException e) {
				sigmaSlider.setValue(filamentDetector.getDetectionParameters().getSigma());
			}

		} else if (event.getSource().equals(sigmaField)) {
			try {
				double newSigmaValue = f.parse(f.format(Double.parseDouble(sigmaField.getText()))).doubleValue();
				sigmaSlider.setValue(newSigmaValue);
				filamentDetector.getDetectionParameters().setSigma(newSigmaValue);
			} catch (NumberFormatException | ParseException | ClassCastException e) {
				sigmaField.setText(Double.toString(filamentDetector.getDetectionParameters().getSigma()));
			}
		}

		else if (event.getSource().equals(lowerThresholdSlider)) {
			double newLowThresholdValue;
			try {
				newLowThresholdValue = f.parse(f.format(lowerThresholdSlider.getValue())).doubleValue();
				if (newLowThresholdValue < filamentDetector.getDetectionParameters().getUpperThresh()) {
					lowerThresholdField.setText(Double.toString(newLowThresholdValue));
					filamentDetector.getDetectionParameters().setLowerThresh(newLowThresholdValue);
				} else {
					lowerThresholdSlider.setValue(filamentDetector.getDetectionParameters().getUpperThresh() - 0.01);
					lowerThresholdField.setText(
							Double.toString(filamentDetector.getDetectionParameters().getUpperThresh() - 0.01));
				}

			} catch (ParseException e) {
				lowerThresholdSlider.setValue(filamentDetector.getDetectionParameters().getLowerThresh());
			}

		} else if (event.getSource().equals(lowerThresholdField)) {
			try {
				double newLowThresholdValue = f.parse(f.format(Double.parseDouble(lowerThresholdField.getText())))
						.doubleValue();
				if (newLowThresholdValue < filamentDetector.getDetectionParameters().getUpperThresh()) {
					lowerThresholdSlider.setValue(newLowThresholdValue);
					filamentDetector.getDetectionParameters().setLowerThresh(newLowThresholdValue);
				} else {
					lowerThresholdField.setText(
							Double.toString(filamentDetector.getDetectionParameters().getUpperThresh() - 0.01));
					lowerThresholdSlider.setValue(filamentDetector.getDetectionParameters().getUpperThresh() - 0.01);
				}

			} catch (NumberFormatException | ParseException | ClassCastException e) {
				lowerThresholdField
						.setText(Double.toString(filamentDetector.getDetectionParameters().getLowerThresh()));
			}
		}

		else if (event.getSource().equals(upperThresholdSlider)) {
			double newUpperThresholdValue;
			try {
				newUpperThresholdValue = f.parse(f.format(upperThresholdSlider.getValue())).doubleValue();
				if (newUpperThresholdValue > filamentDetector.getDetectionParameters().getLowerThresh()) {
					upperThresholdField.setText(Double.toString(newUpperThresholdValue));
					filamentDetector.getDetectionParameters().setUpperThresh(newUpperThresholdValue);
				} else {
					upperThresholdSlider.setValue(filamentDetector.getDetectionParameters().getLowerThresh() + 0.01);
					upperThresholdField.setText(
							Double.toString(filamentDetector.getDetectionParameters().getLowerThresh() + 0.01));
				}

			} catch (ParseException e) {
				upperThresholdSlider.setValue(filamentDetector.getDetectionParameters().getLowerThresh());
			}

		} else if (event.getSource().equals(upperThresholdField)) {
			try {
				double newUpperThresholdValue = f.parse(f.format(Double.parseDouble(upperThresholdField.getText())))
						.doubleValue();
				if (newUpperThresholdValue > filamentDetector.getDetectionParameters().getLowerThresh()) {
					upperThresholdSlider.setValue(newUpperThresholdValue);
					filamentDetector.getDetectionParameters().setUpperThresh(newUpperThresholdValue);
				} else {
					upperThresholdField.setText(
							Double.toString(filamentDetector.getDetectionParameters().getLowerThresh() + 0.01));
					upperThresholdSlider.setValue(filamentDetector.getDetectionParameters().getLowerThresh() + 0.01);
				}

			} catch (NumberFormatException | ParseException | ClassCastException e) {
				upperThresholdField
						.setText(Double.toString(filamentDetector.getDetectionParameters().getLowerThresh()));
			}
		}

		else if (event.getSource().equals(detectCurrentFrameButton)) {
			filamentDetector.getDetectionParameters()
					.setDetectOnlyOnCurrentFrame(detectCurrentFrameButton.isSelected());
		}

		if (liveDetectionButton.isSelected()) {
			this.detect(null);
		}

	}

	@FXML
	void detect(ActionEvent event) {
		status.showStatus("Start detection with the following parameter : ");
		status.showStatus(filamentDetector.getDetectionParameters().toString());
	}

	@FXML
	void liveDetectionClicked(MouseEvent event) {
		if (liveDetectionButton.isSelected()) {
			detectButton.setDisable(true);
		} else {
			detectButton.setDisable(false);

		}
	}

	private void setToolTips() {
		Tooltip tooltip;

		tooltip = new Tooltip("Determines the sigma for the derivatives. It depends on the line width.");
		Tooltip.install(sigmaSlider, tooltip);
		Tooltip.install(sigmaField, tooltip);

		tooltip = new Tooltip("Line points with a response smaller as this threshold are rejected.");
		Tooltip.install(lowerThresholdSlider, tooltip);
		Tooltip.install(lowerThresholdField, tooltip);

		tooltip = new Tooltip("Line points with a response larger as this threshold are accepted.");
		Tooltip.install(lowerThresholdSlider, tooltip);
		Tooltip.install(lowerThresholdField, tooltip);

		tooltip = new Tooltip(
				"Only detect filaments on the current frame (use for quick detection parameters tuning).");
		Tooltip.install(detectCurrentFrameButton, tooltip);

		tooltip = new Tooltip("Perform detection each time a parameter is modified.");
		Tooltip.install(liveDetectionButton, tooltip);
	}

}
