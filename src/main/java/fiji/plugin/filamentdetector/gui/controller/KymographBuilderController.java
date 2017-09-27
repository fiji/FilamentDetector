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
import fiji.plugin.filamentdetector.kymograph.KymographGenerator;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class KymographBuilderController extends Controller implements Initializable {

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private GUIStatusService status;

	@FXML
	private Label nTrackedFilamentsField;

	@FXML
	private ComboBox<?> lineDrawerCombobox;

	@FXML
	private Label lineDrawerDescription;

	@FXML
	private CheckBox saveCheckbox;

	@FXML
	private CheckBox onlyBuildOneCheckbox;

	@FXML
	private CheckBox showKymographsCheckbox;

	@FXML
	private TextField lineThicknessField;

	@FXML
	private TextField startOffsetField;

	@FXML
	private TextField endOffsetField;

	private Thread kymographThread;
	private Task<Integer> kymographTask;

	private FilamentWorkflow filamentWorkflow;
	private KymographGenerator kymographGenerator;

	public KymographBuilderController(Context context, FilamentWorkflow filamentWorkflow) {
		context.inject(this);
		this.filamentWorkflow = filamentWorkflow;
		this.kymographGenerator = new KymographGenerator(context);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		saveCheckbox.setSelected(kymographGenerator.getKymographParameters().isSaveKymographs());
		onlyBuildOneCheckbox.setSelected(kymographGenerator.getKymographParameters().isBuildOneRandomKymograph());
		showKymographsCheckbox.setSelected(kymographGenerator.getKymographParameters().isShowKymographs());

		lineThicknessField.setText(Double.toString(kymographGenerator.getKymographParameters().getLineThickness()));
		startOffsetField.setText(Double.toString(kymographGenerator.getKymographParameters().getStartOffsetLength()));
		endOffsetField.setText(Double.toString(kymographGenerator.getKymographParameters().getEndOffsetLength()));

	}

	public void initPane() {
		if (this.filamentWorkflow.getTrackedFilaments() == null
				|| this.filamentWorkflow.getTrackedFilaments().size() == 0) {
			status.showStatus("No tracked filaments detected. Please use the \"Track Filaments\" panel first.");
			this.getPane().setDisable(true);
			nTrackedFilamentsField.setText("");
		} else {
			this.getPane().setDisable(false);
			nTrackedFilamentsField.setText(Integer.toString(filamentWorkflow.getTrackedFilaments().size()));
			this.kymographGenerator.setImageDisplay(filamentWorkflow.getImageDisplay());
			this.kymographGenerator.setTrackedFilaments(filamentWorkflow.getTrackedFilaments());
		}
	}

	public void updateParameters() {
		kymographGenerator.getKymographParameters().setBuildOneRandomKymograph(onlyBuildOneCheckbox.isSelected());
		kymographGenerator.getKymographParameters().setSaveKymographs(saveCheckbox.isSelected());
		kymographGenerator.getKymographParameters().setShowKymographs(showKymographsCheckbox.isSelected());

		kymographGenerator.getKymographParameters().setLineThickness(Double.parseDouble(lineThicknessField.getText()));
		kymographGenerator.getKymographParameters()
				.setStartOffsetLength(Double.parseDouble(startOffsetField.getText()));
		kymographGenerator.getKymographParameters().setEndOffsetLength(Double.parseDouble(endOffsetField.getText()));
	}

	@FXML
	void buildKymographs(ActionEvent event) {
		if (kymographTask != null) {
			kymographTask.cancel();
		}

		if (kymographThread != null) {
			kymographThread.stop();
		}

		kymographTask = new Task<Integer>() {
			@Override
			protected Integer call() throws Exception {
				kymographGenerator.build();
				return 0;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				String statusMessage = kymographGenerator.nKymographs() + " kymograph(s) has been generated";
				if (kymographGenerator.getKymographParameters().isSaveKymographs()) {
					statusMessage += " and saved";
				}
				statusMessage += " with the following parameters : ";
				status.showStatus(statusMessage);
				status.showStatus(kymographGenerator.getKymographParameters().toString());
			}

			@Override
			protected void cancelled() {
				super.cancelled();
			}

			@Override
			protected void failed() {
				super.failed();
				status.showStatus("Something failed during kymograph building : ");
				StackTraceElement[] stackTrace = this.getException().getStackTrace();
				status.showStatus(
						Arrays.stream(stackTrace).map(StackTraceElement::toString).collect(Collectors.joining("\n")));
			}
		};

		kymographThread = new Thread(kymographTask);
		kymographThread.setDaemon(true);
		kymographThread.start();
	}
}
