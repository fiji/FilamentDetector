package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import fiji.plugin.filamentdetector.model.TrackedFilament;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DetailedTrackedFilamentController extends AbstractController implements Initializable {

	@FXML
	private Label idLabel;

	@FXML
	private Label sizeLabel;

	@FXML
	private Label colorLabel;

	@FXML
	public Button removeFilamentLabel;

	private TrackedFilament trackedFilament;

	public DetailedTrackedFilamentController(TrackedFilament trackedFilament) {
		this.trackedFilament = trackedFilament;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		DecimalFormat f = new DecimalFormat("##.00");

		idLabel.setText(Integer.toString(trackedFilament.getId()));
		sizeLabel.setText(Integer.toString(trackedFilament.size()));
		colorLabel.setStyle("-fx-background-color:" + trackedFilament.getColorAsHex());
	}

	public Button getRemoveFilamentLabel() {
		return removeFilamentLabel;
	}

	public TrackedFilament getTrackedFilament() {
		return trackedFilament;
	}
}
