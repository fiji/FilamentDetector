/*-
 * #%L
 * A Fiji plugin that allow easy, fast and accurate detection and tracking of biological filaments.
 * %%
 * Copyright (C) 2016 - 2020 Fiji developers.
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

package sc.fiji.filamentdetector.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import ij.ImagePlus;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import net.imagej.ImageJ;
import net.imagej.display.ImageDisplay;
import sc.fiji.filamentdetector.FilamentWorkflow;
import sc.fiji.filamentdetector.gui.controller.MainController;

public class MainAppFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	@Parameter
	private LogService log;

	@Parameter
	private Context context;

	@Parameter
	private ConvertService convert;

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
		// The call to runLater() avoid a mix between JavaFX thread and Swing
		// thread.
		Platform.runLater(() -> {
			initFX(fxPanel);
		});

	}

	public void initFX(JFXPanel fxPanel) {
		try {
			// Load the main UI
			URL fxmlUrl = MainAppFrame.class.getResource("/sc/fiji/filamentdetector/gui/view/MainView.fxml");
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
			ImagePlus imp = convert.convert(filamentDetector.getImageDisplay(), ImagePlus.class);
			GUIUtils.positionWindow(this, imp.getWindow());

			mainController.loadPanes();

		} catch (IOException e) {
			log.error(e);
		}
	}

}
