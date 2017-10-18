package fiji.plugin.filamentdetector.gui.view;

import java.text.DecimalFormat;
import java.util.List;

import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

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
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FilamentsTableView extends TableView<FilamentModel> {

	@Parameter
	FilamentOverlayService overlayService;

	@Parameter
	private LogService log;

	@Parameter
	private GUIStatusService status;

	@Parameter
	private EventService eventService;

	private DecimalFormat formatter;

	private AnchorPane detailPane;

	private Filaments filaments;

	private Label nFilamentsField;
	private Pane infoPane;

	public FilamentsTableView(Context context, Filaments filaments) {
		context.inject(this);

		// Enable multiple selection
		this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		TableColumn<FilamentModel, Integer> idColumn = new TableColumn<>("ID");
		TableColumn<FilamentModel, Double> lengthColumn = new TableColumn<>("Length");
		TableColumn<FilamentModel, Integer> frameColumn = new TableColumn<>("Frame");
		TableColumn<FilamentModel, String> colorColumn = new TableColumn<>("Color");

		this.getColumns().add(idColumn);
		this.getColumns().add(lengthColumn);
		this.getColumns().add(frameColumn);
		this.getColumns().add(colorColumn);

		idColumn.setStyle("-fx-alignment: CENTER;");
		lengthColumn.setStyle("-fx-alignment: CENTER;");
		frameColumn.setStyle("-fx-alignment: CENTER;");
		colorColumn.setStyle("-fx-alignment: CENTER;");

		idColumn.setCellValueFactory(cellData -> cellData.getValue().getId().asObject());
		lengthColumn.setCellValueFactory(cellData -> cellData.getValue().getLength().asObject());
		frameColumn.setCellValueFactory(cellData -> cellData.getValue().getFrame().asObject());
		colorColumn.setCellValueFactory(cellData -> cellData.getValue().getColor());

		formatter = new DecimalFormat("#0.00");

		lengthColumn.setCellFactory(column -> {
			return new TableCell<FilamentModel, Double>() {
				@Override
				protected void updateItem(Double item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
					} else {
						setText(formatter.format(item));
					}
				};
			};
		});

		colorColumn.setCellFactory(column -> {
			return new TableCell<FilamentModel, String>() {

				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty) {
						setText(null);
						setStyle("-fx-background-color: none");
					} else {
						setText(" ");
						setStyle("-fx-background-color:" + item);
					}
				};
			};
		});

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

			List<FilamentModel> filamentModels = this.getSelectionModel().getSelectedItems();

			if (filamentModels.size() == 1 && oldSelection != newSelection) {
				// Set the overlay
				overlayService.setSelected(newSelection.getFilament(), true, true);

				// Fill the detailed view
				DetailedFilamentController controller = new DetailedFilamentController(newSelection.getFilament());
				Pane pane = GUIUtils.loadFXML(
						"/fiji/plugin/filamentdetector/gui/view/detection/DetailedFilamentView.fxml", controller);

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

			} else if (filamentModels.size() > 1) {
				setMultipleSelectionDetail();

			} else {
				setNoDetail();
			}
		});

		// Handle filament selection
		eventService.subscribe(this);

		// Setup info pane
		this.infoPane = new Pane();
		this.infoPane.setMinHeight(25);
		VBox vbox = new VBox();
		vbox.setPadding(new Insets(3, 3, 3, 3));
		vbox.setSpacing(3);
		this.nFilamentsField = new Label("");
		vbox.getChildren().add(this.nFilamentsField);
		this.infoPane.getChildren().add(vbox);

		// Add filaments
		setFilaments(filaments);
	}

	public Filaments getFilaments() {
		return this.filaments;
	}

	public void setFilaments(Filaments filaments) {

		this.filaments = filaments;

		ObservableList<FilamentModel> filamentModelList = FXCollections.observableArrayList();

		for (Filament filament : filaments) {
			filamentModelList.add(new FilamentModel(filament));
		}
		this.setItems(filamentModelList);

		// Update overlay
		overlayService.reset();
		overlayService.add(filaments);
		this.updateNFilamentsField();
	}

	public void addFilament(Filament filament) {
		this.getItems().add(new FilamentModel(filament));
		this.filaments.add(filament);
		overlayService.add(filament);
		this.updateNFilamentsField();
	}

	public Pane getDetailPane() {
		return detailPane;
	}

	private void setNoDetail() {
		Label noDetail = new Label("No filament selected.");
		detailPane.getChildren().clear();
		detailPane.getChildren().add(noDetail);
	}

	private void setMultipleSelectionDetail() {

		List<FilamentModel> filamentModels = this.getSelectionModel().getSelectedItems();

		// Set the overlay
		for (FilamentModel filamentModel : filamentModels) {
			overlayService.setSelected(filamentModel.getFilament(), true, false);
		}

		VBox vbox = new VBox();

		vbox.setPadding(new Insets(10, 10, 10, 10));
		vbox.setSpacing(10);

		Label label = new Label(filamentModels.size() + " filaments selected.");
		Button button = new Button("Delete Filaments");

		button.setOnAction((event) -> {
			removeFilaments(this.getSelectionModel().getSelectedItems());
			this.getSelectionModel().clearSelection();
		});

		vbox.getChildren().add(label);
		vbox.getChildren().add(button);

		detailPane.getChildren().clear();
		detailPane.getChildren().add(vbox);
	}

	private void removeFilament(Filament filament) {
		FilamentModel filamentModel = this.getItems().stream().filter(f -> f.getFilament().equals(filament)).findFirst()
				.orElse(null);
		removeFilament(filamentModel);
	}

	private void removeFilament(FilamentModel filamentModel) {
		this.getItems().remove(filamentModel);
		this.filaments.remove(filamentModel.getFilament());
		overlayService.remove(filamentModel.getFilament());
		this.updateNFilamentsField();
	}

	private void removeFilaments(List<FilamentModel> filamentModels) {
		for (FilamentModel filamentModel : filamentModels) {
			overlayService.remove(filamentModel.getFilament());
			this.filaments.remove(filamentModel.getFilament());
		}
		this.getItems().removeAll(filamentModels);
		this.updateNFilamentsField();
	}

	@EventHandler
	public void filamentSelected(FilamentSelectedEvent event) {
		FilamentModel filamentModel = this.getItems().stream().filter(f -> f.getFilament().equals(event.getFilament()))
				.findFirst().orElse(null);
		if (filamentModel != null) {
			// Require to not mix AWT and JavaFX thread.
			Platform.runLater(() -> {
				this.getSelectionModel().clearSelection();
				this.getSelectionModel().select(filamentModel);
			});
		}
	}

	public Pane getInfoPane() {
		return infoPane;
	}

	public void updateNFilamentsField() {
		if (this.filaments.size() == 0) {
			this.nFilamentsField.setText("");
		} else {
			this.nFilamentsField.setText(this.filaments.size() + " Filaments.");
		}
	}

}
