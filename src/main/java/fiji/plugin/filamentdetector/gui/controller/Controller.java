package fiji.plugin.filamentdetector.gui.controller;

import javafx.scene.layout.Pane;

public interface Controller {

	void setPane(Pane pane);

	Pane getPane();

	Pane loadPane();

}