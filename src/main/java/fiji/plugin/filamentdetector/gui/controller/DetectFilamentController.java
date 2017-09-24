
package fiji.plugin.filamentdetector.gui.controller;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.FilamentDetector;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.gui.view.FilamentsTableView;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
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

	private FilamentsTableView filamentsTableView;

	private FilamentDetector filamentDetector;

	private Thread detectionThread;
	private Task<Integer> detectionTask;

	public DetectFilamentController(Context context, FilamentDetector filamentDetector) {
		context.inject(this);
		this.filamentDetector = filamentDetector;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		this.detectionProgressIndicator.setVisible(false);

		this.filamentDetector.initDetection();

		// Fill fields with default values
		sigmaField.setText(Double.toString(filamentDetector.getDetectionParameters().getSigma()));
		sigmaSlider.setValue(filamentDetector.getDetectionParameters().getSigma());

		lowerThresholdField.setText(Double.toString(filamentDetector.getDetectionParameters().getLowerThresh()));
		lowerThresholdSlider.setValue(filamentDetector.getDetectionParameters().getLowerThresh());

		upperThresholdField.setText(Double.toString(filamentDetector.getDetectionParameters().getUpperThresh()));
		upperThresholdSlider.setValue(filamentDetector.getDetectionParameters().getUpperThresh());

		detectCurrentFrameButton.setSelected(filamentDetector.getDetectionParameters().isDetectOnlyOnCurrentFrame());

		this.setToolTips();

		// Initialize filaments list
		filamentsTableView = new FilamentsTableView(context, filamentDetector.getCalibrations());
		filamentViewContainer.getChildren().add(0, filamentsTableView);
		detailViewContainer.getChildren().add(filamentsTableView.getDetailPane());

		// Initialize overlay on the image
		overlayService.setImageDisplay(filamentDetector.getImageDisplay());
	}

	private void updateFilamentsList() {
		filamentsTableView.setFilaments(filamentDetector.getFilaments());
	}
	
    @FXML
    void importLinesfromROIManager(MouseEvent event) {
    	
    }

	@FXML
	public void updateDetectionParameters(Event event) {

		DecimalFormat f = new DecimalFormat("##.00");

		if (event.getSource().equals(sigmaSlider)) {
			double newSigmaValue;
			try {
				newSigmaValue = f.parse(f.format(sigmaSlider.getValue())).doubleValue();
				sigmaField.setText(Double.toString(newSigmaValue));
				filamentDetector.getDetectionParameters().setSigma(newSigmaValue);
			} catch (ParseException e) {
				sigmaSlider.setValue(filamentDetector.getDetectionParameters().getSigma());
			}

		} else if (event.getSource().equals(sigmaField)) {
			try {
				double newSigmaValue = f.parse(f.format(Double.parseDouble(sigmaField.getText()))).doubleValue();
				sigmaSlider.setValue(newSigmaValue);
				filamentDetector.getDetectionParameters().setSigma(newSigmaValue);
			} catch (NumberFormatException | ParseException | ClassCastException e) {
				sigmaField.setText(Double.toString(filamentDetector.getDetectionParameters().getSigma()));
			}
		}

		else if (event.getSource().equals(lowerThresholdSlider)) {
			double newLowThresholdValue;
			try {
				newLowThresholdValue = f.parse(f.format(lowerThresholdSlider.getValue())).doubleValue();
				if (newLowThresholdValue < filamentDetector.getDetectionParameters().getUpperThresh()) {
					lowerThresholdField.setText(Double.toString(newLowThresholdValue));
					filamentDetector.getDetectionParameters().setLowerThresh(newLowThresholdValue);
				} else {
					lowerThresholdSlider.setValue(filamentDetector.getDetectionParameters().getUpperThresh() - 0.01);
					lowerThresholdField.setText(
							Double.toString(filamentDetector.getDetectionParameters().getUpperThresh() - 0.01));
				}

			} catch (ParseException e) {
				lowerThresholdSlider.setValue(filamentDetector.getDetectionParameters().getLowerThresh());
			}

		} else if (event.getSource().equals(lowerThresholdField)) {
			try {
				double newLowThresholdValue = f.parse(f.format(Double.parseDouble(lowerThresholdField.getText())))
						.doubleValue();
				if (newLowThresholdValue < filamentDetector.getDetectionParameters().getUpperThresh()) {
					lowerThresholdSlider.setValue(newLowThresholdValue);
					filamentDetector.getDetectionParameters().setLowerThresh(newLowThresholdValue);
				} else {
					lowerThresholdField.setText(
							Double.toString(filamentDetector.getDetectionParameters().getUpperThresh() - 0.01));
					lowerThresholdSlider.setValue(filamentDetector.getDetectionParameters().getUpperThresh() - 0.01);
				}

			} catch (NumberFormatException | ParseException | ClassCastException e) {
				lowerThresholdField
						.setText(Double.toString(filamentDetector.getDetectionParameters().getLowerThresh()));
			}
		}

		else if (event.getSource().equals(upperThresholdSlider)) {
			double newUpperThresholdValue;
			try {
				newUpperThresholdValue = f.parse(f.format(upperThresholdSlider.getValue())).doubleValue();
				if (newUpperThresholdValue > filamentDetector.getDetectionParameters().getLowerThresh()) {
					upperThresholdField.setText(Double.toString(newUpperThresholdValue));
					filamentDetector.getDetectionParameters().setUpperThresh(newUpperThresholdValue);
				} else {
					upperThresholdSlider.setValue(filamentDetector.getDetectionParameters().getLowerThresh() + 0.01);
					upperThresholdField.setText(
							Double.toString(filamentDetector.getDetectionParameters().getLowerThresh() + 0.01));
				}

			} catch (ParseException e) {
				upperThresholdSlider.setValue(filamentDetector.getDetectionParameters().getLowerThresh());
			}

		} else if (event.getSource().equals(upperThresholdField)) {
			try {
				double newUpperThresholdValue = f.parse(f.format(Double.parseDouble(upperThresholdField.getText())))
						.doubleValue();
				if (newUpperThresholdValue > filamentDetector.getDetectionParameters().getLowerThresh()) {
					upperThresholdSlider.setValue(newUpperThresholdValue);
					filamentDetector.getDetectionParameters().setUpperThresh(newUpperThresholdValue);
				} else {
					upperThresholdField.setText(
							Double.toString(filamentDetector.getDetectionParameters().getLowerThresh() + 0.01));
					upperThresholdSlider.setValue(filamentDetector.getDetectionParameters().getLowerThresh() + 0.01);
				}

			} catch (NumberFormatException | ParseException | ClassCastException e) {
				upperThresholdField
						.setText(Double.toString(filamentDetector.getDetectionParameters().getLowerThresh()));
			}
		}

		else if (event.getSource().equals(detectCurrentFrameButton)) {
			filamentDetector.getDetectionParameters()
					.setDetectOnlyOnCurrentFrame(detectCurrentFrameButton.isSelected());
		}

		if (liveDetectionButton.isSelected()) {
			this.detect(null);
		}

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
			}

			@Override
			protected void cancelled() {
				super.cancelled();
			}

			@Override
			protected void failed() {
				super.failed();
				status.showStatus("Detection failed.");
				detectionProgressIndicator.setVisible(false);
			}
		};

		detectionThread = new Thread(detectionTask);
		detectionThread.setDaemon(true);
		detectionThread.start();

	}

	@FXML
	public void liveDetectionClicked(MouseEvent event) {
		if (liveDetectionButton.isSelected()) {
			detectButton.setDisable(true);
		} else {
			detectButton.setDisable(false);

		}
	}

	private void setToolTips() {
		Tooltip tooltip;

		tooltip = new Tooltip("Determines the sigma for the derivatives. It depends on the line width.");
		Tooltip.install(sigmaSlider, tooltip);
		Tooltip.install(sigmaField, tooltip);

		tooltip = new Tooltip("Line points with a response smaller as this threshold are rejected.");
		Tooltip.install(lowerThresholdSlider, tooltip);
		Tooltip.install(lowerThresholdField, tooltip);

		tooltip = new Tooltip("Line points with a response larger as this threshold are accepted.");
		Tooltip.install(upperThresholdSlider, tooltip);
		Tooltip.install(upperThresholdSlider, tooltip);

		tooltip = new Tooltip(
				"Only detect filaments on the current frame (use for quick detection parameters tuning).");
		Tooltip.install(detectCurrentFrameButton, tooltip);

		tooltip = new Tooltip("Perform detection each time a parameter is modified.");
		Tooltip.install(liveDetectionButton, tooltip);
	}

}
