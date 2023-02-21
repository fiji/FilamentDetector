/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2023 Fiji developers.
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
package sc.fiji.filamentdetector.gui.view;

import java.util.List;

import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

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
import sc.fiji.filamentdetector.event.TrackedFilamentSelectedEvent;
import sc.fiji.filamentdetector.gui.GUIUtils;
import sc.fiji.filamentdetector.gui.controller.DetailedTrackedFilamentController;
import sc.fiji.filamentdetector.gui.model.TrackedFilamentModel;
import sc.fiji.filamentdetector.model.TrackedFilament;
import sc.fiji.filamentdetector.model.TrackedFilaments;
import sc.fiji.filamentdetector.overlay.FilamentOverlayService;

public class TrackedFilamentsTableView extends TableView<TrackedFilamentModel> {

	@Parameter
	FilamentOverlayService overlayService;

	@Parameter
	private LogService log;

	@Parameter
	private StatusService status;

	@Parameter
	private EventService eventService;

	private AnchorPane detailPane;

	private TrackedFilaments trackedFilaments;

	private Label nFilamentsField;
	private Pane infoPane;

	public TrackedFilamentsTableView(Context context, TrackedFilaments trackedFilaments) {
		context.inject(this);

		// Enable multiple selection
		this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		TableColumn<TrackedFilamentModel, Integer> idColumn = new TableColumn<>("ID");
		TableColumn<TrackedFilamentModel, Integer> sizeColumn = new TableColumn<>("Size");
		TableColumn<TrackedFilamentModel, String> colorColumn = new TableColumn<>("Color");

		this.getColumns().add(idColumn);
		this.getColumns().add(sizeColumn);
		this.getColumns().add(colorColumn);

		idColumn.setStyle("-fx-alignment: CENTER;");
		sizeColumn.setStyle("-fx-alignment: CENTER;");
		colorColumn.setStyle("-fx-alignment: CENTER;");

		idColumn.setCellValueFactory(cellData -> cellData.getValue().getId().asObject());
		sizeColumn.setCellValueFactory(cellData -> cellData.getValue().getSize().asObject());
		colorColumn.setCellValueFactory(cellData -> cellData.getValue().getColor());

		colorColumn.setCellFactory(column -> {
			return new TableCell<TrackedFilamentModel, String>() {
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

			List<TrackedFilamentModel> trackedFilamentModels = this.getSelectionModel().getSelectedItems();

			if (trackedFilamentModels.size() == 1 && oldSelection != newSelection) {
				// Set the overlay
				overlayService.setSelected(newSelection.getTrackedFilament(), true, true);

				// Fill the detailed view
				DetailedTrackedFilamentController controller = new DetailedTrackedFilamentController(
						newSelection.getTrackedFilament());
				Pane pane = GUIUtils.loadFXML(
						"/sc/fiji/filamentdetector/gui/view/tracking/DetailedTrackedFilamentView.fxml", controller);

				detailPane.getChildren().clear();
				detailPane.getChildren().add(pane);

				AnchorPane.setTopAnchor(pane, 0.0);
				AnchorPane.setLeftAnchor(pane, 0.0);
				AnchorPane.setRightAnchor(pane, 0.0);
				AnchorPane.setBottomAnchor(pane, 0.0);
				pane.setPrefHeight(Region.USE_COMPUTED_SIZE);
				pane.setPrefWidth(Region.USE_COMPUTED_SIZE);

				controller.getRemoveFilamentLabel().setOnAction((event) -> {
					removeTrackedFilament(controller.getTrackedFilament());
				});

			} else if (trackedFilamentModels.size() > 1) {
				setMultipleSelectionDetail();

			} else {
				setNoDetail();
			}
		});

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
		setTrackedFilaments(trackedFilaments);

		// Handle filament selection
		eventService.subscribe(this);
	}

	public TrackedFilaments getTrackedFilaments() {
		return this.trackedFilaments;
	}

	public void setTrackedFilaments(TrackedFilaments trackedFilaments) {

		this.trackedFilaments = trackedFilaments;

		ObservableList<TrackedFilamentModel> trackedFilamentModelList = FXCollections.observableArrayList();
		for (TrackedFilament trackedFilament : trackedFilaments) {
			trackedFilamentModelList.add(new TrackedFilamentModel(trackedFilament));
		}
		this.setItems(trackedFilamentModelList);

		// Update overlay
		overlayService.reset();
		overlayService.add(trackedFilaments);
		this.updateNFilamentsField();
	}

	public void addTrackedFilament(TrackedFilament trackedFilament) {
		this.getItems().add(new TrackedFilamentModel(trackedFilament));
		this.trackedFilaments.add(trackedFilament);
		overlayService.add(trackedFilament);
		this.updateNFilamentsField();
	}

	public Pane getDetailPane() {
		return detailPane;
	}

	private void setNoDetail() {
		Label noDetail = new Label("No tracked filament selected.");
		detailPane.getChildren().clear();
		detailPane.getChildren().add(noDetail);
	}

	private void setMultipleSelectionDetail() {

		List<TrackedFilamentModel> trackedFilamentModels = this.getSelectionModel().getSelectedItems();

		// Set the overlay
		for (TrackedFilamentModel trackedFilamentModel : trackedFilamentModels) {
			overlayService.setSelected(trackedFilamentModel.getTrackedFilament(), true, false);
		}

		VBox vbox = new VBox();

		vbox.setPadding(new Insets(10, 10, 10, 10));
		vbox.setSpacing(10);

		Label label = new Label(trackedFilamentModels.size() + " tracked filaments selected.");
		Button button = new Button("Delete Tracked Filaments");

		button.setOnAction((event) -> {
			removeTrackedFilaments(this.getSelectionModel().getSelectedItems());
			this.getSelectionModel().clearSelection();
		});

		vbox.getChildren().add(label);
		vbox.getChildren().add(button);

		detailPane.getChildren().clear();
		detailPane.getChildren().add(vbox);
	}

	private void removeTrackedFilament(TrackedFilament trackedFilament) {
		TrackedFilamentModel trackedFilamentModel = this.getItems().stream()
				.filter(f -> f.getTrackedFilament().equals(trackedFilament)).findFirst().orElse(null);
		removeTrackedFilament(trackedFilamentModel);
	}

	private void removeTrackedFilament(TrackedFilamentModel trackedFilamentModel) {
		this.getItems().remove(trackedFilamentModel);
		this.trackedFilaments.remove(trackedFilamentModel.getTrackedFilament());
		overlayService.remove(trackedFilamentModel.getTrackedFilament());
		this.updateNFilamentsField();
	}

	private void removeTrackedFilaments(List<TrackedFilamentModel> trackedFilamentModels) {
		for (TrackedFilamentModel trackedFilamentModel : trackedFilamentModels) {
			overlayService.remove(trackedFilamentModel.getTrackedFilament());
			this.trackedFilaments.remove(trackedFilamentModel.getTrackedFilament());
		}
		this.getItems().removeAll(trackedFilamentModels);
		this.updateNFilamentsField();
	}

	@EventHandler
	public void filamentSelected(TrackedFilamentSelectedEvent event) {
		TrackedFilamentModel trackedFilamentModel = this.getItems().stream()
				.filter(f -> f.getTrackedFilament().equals(event.getTrackedFilament())).findFirst().orElse(null);
		if (trackedFilamentModel != null) {
			// Require to not mix AWT and JavaFX thread.
			Platform.runLater(() -> {
				this.getSelectionModel().clearSelection();
				this.getSelectionModel().select(trackedFilamentModel);
			});
		}
	}

	public Pane getInfoPane() {
		return infoPane;
	}

	public void updateNFilamentsField() {
		if (this.trackedFilaments.size() == 0) {
			this.nFilamentsField.setText("");
		} else {
			this.nFilamentsField.setText(this.trackedFilaments.size() + " Tracked Filaments.");
		}
	}

}
