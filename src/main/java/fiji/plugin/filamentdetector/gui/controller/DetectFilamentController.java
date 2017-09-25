
package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.FilamentDetector;
import fiji.plugin.filamentdetector.detection.FilteringParameters;
import fiji.plugin.filamentdetector.event.FilterFilamentEvent;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.gui.controller.helper.SliderLabelSynchronizer;
import fiji.plugin.filamentdetector.gui.controller.helper.UpperLowerSynchronizer;
import fiji.plugin.filamentdetector.gui.view.FilamentsTableView;
import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import ij.gui.Line;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class DetectFilamentController extends Controller implements Initializable {

	@Parameter
	private Context context;

	@Parameter
	private GUIStatusService status;

	@Parameter
	private EventService eventService;

	@Parameter
	private LogService log;

	@Parameter
	FilamentOverlayService overlayService;

	@FXML
	private Slider sigmaSlider;

	@FXML
	private TextField sigmaField;

	@FXML
	private Slider lowerThresholdSlider;

	@FXML
	private TextField lowerThresholdField;

	@FXML
	private Slider upperThresholdSlider;

	@FXML
	private TextField upperThresholdField;

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

	private FilamentDetector filamentDetector;

	private Thread detectionThread;
	private Task<Integer> detectionTask;

	private Thread filterThread;
	private Task<Integer> filterTask;

	private FilteringParameters filteringParameters;

	private SliderLabelSynchronizer sigmaSync;
	private UpperLowerSynchronizer thresholdSync;
	private UpperLowerSynchronizer lengthSync;
	private UpperLowerSynchronizer sinuositySync;

	public DetectFilamentController(Context context, FilamentDetector filamentDetector) {
		context.inject(this);
		this.filamentDetector = filamentDetector;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		this.detectionProgressIndicator.setVisible(false);

		this.filamentDetector.initDetection();

		// Fill fields with default values
		sigmaSync = new SliderLabelSynchronizer(sigmaSlider, sigmaField);
		sigmaSync.setTooltip("Determines the sigma for the derivatives. It depends on the line width.");
		sigmaSync.setValue(filamentDetector.getDetectionParameters().getSigma());

		thresholdSync = new UpperLowerSynchronizer(lowerThresholdSlider, lowerThresholdField, upperThresholdSlider,
				upperThresholdField);
		thresholdSync.setLowerTooltip("Line points with a response smaller as this threshold are rejected.");
		thresholdSync.setUpperTooltip("Line points with a response larger as this threshold are accepted.");
		thresholdSync.setLowerValue(filamentDetector.getDetectionParameters().getLowerThresh());
		thresholdSync.setUpperValue(filamentDetector.getDetectionParameters().getUpperThresh());

		detectCurrentFrameButton.setSelected(filamentDetector.getDetectionParameters().isDetectOnlyCurrentFrame());
		simplifyFilamentsCheckbox.setSelected(filamentDetector.getDetectionParameters().isSimplifyFilaments());
		simplifyToleranceDistanceField
				.setText(Double.toString(filamentDetector.getDetectionParameters().getSimplifyToleranceDistance()));

		// Fill filtering fields
		filteringParameters = new FilteringParameters();
		filteringParameters.setDisableFiltering(true);

		lengthSync = new UpperLowerSynchronizer(minLengthSlider, minLengthField, maxLengthSlider, maxLengthField);
		lengthSync.setLowerValue(filteringParameters.getMinLength());
		lengthSync.setUpperValue(filteringParameters.getMaxLength());

		sinuositySync = new UpperLowerSynchronizer(minSinuositySlider, minSinuosityField, maxSinuositySlider,
				maxSinuosityField);
		sinuositySync.setLowerTooltip(
				"The sinuosity define how 'straight' is a line. 1 means it's straight, more is less straight.");
		sinuositySync.setUpperTooltip(
				"The sinuosity define how 'straight' is a line. 1 means it's straight, more is less straight.");
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
		filamentsTableView = new FilamentsTableView(context, filamentDetector.getFilaments(),
				filamentDetector.getCalibrations());
		filamentViewContainer.getChildren().add(0, filamentsTableView);
		detailViewContainer.getChildren().add(filamentsTableView.getDetailPane());

		// Initialize overlay on the image
		overlayService.setImageDisplay(filamentDetector.getImageDisplay());
	}

	private void updateFilamentsList() {
		filamentsTableView.setFilaments(filamentDetector.getFilaments());
		filamentsTableView.updateFilaments();
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
				float[] x = new float[] { (float) line.x1d, (float) line.x2d };
				float[] y = new float[] { (float) line.y1d, (float) line.y2d };
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

		if (sigmaSync.isEvent(event)) {
			sigmaSync.update(event);
			filamentDetector.getDetectionParameters().setSigma(sigmaSync.getValue());

		} else if (thresholdSync.isEvent(event)) {
			thresholdSync.update(event);
			filamentDetector.getDetectionParameters().setLowerThresh(thresholdSync.getLowerValue());
			filamentDetector.getDetectionParameters().setUpperThresh(thresholdSync.getUpperValue());
		}

		else if (event.getSource().equals(detectCurrentFrameButton)) {
			filamentDetector.getDetectionParameters().setDetectOnlyCurrentFrame(detectCurrentFrameButton.isSelected());
		}

		else if (event.getSource().equals(simplifyToleranceDistanceField)) {
			double newValue = Double.parseDouble(simplifyToleranceDistanceField.getText());
			if (newValue < 0) {
				simplifyToleranceDistanceField.setText(
						Double.toString(filamentDetector.getDetectionParameters().getSimplifyToleranceDistance()));
			} else {
				filamentDetector.getDetectionParameters().setSimplifyToleranceDistance(newValue);
			}
		}

		else if (event.getSource().equals(simplifyFilamentsCheckbox)) {
			filamentDetector.getDetectionParameters().setSimplifyFilaments(simplifyFilamentsCheckbox.isSelected());
		}

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

		if (detectionThread != null) {
			detectionThread.stop();
		}

		this.detectionProgressIndicator.setVisible(true);

		detectionTask = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {
				if (detectCurrentFrameButton.isSelected()) {
					filamentDetector.detectCurrentFrame();
				} else {
					filamentDetector.detect();
				}

				return filamentDetector.getFilaments().size();
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				status.showStatus(
						filamentDetector.getFilaments().size() + " has been detected with the following parameters : ");
				status.showStatus(filamentDetector.getDetectionParameters().toString());
				detectionProgressIndicator.setVisible(false);
				updateFilamentsList();
				eventService.publish(new FilterFilamentEvent(filteringParameters));
			}

			@Override
			protected void cancelled() {
				super.cancelled();
			}

			@Override
			protected void failed() {
				super.failed();
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

					filamentDetector.filterFilament(event.getFilteringParameters());
					updateFilamentsList();
				});

				return filamentDetector.getFilaments().size();
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				if (!filteringParameters.isDisableFiltering()) {
					status.showStatus("Filtering with the following parameters : ");
					status.showStatus(filteringParameters.toString());
					status.showStatus(filamentDetector.getFilaments().size() + " filament(s) remain.");
				} else {
					status.showStatus("Filtering is disabled. " + filamentDetector.getFilaments().size()
							+ " filaments detected.");
				}
			}

			@Override
			protected void cancelled() {
				super.cancelled();
			}

			@Override
			protected void failed() {
				super.failed();
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
