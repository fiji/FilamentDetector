package fiji.plugin.filamentdetector.gui.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.FilamentDetector;
import fiji.plugin.filamentdetector.exporter.DataExporter;
import fiji.plugin.filamentdetector.exporter.IJ1RoiFilamentExporter;
import fiji.plugin.filamentdetector.exporter.JSONFilamentExporter;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.model.TrackedFilaments;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.util.Callback;

public class DataExporterController extends Controller implements Initializable {

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

	private FilamentDetector filamentDetector;

	private List<DataExporter<Filaments>> filamentsExporters;
	private List<DataExporter<TrackedFilaments>> trackedFilamentsExporters;

	public DataExporterController(Context context, FilamentDetector filamentDetector) {
		context.inject(this);
		this.filamentDetector = filamentDetector;
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
									setTooltip(new Tooltip(t.getDescription()));
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
									setTooltip(new Tooltip(t.getDescription()));
								} else {
									setText(null);
								}

							}
						};
					}
				});

		// Add exporters to lists.
		// TODO: do the exporters discovery automatically based on type (can
		// SciJavaPlugin be used ?).
		filamentsExporters = new ArrayList<>();
		filamentsExporters.add(new JSONFilamentExporter(context));
		filamentsExporters.add(new IJ1RoiFilamentExporter(context));

		trackedFilamentsExporters = new ArrayList<>();

		// Add lists to combo boxes.
		filamentsExporterBox.setItems(FXCollections.observableList(filamentsExporters));
		filamentsExporterBox.getSelectionModel().selectFirst();

		trackedFilamentsExporterBox.setItems(FXCollections.observableList(trackedFilamentsExporters));
		trackedFilamentsExporterBox.getSelectionModel().selectFirst();
	}

	@FXML
	public void refreshData(MouseEvent event) {
		if (filamentDetector.getFilaments() != null && filamentDetector.getFilaments().size() > 0) {
			filamentsLabel.setText(filamentDetector.getFilaments().size() + " filaments");
			filamentsExporterBox.setDisable(false);
			exportFilamentsButton.setDisable(false);
		} else {
			filamentsLabel.setText("No filaments");
			filamentsExporterBox.setDisable(true);
			exportFilamentsButton.setDisable(true);
		}

		if (filamentDetector.getTrackedFilaments() != null && filamentDetector.getTrackedFilaments().size() > 0) {
			trackedFilamentsLabel.setText(filamentDetector.getTrackedFilaments().size() + " tracked filaments");
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

		FileChooser fileChooser = new FileChooser();

		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(exporter.getExtensionDescription(),
				exporter.getExtension());
		fileChooser.getExtensionFilters().add(extFilter);
		File file = fileChooser.showSaveDialog(this.getPane().getScene().getWindow());

		if (file != null) {
			exporter.export(filamentDetector.getFilaments(), file);
			status.showStatus("Filaments have been saved at " + file.getAbsolutePath());
		}
	}

	@FXML
	void exportTrackedFilaments(MouseEvent event) {

	}

}
