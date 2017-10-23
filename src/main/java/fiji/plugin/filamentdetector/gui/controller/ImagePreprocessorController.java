package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.Arrays;
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
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;

public class ImagePreprocessorController extends Controller implements Initializable {

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
	private CheckBox convert8BitCheckbox;

	@FXML
	private CheckBox doGaussianFilterCheckbox;

	@FXML
	private TextField gaussianFilterSizeField;

	@FXML
	private CheckBox saveImageCheckbox;

	@FXML
	private CheckBox showImageCheckbox;

	@FXML
	private ProgressIndicator detectionProgressIndicator;

	@FXML
	private TextField sigma1DOGField;

	@FXML
	private TextField sigma2DOGField;

	@FXML
	private CheckBox doDifferenceOfGaussianFilterCheckbox;

	@FXML
	private CheckBox doflatFieldCorrectionCheckbox;

	@FXML
	private TextField flatFieldCorrectionGaussianFilterSizeField;

	@FXML
	private CheckBox useForOverlayCheckbox;

	private Thread thread;
	private Task<Integer> task;

	private ImagePreprocessor imagePreprocessor;
	private FilamentWorkflow filamentWorkflow;

	public ImagePreprocessorController(Context context, FilamentWorkflow filamentWorkflow) {
		context.inject(this);
		this.filamentWorkflow = filamentWorkflow;
		this.imagePreprocessor = filamentWorkflow.getImagePreprocessor();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		this.detectionProgressIndicator.setVisible(false);
		convert8BitCheckbox.setSelected(imagePreprocessor.isConvertTo8Bit());

		doGaussianFilterCheckbox.setSelected(imagePreprocessor.isDoGaussianFilter());
		gaussianFilterSizeField.setText(Double.toString(imagePreprocessor.getGaussianFilterSize()));

		saveImageCheckbox.setSelected(imagePreprocessor.isSavePreprocessedImage());
		showImageCheckbox.setSelected(imagePreprocessor.isShowPreprocessedImage());

		doDifferenceOfGaussianFilterCheckbox.setSelected(imagePreprocessor.isDoDifferenceOfGaussianFilter());
		sigma1DOGField.setText(Double.toString(imagePreprocessor.getSigma1()));
		sigma2DOGField.setText(Double.toString(imagePreprocessor.getSigma2()));

		doflatFieldCorrectionCheckbox.setSelected(imagePreprocessor.isDoPseudoFlatFieldCorrection());
		flatFieldCorrectionGaussianFilterSizeField
				.setText(Double.toString(imagePreprocessor.getFlatFieldCorrectionGaussianFilterSize()));

		useForOverlayCheckbox.setSelected(imagePreprocessor.isUseForOverlay());

		useForOverlayCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
				if (old_val != new_val) {
					usePreprocessedImageForOverlay();
				}
			}
		});

	}

	public void updateParameters() {
		imagePreprocessor.setConvertTo8Bit(convert8BitCheckbox.isSelected());

		imagePreprocessor.setDoGaussianFilter(doGaussianFilterCheckbox.isSelected());
		imagePreprocessor.setGaussianFilterSize(Double.parseDouble(gaussianFilterSizeField.getText()));

		imagePreprocessor.setSavePreprocessedImage(saveImageCheckbox.isSelected());
		imagePreprocessor.setShowPreprocessedImage(showImageCheckbox.isSelected());

		imagePreprocessor.setDoDifferenceOfGaussianFilter(doDifferenceOfGaussianFilterCheckbox.isSelected());
		imagePreprocessor.setSigma1(Double.parseDouble(sigma1DOGField.getText()));
		imagePreprocessor.setSigma2(Double.parseDouble(sigma2DOGField.getText()));

		imagePreprocessor.setDoPseudoFlatFieldCorrection(doflatFieldCorrectionCheckbox.isSelected());
		imagePreprocessor.setFlatFieldCorrectionGaussianFilterSize(
				Double.parseDouble(flatFieldCorrectionGaussianFilterSizeField.getText()));

		imagePreprocessor.setUseForOverlay(useForOverlayCheckbox.isSelected());
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
				imagePreprocessor.preprocess();
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
			Dataset dataset = imagePreprocessor.getPreprocessedImage();

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

				log.info(imageDisplay);
				if (imageDisplay != null) {
					overlay.setImageDisplay(imageDisplay);
				}
			}

		} else {
			overlay.setImageDisplay(imagePreprocessor.getImageDisplay());
		}
	}
}
