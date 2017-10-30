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

import fiji.plugin.filamentdetector.FilamentWorkflow;
import fiji.plugin.filamentdetector.event.FilterTrackedFilamentEvent;
import fiji.plugin.filamentdetector.event.PreventPanelSwitchEvent;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.gui.controller.helper.SliderLabelSynchronizer;
import fiji.plugin.filamentdetector.gui.controller.helper.UpperLowerSynchronizer;
import fiji.plugin.filamentdetector.gui.view.TrackedFilamentsTableView;
import fiji.plugin.filamentdetector.tracking.FilamentsTracker;
import fiji.plugin.filamentdetector.tracking.FilteringTrackedFilamentsParameters;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class TrackingFilamentController extends AbstractController implements Initializable {

	private static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/tracking/TrackingFilamentView.fxml";

	@Parameter
	private Context context;

	@Parameter
	private GUIStatusService status;

	@Parameter
	private EventService eventService;

	@Parameter
	private LogService log;

	@FXML
	private Label nFilamentsField;

	@FXML
	private Slider costThresholdSlider;

	@FXML
	private TextField costThresholdField;

	@FXML
	private Slider maxFrameGapSlider;

	@FXML
	private TextField maxFrameGapField;

	@FXML
	private CheckBox interpolateFilamentsCheckbox;

	@FXML
	private ProgressIndicator trackingProgressIndicator;

	@FXML
	private Slider maxSizeSlider;

	@FXML
	private TextField maxSizeField;

	@FXML
	private Slider minSizeSlider;

	@FXML
	private TextField minSizeField;

	@FXML
	private CheckBox disableFilteringBox;

	@FXML
	private VBox trackedFilamentViewContainer;

	@FXML
	private AnchorPane detailViewContainer;

	private Thread trackingThread;
	private Task<Integer> trackingTask;

	private Thread filterThread;
	private Task<Integer> filterTask;

	private SliderLabelSynchronizer costThresholdSync;
	private SliderLabelSynchronizer maxFrameGapSync;
	private UpperLowerSynchronizer sizeSync;

	private FilamentWorkflow filamentWorkflow;

	private FilteringTrackedFilamentsParameters filteringParameters;

	private TrackedFilamentsTableView trackedFilamentsTableView;

	public TrackingFilamentController(Context context, FilamentWorkflow filamentDetector) {
		context.inject(this);
		setFXMLPath(FXML_PATH);
		this.filamentWorkflow = filamentDetector;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		this.trackingProgressIndicator.setVisible(false);

		FilamentsTracker filamentTracker = new FilamentsTracker(context);
		this.filamentWorkflow.initTracking(filamentTracker);

		// Fill fields with default values
		costThresholdSync = new SliderLabelSynchronizer(costThresholdSlider, costThresholdField);
		costThresholdSync.setTooltip(
				"Discard links between filaments when the IoU of the bounding boxes is below this value (0 to 1).");
		costThresholdSync.setValue(this.filamentWorkflow.getFilamentsTracker().getCostThreshold());

		maxFrameGapSync = new SliderLabelSynchronizer(maxFrameGapSlider, maxFrameGapField);
		maxFrameGapSync.setValue(this.filamentWorkflow.getFilamentsTracker().getMaxFrameGap());

		interpolateFilamentsCheckbox.setSelected(this.filamentWorkflow.getFilamentsTracker().isInterpolateFilaments());

		// Fill filtering fields
		filteringParameters = new FilteringTrackedFilamentsParameters();
		filteringParameters.setDisableFiltering(true);

		sizeSync = new UpperLowerSynchronizer(minSizeSlider, minSizeField, maxSizeSlider, maxSizeField);
		sizeSync.setLowerTooltip("Keep tracks with a minimum number of filaments.");
		sizeSync.setUpperTooltip("Keep tracks with a maximum number of filaments.");
		sizeSync.setLowerValue(filteringParameters.getMinSize());
		sizeSync.setUpperValue(filteringParameters.getMaxSize());

		disableFilteringBox.setSelected(filteringParameters.isDisableFiltering());

		// Initialize tracked filaments list
		trackedFilamentsTableView = new TrackedFilamentsTableView(context, filamentWorkflow.getTrackedFilaments());
		trackedFilamentViewContainer.getChildren().add(0, trackedFilamentsTableView.getInfoPane());
		trackedFilamentViewContainer.getChildren().add(1, trackedFilamentsTableView);
		detailViewContainer.getChildren().add(trackedFilamentsTableView.getDetailPane());
	}

	public void initPane() {
		if (this.filamentWorkflow.getDataset().getFrames() == 1) {
			status.showStatus("The image only has 1 timepoints. Tracking can't be done.");
			this.getPane().setDisable(true);
			nFilamentsField.setText("");
		} else if (this.filamentWorkflow.getFilaments() == null || this.filamentWorkflow.getFilaments().size() == 0) {
			status.showStatus("No filaments detected. Please use the \"Detect Filaments\" panel first.");
			this.getPane().setDisable(true);
			nFilamentsField.setText("");
		} else {
			FilamentsTracker filamentTracker = new FilamentsTracker(context);
			this.filamentWorkflow.initTracking(filamentTracker);
			this.getPane().setDisable(false);
			nFilamentsField.setText(Integer.toString(filamentWorkflow.getFilaments().size()));
		}
	}

	private void updateTrackedFilamentsList() {
		trackedFilamentsTableView.setTrackedFilaments(filamentWorkflow.getTrackedFilaments());
	}

	@FXML
	void track(ActionEvent event) {
		if (trackingTask != null) {
			trackingTask.cancel();
		}

		if (trackingThread != null) {
			trackingThread.stop();
		}

		this.trackingProgressIndicator.setVisible(true);

		trackingTask = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {
				eventService.publish(new PreventPanelSwitchEvent(true));
				filamentWorkflow.track();
				return filamentWorkflow.getTrackedFilaments().size();
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				status.showStatus(filamentWorkflow.getTrackedFilaments().size()
						+ " tracks has been created with the following parameters : ");
				status.showStatus(filamentWorkflow.getFilamentsTracker().toString());
				trackingProgressIndicator.setVisible(false);
				updateTrackedFilamentsList();
				eventService.publish(new FilterTrackedFilamentEvent(filteringParameters));
				eventService.publish(new PreventPanelSwitchEvent(false));
			}

			@Override
			protected void cancelled() {
				super.cancelled();
				eventService.publish(new PreventPanelSwitchEvent(false));
				trackingProgressIndicator.setVisible(false);
			}

			@Override
			protected void failed() {
				super.failed();
				status.showStatus("Something failed during tracking: ");
				StackTraceElement[] stackTrace = this.getException().getStackTrace();
				status.showStatus(
						Arrays.stream(stackTrace).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
				trackingProgressIndicator.setVisible(false);
				eventService.publish(new PreventPanelSwitchEvent(false));
			}
		};

		trackingThread = new Thread(trackingTask);
		trackingThread.setDaemon(true);
		trackingThread.start();
	}

	@FXML
	void updateFilteringParameters(Event event) {
		if (sizeSync.isEvent(event)) {
			sizeSync.update(event);
			filteringParameters.setMinSize(sizeSync.getLowerValue());
			filteringParameters.setMaxSize(sizeSync.getUpperValue());
		} else if (event.getSource().equals(disableFilteringBox)) {
			filteringParameters.setDisableFiltering(disableFilteringBox.isSelected());
		}

		eventService.publish(new FilterTrackedFilamentEvent(filteringParameters));
	}

	@FXML
	void updateTrackingParameters(Event event) {
		if (costThresholdSync.isEvent(event)) {
			costThresholdSync.update(event);
			filamentWorkflow.getFilamentsTracker().setCostThreshold(costThresholdSync.getValue());
		} else if (maxFrameGapSync.isEvent(event)) {
			maxFrameGapSync.update(event);
			filamentWorkflow.getFilamentsTracker().setMaxFrameGap(maxFrameGapSync.getValue());
		} else if (event.getSource().equals(interpolateFilamentsCheckbox)) {
			filamentWorkflow.getFilamentsTracker().setInterpolateFilaments(interpolateFilamentsCheckbox.isSelected());
		}
	}

	@FXML
	void filter(ActionEvent event) {
		eventService.publish(new FilterTrackedFilamentEvent(filteringParameters));
	}

	@EventHandler
	public void filter(FilterTrackedFilamentEvent event) {

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
					eventService.publish(new PreventPanelSwitchEvent(true));
					filamentWorkflow.filterTrackedFilament(event.getFilteringTrackedFilamentsParameters());
					updateTrackedFilamentsList();
				});

				return filamentWorkflow.getTrackedFilaments().size();
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				if (!filteringParameters.isDisableFiltering()) {
					status.showStatus("Filtering with the following parameters : ");
					status.showStatus(filteringParameters.toString());
					status.showStatus(filamentWorkflow.getTrackedFilaments().size() + " track(s) remain.");
				} else {
					status.showStatus("Filtering is disabled. " + filamentWorkflow.getTrackedFilaments().size()
							+ " tracks detected.");
				}
				eventService.publish(new PreventPanelSwitchEvent(false));
			}

			@Override
			protected void cancelled() {
				super.cancelled();
				eventService.publish(new PreventPanelSwitchEvent(false));
			}

			@Override
			protected void failed() {
				super.failed();
				status.showStatus("Something failed during filtering: ");
				StackTraceElement[] stackTrace = this.getException().getStackTrace();
				status.showStatus(
						Arrays.stream(stackTrace).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
				eventService.publish(new PreventPanelSwitchEvent(false));
			}
		};

		filterThread = new Thread(filterTask);
		filterThread.setDaemon(true);
		filterThread.start();
	}

}
