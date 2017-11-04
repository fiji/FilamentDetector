package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import fiji.plugin.filamentdetector.gui.controller.tracking.BBoxLAPFilamentsTrackerController;
import fiji.plugin.filamentdetector.gui.controller.tracking.FilamentsTrackerController;
import fiji.plugin.filamentdetector.gui.fxwidgets.SliderLabelSynchronizer;
import fiji.plugin.filamentdetector.gui.fxwidgets.UpperLowerSynchronizer;
import fiji.plugin.filamentdetector.gui.view.TrackedFilamentsTableView;
import fiji.plugin.filamentdetector.tracking.BBoxLAPFilamentsTracker;
import fiji.plugin.filamentdetector.tracking.FilamentTrackerService;
import fiji.plugin.filamentdetector.tracking.FilamentsTracker;
import fiji.plugin.filamentdetector.tracking.FilteringTrackedFilamentsParameters;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class TrackingFilamentController extends AbstractController implements Initializable {

	private static String FXML_PATH = "/fiji/plugin/filamentdetector/gui/view/TrackingFilamentView.fxml";

	@Parameter
	private Context context;

	@Parameter
	private GUIStatusService status;

	@Parameter
	private EventService eventService;

	@Parameter
	private LogService log;

	@Parameter
	private FilamentTrackerService trackerService;

	@FXML
	private ComboBox<FilamentsTracker> trackerComboBox;

	@FXML
	private AnchorPane trackerParametersPane;

	@FXML
	private Label nFilamentsField;

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
	private Slider limitBorderSlider;

	@FXML
	private TextField limitBorderField;

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

	private UpperLowerSynchronizer sizeSync;
	private SliderLabelSynchronizer limitBorderSync;

	private List<FilamentsTracker> filamentsTrackers;

	private FilamentWorkflow filamentWorkflow;

	private FilteringTrackedFilamentsParameters filteringParameters;

	private TrackedFilamentsTableView trackedFilamentsTableView;

	public TrackingFilamentController(Context context, FilamentWorkflow filamentDetector) {
		context.inject(this);
		setFXMLPath(FXML_PATH);
		this.filamentWorkflow = filamentDetector;

		this.filamentsTrackers = new ArrayList<>();
		this.filamentsTrackers.add(trackerService.getBBoxTracker());
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		this.trackingProgressIndicator.setVisible(false);

		this.initTrackerComboBox();
		this.setFilamentsTracker(this.filamentsTrackers.get(0));

		// Fill filtering fields
		filteringParameters = new FilteringTrackedFilamentsParameters();

		sizeSync = new UpperLowerSynchronizer(minSizeSlider, minSizeField, maxSizeSlider, maxSizeField);
		sizeSync.setLowerTooltip("Keep tracks with a minimum number of filaments.");
		sizeSync.setUpperTooltip("Keep tracks with a maximum number of filaments.");
		sizeSync.setLowerValue(filteringParameters.getMinSize());
		sizeSync.setUpperValue(filteringParameters.getMaxSize());

		limitBorderSync = new SliderLabelSynchronizer(limitBorderSlider, limitBorderField);
		limitBorderSync.setValue(filteringParameters.getBorderLimit());

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
			this.trackerComboBox.getSelectionModel().select(0);
			this.getPane().setDisable(false);
			nFilamentsField.setText(Integer.toString(filamentWorkflow.getFilaments().size()));
		}
	}

	private void setFilamentsTracker(FilamentsTracker filamentTracker) {
		this.filamentWorkflow.setFilamentsTracker(filamentTracker);

		FilamentsTrackerController controller = null;
		if (filamentTracker.getClass().equals(BBoxLAPFilamentsTracker.class)) {
			controller = new BBoxLAPFilamentsTrackerController(context, filamentTracker);
		} else {
			log.error("Can't load FilamentsTracker parameters pane.");
		}

		if (controller != null) {
			trackerParametersPane.getChildren().add(controller.loadPane());
		}
	}

	public void initTrackerComboBox() {

		Callback<ListView<FilamentsTracker>, ListCell<FilamentsTracker>> cellFactory = new Callback<ListView<FilamentsTracker>, ListCell<FilamentsTracker>>() {
			@Override
			public ListCell<FilamentsTracker> call(ListView<FilamentsTracker> p) {
				return new ListCell<FilamentsTracker>() {
					@Override
					protected void updateItem(FilamentsTracker t, boolean bln) {
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

		this.trackerComboBox.setButtonCell(cellFactory.call(null));
		this.trackerComboBox.setCellFactory(cellFactory);

		this.trackerComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			if (newValue != null) {
				setFilamentsTracker(newValue);
			} else {
				setFilamentsTracker(this.filamentsTrackers.get(0));
			}
		});

		this.trackerComboBox.setItems(FXCollections.observableList(this.filamentsTrackers));
		this.trackerComboBox.getSelectionModel().selectFirst();
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
		} else if (limitBorderSync.isEvent(event)) {
			limitBorderSync.update(event);
			filteringParameters.setBorderLimit(limitBorderSync.getValue());
		} else if (event.getSource().equals(disableFilteringBox)) {
			filteringParameters.setDisableFiltering(disableFilteringBox.isSelected());
		}

		eventService.publish(new FilterTrackedFilamentEvent(filteringParameters));
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
