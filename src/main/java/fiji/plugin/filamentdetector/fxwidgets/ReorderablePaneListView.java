package fiji.plugin.filamentdetector.fxwidgets;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListView;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class ReorderablePaneListView extends ListView<Pane> {

	private Pane selectedPane = null;

	public ReorderablePaneListView() {
		super();

		this.setCellFactory(param -> {

			ReorderableListCell cell = new ReorderableListCell();

			cell.setOnDragDetected(event -> {
				if (cell.getItem() == null) {
					return;
				}

				Image image = getPaneImage(cell.getItem());
				Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
				ClipboardContent content = new ClipboardContent();
				content.putString("");
				dragboard.setContent(content);
				dragboard.setDragView(image);
				dragboard.setDragViewOffsetY(image.getHeight() / 2);
				selectedPane = cell.getItem();

				this.getSelectionModel().clearSelection();

				event.consume();
			});

			cell.setOnDragOver(event -> {
				if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
					event.acceptTransferModes(TransferMode.MOVE);
				}
				event.consume();
			});

			cell.setOnDragEntered(event -> {
				if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
					cell.setOpacity(0.3);
				}
			});

			cell.setOnDragExited(event -> {
				if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
					cell.setOpacity(1);
				}
			});

			cell.setOnDragDropped(event -> {

				if (cell.getItem() == null) {
					return;
				}

				boolean success = false;
				Dragboard dragboard = event.getDragboard();

				if (dragboard.hasString()) {
					ObservableList<Pane> items = cell.getListView().getItems();
					int draggedIdx = items.indexOf(selectedPane);
					int thisIdx = items.indexOf(cell.getItem());
					items.set(draggedIdx, cell.getItem());
					items.set(thisIdx, selectedPane);
					List<Pane> itemscopy = new ArrayList<>(cell.getListView().getItems());
					cell.getListView().getItems().setAll(itemscopy);
					success = true;
				}

				selectedPane = null;
				event.setDropCompleted(success);
				event.consume();
			});

			cell.setOnDragDone(DragEvent::consume);
			return cell;
		});

	}

	private Image getPaneImage(Pane pane) {
		WritableImage snapshotImage = new WritableImage((int) pane.getWidth(), (int) pane.getHeight());
		pane.snapshot(null, snapshotImage);

		Canvas canvas = new Canvas((int) pane.getWidth(), (int) pane.getHeight());
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		gc.setGlobalBlendMode(BlendMode.SCREEN);
		gc.setGlobalAlpha(0.7);
		gc.drawImage(snapshotImage, 0, 0);

		WritableImage finalImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
		canvas.snapshot(null, finalImage);

		return finalImage;
	}

}
