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

	public void loadPanes() {
		loadWelcome();
		loadDetectFilament();
		loadTrackingFilament();
		loadDataExporter();
		loadKymographBuilder();
		loadAnalyzer();
		loadAbout();
	}
	
	public void loadWelcome() {
		Controller controller = null;
		Pane pane = MainAppFrame.loadFXML("/fiji/plugin/filamentdetector/gui/view/WelcomeView.fxml", controller);
		
		TitledPane titledPane = new TitledPane("Welcome", pane);
		mainPane.getPanes().add(titledPane);
	}

	public void loadDetectFilament() {
		DetectFilamentController controller = new DetectFilamentController(context);
		Pane pane = MainAppFrame.loadFXML("/fiji/plugin/filamentdetector/gui/view/DetectFilamentView.fxml", controller);

		TitledPane titledPane = new TitledPane("Detect Filaments", pane);
		mainPane.getPanes().add(titledPane);

		controller.test();
	}

	public void loadTrackingFilament() {
		TitledPane titledPane = new TitledPane();
		titledPane.setText("Track Filaments");
		mainPane.getPanes().add(titledPane);
	}
	
	public void loadDataExporter() {
		TitledPane titledPane = new TitledPane();
		titledPane.setText("Export Data");
		mainPane.getPanes().add(titledPane);
	}
	
	public void loadKymographBuilder() {
		TitledPane titledPane = new TitledPane();
		titledPane.setText("Build Kymographs");
		mainPane.getPanes().add(titledPane);
	}
	
	public void loadAnalyzer() {
		Controller controller = null;
		Pane pane = MainAppFrame.loadFXML("/fiji/plugin/filamentdetector/gui/view/AnalyzeView.fxml", controller);
		
		TitledPane titledPane = new TitledPane("Analyze the Data", pane);
		mainPane.getPanes().add(titledPane);
	}
	
	public void loadAbout() {
		Controller controller = null;
		Pane pane = MainAppFrame.loadFXML("/fiji/plugin/filamentdetector/gui/view/AboutView.fxml", controller);
		
		TitledPane titledPane = new TitledPane("About FilamentDetector", pane);
		mainPane.getPanes().add(titledPane);
	}

}
