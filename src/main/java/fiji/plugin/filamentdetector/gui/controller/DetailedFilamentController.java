package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

import fiji.plugin.filamentdetector.model.Filament;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class DetailedFilamentController extends AbstractController implements Initializable {

	@FXML
	private Label idLabel;

	@FXML
	private Label frameLabel;

	@FXML
	private Label lenghtLabel;

	@FXML
	private Label sizeLabel;

	@FXML
	private Label sinuosityLabel;

	@FXML
	private Label colorLabel;

	@FXML
	public Button removeFilamentLabel;

	private Filament filament;

	public DetailedFilamentController(Filament filament) {
		this.filament = filament;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		DecimalFormat f = new DecimalFormat("##.00");

		idLabel.setText(Integer.toString(filament.getId()));
		frameLabel.setText(Integer.toString(filament.getFrame()));
		lenghtLabel.setText(f.format(filament.getLength()));
		sizeLabel.setText(Integer.toString(filament.getSize()));
		sinuosityLabel.setText(f.format(filament.getSinuosity()));
		colorLabel.setStyle("-fx-background-color:" + filament.getColorAsHex());
	}

	public Button getRemoveFilamentLabel() {
		return removeFilamentLabel;
	}

	public Filament getFilament() {
		return filament;
	}
}
