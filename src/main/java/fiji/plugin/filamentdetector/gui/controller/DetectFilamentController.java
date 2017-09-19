
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
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

public class DetectFilamentController extends Controller implements Initializable {

	@Parameter
	private GUIStatusService status;

	@Parameter
	private LogService log;

	@FXML
	private Slider sigmaSlider;

	@FXML
	private TextField sigmaField;

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
				status.showStatus(event.getSource().toString());
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
	}

}
