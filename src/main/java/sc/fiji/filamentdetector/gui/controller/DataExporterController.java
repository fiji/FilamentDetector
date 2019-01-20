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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import net.imagej.Dataset;
import sc.fiji.filamentdetector.FilamentWorkflow;
import sc.fiji.filamentdetector.FilesUtils;
import sc.fiji.filamentdetector.exporter.CSVFilamentExporter;
import sc.fiji.filamentdetector.exporter.CSVTrackedFilamentExporter;
import sc.fiji.filamentdetector.exporter.DataExporter;
import sc.fiji.filamentdetector.exporter.IJ1RoiFilamentExporter;
import sc.fiji.filamentdetector.exporter.JSONFilamentExporter;
import sc.fiji.filamentdetector.exporter.JSONTrackedFilamentExporter;
import sc.fiji.filamentdetector.gui.GUIStatusService;
import sc.fiji.filamentdetector.model.Filaments;
import sc.fiji.filamentdetector.model.TrackedFilaments;

public class DataExporterController extends AbstractController implements Initializable {

	private static String FXML_PATH = "/sc/fiji/filamentdetector/gui/view/DataExporterView.fxml";

	@Parameter
	private Context context;

	@Parameter
	private LogService log;

	@Parameter
	private GUIStatusService status;

	@FXML
	private Label filamentsLabel;

	@FXML
	private ComboBox<DataExporter<Filaments>> filamentsExporterBox;

	@FXML
	private Button exportFilamentsButton;

	@FXML
	private Label trackedFilamentsLabel;

	@FXML
	private ComboBox<DataExporter<TrackedFilaments>> trackedFilamentsExporterBox;

	@FXML
	private Button exportTrackedFilamentsButton;

	@FXML
	private Label filamentExporterDescription;

	@FXML
	private Label trackedFilamentExporterDescription;

	private FilamentWorkflow filamentWorkflow;

	private List<DataExporter<Filaments>> filamentsExporters;
	private List<DataExporter<TrackedFilaments>> trackedFilamentsExporters;

	public DataExporterController(Context context, FilamentWorkflow filamentWorkflow) {
		context.inject(this);
		setFXMLPath(FXML_PATH);
		this.filamentWorkflow = filamentWorkflow;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		refreshData(null);

		filamentsExporterBox
				.setCellFactory(new Callback<ListView<DataExporter<Filaments>>, ListCell<DataExporter<Filaments>>>() {
					@Override
					public ListCell<DataExporter<Filaments>> call(ListView<DataExporter<Filaments>> p) {
						return new ListCell<DataExporter<Filaments>>() {
							@Override
							protected void updateItem(DataExporter<Filaments> t, boolean bln) {
								super.updateItem(t, bln);
								if (t != null) {
									setText(t.getName());
								} else {
									setText(null);
								}
							}
						};
					}
				});

		trackedFilamentsExporterBox.setCellFactory(
				new Callback<ListView<DataExporter<TrackedFilaments>>, ListCell<DataExporter<TrackedFilaments>>>() {
					@Override
					public ListCell<DataExporter<TrackedFilaments>> call(ListView<DataExporter<TrackedFilaments>> p) {
						return new ListCell<DataExporter<TrackedFilaments>>() {
							@Override
							protected void updateItem(DataExporter<TrackedFilaments> t, boolean bln) {
								super.updateItem(t, bln);
								if (t != null) {
									setText(t.getName());
								} else {
									setText(null);
								}
							}
						};
					}
				});

		filamentsExporterBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			if (newValue != null) {
				filamentExporterDescription.setText(newValue.getDescription());
			}
		});

		trackedFilamentsExporterBox.getSelectionModel().selectedItemProperty()
				.addListener((options, oldValue, newValue) -> {
					if (newValue != null) {
						trackedFilamentExporterDescription.setText(newValue.getDescription());
					}
				});

		// Add exporters to lists.
		// TODO: do the exporters discovery automatically based on type (can
		// SciJavaPlugin be used ?).
		filamentsExporters = new ArrayList<>();
		filamentsExporters.add(new JSONFilamentExporter(context, filamentWorkflow.getCalibrations()));
		filamentsExporters.add(new IJ1RoiFilamentExporter(context));
		filamentsExporters.add(new CSVFilamentExporter(context, filamentWorkflow.getCalibrations()));

		trackedFilamentsExporters = new ArrayList<>();
		trackedFilamentsExporters.add(new JSONTrackedFilamentExporter(context, filamentWorkflow.getCalibrations()));
		trackedFilamentsExporters.add(new CSVTrackedFilamentExporter(context, filamentWorkflow.getCalibrations()));

		// Add lists to combo boxes.
		filamentsExporterBox.setItems(FXCollections.observableList(filamentsExporters));
		filamentsExporterBox.getSelectionModel().selectFirst();

		trackedFilamentsExporterBox.setItems(FXCollections.observableList(trackedFilamentsExporters));
		trackedFilamentsExporterBox.getSelectionModel().selectFirst();
	}

	@FXML
	public void refreshData(MouseEvent event) {
		if (filamentWorkflow.getFilaments() != null && filamentWorkflow.getFilaments().size() > 0) {
			filamentsLabel.setText(filamentWorkflow.getFilaments().size() + " filaments");
			filamentsExporterBox.setDisable(false);
			exportFilamentsButton.setDisable(false);
		} else {
			filamentsLabel.setText("No filaments");
			filamentsExporterBox.setDisable(true);
			exportFilamentsButton.setDisable(true);
		}

		if (filamentWorkflow.getTrackedFilaments() != null && filamentWorkflow.getTrackedFilaments().size() > 0) {
			trackedFilamentsLabel.setText(filamentWorkflow.getTrackedFilaments().size() + " tracked filaments");
			trackedFilamentsExporterBox.setDisable(false);
			exportTrackedFilamentsButton.setDisable(false);
		} else {
			trackedFilamentsLabel.setText("No tracked filaments");
			trackedFilamentsExporterBox.setDisable(true);
			exportTrackedFilamentsButton.setDisable(true);
		}
	}

	@FXML
	void exportFilaments(MouseEvent event) {
		DataExporter<Filaments> exporter = filamentsExporterBox.getSelectionModel().getSelectedItem();
		File file = fileChooser(exporter);
		if (file != null) {
			exporter.export(filamentWorkflow.getFilaments(), file);
			status.showStatus("Filaments have been saved at " + file.getAbsolutePath());
		}
	}

	@FXML
	void exportTrackedFilaments(MouseEvent event) {
		DataExporter<TrackedFilaments> exporter = trackedFilamentsExporterBox.getSelectionModel().getSelectedItem();
		File file = fileChooser(exporter);
		if (file != null) {
			exporter.export(filamentWorkflow.getTrackedFilaments(), file);
			status.showStatus("Filaments have been saved at " + file.getAbsolutePath());
		}
	}

	private File fileChooser(DataExporter<?> exporter) {
		FileChooser fileChooser = new FileChooser();

		Dataset dataset = (Dataset) filamentWorkflow.getSourceImage().getActiveView().getData();
		File initialDirectory = null;
		if (dataset.getSource() != null) {
			String parentPath = new File(dataset.getSource()).getParent();
			if (parentPath != null) {
				initialDirectory = new File(parentPath);
			}
		}
		if (initialDirectory == null) {
			initialDirectory = new File(System.getProperty("user.home"));
		}
		fileChooser.setInitialDirectory(initialDirectory);

		if (dataset.getName() != null) {
			String fname = FilesUtils.getFileNameWithoutExtension(dataset.getName());
			fileChooser.setInitialFileName(fname);
		}

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(exporter.getExtensionDescription(),
				exporter.getExtensionFilters());
		fileChooser.getExtensionFilters().add(extFilter);
		return fileChooser.showSaveDialog(this.getPane().getScene().getWindow());
	}

}
