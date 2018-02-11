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
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import ij.gui.Line;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import sc.fiji.filamentdetector.FilamentWorkflow;
import sc.fiji.filamentdetector.detection.FilamentDetector;
import sc.fiji.filamentdetector.detection.FilamentDetectorService;
import sc.fiji.filamentdetector.detection.FilteringParameters;
import sc.fiji.filamentdetector.detection.IJ2RidgeDetectionFilamentDetector;
import sc.fiji.filamentdetector.detection.RidgeDetectionFilamentDetector;
import sc.fiji.filamentdetector.event.FilterFilamentEvent;
import sc.fiji.filamentdetector.event.PreventPanelSwitchEvent;
import sc.fiji.filamentdetector.gui.GUIStatusService;
import sc.fiji.filamentdetector.gui.controller.detection.FilamentDetectorController;
import sc.fiji.filamentdetector.gui.controller.detection.IJ2RidgeDetectionFilamentDetectorController;
import sc.fiji.filamentdetector.gui.controller.detection.RidgeDetectionFilamentDetectorController;
import sc.fiji.filamentdetector.gui.event.DetectionParametersUpdatedEvent;
import sc.fiji.filamentdetector.gui.fxwidgets.UpperLowerSynchronizer;
import sc.fiji.filamentdetector.gui.view.FilamentsTableView;
import sc.fiji.filamentdetector.model.Filament;
import sc.fiji.filamentdetector.overlay.FilamentOverlayService;

public class DetectFilamentController extends AbstractController implements Initializable {

	private static String FXML_PATH = "/sc/fiji/filamentdetector/gui/view/DetectFilamentView.fxml";

	@Parameter
	private Context context;

	@Parameter
	private GUIStatusService status;

	@Parameter
	private EventService eventService;

	@Parameter
	private LogService log;

	@Parameter
	private FilamentOverlayService overlayService;

	@Parameter
	private FilamentDetectorService filamentDetectorService;

	@FXML
	private ComboBox<FilamentDetector> detectorComboBox;

	@FXML
	private AnchorPane detectorParametersPane;

	@FXML
	private Button detectButton;

	@FXML
	private CheckBox detectCurrentFrameButton;

	@FXML
	private CheckBox liveDetectionButton;

	@FXML
	private ProgressIndicator detectionProgressIndicator;

	@FXML
	private VBox filamentViewContainer;

	@FXML
	private AnchorPane detailViewContainer;

	@FXML
	private Slider maxLengthSlider;

	@FXML
	private TextField maxLengthField;

	@FXML
	private Slider minLengthSlider;

	@FXML
	private TextField minLengthField;

	@FXML
	private Slider maxSinuositySlider;

	@FXML
	private TextField maxSinuosityField;

	@FXML
	private Slider minSinuositySlider;

	@FXML
	private TextField minSinuosityField;

	@FXML
	private CheckBox simplifyFilamentsCheckbox;

	@FXML
	private CheckBox disableFilteringBox;

	@FXML
	private TextField simplifyToleranceDistanceField;

	private FilamentsTableView filamentsTableView;

	private FilamentWorkflow filamentWorkflow;

	private Thread detectionThread;
	private Task<Integer> detectionTask;

	private Thread filterThread;
	private Task<Integer> filterTask;

	private FilteringParameters filteringParameters;

	private UpperLowerSynchronizer lengthSync;
	private UpperLowerSynchronizer sinuositySync;

	private List<FilamentDetector> filamentDetectors;

	public DetectFilamentController(Context context, FilamentWorkflow filamentWorkflow) {
		context.inject(this);
		setFXMLPath(FXML_PATH);
		this.filamentWorkflow = filamentWorkflow;
		this.filamentDetectors = filamentDetectorService.getDetectors();
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		this.detectionProgressIndicator.setVisible(false);

		this.initDetectionComboBox();

		detectCurrentFrameButton.setSelected(filamentWorkflow.getFilamentDetector().isDetectOnlyCurrentFrame());
		simplifyFilamentsCheckbox.setSelected(filamentWorkflow.getFilamentDetector().isSimplifyFilaments());
		simplifyToleranceDistanceField
				.setText(Double.toString(filamentWorkflow.getFilamentDetector().getSimplifyToleranceDistance()));

		// Fill filtering fields
		filteringParameters = new FilteringParameters();
		filteringParameters.setDisableFiltering(true);

		lengthSync = new UpperLowerSynchronizer(minLengthSlider, minLengthField, maxLengthSlider, maxLengthField);
		lengthSync.setLowerValue(filteringParameters.getMinLength());
		lengthSync.setUpperValue(filteringParameters.getMaxLength());

		sinuositySync = new UpperLowerSynchronizer(minSinuositySlider, minSinuosityField, maxSinuositySlider,
				maxSinuosityField);
		sinuositySync.setLowerValue(filteringParameters.getMinSinuosity());
		sinuositySync.setUpperValue(filteringParameters.getMaxSinuosity());

		disableFilteringBox.setSelected(filteringParameters.isDisableFiltering());

		Tooltip tooltip;
		tooltip = new Tooltip(
				"Only detect filaments on the current frame (use for quick detection parameters tuning).");
		Tooltip.install(detectCurrentFrameButton, tooltip);

		tooltip = new Tooltip("Perform detection each time a parameter is modified.");
		Tooltip.install(liveDetectionButton, tooltip);

		// Initialize filaments list
		filamentsTableView = new FilamentsTableView(context, filamentWorkflow.getFilaments());
		filamentViewContainer.getChildren().add(0, filamentsTableView.getInfoPane());
		filamentViewContainer.getChildren().add(1, filamentsTableView);
		detailViewContainer.getChildren().add(filamentsTableView.getDetailPane());
	}

	public void initPane() {
		this.detectorComboBox.getSelectionModel().select(0);
		status.showStatus("Initialize detection.");
	}

	private void setFilamentDetector(FilamentDetector filamentDetector) {
		this.filamentWorkflow.setFilamentDetector(filamentDetector);

		FilamentDetectorController controller = null;
		if (filamentDetector.getClass().equals(RidgeDetectionFilamentDetector.class)) {
			controller = new RidgeDetectionFilamentDetectorController(context, filamentDetector);
			
		} else if (filamentDetector.getClass().equals(IJ2RidgeDetectionFilamentDetector.class)) {
			controller = new IJ2RidgeDetectionFilamentDetectorController(context, filamentDetector);
			
		} else {
			log.error("Can't load FilamentDetector parameters pane.");
		}

		if (controller != null) {
			detectorParametersPane.getChildren().clear();
			detectorParametersPane.getChildren().add(controller.loadPane());
		}
	}

	public void initDetectionComboBox() {

		Callback<ListView<FilamentDetector>, ListCell<FilamentDetector>> cellFactory = new Callback<ListView<FilamentDetector>, ListCell<FilamentDetector>>() {
			@Override
			public ListCell<FilamentDetector> call(ListView<FilamentDetector> p) {
				return new ListCell<FilamentDetector>() {
					@Override
					protected void updateItem(FilamentDetector t, boolean bln) {
						super.updateItem(t, bln);
						if (t != null) {
							setText(t.getName());
						} else {
							setText(null);
						}
					}
				};
			}
		};

		this.detectorComboBox.setButtonCell(cellFactory.call(null));
		this.detectorComboBox.setCellFactory(cellFactory);

		this.detectorComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			if (newValue != null) {
				setFilamentDetector(newValue);
			} else {
				setFilamentDetector(this.filamentDetectors.get(0));
			}
		});

		this.detectorComboBox.setItems(FXCollections.observableList(this.filamentDetectors));
		this.detectorComboBox.getSelectionModel().selectFirst();
	}

	private void updateFilamentsList() {
		filamentsTableView.setFilaments(filamentWorkflow.getFilaments());
	}

	@FXML
	public void importLinesfromROIManager(MouseEvent event) {

		updateFilamentsList();

		RoiManager rm = RoiManager.getInstance();

		if (rm == null) {
			status.showStatus("Roi Manager not found. Press 't' to add a line to it.");
			return;
		}

		int total = 0;
		int i = 1;
		Filament filament = null;
		for (Roi roi : rm.getRoisAsArray()) {

			if (roi.getType() == Roi.FREELINE || roi.getType() == Roi.POLYLINE) {

				PolygonRoi line = (PolygonRoi) roi;
				float[] x = line.getFloatPolygon().xpoints;
				float[] y = line.getFloatPolygon().ypoints;
				filament = new Filament(x, y, roi.getTPosition());
				rm.select(i);
				rm.runCommand("Delete");
				total++;
			} else if (roi.getType() == Roi.LINE) {

				Line line = (Line) roi;
				double[] x = new double[] { line.x1d, line.x2d };
				double[] y = new double[] { line.y1d, line.y2d };
				filament = new Filament(x, y, roi.getTPosition());
				rm.select(i);
				rm.runCommand("Delete");
				total++;
			}

			if (filament != null) {
				filamentsTableView.addFilament(filament);
			}
			i++;
		}

		status.showStatus(total + " filament(s) has been added.");
		eventService.publish(new FilterFilamentEvent(filteringParameters));
	}

	@FXML
	public void updateDetectionParameters(Event event) {
		if (event.getSource().equals(detectCurrentFrameButton)) {
			filamentWorkflow.getFilamentDetector().setDetectOnlyCurrentFrame(detectCurrentFrameButton.isSelected());
		}

		else if (event.getSource().equals(simplifyToleranceDistanceField)) {
			double newValue = Double.parseDouble(simplifyToleranceDistanceField.getText());
			if (newValue < 0) {
				simplifyToleranceDistanceField.setText(
						Double.toString(filamentWorkflow.getFilamentDetector().getSimplifyToleranceDistance()));
			} else {
				filamentWorkflow.getFilamentDetector().setSimplifyToleranceDistance(newValue);
			}
		}

		else if (event.getSource().equals(simplifyFilamentsCheckbox)) {
			filamentWorkflow.getFilamentDetector().setSimplifyFilaments(simplifyFilamentsCheckbox.isSelected());
		}

		if (liveDetectionButton.isSelected()) {
			this.detect(null);
		}
	}

	@EventHandler
	public void updateDetectionParameters(DetectionParametersUpdatedEvent event) {
		if (liveDetectionButton.isSelected()) {
			this.detect(null);
		}
	}

	@FXML
	public void updateFilteringParameters(Event event) {

		if (lengthSync.isEvent(event)) {
			lengthSync.update(event);
			filteringParameters.setMinLength(lengthSync.getLowerValue());
			filteringParameters.setMaxLength(lengthSync.getUpperValue());
		} else if (sinuositySync.isEvent(event)) {
			sinuositySync.update(event);
			filteringParameters.setMinSinuosity(sinuositySync.getLowerValue());
			filteringParameters.setMaxSinuosity(sinuositySync.getUpperValue());
		} else if (event.getSource().equals(disableFilteringBox)) {
			filteringParameters.setDisableFiltering(disableFilteringBox.isSelected());
		}

		eventService.publish(new FilterFilamentEvent(filteringParameters));
	}

	@FXML
	public void detect(ActionEvent event) {

		if (detectionTask != null) {
			detectionTask.cancel();
		}

		this.detectionProgressIndicator.setVisible(true);

		detectionTask = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {
				if (!liveDetectionButton.isSelected()) {
					Platform.runLater(() -> {
						eventService.publish(new PreventPanelSwitchEvent(true));
					});
				}
				if (detectCurrentFrameButton.isSelected()) {
					filamentWorkflow.detectCurrentFrame();
				} else {
					filamentWorkflow.detect();
				}

				return filamentWorkflow.getFilaments().size();
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				status.showStatus(filamentWorkflow.getFilaments().size()
						+ " filaments has been detected with the following parameters : ");
				status.showStatus(filamentWorkflow.getFilamentDetector().toString());
				detectionProgressIndicator.setVisible(false);
				updateFilamentsList();
				eventService.publish(new FilterFilamentEvent(filteringParameters));

				if (!liveDetectionButton.isSelected()) {
					eventService.publish(new PreventPanelSwitchEvent(false));
				}
			}

			@Override
			protected void cancelled() {
				super.cancelled();
				detectionProgressIndicator.setVisible(false);
				if (!liveDetectionButton.isSelected()) {
					eventService.publish(new PreventPanelSwitchEvent(false));
				}
			}

			@Override
			protected void failed() {
				super.failed();
				if (!liveDetectionButton.isSelected()) {
					eventService.publish(new PreventPanelSwitchEvent(false));
				}
				status.showStatus("Something failed during detection: ");
				StackTraceElement[] stackTrace = this.getException().getStackTrace();
				status.showStatus(
						Arrays.stream(stackTrace).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
				detectionProgressIndicator.setVisible(false);
			}
		};

		detectionThread = new Thread(detectionTask);
		detectionThread.setDaemon(true);
		detectionThread.start();
	}

	@FXML
	public void filter(ActionEvent event) {
		eventService.publish(new FilterFilamentEvent(filteringParameters));
	}

	@EventHandler
	public void filter(FilterFilamentEvent event) {

		if (filterTask != null) {
			filterTask.cancel();
		}

		if (filterThread != null) {
			filterThread.stop();
		}

		filterTask = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {

				Platform.runLater(() -> {
					if (!liveDetectionButton.isSelected()) {
						eventService.publish(new PreventPanelSwitchEvent(true));
					}
					filamentWorkflow.filterFilament(event.getFilteringParameters());
					updateFilamentsList();
				});

				return filamentWorkflow.getFilaments().size();
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				if (!liveDetectionButton.isSelected()) {
					Platform.runLater(() -> {
						eventService.publish(new PreventPanelSwitchEvent(false));
					});
				}
				if (!filteringParameters.isDisableFiltering()) {
					status.showStatus("Filtering with the following parameters : ");
					status.showStatus(filteringParameters.toString());
					status.showStatus(filamentWorkflow.getFilaments().size() + " filament(s) remain.");
				} else {
					status.showStatus("Filtering is disabled. " + filamentWorkflow.getFilaments().size()
							+ " filaments detected.");
				}
			}

			@Override
			protected void cancelled() {
				super.cancelled();
				if (!liveDetectionButton.isSelected()) {
					eventService.publish(new PreventPanelSwitchEvent(false));
				}
			}

			@Override
			protected void failed() {
				super.failed();
				if (!liveDetectionButton.isSelected()) {
					eventService.publish(new PreventPanelSwitchEvent(false));
				}
				status.showStatus("Something failed during filtering: ");
				StackTraceElement[] stackTrace = this.getException().getStackTrace();
				status.showStatus(
						Arrays.stream(stackTrace).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
			}
		};

		filterThread = new Thread(filterTask);
		filterThread.setDaemon(true);
		filterThread.start();
	}

	@FXML
	public void liveDetectionClicked(MouseEvent event) {
		if (liveDetectionButton.isSelected()) {
			detectButton.setDisable(true);
			this.detect(null);
		} else {
			detectButton.setDisable(false);
		}
	}

}
