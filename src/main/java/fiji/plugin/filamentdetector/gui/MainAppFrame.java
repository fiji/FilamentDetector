package fiji.plugin.filamentdetector.gui;

import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.gui.controller.Controller;
import fiji.plugin.filamentdetector.gui.controller.MainController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import net.imagej.display.ImageDisplay;

public class MainAppFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	@Parameter
	private LogService log;

	@Parameter
	private Context context;

	private JFXPanel fxPanel;
	private final ImageDisplay image;

	public MainAppFrame(Context context, ImageDisplay image) {
		context.inject(this);
		this.image = image;
	}

	/**
	 * Create the JFXPanel that make the link between Swing (IJ) and JavaFX plugin.
	 */
	public void init() {
		this.fxPanel = new JFXPanel();
		this.add(this.fxPanel);
		this.setVisible(true);

		// The call to runLater() avoid a mix between JavaFX thread and Swing thread.
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(fxPanel);
			}
		});

	}

	public void initFX(JFXPanel fxPanel) {
		try {
			// Load the main UI
			URL fxmlUrl = MainAppFrame.class.getResource("/fiji/plugin/filamentdetector/gui/view/Scene.fxml");
			FXMLLoader loader = new FXMLLoader(fxmlUrl);

			// Create and set the main controller
			MainController mainController = new MainController(context);
			loader.setController(mainController);

			// Show the scene containing the root layout.
			AnchorPane mainScreen = (AnchorPane) loader.load();
			Scene scene = new Scene(mainScreen);
			this.fxPanel.setScene(scene);

			// Resize the JFrame to the JavaFX scene
			this.setSize((int) scene.getWidth(), (int) scene.getHeight());

			mainController.loadPanes();

		} catch (IOException e) {
			log.error(e);
		}
	}

	public static Pane loadFXML(String fxml, Controller controller) {
		try {
			URL fxmlUrl = MainAppFrame.class.getResource(fxml);
			FXMLLoader loader = new FXMLLoader(fxmlUrl);
			loader.setController(controller);
			return (AnchorPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
