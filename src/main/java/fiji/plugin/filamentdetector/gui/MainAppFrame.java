package fiji.plugin.filamentdetector.gui;

import java.io.IOException;

import javax.swing.JFrame;

import org.scijava.Context;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import net.imagej.ImageJ;

public class MainAppFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	@Parameter
    private LogService log;

    private final ImageJ ij;

    private JFXPanel fxPanel;

    public MainAppFrame(ImageJ ij) {
        ij.context().inject(this);
        this.ij = ij;
    }

    /**
     * Create the JFXPanel that make the link between Swing (IJ) and JavaFX plugin.
     */
    public void init() {
        this.fxPanel = new JFXPanel();
        this.add(this.fxPanel);
        this.setVisible(true);

        // The call to runLater() avoid a mix between JavaFX thread and Swing thread.
        Platform.runLater(() -> {
            initFX(fxPanel);
        });
        
    }
    
    public void initFX(JFXPanel fxPanel) {
        // Init the root layout
        try {
            FXMLLoader loader = new FXMLLoader();
            System.out.println(getClass().getClassLoader().getResource("/gui/RootLayout.fxml"));
            loader.setLocation(getClass().getClassLoader().getResource("/gui/RootLayout.fxml"));
            AnchorPane rootLayout = (AnchorPane) loader.load();
            
            // Get the controller and add an ImageJ context to it.
            RootLayoutController controller = loader.getController();
            controller.setContext(ij.context());

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            this.fxPanel.setScene(scene);

            // Resize the JFrame to the JavaFX scene
            this.setSize((int) scene.getWidth(), (int) scene.getHeight());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(final String... args) throws Exception {
		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		MainAppFrame app = new MainAppFrame(ij);
		app.setTitle("test GUI");
		app.init();
	}

}