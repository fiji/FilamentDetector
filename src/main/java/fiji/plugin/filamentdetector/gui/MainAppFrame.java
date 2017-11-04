package fiji.plugin.filamentdetector.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import fiji.plugin.filamentdetector.FilamentWorkflow;
import fiji.plugin.filamentdetector.gui.controller.MainController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;

public class MainAppFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	@Parameter
	private LogService log;

	@Parameter
	private Context context;

	private ImageJ ij;

	private JFXPanel fxPanel;

	private final FilamentWorkflow filamentDetector;
	private final ImageDisplay imd;

	public MainAppFrame(ImageJ ij, FilamentWorkflow filamentDetector) {
		ij.context().inject(this);
		this.ij = ij;
		this.filamentDetector = filamentDetector;
		this.imd = filamentDetector.getImageDisplay();

		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				log.info("Quitting FilamentDetector... Bye bye !");
				SwingUtilities.invokeLater(() -> {
					setVisible(true);
					dispose();
				});
			}
		});
	}

	/**
	 * Create the JFXPanel that make the link between Swing (IJ) and JavaFX plugin.
	 */
	public void initialize() throws Exception {

		try {
			filamentDetector.initialize();
		} catch (Exception e) {
			throw new Exception(e);
		}

		GUIUtils.userCheckImpDimensions(ij, filamentDetector.getDataset());

		Platform.setImplicitExit(false);
		
		// Create the JavaFX panel
		this.fxPanel = new JFXPanel();
		this.add(this.fxPanel);
		this.setVisible(true);

		// Initialize the JavaFX panel
		// The call to runLater() avoid a mix between JavaFX thread and Swing thread.
		Platform.runLater(() -> {
			initFX(fxPanel);
		});

	}

	public void initFX(JFXPanel fxPanel) {
		try {
			// Load the main UI
			URL fxmlUrl = MainAppFrame.class.getResource("/fiji/plugin/filamentdetector/gui/view/MainView.fxml");
			FXMLLoader loader = new FXMLLoader(fxmlUrl);

			// Create and set the main controller
			MainController mainController = new MainController(context, filamentDetector);
			loader.setController(mainController);

			// Show the scene containing the root layout.
			AnchorPane mainScreen = (AnchorPane) loader.load();
			Scene scene = new Scene(mainScreen);
			this.fxPanel.setScene(scene);
			mainController.setPane(mainScreen);

			// Resize the JFrame to the JavaFX scene
			this.setSize((int) scene.getWidth(), (int) scene.getHeight());

			// Position the window
			GUIUtils.positionWindow(this, filamentDetector.getImagePlus().getWindow());

			mainController.loadPanes();

		} catch (IOException e) {
			log.error(e);
		}
	}

}
