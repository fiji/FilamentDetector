package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.FilamentDetector;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.gui.GUIUtils;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class MainController extends Controller implements Initializable {

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private GUIStatusService status;

	@Parameter
	private FilamentOverlayService overlay;

	@FXML
	private Accordion mainPane;

	@FXML
	private TextArea logField;

	@FXML
	private Slider transparencySlider;

	@FXML
	private Label transparencyValueLabel;

	@FXML
	private Slider lineWidthSlider;

	@FXML
	private Label lineWidthValueLabel;

	@FXML
	private ColorPicker colorChooser;

	@FXML
	private Label colorValueLabel;

	@FXML
	private CheckBox disableOverlaysCheckbox;

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

		// Initialize overlay settings UI
		initOverlaySettings();

		// Enable the first welcome pane
		mainPane.setExpandedPane(getTitledPane("Welcome"));
		welcomeController.loadImageCalibrations();

		status.showStatus("FilamentDetector has been correctly initialized.");
	}

	private void initOverlaySettings() {

		transparencySlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				if (newValue == null) {
					transparencyValueLabel.setText("");
					return;
				}
				transparencyValueLabel.setText(Math.round(newValue.intValue()) + "");
			}
		});

		lineWidthSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				if (newValue == null) {
					lineWidthValueLabel.setText("");
					return;
				}
				lineWidthValueLabel.setText(Math.round(newValue.intValue()) + "");
			}
		});

		transparencySlider.setValue(overlay.getColorAlpha());
		lineWidthSlider.setValue(overlay.getFilamentWidth());
		colorChooser.setValue(overlay.getFilamentColorAsJavaFX());

		overlay.disableOverlay(false);
		disableOverlaysCheckbox.setSelected(false);
	}

	@FXML
	void updateOverlaySettings(Event event) {
		overlay.setColorAlpha((int) transparencySlider.getValue());
		overlay.setFilamentWidth((int) lineWidthSlider.getValue());
		overlay.setFilamentColor(colorChooser.getValue());
		overlay.refresh();
	}

	@FXML
	void exportToROIManager(MouseEvent event) {
		overlay.exportToROIManager();
	}

	@FXML
	void updateHideOverlay(MouseEvent event) {
		overlay.disableOverlay(!disableOverlaysCheckbox.isSelected());
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
