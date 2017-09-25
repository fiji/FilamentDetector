package fiji.plugin.filamentdetector.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;

import org.scijava.ui.DialogPrompt;

import fiji.plugin.filamentdetector.gui.controller.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import net.imagej.Dataset;
import net.imagej.ImageJ;

public class GUIUtils {

	@SuppressWarnings("unchecked")
	public static Pane loadFXML(String fxml, Controller controller) {
		try {
			URL fxmlUrl = MainAppFrame.class.getResource(fxml);
			FXMLLoader loader = new FXMLLoader(fxmlUrl);
			loader.setController(controller);
			Pane pane = (Pane) loader.load();
			if (controller != null) {
				controller.setPane(pane);
			}
			return pane;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Positions a JFrame more or less cleverly next a {@link Component}. Source:
	 * https://github.com/fiji/TrackMate/blob/master/src/main/java/fiji/plugin/trackmate/gui/GuiUtils.java
	 */
	public static void positionWindow(final JFrame gui, final Component component) {

		if (null != component) {
			// Get total size of all screens
			final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			final GraphicsDevice[] gs = ge.getScreenDevices();
			int screenWidth = 0;
			for (int i = 0; i < gs.length; i++) {
				final DisplayMode dm = gs[i].getDisplayMode();
				screenWidth += dm.getWidth();
			}

			final Point windowLoc = component.getLocation();
			final Dimension windowSize = component.getSize();
			final Dimension guiSize = gui.getSize();
			if (guiSize.width > windowLoc.x) {
				if (guiSize.width > screenWidth - (windowLoc.x + windowSize.width)) {
					gui.setLocationRelativeTo(null); // give up
				} else {
					// put it to the right
					gui.setLocation(windowLoc.x + windowSize.width, windowLoc.y);
				}
			} else {
				// put it to the left
				gui.setLocation(windowLoc.x - guiSize.width, windowLoc.y);
			}

		} else {
			gui.setLocationRelativeTo(null);
		}
	}

	public static final void userCheckImpDimensions(ImageJ ij, Dataset dataset) {
		if (dataset.getFrames() < dataset.getDepth()) {
			String message = "It appears this image has " + dataset.getFrames() + " timepoint (T) but "
					+ dataset.getDepth() + " slices (Z).\nIt could be that dimensions are swapped."
					+ "If you think it's the case please use \"Image > Hyperstacks > Re order Hyperstacks\".";
			ij.ui().showDialog(message, "Are Z and T dimensions swapped ?", DialogPrompt.MessageType.WARNING_MESSAGE);
		}
	}
}
