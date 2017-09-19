package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.FilamentDetector;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.gui.GUIUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;

public class MainController extends Controller implements Initializable {

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private GUIStatusService status;

	@FXML
	private Accordion mainPane;

	@FXML
	private TextArea logField;

	private WelcomeController welcomeController;
	private DetectFilamentController detectFilamentController;
	private AboutController aboutController;
	private FilamentDetector filamentDetector;

	public MainController(Context context, FilamentDetector filamentDetector) {
		context.inject(this);
		this.filamentDetector = filamentDetector;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
	}

	public void loadPanes() {

		// Allow GUIStatusService to display message in the log window
		status.setTextField(logField);

		// Load all the panes
		loadWelcome();
		loadDetectFilament();
		loadTrackingFilament();
		loadDataExporter();
		loadKymographBuilder();
		loadAnalyzer();
		loadAbout();

		// Enable the first welcome pane
		mainPane.setExpandedPane(getTitledPane("Welcome"));
		welcomeController.loadImageCalibrations();

		status.showStatus("FilamentDetector has been correctly initialized.");
	}

	public TitledPane getTitledPane(String text) {
		return mainPane.getPanes().stream().filter(x -> x.getText().equals(text)).findFirst().orElse(null);
	}

	public void loadWelcome() {
		welcomeController = new WelcomeController(context, filamentDetector);
		Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/WelcomeView.fxml", welcomeController);

		TitledPane titledPane = new TitledPane("Welcome", pane);
		mainPane.getPanes().add(titledPane);
	}

	public void loadDetectFilament() {
		detectFilamentController = new DetectFilamentController(context, filamentDetector);
		Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/DetectFilamentView.fxml",
				detectFilamentController);

		TitledPane titledPane = new TitledPane("Detect Filaments", pane);
		mainPane.getPanes().add(titledPane);
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
		Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/AnalyzeView.fxml", controller);

		TitledPane titledPane = new TitledPane("Analyze the Data", pane);
		mainPane.getPanes().add(titledPane);
	}

	public void loadAbout() {
		aboutController = new AboutController(context);
		Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/AboutView.fxml", aboutController);

		TitledPane titledPane = new TitledPane("About FilamentDetector", pane);
		mainPane.getPanes().add(titledPane);
	}

}
