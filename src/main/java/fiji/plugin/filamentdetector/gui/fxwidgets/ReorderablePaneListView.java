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
package fiji.plugin.filamentdetector.gui.fxwidgets;

import java.util.ArrayList;
import java.util.List;

import fiji.plugin.filamentdetector.gui.controller.Controller;
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

	public <T extends Controller> ReorderablePaneListView() {
		new ReorderablePaneListView(null);
	}

	public <T extends Controller> ReorderablePaneListView(List<T> controllers) {
		super();

		this.setStyle(".table-row-cell:empty {\n" + "    -fx-background-color: transparent;\n" + "}");

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

					// Switch controllers
					if (controllers != null) {
						Controller controller1 = controllers.get(draggedIdx);
						Controller controller2 = controllers.get(thisIdx);
						controllers.set(thisIdx, (T) controller1);
						controllers.set(draggedIdx, (T) controller2);
					}

					success = true;
				}

				selectedPane = null;
				event.setDropCompleted(success);
				event.consume();
			});

			cell.setOnDragDone(DragEvent::consume);
			return cell;
		});

		if (controllers != null) {
			for (Controller controller : controllers) {
				this.getItems().add(controller.loadPane());
			}
		}

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
