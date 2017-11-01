package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.FilamentWorkflow;
import fiji.plugin.filamentdetector.event.ImageNotFoundEvent;
import fiji.plugin.filamentdetector.event.PreventPanelSwitchEvent;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import fiji.plugin.filamentdetector.overlay.ImageDisplayMode;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class MainController extends AbstractController implements Initializable {

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

	@FXML
	Button compositeButton;

	private FilamentWorkflow filamentWorkflow;

	private WelcomeController welcomeController;
	private ImagePreprocessorsController imagePreprocessorController;
	private DetectFilamentController detectFilamentController;
	private AboutController aboutController;
	private DataExporterController dataExporterController;
	private TrackingFilamentController trackingFilamentController;
	private KymographBuilderController kymographBuilderController;
	private AnalyzeController analyzerController;

	private static boolean originalImageDisapearsMessageShown = false;

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
					} else if (new_val.getContent().equals(detectFilamentController.getPane())) {
						detectFilamentController.initPane();
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
		overlay.setImageDisplay(filamentWorkflow.getImageDisplay());
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

		compositeButton.setOnAction((event) -> {
			String compositeText = "Composite View";
			String colorText = "One Color View";
			String grayText = "Gray View";

			if (compositeButton.getText().equals(compositeText)) {
				overlay.setViewMode(ImageDisplayMode.COMPOSITE);
				compositeButton.setText(colorText);
			} else if (compositeButton.getText().equals(colorText)) {
				overlay.setViewMode(ImageDisplayMode.COLOR);
				compositeButton.setText(grayText);
			} else if (compositeButton.getText().equals(grayText)) {
				overlay.setViewMode(ImageDisplayMode.GRAYSCALE);
				compositeButton.setText(compositeText);
			} else {
				compositeButton.setText(colorText);
			}
		});
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
		Pane pane = welcomeController.loadPane();
		mainPane.getPanes().add(new TitledPane("Welcome", pane));
	}

	public void loadImagePreprocessor() {
		imagePreprocessorController = new ImagePreprocessorsController(context, filamentWorkflow);
		Pane pane = imagePreprocessorController.loadPane();
		mainPane.getPanes().add(new TitledPane("Preprocessing", pane));
	}

	public void loadDetectFilament() {
		detectFilamentController = new DetectFilamentController(context, filamentWorkflow);
		Pane pane = detectFilamentController.loadPane();
		mainPane.getPanes().add(new TitledPane("Detect Filaments", pane));
	}

	public void loadTrackingFilament() {
		trackingFilamentController = new TrackingFilamentController(context, filamentWorkflow);
		Pane pane = trackingFilamentController.loadPane();
		mainPane.getPanes().add(new TitledPane("Track Filaments", pane));
	}

	public void loadDataExporter() {
		dataExporterController = new DataExporterController(context, filamentWorkflow);
		Pane pane = dataExporterController.loadPane();
		mainPane.getPanes().add(new TitledPane("Export Filaments", pane));
	}

	public void loadKymographBuilder() {
		kymographBuilderController = new KymographBuilderController(context, filamentWorkflow);
		Pane pane = kymographBuilderController.loadPane();
		mainPane.getPanes().add(new TitledPane("Build Kymographs", pane));
	}

	public void loadAnalyzer() {
		analyzerController = new AnalyzeController(context, filamentWorkflow);
		Pane pane = analyzerController.loadPane();
		mainPane.getPanes().add(new TitledPane("Analyze Filaments", pane));
	}

	public void loadAbout() {
		aboutController = new AboutController(context);
		Pane pane = aboutController.loadPane();
		mainPane.getPanes().add(new TitledPane("About FilamentDetector", pane));
	}

	@EventHandler
	private void disableInterface(ImageNotFoundEvent event) {
		Platform.runLater(() -> {
			if (!originalImageDisapearsMessageShown) {
				originalImageDisapearsMessageShown = true;
				getPane().setDisable(true);
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information Dialog");
				Label label = new Label("It looks like the image has been closed. Please restart the plugin.");
				label.setWrapText(true);
				alert.getDialogPane().setContent(label);
				alert.showAndWait();
			}
		});
	}

	@EventHandler
	private void preventPanelSwitch(PreventPanelSwitchEvent event) {
		if (event.getPreventPaneSwitch()) {
			for (TitledPane pane : mainPane.getPanes()) {
				pane.setDisable(true);
			}
		} else {
			for (TitledPane pane : mainPane.getPanes()) {
				pane.setDisable(false);
			}
		}
	}

}
