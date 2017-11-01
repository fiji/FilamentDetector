package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

import fiji.plugin.filamentdetector.FilamentWorkflow;
import fiji.plugin.filamentdetector.event.PreventPanelSwitchEvent;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.gui.controller.imagepreprocessor.AbstractImagePreprocessorController;
import fiji.plugin.filamentdetector.gui.controller.imagepreprocessor.Convert8BitController;
import fiji.plugin.filamentdetector.gui.controller.imagepreprocessor.DOGFilterController;
import fiji.plugin.filamentdetector.gui.controller.imagepreprocessor.FrangiFilterController;
import fiji.plugin.filamentdetector.gui.controller.imagepreprocessor.GaussianFilterController;
import fiji.plugin.filamentdetector.gui.controller.imagepreprocessor.NormalizeIntensitiesController;
import fiji.plugin.filamentdetector.gui.controller.imagepreprocessor.PseudoFlatFieldCorrectionController;
import fiji.plugin.filamentdetector.gui.controller.imagepreprocessor.TubenessFilterController;
import fiji.plugin.filamentdetector.gui.fxwidgets.ReorderablePaneListView;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import fiji.plugin.filamentdetector.preprocessing.Convert8BitPreprocessor;
import fiji.plugin.filamentdetector.preprocessing.DOGFilterPreprocessor;
import fiji.plugin.filamentdetector.preprocessing.FrangiFilterPreprocessor;
import fiji.plugin.filamentdetector.preprocessing.GaussianFilterPreprocessor;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessors;
import fiji.plugin.filamentdetector.preprocessing.NormalizeIntensitiesPreprocessor;
import fiji.plugin.filamentdetector.preprocessing.PseudoFlatFieldCorrectionPreprocessor;
import fiji.plugin.filamentdetector.preprocessing.TubenessFilterPreprocessor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;

public class ImagePreprocessorsController extends AbstractController implements Initializable {

	private static String FXMl_PATH = "/fiji/plugin/filamentdetector/gui/view/ImagePreprocessorView.fxml";

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private GUIStatusService status;

	@Parameter
	private EventService eventService;

	@Parameter
	private FilamentOverlayService overlay;

	@Parameter
	private UIService ui;

	@Parameter
	private ImageDisplayService ids;

	@FXML
	private CheckBox saveImageCheckbox;

	@FXML
	private CheckBox showImageCheckbox;

	@FXML
	private CheckBox useForOverlayCheckbox;

	@FXML
	private ProgressIndicator detectionProgressIndicator;

	@FXML
	private AnchorPane preprocessorContainer;

	private Thread thread;
	private Task<Integer> task;

	private ImagePreprocessors imagePreprocessors;
	private FilamentWorkflow filamentWorkflow;

	private ReorderablePaneListView processorsControllers;
	private List<AbstractImagePreprocessorController> imagePreprocessorControllers;

	public ImagePreprocessorsController(Context context, FilamentWorkflow filamentWorkflow) {
		context.inject(this);
		setFXMLPath(FXMl_PATH);
		this.filamentWorkflow = filamentWorkflow;
		this.imagePreprocessors = filamentWorkflow.getImagePreprocessor();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		this.detectionProgressIndicator.setVisible(false);

		VBox vbox = new VBox();
		this.preprocessorContainer.getChildren().add(vbox);

		this.imagePreprocessorControllers = new ArrayList<>();
		AbstractImagePreprocessorController imagePreprocessorController = null;

		for (ImagePreprocessor imagePreprocessor : this.imagePreprocessors.getImagePreprocessors()) {

			if (imagePreprocessor.getClass().equals(Convert8BitPreprocessor.class)) {
				imagePreprocessorController = new Convert8BitController(context, imagePreprocessor);

			} else if (imagePreprocessor.getClass().equals(GaussianFilterPreprocessor.class)) {
				imagePreprocessorController = new GaussianFilterController(context, imagePreprocessor);

			} else if (imagePreprocessor.getClass().equals(PseudoFlatFieldCorrectionPreprocessor.class)) {
				imagePreprocessorController = new PseudoFlatFieldCorrectionController(context, imagePreprocessor);

			} else if (imagePreprocessor.getClass().equals(DOGFilterPreprocessor.class)) {
				imagePreprocessorController = new DOGFilterController(context, imagePreprocessor);

			} else if (imagePreprocessor.getClass().equals(NormalizeIntensitiesPreprocessor.class)) {
				imagePreprocessorController = new NormalizeIntensitiesController(context, imagePreprocessor);

			} else if (imagePreprocessor.getClass().equals(FrangiFilterPreprocessor.class)) {
				imagePreprocessorController = new FrangiFilterController(context, imagePreprocessor);

			} else if (imagePreprocessor.getClass().equals(TubenessFilterPreprocessor.class)) {
				imagePreprocessorController = new TubenessFilterController(context, imagePreprocessor);

			} else {
				log.error(imagePreprocessor + " is can't be loaded.");
			}

			if (imagePreprocessorController != null) {
				this.imagePreprocessorControllers.add(imagePreprocessorController);
			}
		}

		this.processorsControllers = new ReorderablePaneListView(imagePreprocessorControllers);
		this.preprocessorContainer.getChildren().add(this.processorsControllers);

		AnchorPane.setBottomAnchor(this.processorsControllers, 0.0);
		AnchorPane.setTopAnchor(this.processorsControllers, 0.0);
		AnchorPane.setLeftAnchor(this.processorsControllers, 0.0);
		AnchorPane.setRightAnchor(this.processorsControllers, 0.0);

		saveImageCheckbox.setSelected(imagePreprocessors.isSavePreprocessedImage());
		showImageCheckbox.setSelected(imagePreprocessors.isShowPreprocessedImage());

		useForOverlayCheckbox.setSelected(imagePreprocessors.isUseForOverlay());

		useForOverlayCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				if (old_val != new_val) {
					usePreprocessedImageForOverlay();
				}
			}
		});

		// Enable tooltips
		for (AbstractImagePreprocessorController controller : this.imagePreprocessorControllers) {
			controller.enableTooltip();
		}

	}

	public void updateParameters() {
		imagePreprocessors.setSavePreprocessedImage(saveImageCheckbox.isSelected());
		imagePreprocessors.setShowPreprocessedImage(showImageCheckbox.isSelected());
		imagePreprocessors.setUseForOverlay(useForOverlayCheckbox.isSelected());
	}

	@FXML
	void preprocessImage(ActionEvent event) {
		if (task != null) {
			task.cancel();
		}

		if (thread != null) {
			thread.stop();
		}
		this.detectionProgressIndicator.setVisible(true);

		task = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {
				eventService.publish(new PreventPanelSwitchEvent(true));

				String statusMessage = "Preprocessing steps are : \n" + imagePreprocessorControllers.stream()
						.filter(c -> c.getImagePreprocessor().isDoPreprocess())
						.map(c -> c.getImagePreprocessor().getClass().getName()).collect(Collectors.joining("\n"));
				status.showStatus(statusMessage);

				imagePreprocessors.setImagePreprocessors(imagePreprocessorControllers.stream()
						.map(c -> c.getImagePreprocessor()).collect(Collectors.toList()));

				imagePreprocessors.preprocess();
				return 0;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				eventService.publish(new PreventPanelSwitchEvent(false));
				String statusMessage = "The image has been successfully preprocessed.";
				status.showStatus(statusMessage);
				detectionProgressIndicator.setVisible(false);
				usePreprocessedImageForOverlay();
			}

			@Override
			protected void cancelled() {
				super.cancelled();
				detectionProgressIndicator.setVisible(false);
				eventService.publish(new PreventPanelSwitchEvent(false));
			}

			@Override
			protected void failed() {
				super.failed();
				status.showStatus("Something failed during preprocessing : ");
				StackTraceElement[] stackTrace = this.getException().getStackTrace();
				status.showStatus(
						Arrays.stream(stackTrace).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
				detectionProgressIndicator.setVisible(false);
				eventService.publish(new PreventPanelSwitchEvent(false));
			}
		};

		thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}

	private void usePreprocessedImageForOverlay() {
		if (useForOverlayCheckbox.isSelected()) {
			Dataset dataset = imagePreprocessors.getPreprocessedDataset();

			if (dataset != null) {

				ImageDisplay imageDisplay = ids.getImageDisplays().stream()
						.filter(imd -> ((Dataset) imd.getActiveView().getData()).equals(dataset)).findFirst()
						.orElse(null);

				if (imageDisplay == null) {
					ui.show(dataset);
					imageDisplay = ids.getImageDisplays().stream()
							.filter(imd -> ((Dataset) imd.getActiveView().getData()).equals(dataset)).findFirst()
							.orElse(null);
				}

				if (imageDisplay != null) {
					overlay.setImageDisplay(imageDisplay);
					filamentWorkflow.setImageDisplay(imageDisplay);
					status.showStatus("Using preprocessed image for overlay.");
				} else {
					overlay.setImageDisplay(imagePreprocessors.getImageDisplay());
					filamentWorkflow.setImageDisplay(imagePreprocessors.getImageDisplay());
					log.error("Cannot use the preprocessed image for overlay.");
				}
			}

		} else {
			overlay.setImageDisplay(imagePreprocessors.getImageDisplay());
			filamentWorkflow.setImageDisplay(imagePreprocessors.getImageDisplay());
		}
	}
}
