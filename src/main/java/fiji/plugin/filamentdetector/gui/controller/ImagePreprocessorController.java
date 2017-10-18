package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.FilamentWorkflow;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.preprocessing.ImagePreprocessor;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

public class ImagePreprocessorController extends Controller implements Initializable {

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private GUIStatusService status;

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

	private Thread thread;
	private Task<Integer> task;

	private ImagePreprocessor imagePreprocessor;

	public ImagePreprocessorController(Context context, FilamentWorkflow filamentWorkflow) {
		context.inject(this);
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
	}

	public void updateParameters() {
		imagePreprocessor.setConvertTo8Bit(convert8BitCheckbox.isSelected());
		imagePreprocessor.setDoGaussianFilter(doGaussianFilterCheckbox.isSelected());
		imagePreprocessor.setGaussianFilterSize(Double.parseDouble(gaussianFilterSizeField.getText()));
		imagePreprocessor.setSavePreprocessedImage(saveImageCheckbox.isSelected());
		imagePreprocessor.setShowPreprocessedImage(showImageCheckbox.isSelected());
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
				imagePreprocessor.preprocess();
				return 0;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				String statusMessage = "The image has been successfully preprocessed.";
				status.showStatus(statusMessage);
				detectionProgressIndicator.setVisible(false);
			}

			@Override
			protected void cancelled() {
				super.cancelled();
				detectionProgressIndicator.setVisible(false);
			}

			@Override
			protected void failed() {
				super.failed();
				status.showStatus("Something failed during preprocessing : ");
				StackTraceElement[] stackTrace = this.getException().getStackTrace();
				status.showStatus(
						Arrays.stream(stackTrace).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
				detectionProgressIndicator.setVisible(false);
			}
		};

		thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();
	}
}
