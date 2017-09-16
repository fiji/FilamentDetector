package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.gui.MainAppFrame;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;

public class MainController extends Controller implements Initializable {

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@FXML
	private Accordion mainPane;

	public MainController(Context context) {
		context.inject(this);
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	}

	public void loadFilamentViewer() {
		FilamentViewerController controller = new FilamentViewerController(context);
		Pane pane = MainAppFrame.loadFXML("/fiji/plugin/filamentdetector/gui/view/FilamentViewer.fxml", controller);
		
		TitledPane titledPane = new TitledPane("Filament Viewer", pane);
		mainPane.getPanes().add(titledPane);
		
		controller.test();
	}

}
