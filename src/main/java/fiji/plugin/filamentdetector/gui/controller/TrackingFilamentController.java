package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.FilamentDetector;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import javafx.fxml.Initializable;

public class TrackingFilamentController extends Controller implements Initializable {

	@Parameter
	private Context context;

	@Parameter
	private GUIStatusService status;

	@Parameter
	private EventService eventService;

	@Parameter
	private LogService log;

	private FilamentDetector filamentDetector;

	public TrackingFilamentController(Context context, FilamentDetector filamentDetector) {
		context.inject(this);
		this.filamentDetector = filamentDetector;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

	}

	public void initPane() {
		if (this.filamentDetector.getDataset().getFrames() == 1) {
			status.showStatus("The image only has 1 timepoints. Tracking can't be done.");
			this.getPane().setDisable(true);
		} else if (this.filamentDetector.getFilaments() == null || this.filamentDetector.getFilaments().size() == 0) {
			status.showStatus("No filaments detected. Please use the \"Detect Filaments\" panel first.");
			this.getPane().setDisable(true);
		} else {
			this.getPane().setDisable(false);
		}
	}

}
