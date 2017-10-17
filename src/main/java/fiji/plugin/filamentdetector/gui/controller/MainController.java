package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.FilamentWorkflow;
import fiji.plugin.filamentdetector.event.ImageNotFoundEvent;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.gui.GUIUtils;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
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
	private CheckBox disableOverlaysCheckbox;

	@FXML
	private CheckBox drawBoundsCheckbox;

	@FXML
	private CheckBox drawPlusTipsCheckbox;

	@FXML
	private CheckBox drawMinusTipsCheckbox;

	@FXML
	private Slider tipDiameterSlider;

	@FXML
	private Label tipDiameterValueLabel;

	private FilamentWorkflow filamentWorkflow;

	private WelcomeController welcomeController;
	private ImagePreprocessorController imagePreprocessorController;
	private DetectFilamentController detectFilamentController;
	private AboutController aboutController;
	private DataExporterController dataExporterController;
	private TrackingFilamentController trackingFilamentController;
	private KymographBuilderController kymographBuilderController;
	private AnalyzeController analyzerController;

	public MainController(Context context, FilamentWorkflow filamentDetector) {
		context.inject(this);
		this.filamentWorkflow = filamentDetector;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {

		// Call children controller methods when their respective panes are expanded.
		mainPane.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
			@Override
			public void changed(ObservableValue<? extends TitledPane> ov, TitledPane old_val, TitledPane new_val) {
				if (new_val != null && new_val.getContent() != null) {
					if (new_val.getContent().equals(dataExporterController.getPane())) {
						dataExporterController.refreshData(null);
					} else if (new_val.getContent().equals(trackingFilamentController.getPane())) {
						trackingFilamentController.initPane();
					} else if (new_val.getContent().equals(kymographBuilderController.getPane())) {
						kymographBuilderController.initPane();
					} else if (new_val.getContent().equals(analyzerController.getPane())) {
						analyzerController.initPane();
					}
				}
			}
		});
	}

	public void loadPanes() {

		// Allow GUIStatusService to display message in the log window
		status.setTextField(logField);

		// Load all the panes
		loadWelcome();
		loadImagePreprocessor();
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

		tipDiameterSlider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				if (newValue == null) {
					tipDiameterValueLabel.setText("");
					return;
				}
				tipDiameterValueLabel.setText(Math.round(newValue.intValue()) + "");
			}
		});

		transparencySlider.setValue(overlay.getColorAlpha());
		lineWidthSlider.setValue(overlay.getFilamentWidth());
		tipDiameterSlider.setValue(overlay.getTipDiameter());

		overlay.disableOverlay(false);
		disableOverlaysCheckbox.setSelected(false);
		drawBoundsCheckbox.setSelected(overlay.isDrawBoundingBoxes());

		drawPlusTipsCheckbox.setSelected(overlay.isDrawPlusTips());
		drawMinusTipsCheckbox.setSelected(overlay.isDrawMinusTips());
	}

	@FXML
	void updateOverlaySettings(Event event) {

		if (event.getSource().equals(transparencySlider)) {
			overlay.setColorAlpha((int) transparencySlider.getValue());
			overlay.updateTransparency();

		} else if (event.getSource().equals(lineWidthSlider)) {
			overlay.setFilamentWidth((int) lineWidthSlider.getValue());
			overlay.updateLineWidth();

		} else {
			overlay.setTipDiameter((int) tipDiameterSlider.getValue());
			overlay.setDrawBoundingBoxes(drawBoundsCheckbox.isSelected());
			overlay.setDrawPlusTips(drawPlusTipsCheckbox.isSelected());
			overlay.setDrawMinusTips(drawMinusTipsCheckbox.isSelected());
			overlay.refresh();
		}
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
		welcomeController = new WelcomeController(context, filamentWorkflow);
		Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/WelcomeView.fxml", welcomeController);

		TitledPane titledPane = new TitledPane("Welcome", pane);
		mainPane.getPanes().add(titledPane);
	}
	
	public void loadImagePreprocessor() {
		imagePreprocessorController = new ImagePreprocessorController(context, filamentWorkflow);
		Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/ImagePreprocessorView.fxml", imagePreprocessorController);

		TitledPane titledPane = new TitledPane("Preprocessing", pane);
		mainPane.getPanes().add(titledPane);
	}

	public void loadDetectFilament() {
		detectFilamentController = new DetectFilamentController(context, filamentWorkflow);
		Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/detection/DetectFilamentView.fxml",
				detectFilamentController);

		TitledPane titledPane = new TitledPane("Detect Filaments", pane);
		mainPane.getPanes().add(titledPane);
	}

	public void loadTrackingFilament() {
		trackingFilamentController = new TrackingFilamentController(context, filamentWorkflow);
		Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/tracking/TrackingFilamentView.fxml",
				trackingFilamentController);

		TitledPane titledPane = new TitledPane("Track Filaments", pane);
		mainPane.getPanes().add(titledPane);
	}

	public void loadDataExporter() {
		dataExporterController = new DataExporterController(context, filamentWorkflow);
		Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/DataExporterView.fxml",
				dataExporterController);

		TitledPane titledPane = new TitledPane("Export Data", pane);
		mainPane.getPanes().add(titledPane);
	}

	public void loadKymographBuilder() {
		kymographBuilderController = new KymographBuilderController(context, filamentWorkflow);
		Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/KymographBuilderView.fxml",
				kymographBuilderController);

		TitledPane titledPane = new TitledPane("Build Kymographs", pane);
		mainPane.getPanes().add(titledPane);
	}

	public void loadAnalyzer() {
		analyzerController = new AnalyzeController(context, filamentWorkflow);
		Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/AnalyzerView.fxml", analyzerController);

		TitledPane titledPane = new TitledPane("Analyze the Data", pane);
		mainPane.getPanes().add(titledPane);
	}

	public void loadAbout() {
		aboutController = new AboutController(context);
		Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/AboutView.fxml", aboutController);

		TitledPane titledPane = new TitledPane("About FilamentDetector", pane);
		mainPane.getPanes().add(titledPane);
	}

	@EventHandler
	private void disableInterface(ImageNotFoundEvent event) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				getPane().setDisable(true);
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information Dialog");
				alert.setContentText("It looks like the original image disapears. Please restart the plugin.");
				alert.showAndWait();
			}
		});

	}

}
