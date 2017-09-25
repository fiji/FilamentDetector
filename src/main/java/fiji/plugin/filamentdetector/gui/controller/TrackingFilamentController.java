package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;

import fiji.plugin.filamentdetector.FilamentDetector;
import javafx.fxml.Initializable;

public class TrackingFilamentController extends Controller implements Initializable {

	private FilamentDetector filamentDetector;

	public TrackingFilamentController(Context context, FilamentDetector filamentDetector) {
		context.inject(this);
		this.filamentDetector = filamentDetector;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

	}

}
