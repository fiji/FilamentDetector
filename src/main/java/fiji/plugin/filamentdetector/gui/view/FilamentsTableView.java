package fiji.plugin.filamentdetector.gui.view;

import java.text.DecimalFormat;

import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.Calibrations;
import fiji.plugin.filamentdetector.event.FilamentSelectedEvent;
import fiji.plugin.filamentdetector.gui.GUIStatusService;
import fiji.plugin.filamentdetector.gui.GUIUtils;
import fiji.plugin.filamentdetector.gui.controller.DetailedFilamentController;
import fiji.plugin.filamentdetector.gui.model.FilamentModel;
import fiji.plugin.filamentdetector.model.Filament;
import fiji.plugin.filamentdetector.model.Filaments;
import fiji.plugin.filamentdetector.overlay.FilamentOverlayService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class FilamentsTableView extends TableView<FilamentModel> {

	@Parameter
	FilamentOverlayService overlayService;

	@Parameter
	private LogService log;

	@Parameter
	private GUIStatusService status;

	@Parameter
	private EventService eventService;

	private ObservableList<FilamentModel> filamentModelList;
	private Filaments filaments;

	private DecimalFormat formatter;
	private Calibrations calibrations;

	private AnchorPane detailPane;

	public FilamentsTableView(Context context, Filaments filaments, Calibrations calibrations) {
		context.inject(this);
		this.calibrations = calibrations;

		this.filamentModelList = FXCollections.observableArrayList();
		this.filaments = filaments;

		TableColumn<FilamentModel, Integer> idColumn = new TableColumn<>("ID");
		TableColumn<FilamentModel, Double> lengthColumn = new TableColumn<>("Length (" + calibrations.getUnitX() + ")");
		TableColumn<FilamentModel, Integer> frameColumn = new TableColumn<>("Frame");

		this.getColumns().add(idColumn);
		this.getColumns().add(lengthColumn);
		this.getColumns().add(frameColumn);

		idColumn.setStyle("-fx-alignment: CENTER;");
		lengthColumn.setStyle("-fx-alignment: CENTER;");
		frameColumn.setStyle("-fx-alignment: CENTER;");

		idColumn.setCellValueFactory(cellData -> cellData.getValue().getID().asObject());
		lengthColumn.setCellValueFactory(cellData -> cellData.getValue().getLength().asObject());
		frameColumn.setCellValueFactory(cellData -> cellData.getValue().getFrame().asObject());

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

		// Initialize detailed view
		detailPane = new AnchorPane(this);
		AnchorPane.setTopAnchor(detailPane, 0.0);
		AnchorPane.setLeftAnchor(detailPane, 0.0);
		AnchorPane.setRightAnchor(detailPane, 0.0);
		AnchorPane.setBottomAnchor(detailPane, 0.0);
		detailPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
		detailPane.setPrefWidth(Region.USE_COMPUTED_SIZE);
		setNoDetail();

		this.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null && oldSelection != newSelection) {
				// Set the overlay
				overlayService.setSelected(newSelection.getFilament(), true);

				// Fill the detailed view
				DetailedFilamentController controller = new DetailedFilamentController(newSelection.getFilament());
				Pane pane = GUIUtils.loadFXML("/fiji/plugin/filamentdetector/gui/view/DetailedFilamentView.fxml",
						controller);

				detailPane.getChildren().clear();
				detailPane.getChildren().add(pane);

				AnchorPane.setTopAnchor(pane, 0.0);
				AnchorPane.setLeftAnchor(pane, 0.0);
				AnchorPane.setRightAnchor(pane, 0.0);
				AnchorPane.setBottomAnchor(pane, 0.0);
				pane.setPrefHeight(Region.USE_COMPUTED_SIZE);
				pane.setPrefWidth(Region.USE_COMPUTED_SIZE);

				controller.getRemoveFilamentLabel().setOnAction((event) -> {
					removeFilament(controller.getFilament());
				});

			} else {
				setNoDetail();
			}
		});

		// Handle filament selection
		eventService.subscribe(this);
	}

	public Filaments getFilaments() {
		return filaments;
	}

	public void setFilaments(Filaments filaments) {
		this.filaments = filaments;
	}

	public void updateFilaments() {

		filamentModelList = FXCollections.observableArrayList();
		for (Filament filament : filaments) {
			filamentModelList.add(new FilamentModel(filament));
		}
		this.setItems(filamentModelList);

		// Update overlay
		overlayService.reset();
		overlayService.add(filaments);
	}

	public void addFilament(Filament filament) {
		filaments.add(filament);
		filamentModelList.add(new FilamentModel(filament));
		overlayService.add(filament);
	}

	public Pane getDetailPane() {
		return detailPane;
	}

	private void setNoDetail() {
		Label noDetail = new Label("No filament selected.");
		detailPane.getChildren().clear();
		detailPane.getChildren().add(noDetail);
	}

	private void removeFilament(Filament filament) {
		FilamentModel filamentModel = filamentModelList.stream().filter(f -> f.getFilament().equals(filament))
				.findFirst().orElse(null);
		filamentModelList.remove(filamentModel);
		filaments.remove(filament);

		overlayService.remove(filament);
	}

	@EventHandler
	public void filamentSelected(FilamentSelectedEvent event) {
		FilamentModel filamentModel = filamentModelList.stream()
				.filter(f -> f.getFilament().equals(event.getFilament())).findFirst().orElse(null);
		if (filamentModel != null) {
			// Require to not mix AWT and JavaFX thread.
			Platform.runLater(() -> {
				this.getSelectionModel().clearSelection();
				this.getSelectionModel().select(filamentModel);
			});
		}
	}

}
