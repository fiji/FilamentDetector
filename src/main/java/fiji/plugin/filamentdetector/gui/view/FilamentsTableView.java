package fiji.plugin.filamentdetector.gui.view;

import java.text.DecimalFormat;

import org.scijava.Context;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.Calibrations;
import fiji.plugin.filamentdetector.gui.model.FilamentModel;
import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class FilamentsTableView extends TableView<FilamentModel> {

	@Parameter
	FilamentOverlayService overlayService;

	private ObservableList<FilamentModel> filamentModelList;
	private Filaments filaments;

	private DecimalFormat formatter;
	private Calibrations calibrations;

	public FilamentsTableView(Context context, Calibrations calibrations) {
		context.inject(this);
		this.calibrations = calibrations;

		filamentModelList = FXCollections.observableArrayList();

		TableColumn<FilamentModel, Integer> idColumn = new TableColumn<>("ID");
		TableColumn<FilamentModel, Double> lengthColumn = new TableColumn<>("Length (" + calibrations.getUnitX() + ")");

		this.getColumns().add(idColumn);
		this.getColumns().add(lengthColumn);

		idColumn.setStyle("-fx-alignment: CENTER;");
		lengthColumn.setStyle("-fx-alignment: CENTER;");

		idColumn.setCellValueFactory(cellData -> cellData.getValue().getID().asObject());
		lengthColumn.setCellValueFactory(cellData -> cellData.getValue().getLength().asObject());

		formatter = new DecimalFormat("#0.00");

		lengthColumn.setCellFactory(column -> {
			return new TableCell<FilamentModel, Double>() {
				@Override
				protected void updateItem(Double item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
					} else {
						item = item * calibrations.getDx();
						setText(formatter.format(item));
					}
				};
			};
		});

		this.setItems(filamentModelList);
	}

	public Filaments getFilaments() {
		return filaments;
	}

	public void setFilaments(Filaments filaments) {
		this.filaments = filaments;

		filamentModelList = FXCollections.observableArrayList();
		for (Filament filament : filaments) {
			filamentModelList.add(new FilamentModel(filament));
		}
		this.setItems(filamentModelList);
		
		// Update overlay
		overlayService.reset();
		overlayService.add(filaments);
	}

}
