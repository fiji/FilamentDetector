package fiji.plugin.filamentdetector.gui.controller;

import javafx.scene.layout.Pane;

public abstract class Controller {

	protected Pane pane;

	public void setPane(Pane pane) {
		this.pane = pane;
	}

	public Pane getPane() {
		return pane;
	}

}
