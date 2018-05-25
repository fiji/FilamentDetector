/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2017 Hadrien Mary
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package sc.fiji.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import sc.fiji.filamentdetector.FilamentWorkflow;
import sc.fiji.filamentdetector.event.ImageNotFoundEvent;
import sc.fiji.filamentdetector.event.PreventPanelSwitchEvent;
import sc.fiji.filamentdetector.gui.GUIStatusService;
import sc.fiji.filamentdetector.overlay.FilamentOverlayService;
import sc.fiji.filamentdetector.overlay.ImageDisplayMode;

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
	private AnchorPane mainPaneContainer;

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
	private Button compositeButton;

	@FXML
	private Button autoScaleButton;

	private FilamentWorkflow filamentWorkflow;

	private Accordion mainPane;

	private WelcomeController welcomeController;
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

	}

	public void loadPanes() {

		ProgressIndicator startupProgress = new ProgressIndicator();
		this.mainPaneContainer.getChildren().add(startupProgress);
		AnchorPane.setTopAnchor(startupProgress, 300.0);
		AnchorPane.setLeftAnchor(startupProgress, 300.0);
		AnchorPane.setRightAnchor(startupProgress, 300.0);
		AnchorPane.setBottomAnchor(startupProgress, 300.0);

		Task<Accordion> startupTask = new Task<Accordion>() {

			@Override
			protected Accordion call() throws Exception {

				Accordion mainPane = new Accordion();
				AnchorPane.setTopAnchor(mainPane, 0.0);
				AnchorPane.setLeftAnchor(mainPane, 0.0);
				AnchorPane.setRightAnchor(mainPane, 0.0);
				AnchorPane.setBottomAnchor(mainPane, 0.0);

				// Load all the panes
				Platform.runLater(() -> {
					mainPane.getPanes().add(loadWelcome());
					mainPane.getPanes().add(loadDetectFilament());
					mainPane.getPanes().add(loadTrackingFilament());
					mainPane.getPanes().add(loadDataExporter());
					mainPane.getPanes().add(loadKymographBuilder());
					mainPane.getPanes().add(loadAnalyzer());
					mainPane.getPanes().add(loadAbout());

					// Initialize overlay settings UI
					overlay.reset();
					overlay.setImageDisplay(filamentWorkflow.getImageDisplay());
					initOverlaySettings();

				});

				setMainPane(mainPane);
				return mainPane;
			}
		};
		startupTask.setOnSucceeded(evt -> {
			// Enable the first welcome pane
			mainPane.setExpandedPane(getTitledPane("Welcome"));
			welcomeController.loadImageCalibrations();

			mainPaneContainer.getChildren().clear();
			mainPaneContainer.getChildren().add(mainPane);
			status.showStatus("FilamentDetector has been correctly initialized.");

			// Call children controller methods when their respective panes are expanded.
			this.mainPane.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {
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
		});

		startupProgress.progressProperty().bind(startupTask.progressProperty());
		new Thread(startupTask).start();

		// Allow GUIStatusService to display message in the log window
		status.setTextField(logField);
	}

	private void setMainPane(Accordion mainPane) {
		this.mainPane = mainPane;
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

		autoScaleButton.setOnAction((event) -> {
			overlay.autoScaleImage();
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

	public TitledPane loadWelcome() {
		welcomeController = new WelcomeController(context, filamentWorkflow);
		Pane pane = welcomeController.loadPane();
		return new TitledPane("Welcome", pane);
	}

	public TitledPane loadDetectFilament() {
		detectFilamentController = new DetectFilamentController(context, filamentWorkflow);
		Pane pane = detectFilamentController.loadPane();
		return new TitledPane("Detect Filaments", pane);
	}

	public TitledPane loadTrackingFilament() {
		trackingFilamentController = new TrackingFilamentController(context, filamentWorkflow);
		Pane pane = trackingFilamentController.loadPane();
		return new TitledPane("Track Filaments", pane);
	}

	public TitledPane loadDataExporter() {
		dataExporterController = new DataExporterController(context, filamentWorkflow);
		Pane pane = dataExporterController.loadPane();
		return new TitledPane("Export Filaments", pane);
	}

	public TitledPane loadKymographBuilder() {
		kymographBuilderController = new KymographBuilderController(context, filamentWorkflow);
		Pane pane = kymographBuilderController.loadPane();
		return new TitledPane("Build Kymographs", pane);
	}

	public TitledPane loadAnalyzer() {
		analyzerController = new AnalyzeController(context, filamentWorkflow);
		Pane pane = analyzerController.loadPane();
		return new TitledPane("Analyze Filaments", pane);
	}

	public TitledPane loadAbout() {
		aboutController = new AboutController(context);
		Pane pane = aboutController.loadPane();
		return new TitledPane("About FilamentDetector", pane);
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
