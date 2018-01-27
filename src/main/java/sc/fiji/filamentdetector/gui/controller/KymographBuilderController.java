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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import sc.fiji.filamentdetector.FilamentWorkflow;
import sc.fiji.filamentdetector.gui.GUIStatusService;
import sc.fiji.filamentdetector.kymograph.KymographGenerator;
import sc.fiji.filamentdetector.kymograph.linedrawer.LineDrawer;
import sc.fiji.filamentdetector.kymograph.linedrawer.LongestFilamentLineDrawer;

public class KymographBuilderController extends AbstractController implements Initializable {

	private static String FXML_PATH = "/sc/fiji/filamentdetector/gui/view/KymographBuilderView.fxml";

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private GUIStatusService status;

	@FXML
	private Label nTrackedFilamentsField;

	@FXML
	private ComboBox<LineDrawer> lineDrawerCombobox;

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

	@FXML
	private CheckBox saveLinesCheckbox;

	private Thread kymographThread;
	private Task<Integer> kymographTask;

	private FilamentWorkflow filamentWorkflow;
	private KymographGenerator kymographGenerator;

	private List<LineDrawer> lineDrawers;

	public KymographBuilderController(Context context, FilamentWorkflow filamentWorkflow) {
		context.inject(this);
		setFXMLPath(FXML_PATH);
		this.filamentWorkflow = filamentWorkflow;
		this.kymographGenerator = new KymographGenerator(context);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		saveCheckbox.setSelected(kymographGenerator.getKymographParameters().isSaveKymographs());
		onlyBuildOneCheckbox.setSelected(kymographGenerator.getKymographParameters().isBuildOneRandomKymograph());
		showKymographsCheckbox.setSelected(kymographGenerator.getKymographParameters().isShowKymographs());
		saveLinesCheckbox.setSelected(kymographGenerator.getKymographParameters().isSaveKymographLines());

		lineThicknessField.setText(Double.toString(kymographGenerator.getKymographParameters().getLineThickness()));
		startOffsetField.setText(Double.toString(kymographGenerator.getKymographParameters().getStartOffsetLength()));
		endOffsetField.setText(Double.toString(kymographGenerator.getKymographParameters().getEndOffsetLength()));

		lineDrawerCombobox.setCellFactory(new Callback<ListView<LineDrawer>, ListCell<LineDrawer>>() {
			@Override
			public ListCell<LineDrawer> call(ListView<LineDrawer> p) {
				return new ListCell<LineDrawer>() {
					@Override
					protected void updateItem(LineDrawer t, boolean bln) {
						super.updateItem(t, bln);
						if (t != null) {
							setText(t.toString());
						} else {
							setText(null);
						}
					}
				};
			}
		});

		lineDrawerCombobox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			if (newValue != null) {
				lineDrawerDescription.setText(newValue.getDescription());
			}
		});

		lineThicknessField.textProperty().addListener((observable, oldValue, newValue) -> {
			updateParameters();
		});

		startOffsetField.textProperty().addListener((observable, oldValue, newValue) -> {
			updateParameters();
		});

		endOffsetField.textProperty().addListener((observable, oldValue, newValue) -> {
			updateParameters();
		});

		// Add line drawers to Combobox
		lineDrawers = new ArrayList<>();
		lineDrawers.add(new LongestFilamentLineDrawer());
		// lineDrawers.add(new AverageFilamentLineDrawer());

		lineDrawerCombobox.setItems(FXCollections.observableList(lineDrawers));
		lineDrawerCombobox.getSelectionModel().selectFirst();
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
		kymographGenerator.getKymographParameters().setSaveKymographLines(saveLinesCheckbox.isSelected());

		kymographGenerator.getKymographParameters().setLineThickness(Double.parseDouble(lineThicknessField.getText()));
		kymographGenerator.getKymographParameters()
				.setStartOffsetLength(Double.parseDouble(startOffsetField.getText()));
		kymographGenerator.getKymographParameters().setEndOffsetLength(Double.parseDouble(endOffsetField.getText()));

		kymographGenerator.getKymographParameters()
				.setLineDrawer(lineDrawerCombobox.getSelectionModel().getSelectedItem());
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
				kymographGenerator.setImageDisplay(filamentWorkflow.getImageDisplay());
				kymographGenerator.setTrackedFilaments(filamentWorkflow.getTrackedFilaments());
				kymographGenerator.build();
				return 0;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				String statusMessage = kymographGenerator.nKymographs()
						+ " kymograph(s) has been generated with the following parameters : ";
				status.showStatus(statusMessage);
				status.showStatus(kymographGenerator.getKymographParameters().toString());

				if (kymographGenerator.getKymographParameters().isSaveKymographs()) {
					if (kymographGenerator.kymographsHasBeenSaved()) {
						status.showStatus("Kymographs has been saved in the parent folder of the image.");
					} else {
						status.showStatus(
								"The parent folder of the image is not set. Please save the image and restart the plugin.");
					}
				}
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
