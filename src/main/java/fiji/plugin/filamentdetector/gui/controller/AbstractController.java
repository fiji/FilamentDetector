package fiji.plugin.filamentdetector.gui.controller;

import fiji.plugin.filamentdetector.gui.GUIUtils;
import javafx.scene.layout.Pane;

public abstract class AbstractController implements Controller {

	private String FXMLPath;
	protected Pane pane;

	@Override
	public void setPane(Pane pane) {
		this.pane = pane;
	}

	@Override
	public Pane getPane() {
		return pane;
	}

	protected String getFXMLPath() {
		return FXMLPath;
	}

	protected void setFXMLPath(String fXMLPath) {
		this.FXMLPath = fXMLPath;
	}

	@Override
	public Pane loadPane() {
		Pane pane = GUIUtils.loadFXML(getFXMLPath(), this);
		return pane;
	}

}
