package fiji.plugin.filamentdetector.gui.fxwidgets;

import javafx.scene.control.ListCell;
import javafx.scene.layout.Pane;

public class ReorderableListCell extends ListCell<Pane> {

	public ReorderableListCell() {
		super();
	}

	@Override
	protected void updateItem(Pane pane, boolean empty) {
		super.updateItem(pane, empty);
		if (empty || pane == null) {
			setGraphic(null);
		} else {
			setGraphic(pane);
		}
	}

}
